/*
 * Copyright (C) Scott Bennett
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

/**
 * @author Scott Bennett
 * Date: 7th April 2003
 *
 * Class to randomly position the city tiles on the game map, within a 5x5 tile
 * area around a city. A random number of between 1 and 6 tiles are initially
 * chosen with the idea to have these increase over the period of a game.
 *
 * Updated 2nd November 2003 by Scott Bennett
 *
 * Class now randomly positions 1-6 urban tiles, 0-2 industry tiles and 0-2
 * resource tiles within the 5x5 grid that is a city. Subtypes of each of these
 * categories are randomly chosen also. The maximums for these categories
 * are currently hard-coded, another solution would be preferable i think.
 */
package jfreerails.server;

import java.awt.Point;

import java.util.ArrayList;
import jfreerails.world.city.*;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.building.*;
import jfreerails.world.player.*;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;


class CityTilePositioner {
    private World w;
    private double PROBABILITY_MULTIPLIER = 0.04; //Represents a 1/25 probability, ie. 1 tile, based on a 5x5 city
    private BuildingType type;
    private FreerailsTile tile;
    private ArrayList urbanTerrainTypes;
    private ArrayList industryTerrainTypes;
    private ArrayList resourceTerrainTypes;

    public CityTilePositioner(World world) {
        this.w = world;
        urbanTerrainTypes = new ArrayList();
        industryTerrainTypes = new ArrayList();
        resourceTerrainTypes = new ArrayList();

        //get the different types of Urban/Industry/Resource terrain
        for (int i = 0; i < w.size(KEY.BUILDING_TYPES); i++) {
            type = (BuildingType)w.get(KEY.BUILDING_TYPES, i);

	    switch (type.getCategory()) {
		case BuildingType.CATEGORY_URBAN:
		    urbanTerrainTypes.add(new Integer(i));
		    break;
		case BuildingType.CATEGORY_INDUSTRY:
		    industryTerrainTypes.add(new Integer(i));
		    break;
		case BuildingType.CATEGORY_RESOURCE:
		    resourceTerrainTypes.add(new Integer(i));
		    break;
		default:
		    // ignore
            }
        }

        doTilePositioning(6, 4, 2);
        //hard-coded limits at the moment (urban, industry, resource)
    }

    private void doTilePositioning(int urbMax, int indMax, int resMax) {
        for (int i = 0; i < w.size(KEY.CITIES); i++) {
            CityModel tempCity = (CityModel)w.get(KEY.CITIES, i);

            calculateAndPositionTiles(tempCity.getCityX(), tempCity.getCityY(),
                calcNumberOfInitialTiles(urbMax),
                calcNumberOfInitialTiles(indMax + 1) - 1,
                calcNumberOfInitialTiles(resMax + 1) - 1);
        }
    }

    private int calcNumberOfInitialTiles(int max) {
	return randomSelector(max);
    }

    private Integer selectBuildingType(int x, int y, int tileCategory) {
	final int MAX_TRIES = 3;
	int i = 0;
	Integer typeToAdd = null;
	do {
	    switch (tileCategory) {
		case BuildingType.CATEGORY_INDUSTRY:
		    typeToAdd = (Integer)
			industryTerrainTypes.get
			(randomSelector(industryTerrainTypes.size()) - 1);
		    break;
		case BuildingType.CATEGORY_URBAN:
		    typeToAdd = (Integer)
			urbanTerrainTypes.get
			(randomSelector(urbanTerrainTypes.size()) - 1);
		    break;
		case BuildingType.CATEGORY_RESOURCE:
		    typeToAdd = (Integer)
			resourceTerrainTypes.get
			(randomSelector(resourceTerrainTypes.size()) - 1);
		    break;
	    }
	    BuildingType bt = (BuildingType) w.get(KEY.BUILDING_TYPES,
		    typeToAdd.intValue(), Player.AUTHORITATIVE);
	    if (bt.canBuildHere(w, new Point(x,y)))
		return typeToAdd;
	    i++;
	} while (i < MAX_TRIES);
	return null;
    }

	/**
	 * @return a random value between 1 and max inclusive
	 */
    private int randomSelector(int max) {
	return ((int) (Math.random() * max)) + 1;
    }

    private int getCategoryForTile(int x, int y) {
        int tileTypeNumber = w.getTile(x, y).getTerrainTypeNumber();
	int category = ((TerrainType)w.get(KEY.TERRAIN_TYPES,
		    tileTypeNumber)).getTerrainCategory();

        return category;
    }

    private void calculateAndPositionTiles(int x, int y, int urbNo, int indNo,
        int resNo) {
        int cityX = x;
        int cityY = y;
        int urbanTiles = urbNo;
        int industryTiles = indNo;
        int resourceTiles = resNo;

        ArrayList industriesNotAtCity = new ArrayList(this.industryTerrainTypes);

        double tileProbability = (double)PROBABILITY_MULTIPLIER * (urbanTiles +
            industryTiles + resourceTiles);

        /*
         * loop until the correct amount of tiles have been built, sometimes
         * all the tiles may not get built due to ocean or something else
         * getting in the way, looping round tries a couple more times.
         */
        int loopCount = 0;

        while (((urbanTiles + industryTiles + resourceTiles) > 0) &&
                (loopCount < 3)) {
            for (int Y = cityY - 2; Y < cityY + 3; Y++) {
                for (int X = cityX - 2; X < cityX + 3; X++) {
                    if (w.boundsContain(X, Y)) {
                        if (Math.random() < tileProbability) {
			    Integer typeToAdd = null;
			    int tileTypeToBuild = randomSelector(3);
			    double myRand = Math.random();

			    if ((tileTypeToBuild == 1) && (urbanTiles > 0)) {
				urbanTiles--;
				typeToAdd = selectBuildingType(X, Y,
					BuildingType.CATEGORY_URBAN);
			    } else if ((tileTypeToBuild == 2) &&
                                        (industryTiles > 0) &&
                                        industriesNotAtCity.size() > 0) {
                                    /* We only want one of any industry type in the city.*/
				    int i = randomSelector
					(industriesNotAtCity.size()) - 1;
				    typeToAdd = selectBuildingType(X, Y,
					    BuildingType.CATEGORY_INDUSTRY);
                                    industryTiles--;
                                } else if ((tileTypeToBuild == 3) &&
                                        (resourceTiles > 0)) {
                                    resourceTiles--;
                                    typeToAdd = selectBuildingType(X, Y,
					    BuildingType.CATEGORY_RESOURCE);
                                }

                                if (typeToAdd != null) {
				    BuildingTile bt = new
					BuildingTile(typeToAdd.intValue());
                                    tile = w.getTile(X, Y);
				    tile = new FreerailsTile(tile, bt);
                                    w.setTile(X, Y, tile);
                                }
                            }
                    } //end bounds check
                } //end inner loop
            } //end outer loop
            loopCount += 1;
        } //end while
    }
}
