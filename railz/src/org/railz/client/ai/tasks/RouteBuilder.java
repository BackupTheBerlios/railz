/*
 * Copyright (C) 2005 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.railz.client.ai.tasks;

import java.awt.*;
import java.util.*;
import java.util.logging.*;

import org.railz.client.ai.*;
import org.railz.controller.*;
import org.railz.controller.RouteBuilderPathExplorer.RouteBuilderPathExplorerSettings;
import org.railz.world.accounts.*;
import org.railz.world.building.*;
import org.railz.world.cargo.*;
import org.railz.world.city.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.terrain.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;

/**
 * Contains AI for constructing routes between destinations.
 */
class RouteBuilder extends TaskPlanner {
    private static final Logger logger = Logger.getLogger("ai");
    private RouteBuilderMoveFactory routeBuilderMoveFactory;
    
    /** Max age of cache in ticks */
    private final long MAX_CACHE_AGE;

    /** The amount of money we should try to have left after building the
     * route */
    private static final long COMFORT_ZONE = 200000L;

    private GameTime routeCacheTimeStamp;
    private boolean shouldRebuildCache = true;
    /** The approximate amount of cash the player has for building */
    private long cashAvailable;

    /**
     * ArrayList of detailed route cost estimates. The cost estimates here
     * are a mixture of "cheap" cost estimates and the detailed ones.
     * This is periodically flushed (once a month?) at which point we do a
     * complete recalculation of all the cheap estimates, and restart our
     * detailed costing analysis.
     */
    private ArrayList routeCache = new ArrayList();

    /**
     * ArrayList of CityEntry storing inter-city distances, sorted by
     * ascending order of distance.
     */
    private ArrayList cityDistances = new ArrayList();

    private static final long MIN_BALANCE = 60000L;
    /** Cost of a large station */
    private long LARGE_STATION_COST = 0L;
    /** Station radius of a large station */
    private int LARGE_STATION_RADIUS = 0;
    /** The standard load used to determine the reference engine type */
    private static final int STANDARD_LOAD = 180;
    /** The reference engine type - which is the best at hauling the standard
     * load over zero gradient */
    private EngineType REFERENCE_ENGINE_TYPE;
    private float REFERENCE_ENGINE_TYPE_MAX_SPEED;
    private int REFERENCE_ENGINE_TYPE_INDEX;

    private final AIClient aiClient;
    private CityModelViewer cityModelViewer;
    private TrackTileViewer trackTileViewer;
    private TrackMaintenanceMoveGenerator trackMaintenanceMoveGenerator;
    private CargoPaymentCalculator cargoPaymentCalculator;
    private boolean haveNoStations;
    private PathFinder pathFinder;

    private final int nCargoTypes;

    /** The "best" plan */
    private CityEntry taskPlan;

    public RouteBuilder (AIClient aic) {
	aiClient = aic;
	MAX_CACHE_AGE = ((GameCalendar) aiClient.getWorld().get(ITEM.CALENDAR,
		Player.AUTHORITATIVE)).getTicksPerDay() * 28;
	nCargoTypes = aiClient.getWorld().size(KEY.CARGO_TYPES,
		Player.AUTHORITATIVE);
	trackMaintenanceMoveGenerator = new TrackMaintenanceMoveGenerator
	    (aiClient.getWorld(), null);
	routeBuilderMoveFactory = new RouteBuilderMoveFactory(aiClient);
    }

    private boolean isInitialised = false;

    private void initialise() {
	if (isInitialised)
	    return;

	cityModelViewer = new CityModelViewer(aiClient.getWorld());
	trackTileViewer = new TrackTileViewer(aiClient.getWorld());
	cargoPaymentCalculator = new
	    CargoPaymentCalculator(aiClient.getWorld());

	/* initialise cost of large station */
	NonNullElements i = new NonNullElements(KEY.BUILDING_TYPES,
		aiClient.getWorld(), Player.AUTHORITATIVE);
	while (i.next()) {
	    BuildingType bt = (BuildingType) i.getElement();
	    if (bt.getStationRadius() > LARGE_STATION_RADIUS) {
		LARGE_STATION_RADIUS = bt.getStationRadius();
		LARGE_STATION_COST = bt.getBaseValue();
	    }	
	}
	
	WorldConstants.init(aiClient.getWorld());
    }

