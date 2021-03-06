/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 26-May-2003
 *
 */
package org.railz.move;

import java.awt.Point;
import org.railz.world.accounts.*;
import org.railz.world.building.*;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.common.GameTime;
import org.railz.world.station.StationModel;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.TrackRule;


/**
 * This {@link CompositeMove} adds a station to the station list and adds a cargo bundle
 * (to store the cargo waiting at the station) to the cargo bundle list.
 *
 * @author Luke
 *
 */
public class AddStationMove extends CompositeMove {
    protected AddStationMove(Move[] moves) {
        super(moves);
    }

    /**
     * @param p the location of the station to add, in map tiles
     * @param w a copy of the world to add the station to
     * @param buildingType index into the BUILDING_TYPES table defining the type
     * of station to build
     * @param stationName the name of the station
     */
    public static AddStationMove generateMove(ReadOnlyWorld w,
	String stationName, Point p, FreerailsPrincipal owner, int
	buildingType) {
        CargoBundle cb = new CargoBundle();
        ObjectKey2 cargoBundleKey = new ObjectKey2(KEY.CARGO_BUNDLES, 
                Player.NOBODY, cb.getUUID());
        Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleKey,
                cb);        
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
        StationModel station = new StationModel(p.x, p.y, stationName,
		w.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE),
		cargoBundleKey, now);

        ObjectKey2 stationKey = new ObjectKey2(KEY.STATIONS, owner, 
                station.getUUID());
        Move addStation = new AddObjectMove(stationKey, station);
	BuildingTile oldBuilding = w.getTile(p).getBuildingTile();
	BuildingTile newBuilding = new BuildingTile(buildingType);
	ChangeBuildingMove cbm = new ChangeBuildingMove(p, oldBuilding,
		newBuilding, owner);

	BuildingType bt = (BuildingType) w.get(KEY.BUILDING_TYPES,
		buildingType, Player.AUTHORITATIVE);

	Transaction t = new AddItemTransaction(now,
		AddItemTransaction.BUILDING, buildingType, 1,
		-bt.getBaseValue());
	Move transactionMove = new AddTransactionMove(0, t, owner);

        return new AddStationMove(new Move[] {
                cbm, addCargoBundleMove, addStation, transactionMove
            });
    }
}
