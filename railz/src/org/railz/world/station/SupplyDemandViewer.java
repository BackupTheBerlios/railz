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
package org.railz.world.station;

import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
/**
 * Calculates aggregated supply and demand information at a specific location
 */
public final class SupplyDemandViewer {
    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo
     */
    private static final int PREREQUISITE_FOR_DEMAND = 16;

    private ReadOnlyWorld world;

    private int tileX;
    private int tileY;

    private SupplyAtStation supplyAtStation;

    private int maxStationRadius = -1;

    private int[] supplies;
    private int[] demand;
    private int[] converts;

    private boolean stationNotBuilt = false;

    /**
     * Sets whether we are considering a prospective station construction, or
     * an existing station. By default this value is false.
     */
    public void setStationNotBuilt(boolean b) {
	stationNotBuilt = b;
    }

    public SupplyDemandViewer(ReadOnlyWorld w) {
	world = w;
	// initialize maxStationRadius
	NonNullElements i = new NonNullElements(KEY.BUILDING_TYPES, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    BuildingType bt = (BuildingType) i.getElement();
	    if (bt.getStationRadius() > maxStationRadius)
		maxStationRadius = bt.getStationRadius();
	}
    }

    /**
     * Update the cache of supply and demand for this location. Call this
     * after all other properties have been set. 
     */
    public void setLocation(int x, int y, int catchmentRadius) {
	int nCargoTypes = world.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
	supplies = new int[nCargoTypes];
	demand = new int[nCargoTypes];
	converts = new int[nCargoTypes];
	for (int i = 0; i < converts.length; i++)
	    converts[i] = ConvertedAtStation.NOT_CONVERTED;

	tileX = x;
	tileY = y;
    
	// xmin/max, ymin/max - boundaries of our station radius
	int xmin = x < catchmentRadius ? 0 : x - catchmentRadius;
	int xmax = x + catchmentRadius;
	if (xmax >= world.getMapWidth())
	    xmax = world.getMapWidth() - 1;
	int ymin = y < catchmentRadius ? 0 : y - catchmentRadius;
	int ymax = y + catchmentRadius;
	if (ymax >= world.getMapHeight())
	    ymax = world.getMapHeight() - 1;
	// Build a cache of demand which could be within sufficient distance
	// to be competing for resources
	int[][] competingStations = 
	    new int[catchmentRadius * 2 + 1][catchmentRadius * 2 + 1];
	// xxmin/max, yymin/max - bounds of where to find competing stations
	int xxmin = x - catchmentRadius - maxStationRadius;
	int xxmax = x + catchmentRadius + maxStationRadius;
	int yymin = y - catchmentRadius - maxStationRadius;
	int yymax = y + catchmentRadius + maxStationRadius;
	xxmin = xxmin < 0 ? 0 : xxmin;
	xxmax = xxmax >= world.getMapWidth() ? world.getMapWidth() - 1 :
	    xxmax;
	yymin = yymin < 0 ? 0 : yymin;
	yymax = yymax >= world.getMapHeight() ? world.getMapHeight() - 1 :
	    yymax;
	
	for (int xx = xxmin; xx <= xxmax; xx++) {
	    for (int yy = yymin; yy <= yymax; yy++) {
		FreerailsTile ft = world.getTile(xx, yy);
		BuildingTile bt = ft.getBuildingTile();
		if (bt == null)
		    continue;

		BuildingType bType = (BuildingType)
		    world.get(KEY.BUILDING_TYPES, bt.getType(),
			    Player.AUTHORITATIVE);

		if (bType.getStationRadius() > 0) {
		    // xxxmin/max, yyymin/max - bounds of competing station
		    // radius
		    int xxxmin = xx - bType.getStationRadius();
		    int yyymin = yy - bType.getStationRadius();
		    int xxxmax = xx + bType.getStationRadius();
		    int yyymax = yy + bType.getStationRadius();
		    xxxmin = xxxmin < xmin ? xmin : xxxmin;
		    xxxmax = xxxmax > xmax ? xmax : xxxmax;
		    yyymin = yyymin < ymin ? ymin : yyymin;
		    yyymax = yyymax > ymax ? ymax : yyymax;
		    for (int yyy = yyymin; yyy <= yyymax; yyy++) {
			for (int xxx = xxxmin; xxx <= xxxmax; xxx++) {
			    competingStations[xxx - xmin][yyy - ymin]++;
			}
		    }
		}
	    }
	}

        //Look at the terrain type of each tile and retrieve the cargo supplied.
        //The station radius determines how many tiles each side we look at. 		
        for (int i = xmin; i <= xmax; i++) {
            for (int j = ymin; j <= ymax; j++) {
		/* If the station is not built yet then the number of stations
		 * is increased by one */
		if (stationNotBuilt)
		    competingStations[i -xmin][j - ymin]++;

                incrementSupplyAndDemand(i, j,
		       	competingStations[i - xmin][j - ymin]);
            }
        }
    }

    private void incrementSupplyAndDemand(int i, int j, int competingStations) {
	BuildingTile bTile = world.getTile(i, j).getBuildingTile();
	if (bTile == null)
	    return;

        int tileTypeNumber = bTile.getType();

        BuildingType buildingType = (BuildingType)world.get(KEY.BUILDING_TYPES,
                tileTypeNumber, Player.AUTHORITATIVE);

        //Calculate supply.
        Production[] production = buildingType.getProduction();

        //loop throught the production array and increment 
        //the supply rates for the station
        for (int m = 0; m < production.length; m++) {
            int type = production[m].getCargoType();
            int rate = production[m].getRate();
	    supplies[type] += rate / competingStations;
        }

        //Now calculate demand.
        Consumption[] consumption = buildingType.getConsumption();

        for (int m = 0; m < consumption.length; m++) {
            int type = consumption[m].getCargoType();
            int prerequisite = consumption[m].getPrerequisite();

            //The prerequisite is the number tiles of this type that must 
            //be within the station radius before the station demands the cargo.			
            demand[type] += PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        Conversion[] conversion = buildingType.getConversion();

        for (int m = 0; m < conversion.length; m++) {
            int type = conversion[m].getInput();

            //Only one tile that converts the cargo type is needed for the station to demand the cargo type.				
            demand[type] += PREREQUISITE_FOR_DEMAND;
            converts[type] = conversion[m].getOutput();
        }
    }

    public SupplyAtStation getSupply() {
	return new SupplyAtStation(supplies);
    }

    public DemandAtStation getDemand() {
        boolean[] demandboolean = new boolean[world.size(KEY.CARGO_TYPES,
		Player.AUTHORITATIVE)];

	for (int i = 0; i < world.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
		i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new DemandAtStation(demandboolean);
    }

    public ConvertedAtStation getConversion() {
        return new ConvertedAtStation(this.converts);
    }

    public int getMaxStationRadius() {
	return maxStationRadius;
    }
}