    /**
     * If we have at least one station, discard all routes from the
     * cityDistances table that do not connect a city with an existing
     * station.
     */
    private void discardAllRoutesNotToExistingStations() {
	ReadOnlyWorld w = aiClient.getWorld();
	FreerailsPrincipal p = aiClient.getPlayerPrincipal();
	
	if (w.size(KEY.STATIONS, p) == 0) {
	    haveNoStations = true;
	    return;
	}
	haveNoStations = false;

	for (int i = 0; i < cityDistances.size(); i++) {
	    CityEntry ce = (CityEntry) cityDistances.get(i);
	    int nStations = 0;
	    cityModelViewer.setCityModel((CityModel) w.get(KEY.CITIES, ce.city1,
			Player.AUTHORITATIVE));
	    if ((ce.station1 = cityModelViewer.hasStation(p)) >= 0)
		nStations++;
	    cityModelViewer.setCityModel((CityModel) w.get(KEY.CITIES, ce.city2,
			Player.AUTHORITATIVE));
	    if ((ce.station2 = cityModelViewer.hasStation(p)) >= 0)
		nStations++;
	    if (nStations == 0 || nStations == 2) {
		logger.log(Level.INFO, "Discarded route " + ce);
		cityDistances.remove(i);
		i--;
	    }
	}
    }
    
    /**
     * @return the fixed construction costs - this includes price of the
     * stations, and the price of a single engine to run between them.
     * TODO include water tower cost
     */
    private long getFixedConstructionCosts() {
	long cost = 0;
	NonNullElements j = new NonNullElements(KEY.ENGINE_TYPES,
		aiClient.getWorld(), Player.AUTHORITATIVE);
	long cheapestEngineCost = Long.MAX_VALUE;
	while (j.next()) {
	    EngineType et = (EngineType) j.getElement();
	    if (et.getPrice() < cheapestEngineCost &&
		    et.isAvailable())
		cheapestEngineCost = et.getPrice();
	}
	// add cost of cheapest available engine
	cost += cheapestEngineCost;
	
	// add in cost for 1 or 2 stations, plus an engine
	if (haveNoStations) {
	    cost += 2 * LARGE_STATION_COST;
	} else {
	    cost += LARGE_STATION_COST;
	}
	return cost;
    }

    private void doCheapCostEstimation() {
	/* base our estimation on the cost per unit track for buying single
	 * track, on ordinary grassland tiles, over the shortest path distance
	 */
	long terrainUnitCost = 0;

	ReadOnlyWorld w = aiClient.getWorld();

	TerrainType tt = (TerrainType) w.get(KEY.TERRAIN_TYPES,
		WorldConstants.get().TT_CLEAR, Player.AUTHORITATIVE);
	terrainUnitCost = tt.getBaseValue();
	TrackTile straightTile = TrackTile.createTrackTile
	    (aiClient.getWorld(),
	     (byte) (CompassPoints.NORTH | CompassPoints.SOUTH),
	       	WorldConstants.get().TR_STANDARD_TRACK);
	TrackTile diagTile = TrackTile.createTrackTile
	    (aiClient.getWorld(),
	     (byte) (CompassPoints.NORTHWEST | CompassPoints.SOUTHEAST),
		WorldConstants.get().TR_STANDARD_TRACK);
	trackTileViewer.setTrackTile(null);
	long straightTrackUnitCost = trackTileViewer.getConstructionCost
	    (straightTile);
	long diagTrackUnitCost = trackTileViewer.getConstructionCost
	    (diagTile);


	for (int i = 0; i < cityDistances.size(); i++) {
	    long costEstimate = 0;
	    CityEntry ce = (CityEntry) cityDistances.get(i);
	    costEstimate = ce.distance.straightLength * 
		(straightTrackUnitCost + terrainUnitCost) + 
		ce.distance.diagLength * (diagTrackUnitCost +
			terrainUnitCost);

	    costEstimate += getFixedConstructionCosts();
	    ce.constructionEstimate = costEstimate;
	}
    }

