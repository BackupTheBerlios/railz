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
    
    /** Max age of cache in ticks */
    private final long MAX_CACHE_AGE;

    /** The amount of money we should try to have left after building the
     * route */
    private static final long COMFORT_ZONE = 200000L;

    private GameTime routeCacheTimeStamp;
    private boolean shouldRebuildCache = true;

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
    /** The standard load used to determine the reference engine type */
    private static final int STANDARD_LOAD = 180;
    /** The reference engine type - which is the best at hauling the standard
     * load over zero gradient */
    private EngineType REFERENCE_ENGINE_TYPE;
    private float REFERENCE_ENGINE_TYPE_MAX_SPEED;

    private final AIClient aiClient;
    private CityModelViewer cityModelViewer;
    private TrackTileViewer trackTileViewer;
    private CargoPaymentCalculator cargoPaymentCalculator;
    private boolean haveNoStations;

    private final int nCargoTypes;

    public RouteBuilder (AIClient aic) {
	aiClient = aic;
	MAX_CACHE_AGE = ((GameCalendar) aiClient.getWorld().get(ITEM.CALENDAR,
		Player.AUTHORITATIVE)).getTicksPerDay() * 28;
	nCargoTypes = aiClient.getWorld().size(KEY.CARGO_TYPES,
		Player.AUTHORITATIVE);
    }

    private boolean isInitialised = false;

    private class CityEntry implements Comparable {
	/** index to CITIES table */
	int city1;
	/** index to CITIES table */
	int city2;
	/** index to STATIONS table */
	int station1 = -1;
	/** index to STATIONS table */
	int station2 = -1;
	
	/** Distance between city1 and city2 */
	PathLength distance;
	/** construction cost estimate */
	long constructionEstimate;
	/** estimate for gross profit per year **/
	long profitEstimate;

	/** whether we have performed the detailed estimate, or just the easy
	 * one */
	boolean detailedEstimate;


	public CityEntry(int c1, int c2, PathLength d) {
	    city1 = c1;
	    city2 = c2;
	    distance = new PathLength(d);
	}

	/**
	 * @return the annual return as a fraction of the construction cost
	 */
	public float getAnnualReturn() {
	    return ((float) profitEstimate) / constructionEstimate;
	}

	public int hashCode() {
	    return Float.floatToRawIntBits(getAnnualReturn());
	}

	public boolean equals(Object o) {
	    if (! (o instanceof CityEntry))
		return false;

	    return compareTo(o) == 0;
	}

	public int compareTo(Object o) {
	    CityEntry ce = (CityEntry) o;
	    if (constructionEstimate == 0)
		return distance.compareTo(ce.distance);

	    float annualReturn = getAnnualReturn();
	    float ceAnnualReturn = ce.getAnnualReturn();
	    // highest annual return is smallest
	    if (annualReturn > ceAnnualReturn) {
		return -1;
	    } else if (annualReturn < ceAnnualReturn) {
		return 1;
	    }
	    return 0;
	}

	public String toString() {
	    CityModel cm1 = (CityModel) aiClient.getWorld().get
		(KEY.CITIES, city1, Player.AUTHORITATIVE);
	    CityModel cm2 = (CityModel) aiClient.getWorld().get
		(KEY.CITIES, city2, Player.AUTHORITATIVE);
	    return cm1.getCityName() + " <=> " + cm2.getCityName() + ", c=" +
		constructionEstimate + ", p= " + profitEstimate;
	}
    }

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
	int stationRadius = 0;
	while (i.next()) {
	    BuildingType bt = (BuildingType) i.getElement();
	    if (bt.getStationRadius() > stationRadius) {
		stationRadius = bt.getStationRadius();
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

	NonNullElements j = new NonNullElements(KEY.ENGINE_TYPES,
		w, Player.AUTHORITATIVE);
	long cheapestEngineCost = Long.MAX_VALUE;
	while (j.next()) {
	    EngineType et = (EngineType) j.getElement();
	    if (et.getPrice() < cheapestEngineCost &&
		    et.isAvailable())
		cheapestEngineCost = et.getPrice();
	}

	for (int i = 0; i < cityDistances.size(); i++) {
	    long costEstimate = 0;
	    CityEntry ce = (CityEntry) cityDistances.get(i);
	    costEstimate = ce.distance.straightLength * 
		(straightTrackUnitCost + terrainUnitCost) + 
		ce.distance.diagLength * (diagTrackUnitCost +
			terrainUnitCost);

	    // add in cost for 1 or 2 stations, plus an engine
	    if (haveNoStations) {
		costEstimate += 2 * LARGE_STATION_COST;
	    } else {
		costEstimate += LARGE_STATION_COST;
	    }
	    
	    // add cost of cheapest available engine
	    costEstimate += cheapestEngineCost;
	    ce.constructionEstimate = costEstimate;
	}
    }

    private void rebuildCache() {
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
		CityEntry ce = new CityEntry(i.getIndex(), j.getIndex(),
			new PathLength(cm1.getCityX(), cm1.getCityY(),
			    cm2.getCityX(), cm2.getCityY()));
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
	long cashAvailable = ((BankAccount) aiClient.getWorld()
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
	    }
	}

	GameCalendar gc = (GameCalendar) aiClient.getWorld().get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	long ticksPerYear = gc.getTicksPerDay() * 365;

	/* Calculate supply and demand at all stations/cities */
	SupplyDemandViewer sdv = new SupplyDemandViewer(aiClient.getWorld());
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

	    // now we have got the supply and demand, work out the total
	    // number of tonnes we can carry in outbound and return journeys
	    // over the course of 1 year
	    int[] cb1 = new int[nCargoTypes];
	    int[] cb2 = new int[nCargoTypes];
	    calculateOutboundReturnCargoBundles(supply1, supply2, demand1,
		    demand2, convert1, convert2, cb1, cb2);

	    // round off tonnage so that they are within 75% of each other
	    // (returning empty wagons is unprofitable)
	    int tonnage1 = 0;
	    int tonnage2 = 0;
	    for (int k = 0; k < nCargoTypes; k++) {
		tonnage1 += cb1[k];
		tonnage2 += cb2[k];
	    }
	    if (tonnage1 > (tonnage2 * 4) / 3) {
		float factor = ((tonnage2 * 4) / 3) / tonnage1;
		for (int k = 0; k < nCargoTypes; k++)
		   cb1[k] = (int) (factor * (float) cb1[k]); 
	    } else if (tonnage2 > (tonnage1 * 4 / 3)) {
		float factor = ((tonnage1 * 4) / 3) / tonnage2;
		for (int k = 0; k < nCargoTypes; k++)
		   cb2[k] = (int) (factor * (float) cb2[k]); 
	    }
	    // calculate our annual revenue
	    long elapsedTicks = (long) ((ce.distance.getLength() *
		    TrackTile.DELTAS_PER_TILE) / maxSpeed);
	    float tripsPerYear = ((float) maxSpeed * ticksPerYear) /
	       	(float) (ce.distance.getLength() * TrackTile.DELTAS_PER_TILE);

	    /* The cargo sent from station 1 */
	    CargoBundle cBundle1 = getCargoBundle(ce.station1, ce.city1, cb1);
	    /* The cargo sent from station 2 */
	    CargoBundle cBundle2 = getCargoBundle(ce.station2, ce.city2,
		    cb2);
	    /* revenue for journey from 1 to 2 */
	    Transaction[] t1 = getTransactions(ce.station2, ce.city2,
		    cBundle1, elapsedTicks);
	    /* revenue for journey from 2 to 1 */
	    Transaction[] t2 = getTransactions(ce.station1, ce.city1,
		    cBundle2, elapsedTicks);
	    long tripRevenue = 0;
	    for (int k = 0; k < t1.length; k++)
		tripRevenue += t1[k].getValue();
	    for (int k = 0; k < t2.length; k++)
		tripRevenue += t2[k].getValue();
	    long annualRevenue = (long) tripRevenue;

	    /* calculate our annual cost */
	    int[] cargo;
	    if (tonnage1 > tonnage2) {
		cargo = cb1;
	    } else {
		cargo = cb2;
	    }
	    int nTrains = calculateTrainsRequired(cargo, tripsPerYear);
	    Economy e = (Economy) aiClient.getWorld().get(ITEM.ECONOMY,
		    Player.AUTHORITATIVE);
	    long annualCost = REFERENCE_ENGINE_TYPE.getMaintenance() +
		REFERENCE_ENGINE_TYPE.getAnnualFuelConsumption() *
		e.getFuelUnitPrice(REFERENCE_ENGINE_TYPE.getFuelType());
	    annualCost *= nTrains;
	    ce.profitEstimate = annualRevenue - annualCost;
	    
	    // remove all those lines with negative profit !
	    if (ce.profitEstimate < 0) {
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
	    if (logger.isLoggable(Level.INFO)) {
		// log top 10 routes
		logger.log(Level.INFO, "Cost estimates initial estimate:");
		for (int i = 0; i < (cityDistances.size() > 10 ? 10 :
			    cityDistances.size()); i++) {
		    logger.log(Level.INFO, cityDistances.get(i).toString());
		}
	    }
	}

	return false;
    }

    /** computes a score based on the potential supply of a given location */
    private int computeSupplyScore(Point p) {
	/* TODO */
	return 0;
    }

    /** computes a score based on the potential demand of a given location */
    private int computeDemandScore(Point p) {
	/* TODO */
	return 0;
    }

    /** computes a score based on the supply and demand of both points */
    private int computeCombinedScore(Point p1, Point p2) {
	/* TODO */
	return 0;
    }

    /** compute a score indicating the priority of building the most favoured
     * route. This can be compared against the priority of other tasks (e.g.
     * purchasing trains, station upgrades etc.) before doing the task in
     * order to determine which activity to undertake first, in the event that
     * there are insufficient resources to perform all tasks. */
    public int getTaskPriority() {
	/* TODO */
	return 0;
    }

    public void doTask() {
	/* TODO */
    }

    public long getTaskCost() {
	/* TODO */
	return 0L;
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
}
