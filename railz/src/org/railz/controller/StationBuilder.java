/*
 * Copyright (C) 2002 Luke Lindsay
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
 * @author Luke Lindsay 08-Nov-2002
 *
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 *
 * Class to build a station at a given point, names station after nearest
 * city. If that name is taken then a "Junction" or "Siding" is added to
 * the name.
 */
package org.railz.controller;

import java.awt.Point;
import org.railz.move.*;
import org.railz.world.building.*;
import org.railz.world.common.*;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.FreerailsTile;
import org.railz.world.track.TrackRule;
import org.railz.world.player.*;

public class StationBuilder {
    private UntriedMoveReceiver moveReceiver;
    private ReadOnlyWorld w;
    private int ruleNumber;
    private FreerailsPrincipal stationOwner;
    private StationBuilderMoveFactory moveFactory;

    public StationBuilder(UntriedMoveReceiver moveReceiver,
        ReadOnlyWorld world, FreerailsPrincipal p) {
        this.moveReceiver = moveReceiver;
	stationOwner = p;
        w = world;

        BuildingType bType;

        int i = -1;

        do {
            i++;
            bType = (BuildingType)w.get(KEY.BUILDING_TYPES, i,
		    Player.AUTHORITATIVE);
        } while (bType.getCategory() != BuildingType.CATEGORY_STATION);

        ruleNumber = i;
        moveFactory = new StationBuilderMoveFactory(world);
    }

    public boolean canBuiltStationHere(Point p) {
        FreerailsTile oldTile = w.getTile(p.x, p.y);
        
        if (oldTile.getTrackTile() == null)
            return false;
        
        /* if there is a building present, it must be a station */
        BuildingTile bTile = oldTile.getBuildingTile();
        if (bTile != null) {
            BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
                    bTile.getType(), Player.AUTHORITATIVE);
            if (bType.getCategory() != BuildingType.CATEGORY_STATION)
                return false;
        }
        
        return true;
    }

    public void buildStation(Point p) {
        if (canBuiltStationHere(p)) {
            Move m = moveFactory.createStationBuilderMove(p, stationOwner, ruleNumber);
            if (m != null)
                moveReceiver.processMove(m);
        }
    }

    /**
     * @param ruleNumber an index into the BUILDING_TYPES table
     */
    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }    
}