    private void rebuildCache() {
	taskPlan = null;
	// create a list of city-city entries, sorted by distance
	NonNullElements i = new NonNullElements(KEY.CITIES,
		aiClient.getWorld(), Player.AUTHORITATIVE);
	while (i.next()) {
	    NonNullElements j = new NonNullElements(KEY.CITIES,
		    aiClient.getWorld(), Player.AUTHORITATIVE);
	    j.gotoIndex(i.getIndex());
	    while (j.next()) {
		CityModel cm1 = (CityModel) i.getElement();
		CityModel cm2 = (CityModel) j.getElement();
		CityEntry ce = new CityEntry(aiClient, i.getIndex(),
			j.getIndex(), new PathLength(cm1.getCityX(),
			    cm1.getCityY(), cm2.getCityX(), cm2.getCityY()));
		cityDistances.add(ce);
	    }
	}

	Collections.sort(cityDistances);

	// if we have at least 1 station, discard all routes not to an
	// existing station
	discardAllRoutesNotToExistingStations();
	
	// Here we should discard all routes that can never be constructed,
	// however we will instead do this if and when we discover this is the
	// case later on
	
	// do our cheap estimation for construction cost
	doCheapCostEstimation();

	// remove all routes we can't afford
	cashAvailable = ((BankAccount) aiClient.getWorld()
	    .get(KEY.BANK_ACCOUNTS, 0, aiClient.getPlayerPrincipal()))
	    .getCurrentBalance() - COMFORT_ZONE;

	for (int j = 0; j < cityDistances.size(); j++) {
	    CityEntry ce = (CityEntry) cityDistances.get(j);
	    if (ce.constructionEstimate > cashAvailable) {
		logger.log(Level.FINE, "Removing route " + ce.toString() +
			" because too expensive");
		cityDistances.remove(j);
		j--;
	    }
	}

	// determine REFERENCE_ENGINE_TYPE
	i = new NonNullElements(KEY.ENGINE_TYPES, aiClient.getWorld(),
		Player.AUTHORITATIVE);
	float bestMaxSpeed = 0.0f;
	float maxSpeed = 0.0f;
	while (i.next()) {
	    EngineType et = (EngineType) i.getElement();
	    if (! et.isAvailable())
		continue;

	    maxSpeed = et.getMaxSpeed(0.0f, STANDARD_LOAD + et.getMass());
	    if (REFERENCE_ENGINE_TYPE == null || maxSpeed > bestMaxSpeed) {
		bestMaxSpeed = maxSpeed;
		REFERENCE_ENGINE_TYPE = et;
		REFERENCE_ENGINE_TYPE_MAX_SPEED = maxSpeed;
		REFERENCE_ENGINE_TYPE_INDEX = i.getIndex();
	    }
	}

	GameCalendar gc = (GameCalendar) aiClient.getWorld().get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	long ticksPerYear = gc.getTicksPerDay() * 365;

	/* Calculate supply and demand at all stations/cities */
	SupplyDemandViewer sdv = new SupplyDemandViewer(aiClient.getWorld());
	CargoInfo ci = new CargoInfo();
	for (int j = 0; j < cityDistances.size(); j++) {
	    CityEntry ce = (CityEntry) cityDistances.get(j);
	    setupSupplyDemandViewer(ce.station1, ce.city1, sdv);
	    SupplyAtStation supply1 = sdv.getSupply();
	    DemandAtStation demand1 = sdv.getDemand();
	    ConvertedAtStation convert1 = sdv.getConversion();
	    setupSupplyDemandViewer(ce.station2, ce.city2, sdv);
	    SupplyAtStation supply2 = sdv.getSupply();
	    DemandAtStation demand2 = sdv.getDemand();
	    ConvertedAtStation convert2 = sdv.getConversion();

	    cargoInfoFromCargoData(supply1, supply2, demand1, demand2,
		    convert1, convert2, ce.distance, maxSpeed, ci, ce);
	    float tripsPerYear = ((float) maxSpeed * ticksPerYear) /
	       	(float) (ce.distance.getLength() * TrackTile.DELTAS_PER_TILE);

	    /* calculate our annual cost */
	    int[] cargo;
	    if (ci.tonnage1 > ci.tonnage2) {
		cargo = ci.cb1;
	    } else {
		cargo = ci.cb2;
	    }
	    int nTrains = calculateTrainsRequired(cargo, tripsPerYear);
	    Economy e = (Economy) aiClient.getWorld().get(ITEM.ECONOMY,
		    Player.AUTHORITATIVE);
	    int[] trackTypes = new int[aiClient.getWorld().size(KEY.TRACK_RULES,
		    Player.AUTHORITATIVE)];
	    trackTypes[WorldConstants.get().TR_STANDARD_TRACK] =
		ce.distance.straightLength + ce.distance.diagLength;
	    trackMaintenanceMoveGenerator.reset();
	    trackMaintenanceMoveGenerator.setTrack(trackTypes);
	    Transaction t = trackMaintenanceMoveGenerator.getTransaction(); 
	    long annualCost = REFERENCE_ENGINE_TYPE.getMaintenance() +
		REFERENCE_ENGINE_TYPE.getAnnualFuelConsumption() *
		e.getFuelUnitPrice(REFERENCE_ENGINE_TYPE.getFuelType()) -
		t.getValue() * 12;
	    annualCost *= nTrains;
	    ce.annualRevenueEstimate = ci.annualRevenue;
	    ce.annualCostEstimate = annualCost;
	    
	    // remove all those lines with negative profit !
	    if (ce.annualRevenueEstimate - ce.annualCostEstimate < 0) {
		logger.log(Level.FINE, "Removing " + ce.toString() + " because"
			+ " route would lose money.");
		cityDistances.remove(j);
		j--;
	    }
	}

	Collections.sort(cityDistances);
    }

