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
package jfreerails.controller;

import java.awt.Point;
import jfreerails.move.*;
import jfreerails.world.building.*;
import jfreerails.world.common.*;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;
import jfreerails.world.player.*;

public class StationBuilder {
    private UntriedMoveReceiver moveReceiver;
    private ReadOnlyWorld w;
    private int ruleNumber;
    private TrackMoveTransactionsGenerator transactionsGenerator;
    private FreerailsPrincipal stationOwner;

    public StationBuilder(UntriedMoveReceiver moveReceiver,
        ReadOnlyWorld world, FreerailsPrincipal p) {
        this.moveReceiver = moveReceiver;
	stationOwner = p;
        w = world;

        BuildingType bType;

        int i = -1;

        do {
            i++;
            bType = (BuildingType)w.get(KEY.BUILDING_TYPES, i);
        } while (bType.getCategory() != BuildingType.CATEGORY_STATION);

        ruleNumber = i;
        transactionsGenerator = new TrackMoveTransactionsGenerator(w, p);
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

	/* further condition is that track must be straight with no branches */
	byte reference = (byte) (CompassPoints.NORTH | CompassPoints.SOUTH);
	byte currentLayout = oldTile.getTrackTile().getTrackConfiguration();
	for (int i = 0; i < 3; i++) {
	    if (currentLayout == reference)
		return true;
	    currentLayout = CompassPoints.rotateClockwise(currentLayout);
	}
	return false;
    }

    public void buildStation(Point p) {
        FreerailsTile oldTile = w.getTile(p.x, p.y);

        //Only build a station if there is track at the specified point.
        if (canBuiltStationHere(p)) {
            String cityName;
            String stationName;

	    BuildingTile bTile = oldTile.getBuildingTile();
	    BuildingType bType = null;
	    if (bTile != null) {
		bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
			bTile.getType(), Player.AUTHORITATIVE);

		if (bTile == null || bType.getCategory() !=
			BuildingType.CATEGORY_STATION) {
		    //There isn't already a station here, we need to pick a name
		    //and add an entry to the station list.
		    CalcNearestCity cNC = new CalcNearestCity(w, p.x, p.y);
		    cityName = cNC.findNearestCity();

		    VerifyStationName vSN = new VerifyStationName(w, cityName);
		    stationName = vSN.getName();

		    if (stationName == null) {
			//there are no cities, this should never happen
			stationName = "Central Station";
		    }

		    //check the terrain to see if we can build a station on it...
		    Move m = AddStationMove.generateMove(w, stationName, p,
			    stationOwner, ruleNumber);

		    this.moveReceiver.processMove
		    (transactionsGenerator.addTransactions(m));
		} else {
		    //Upgrade an existing station.
		    ChangeBuildingMove cbm = new ChangeBuildingMove(p, bTile,
			    new BuildingTile(ruleNumber), stationOwner);
		    this.moveReceiver.processMove(cbm);
		}
            }
        } else {
            System.err.println(
                "Can't build station here");
        }
    }

	/**
	 * @param ruleNumber an index into the BUILDING_TYPES table
	 */
    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }
}
