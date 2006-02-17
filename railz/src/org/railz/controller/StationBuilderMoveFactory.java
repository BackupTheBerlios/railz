/*
 * Copyright (C) 2006 Robert Tuck
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

package org.railz.controller;

import java.awt.Point;
import org.railz.move.AddStationMove;
import org.railz.move.ChangeBuildingMove;
import org.railz.move.Move;
import org.railz.world.building.BuildingTile;
import org.railz.world.building.BuildingType;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.FreerailsTile;

/**
 * This class generates the moves for building or upgrading a station
 * @author bob
 */
public class StationBuilderMoveFactory {
    private ReadOnlyWorld world;
    
    public StationBuilderMoveFactory(ReadOnlyWorld w) {
        world = w;
    }
        
    /**
     * @param p location of the station in map tiles
     * @param stationType an index into the BUILDING_TYPES table for the
     * station to build
     * @param owner the owner of the station to be built
     * @return the Move required to build the station
     */
    public Move createStationBuilderMove(Point p,
            FreerailsPrincipal owner, int stationType) {
        FreerailsTile oldTile = world.getTile(p.x, p.y);
               
        String cityName;
        String stationName;

        BuildingTile bTile = oldTile.getBuildingTile();
        BuildingType bType = null;
        if (bTile != null) {
            bType = (BuildingType) world.get(KEY.BUILDING_TYPES,
                    bTile.getType(), Player.AUTHORITATIVE);
        }

        if (bTile == null || bType.getCategory() !=
                BuildingType.CATEGORY_STATION) {
            //There isn't already a station here, we need to pick a name
            //and add an entry to the station list.
            CalcNearestCity cNC = new CalcNearestCity(world, p.x, p.y);
            cityName = cNC.findNearestCity();

            VerifyStationName vSN = new VerifyStationName(world, cityName);
            stationName = vSN.getName();

            if (stationName == null) {
                //there are no cities, this should never happen
                stationName = "Central Station";
            }

            //check the terrain to see if we can build a station on it...
            return AddStationMove.generateMove(world, stationName, p,
                    owner, stationType);
        } else {
            //Upgrade an existing station.
            return new ChangeBuildingMove(p, bTile,
                    new BuildingTile(stationType), owner);
        }
    }
}