    /**
     * Produce plans for constructing the best route. It is judged by the
     * following criteria:
     * <ul>
     * <li>There is a constructable route between the destinations.
     * <li>We have sufficient funds to build it immediately.
     * <li>It provides the highest return on investment (as an annual 
     * percentage of initial outlay), <b>OR</b> it fulfills a scenario objective
     * (TODO).
     * <li>There is no existing route between the two destinations.
     * <li>If we have at least one station, then all new routes must connect
     * to an existing station.
     * </ul>
     * 
     * In order to determine the route as computationally cheaply as possible,
     * we first process all city combinations using the following criteria
     * applied in order:
     * <ul>
     * <li>If we have at least one station, then discard all routes which do
     * not have one and only one end-point at an existing station.
     * <li>Discard all routes which can never be constructed (determined in a
     * previous invocation of this method).
     * <li>A cheap estimation is used to determine whether the shortest
     * possible route between the destinations cannot possibly be afforded.
     * These routes are discarded.
     * <li>Determine the top speed of the fastest affordable engine using a
     * given load. Use this to determine the time taken for a return journey
     * between each pair of stations.
     * <li>Determine the supply and demand between the station pairs. Using
     * the information in the previous step, calculate the operating profit
     * for each route (revenue per year, less train running costs per year)
     * <li>Determine the return on investment using the above operating profit
     * calculation, and the construction cost already calculated.
     * </ul>
     *
     * After this step, now calculate the profitability of the best route
     * calculated in the above sequence using a more detailed route analysis.
     * Since these above calculations are based on ideal scenarios, they could
     * be cached for some time. In addition, any more detailed analysis we
     * perform will come up with a worse result. Therefore if we perform a
     * detailed analysis and the new result is still the best route, then we
     * know we have found the optimum route.
     * 
     * <ul>
     * <li>If one city has an existing station, then use this station as
     * an endpoint. Otherwise the following analysis is performed for both
     * stations.
     * <li>For the remaining city(ies), we must determine the optimum point to
     * position the station.
     * <ul><li>Pick a spot on the periphery of the city, on the side nearest
     * to the other destination.
     * <li>Determine the optimum router to this point from the far station.
     * If there is no route, then we mark this city combination as never being
     * reachable, and abandon planning.
     * <li>Define a grid around the city and determine all the places where a
     * station may be placed. Each position must be reachable from the point
     * considered in the previous step.
     * <li>Perform the supply/demand analysis to determine the best place to
     * position the station.
     * </ul>
     * </ul>
     * The route to build is thus the route between the station, the point on
     * the city periphery, and the far destination (which may be a point on
     * the other citys periphery, and then a route to the other station from
     * that point.
     */
    public boolean planTask() {
	ReadOnlyWorld w = aiClient.getWorld();

	// don't bother to do anything at all unless we have at least
	// MIN_BALANCE in the bank
	BankAccount ba = (BankAccount) w.get(KEY.BANK_ACCOUNTS, 0,
	       	aiClient.getPlayerPrincipal());
	if (ba.getCurrentBalance() < MIN_BALANCE) 
	    return false;
	
	initialise();
	
	if (shouldRebuildCache) {
	    rebuildCache();
	    shouldRebuildCache = false;
	    dumpTop10();
	}

	/* Calculate a detailed estimate for the best route */
	if (cityDistances.isEmpty())
	    return false;

	CityEntry ce = (CityEntry) cityDistances.get(0);
	if (! ce.detailedEstimate) {
	    doDetailedCostEstimation(ce);
	    logger.log(Level.INFO, "Detailed estimate for: " + ce);
	}
	if (! ce.detailedEstimate) {
	    // no route was possible - ditch this route, and bail out - we
	    // will try again next time
	    cityDistances.remove(0);
	    return false;
	}

	// are we better than the last route?
	Collections.sort(cityDistances);

	ce = (CityEntry) cityDistances.get(0);
	if (ce.detailedEstimate) {
	    // this route must be the best one
	    logger.log(Level.INFO, "The best route is:" + ce);
	    taskPlan = ce;

	    return true;
	}

	return false;
    }

