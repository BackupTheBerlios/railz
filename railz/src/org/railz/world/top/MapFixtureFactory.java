/*
 * Copyright (C) Luke Lindsay
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

package org.railz.world.top;

import java.util.HashSet;

import org.railz.world.building.*;
import org.railz.world.cargo.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.track.*;
import org.railz.world.train.*;

/**
 * This class is used to generate fixures for Junit tests.
 *
 * @author Luke
 *
 */
public class MapFixtureFactory {
    public int w = 10;
    public int h = 10;
    public World world;
    protected byte legalTrackConfigurations[][] = new byte[3][];
    protected boolean legalTrackPlacement[][] = new boolean[3][];

    public MapFixtureFactory(int w, int h) {
	this.w = w;
	this.h = h;
	world = new WorldImpl(w, h);
	generateTrackRuleList(world);
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		world.setTile(x, y, new FreerailsTile(0, null, null));
	    }
	}
    }

    public MapFixtureFactory() {
	world = new WorldImpl(w, h);
        generateTrackRuleList(world);
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		world.setTile(x, y, new FreerailsTile(0, null, null));
	    }
	}
    }

    public void generateTrackRuleList(World world) {
        TrackRule[] trackRulesArray = new TrackRule[3];
        //1st track type..
        byte[] trackTemplates0 = {
            CompassPoints.NORTH,
	    CompassPoints.NORTH | CompassPoints.SOUTH,
	    CompassPoints.NORTHWEST | CompassPoints.WEST | CompassPoints.EAST,
	    CompassPoints.NORTHEAST | CompassPoints.WEST | CompassPoints.EAST,
            CompassPoints.NORTH | CompassPoints.WEST,
	    CompassPoints.NORTHWEST | CompassPoints.WEST,
	    CompassPoints.NORTH | CompassPoints.SOUTHEAST
        };

        legalTrackConfigurations[0] = trackTemplates0;
        legalTrackPlacement[0] = new boolean[] { true, false, true, true };
        trackRulesArray[0] = new TrackRule(0, "standard track", false, 10,
		legalTrackConfigurations[0], 0, legalTrackPlacement[0], false);

        //2nd track type..
        legalTrackConfigurations[1] = new byte[] {
	    CompassPoints.NORTH,
	    CompassPoints.NORTH | CompassPoints.SOUTH
	};

        legalTrackPlacement[1] = new boolean[] { true, false, true, true };
        trackRulesArray[1] = new TrackRule(0, "type1", false, 20,
		legalTrackConfigurations[1], 0, legalTrackPlacement[1], false);

        //3rd track type..
        legalTrackConfigurations[2] = new byte[0];
        legalTrackPlacement[2] = new boolean[] { true, false, true, true };
        trackRulesArray[2] = new TrackRule(0, "type2", false, 30,
		legalTrackConfigurations[2], 0, legalTrackPlacement[2], false);

        //Add track rules to world
        for (int i = 0; i < trackRulesArray.length; i++) {
	    world.add(KEY.TRACK_RULES, trackRulesArray[i],
		    Player.NOBODY);
        }

        //Add a single terrain type..		
        //We need this since when we built track, the terrain type gets check to see if we can
        //built track on it and an exception is thrown if terrain type 0 does not exist.
	world.add(KEY.TERRAIN_TYPES, new TerrainType(0,
		    TerrainType.CATEGORY_COUNTRY, "Clear", 1000L, 0, 0),
	       	Player.NOBODY);
	world.add(KEY.TERRAIN_TYPES, new TerrainType(1,
		    TerrainType.CATEGORY_OCEAN, "Ocean", 0L, 0, 0),
		Player.NOBODY);
    }

    public FreerailsPrincipal addPlayer(String name, int id) {
	Player p = new Player(name);
	p = new Player(name, p.getPublicKey(), id);
	world.add(KEY.PLAYERS, p, Player.AUTHORITATIVE);
	return p.getPrincipal();
    }

    public static final int TT_CLEAR = 0;
    public static final int TT_OCEAN = 1;
    public static final int BT_CITY = 0;
    public static final int CT_PASSENGER = 0;

    public void setupCargoTypes() {
	world.add(KEY.CARGO_TYPES, new CargoType("passenger",
		TransportCategory.PASSENGER, 1000L, 30 * 45, 30 * 90),
	Player.NOBODY); 
    }

    public void setupBuildingTypes() {
	// add a building type demanding and producing passengers
	world.add(KEY.BUILDING_TYPES,
		new BuildingType("city",
		new Production[] {new Production(CT_PASSENGER, 100)},       
		new Consumption[] {new Consumption(CT_PASSENGER)},
		new Conversion[0],
		1000L, 0, new byte[0], new boolean[] {true, false},
	       	new boolean[] {true, true},
	       	new BuildingType.DistributionParams[] 
		{new BuildingType.DistributionParams(1.0, 1.0, 1.0),
		new BuildingType.DistributionParams(1.0, 1.0, 1.0)}),
		Player.NOBODY);
	// add a station building type
	world.add(KEY.BUILDING_TYPES,
		new BuildingType("station",
		    1000L, 2, new byte[] { CompassPoints.NORTH,
		     (byte) (CompassPoints.NORTH | CompassPoints.SOUTH)},
		     new boolean[] {true, false}, new boolean[]
		    {true, true}, new BuildingType.DistributionParams[]
		    {new BuildingType.DistributionParams(1.0, 1.0, 1.0),
		   new BuildingType.DistributionParams(1.0, 1.0, 1.0) }),
		Player.NOBODY);
    }

    public void setupCalendar() {
	world.set(ITEM.CALENDAR, new GameCalendar(30, 1830, 30), Player.NOBODY);
	world.set(ITEM.TIME, new GameTime(0), Player.NOBODY);
    }

    public void setupStationImprovements() {
	world.add(KEY.STATION_IMPROVEMENTS, new StationImprovement
		("WaterTower", "A water tower", 1000L, new int[0], new
		 int[0]), Player.NOBODY);
    }

    public void setupEngineTypes() {
	world.add(KEY.ENGINE_TYPES, new EngineType
		("Test Engine Type", 6000L, 5000L, 600,
		 EngineType.FUEL_TYPE_COAL, 1000, 40, 2000, 400, 0.2f, 0.7f,
		 true), Player.NOBODY);
    }

    public void setupEconomy() {
	world.set(ITEM.ECONOMY, new Economy(25, 5.0f), Player.NOBODY);
    }
}