    /**
     * @param sites an ArrayList to be filled with instances of Point objects
     * indicating possible sites to build a station
     */
    private void setupStationSites(int cityId, ArrayList sites) {
	ReadOnlyWorld w = aiClient.getWorld();
	CityModel cm = (CityModel) w.get(KEY.CITIES, cityId,
		Player.AUTHORITATIVE);
	int xmin = cm.getCityX() - cm.getCityRadius();
	int xmax = cm.getCityX() + cm.getCityRadius();
	int ymin = cm.getCityY() - cm.getCityRadius();
	int ymax = cm.getCityY() + cm.getCityRadius();
	xmin = xmin < 0 ? 0 : xmin;
	ymin = ymin < 0 ? 0 : ymin;
	xmax = xmax >= w.getMapWidth() ? w.getMapWidth() - 1 : xmax;
	ymax = ymax >= w.getMapHeight() ? w.getMapHeight() - 1 : ymax;

	FreerailsPrincipal p = aiClient.getPlayerPrincipal();
	for (int x = xmin; x <= xmax; x++) {
	    for (int y = ymin; y <= ymax; y++) {
		FreerailsTile ft = w.getTile(x, y);
		// if we own the tile or can buy it, and there is not already
		// a building on it.
		if ((Player.AUTHORITATIVE.equals(ft.getOwner()) ||
			p.equals(ft.getOwner())) &&
			ft.getBuildingTile() == null) {
		    sites.add(new Point(x, y));
		}
	    }
	}
    }

    private class CargoInfo {
	/** total tonnage from site1 after rounding */
	int tonnage1 = 0;
	/** total tonnage from site2 after rounding */
	int tonnage2 = 0;

	/** tonnes of cargo shipped annually from site1 */
	int[] cb1 = new int[nCargoTypes];
	/** tonnes of cargo shipped annually from site2 */
	int[] cb2 = new int[nCargoTypes];
	
	long annualRevenue;
    }

    private void cargoInfoFromCargoData(SupplyAtStation s1,
	    SupplyAtStation s2, DemandAtStation d1, DemandAtStation d2,
	    ConvertedAtStation c1, ConvertedAtStation c2, PathLength distance,
	    float maxSpeed, CargoInfo ci, CityEntry ce) {
	    // now we have got the supply and demand, work out the total
	    // number of tonnes we can carry in outbound and return journeys
	    // over the course of 1 year
	    Arrays.fill(ci.cb1, 0);
	    Arrays.fill(ci.cb2, 0);
	    calculateOutboundReturnCargoBundles(s1, s2, d1,
		    d2, c1, c2, ci.cb1, ci.cb2);

	    // round off tonnage so that they are within 75% of each other
	    // (returning empty wagons is unprofitable)
	    ci.tonnage1 = 0;
	    ci.tonnage2 = 0;
	    for (int k = 0; k < nCargoTypes; k++) {
		ci.tonnage1 += ci.cb1[k];
		ci.tonnage2 += ci.cb2[k];
	    }
	    if (ci.tonnage1 > (ci.tonnage2 * 4) / 3) {
		float factor = ((ci.tonnage2 * 4) / 3) / ci.tonnage1;
		for (int k = 0; k < nCargoTypes; k++)
		   ci.cb1[k] = (int) (factor * (float) ci.cb1[k]); 
	    } else if (ci.tonnage2 > (ci.tonnage1 * 4 / 3)) {
		float factor = ((ci.tonnage1 * 4) / 3) / ci.tonnage2;
		for (int k = 0; k < nCargoTypes; k++)
		   ci.cb2[k] = (int) (factor * (float) ci.cb2[k]); 
	    }
	    // calculate our annual revenue
	    long elapsedTicks = (long) ((distance.getLength() *
		    TrackTile.DELTAS_PER_TILE) / maxSpeed);

	    /* The cargo sent from station 1 */
	    CargoBundle cBundle1 = getCargoBundle(ce.station1, ce.city1,
		    ci.cb1);
	    /* The cargo sent from station 2 */
	    CargoBundle cBundle2 = getCargoBundle(ce.station2, ce.city2,
		    ci.cb2);
	    /* revenue for journey from 1 to 2 */
	    Transaction[] t1 = getTransactions(ce.station2, ce.city2,
		    cBundle1, elapsedTicks);
	    /* revenue for journey from 2 to 1 */
	    Transaction[] t2 = getTransactions(ce.station1, ce.city1,
		    cBundle2, elapsedTicks);
	    ci.annualRevenue = 0;
	    for (int k = 0; k < t1.length; k++)
		ci.annualRevenue += t1[k].getValue();
	    for (int k = 0; k < t2.length; k++)
		ci.annualRevenue += t2[k].getValue();
    }

    private void doDetailedCostEstimation(CityEntry ce) {
	ArrayList sites1 = new ArrayList();
	ArrayList sites2 = new ArrayList();
	if (ce.station1 >= 0) {
	    StationModel sm = (StationModel) aiClient.getWorld().get
		(KEY.STATIONS, ce.station1, aiClient.getPlayerPrincipal());
	    ce.site1 = new Point(sm.getStationX(), sm.getStationY());
	    sites1.add(ce.site1);
	} else {
	    setupStationSites(ce.city1, sites1);
	}
	if (ce.station2 >= 0) {
	    StationModel sm = (StationModel) aiClient.getWorld().get
		(KEY.STATIONS, ce.station2, aiClient.getPlayerPrincipal());
	    ce.site2 = new Point(sm.getStationX(), sm.getStationY());
	    sites2.add(ce.site2);
	} else {
	    setupStationSites(ce.city2, sites2);
	}

	/* choose the best combination of locations */
	SupplyDemandViewer sdv = new SupplyDemandViewer(aiClient.getWorld());
	CargoInfo ci = new CargoInfo();
	ArrayList siteList = new ArrayList();
	for (int i = 0; i < sites1.size(); i++) {
	    for (int j = 0; j < sites2.size(); j++) {
		Point p1 = (Point) sites1.get(i);
		Point p2 = (Point) sites2.get(j);
		sdv.setStationNotBuilt(sites1.size() != 1);
		sdv.setLocation(p1.x, p1.y, LARGE_STATION_RADIUS);
		SupplyAtStation s1 = sdv.getSupply();
		DemandAtStation d1 = sdv.getDemand();
		ConvertedAtStation c1 = sdv.getConversion();

		sdv.setStationNotBuilt(sites2.size() != 1);
		sdv.setLocation(p2.x, p2.y, LARGE_STATION_RADIUS);
		SupplyAtStation s2 = sdv.getSupply();
		DemandAtStation d2 = sdv.getDemand();
		ConvertedAtStation c2 = sdv.getConversion();

		cargoInfoFromCargoData(s1, s2, d1, d2, c1, c2, ce.distance,
			REFERENCE_ENGINE_TYPE_MAX_SPEED, ci, ce);

		CargoData cd = new CargoData(p1, p2, ci.annualRevenue);
		siteList.add(cd);
	    }
	}

	/* sort by ascending order of revenue */
	Collections.sort(siteList);
	CargoData cd;

	/* attempt to plan a route between the top 10 routes */
	ArrayList tmp = new ArrayList();
	int sz = siteList.size();
	for (int i = sz - 1; i >= sz - 10 && i >= 0; i--)
	    tmp.add(siteList.remove(i));
	
	siteList = tmp;
	int nStations = siteList.size();
	int currentN = 0;
	while (!siteList.isEmpty()) {
	    currentN++;
		logger.log(Level.FINE, "examining " + currentN
			+ " of " + nStations + " routes");
	    cd = (CargoData) siteList.remove(siteList.size() - 1);
	    RouteBuilderPathExplorerSettings s = new
		RouteBuilderPathExplorerSettings(aiClient.getWorld(),
			WorldConstants.get().TR_STANDARD_TRACK,
			aiClient.getPlayerPrincipal(),
			REFERENCE_ENGINE_TYPE_INDEX, STANDARD_LOAD);
	    PathExplorer pe = new
		RouteBuilderPathExplorer(aiClient.getWorld(), cd.p1.x,
			cd.p1.y, CompassPoints.NORTH, s);
	    PathFinder pf = new PathFinder(pe, cd.p2.x, cd.p2.y, 0,
		    (int) ((cashAvailable + COMFORT_ZONE) / 100));
	    LinkedList results = pf.explore();
	    if (results == null) {
		// no route to this destination
		cd = null;
		continue;
	    }

	    // we found a route, how much ?
	    Iterator i = results.listIterator(0);
	    long cost = 0;
	    while (i.hasNext()) {
		pe = (PathExplorer) i.next();
		cost += pe.getCost();
	    }
	    // Costs in PathExplorers is in 100$ units
	    cost *= 100;
	    ce.detailedEstimate = true;
	    ce.constructionEstimate = cost + getFixedConstructionCosts();
	    ce.annualRevenueEstimate = cd.returnTripRevenue;
	    ce.plannedRoute = results;

	    /* TODO Recalculate annual maintenance and operating expense on
	     * track */
	    logger.log(Level.INFO, "The best route between for " + ce +
		    " was found");

	    return;
	}
    }

    /** compute a score indicating the priority of building the most favoured
     * route. This can be compared against the priority of other tasks (e.g.
     * purchasing trains, station upgrades etc.) before doing the task in
     * order to determine which activity to undertake first, in the event that
     * there are insufficient resources to perform all tasks. */
    public int getTaskPriority() {
	if (taskPlan == null)
	    return 0;
	
	return (int) (taskPlan.annualRevenueEstimate -
		taskPlan.annualCostEstimate);
    }

    public void doTask() {
	if (taskPlan == null)
	    return;

	routeBuilderMoveFactory.processPlannedRoute(taskPlan);
	shouldRebuildCache = true;
	taskPlan = null;


    }

    public long getTaskCost() {
	if (taskPlan == null)
	    return 0L;

	return taskPlan.constructionEstimate;
    }

    /**
     * Setup the SupplyDemandViewer with either the location corresponding to
     * the stationId if specified, or the city using the city radius.
     */
    private void setupSupplyDemandViewer(int stationId, int cityId,
	    SupplyDemandViewer sdv) {
	ReadOnlyWorld w = aiClient.getWorld();
	if (stationId == -1) {
	    CityModel cm = (CityModel) w.get(KEY.CITIES,
		    cityId, Player.AUTHORITATIVE);
	    sdv.setStationNotBuilt(true);
	    sdv.setLocation(cm.getCityX(), cm.getCityY(), cm.getCityRadius());
	} else {
	    StationModel sm = (StationModel) w.get
		(KEY.STATIONS, stationId, aiClient.getPlayerPrincipal());
	    FreerailsTile ft = (FreerailsTile) w.getTile(sm.getStationX(),
		    sm.getStationY());
	    BuildingType bt = (BuildingType) w.get(KEY.BUILDING_TYPES, 
		    ft.getBuildingTile().getType(), Player.AUTHORITATIVE);
	    sdv.setStationNotBuilt(false);
	    sdv.setLocation(sm.getStationX(), sm.getStationY(),
		    bt.getStationRadius());
	}
    }

    /**
     * @param cb1 the cargo sent from station 1
     * @param cb2 the cargo sent from station 2
     */
    private void calculateOutboundReturnCargoBundles
	(SupplyAtStation s1, SupplyAtStation s2, DemandAtStation d1,
	 DemandAtStation d2, ConvertedAtStation c1, ConvertedAtStation c2,
	 int[] cargo1, int[] cargo2) {
	    // stores cargo produced by conversion
	    int[] producedCargo1 = new int[nCargoTypes];
	    int[] producedCargo2 = new int[nCargoTypes];

	    boolean converted = false;
	    for (int i = 0; i < nCargoTypes; i++) {
		cargo1[i] += s1.getSupply(i);
		cargo2[i] += s2.getSupply(i);

		// calculate amount manufactured at far end

		if (c1.isCargoConverted(i) && s2.getSupply(i) > 0) {
		    producedCargo1[c1.getConversion(i)] += s2.getSupply(i);
		    converted = true;
		    logger.log(Level.FINEST, "converted " + i + " to " + 
			    c1.getConversion(i));
		}
		if (c2.isCargoConverted(i) && s1.getSupply(i) > 0) {
		    producedCargo2[c2.getConversion(i)] += s1.getSupply(i);
		    converted = true;
		    logger.log(Level.FINEST, "converted " + i + " to " + 
			    c2.getConversion(i));
		}
	    }

	    if (converted) {
		logger.log(Level.FINEST, "recursing..");
		calculateOutboundReturnCargoBundles
		    (new SupplyAtStation(producedCargo1),
		     new SupplyAtStation(producedCargo2),
		     d1, d2, c1, c2, cargo1, cargo2);
	    }
	}

    private Transaction[] getTransactions(int stationId, int cityId,
	    CargoBundle cb, long t) {
	int x, y;
	if (stationId < 0) {
	    CityModel cm = (CityModel) aiClient.getWorld().get
		(KEY.CITIES, cityId, Player.AUTHORITATIVE);
	    x = cm.getCityX();
	    y = cm.getCityY();
	} else {
	    StationModel sm = (StationModel) aiClient.getWorld().get
		(KEY.STATIONS, stationId, aiClient.getPlayerPrincipal());
	    x = sm.getStationX();
	    y = sm.getStationY();
	}
	return cargoPaymentCalculator.calculatePayment(cb, x, y, new
		GameTime((int) t));
    }

    /**
     * @return a new CargoBatch, created at time t = 0, from the specified
     * station or city.
     */
    private CargoBundle getCargoBundle
	(int stationId, int cityId, int[] amounts) {
	CargoBatch cb;
	CargoBundle cBundle = new CargoBundleImpl();
	int x, y;
	if (stationId >= 0) {
	    StationModel sm = (StationModel) aiClient.getWorld().get
		(KEY.STATIONS, stationId, aiClient.getPlayerPrincipal());
	    x = sm.getStationX();
	    y = sm.getStationY();
	} else {
	    CityModel cm = (CityModel) aiClient.getWorld().get(KEY.CITIES,
		    cityId, Player.AUTHORITATIVE);
	    x = cm.getCityX();
	    y = cm.getCityY();
	}
	for (int i = 0; i < nCargoTypes; i++) {
		cb = new CargoBatch(i, x, y,
		       	0L, stationId);
		cBundle.addCargo(cb, amounts[i]);
	}
	return cBundle;
    }

    /**
     * @return the number of trains required to haul the annual cargo between
     * the two destinations.
     */
    private int calculateTrainsRequired(int[] cb, float tripsPerYear) {
	// calculate the number of wagons required in total
	ReadOnlyWorld w = aiClient.getWorld();
	FreerailsPrincipal p = aiClient.getPlayerPrincipal();
	int nWagons = 0;
	for (int i = 0; i < nCargoTypes; i++) {
	    WagonType wt = null;
	    for (int j = 0; j < w.size(KEY.WAGON_TYPES, Player.AUTHORITATIVE);
		    j++) {
		WagonType tempWt = (WagonType) w.get(KEY.WAGON_TYPES, j,
			Player.AUTHORITATIVE);
		if (tempWt.getCargoType() == i) {
		    wt = tempWt;
		    break;
		}
	    }
	    if (wt == null)
		continue;
	    nWagons += cb[i] / wt.getCapacity();
	    if ((cb[i] % wt.getCapacity()) > 0)
		nWagons++;
	}

	// assume 4 wagons per train
	int nTrains = (int) ((float) nWagons / (4.0f * tripsPerYear));
	if (nTrains == 0)
	    nTrains = 1;
	return nTrains;
    }

    private class CargoData implements Comparable {
	/** Site of station1 */
	Point p1;
	/** Site of station2 */
	Point p2;
	long returnTripRevenue;

	public CargoData(Point p1, Point p2, long r) {
	    this.p1 = p1;
	    this.p2 = p2;
	    returnTripRevenue = r;
	}

	public boolean equals(Object o) {
	    if (! (o instanceof CargoData))
		return false;

	    return returnTripRevenue == ((CargoData) o).returnTripRevenue;
	}

	public int hashCode() {
	    return (int) returnTripRevenue;
	}

	public int compareTo(Object o) {
	    CargoData cd = (CargoData) o;
	    if (returnTripRevenue < cd.returnTripRevenue)
		return -1;
	    if (returnTripRevenue > cd.returnTripRevenue)
		return 1;
	    return 0;
	}
    }

    private void dumpTop10() {
	if (logger.isLoggable(Level.INFO)) {
	    // log top 10 routes
	    logger.log(Level.INFO, "Cost estimates initial estimate:");
	    for (int i = 0; i < (cityDistances.size() > 10 ? 10 :
			cityDistances.size()); i++) {
		logger.log(Level.INFO, cityDistances.get(i).toString());
	    }
	    }
    }
}
