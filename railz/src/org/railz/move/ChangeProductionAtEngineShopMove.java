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
 * Created on 28-Mar-2003
 *
 */
package org.railz.move;

import org.railz.world.station.ProductionAtEngineShop;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;


/**
 * This Move changes what is being built
 * at an engine shop - when a client wants to build a train, it
 * should send an instance of this class to the server.
 *
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMove implements Move {
    final ProductionAtEngineShop before;
    final ProductionAtEngineShop after;
    final ObjectKey2 stationKey;

    private FreerailsPrincipal principal;

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public ChangeProductionAtEngineShopMove(ProductionAtEngineShop b,
        ProductionAtEngineShop a, ObjectKey2 stationKey, FreerailsPrincipal p) {
        this.before = b;
        this.after = a;
        this.stationKey = stationKey;
	this.principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	/* Player must have at least 2 stations */	
	if (w.size(KEY.STATIONS, p) < 2)
	    return MoveStatus.moveFailed("Need at least 2 stations");

        // p must be the same player as owns the station
        if (! p.equals(Player.AUTHORITATIVE) && ! p.equals(stationKey.principal))
            return MoveStatus.moveFailed("Not your station");
        
	/* new engineType must be available */
	if (after != null) {
	    int etIndex = after.getEngineType();
	    EngineType et = (EngineType) w.get(KEY.ENGINE_TYPES,
		    etIndex, Player.AUTHORITATIVE);
	    if (et == null || !et.isAvailable())
		return MoveStatus.MOVE_FAILED;
	}

        return tryMove(w, before, p);
    }

    private MoveStatus tryMove(World w, ProductionAtEngineShop stateA,
	    FreerailsPrincipal p) {
        //Check that the specified station exists.
	StationModel station = (StationModel) w.get(stationKey);

        if (station == null) {
            return MoveStatus.MOVE_FAILED;
        }

        //Check that the station is building what we expect.					
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.MOVE_FAILED;
            }
        } else {
            if (station.getProduction().equals(stateA)) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.MOVE_FAILED;
            }
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, after, p);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel) w.get(stationKey);
            station = new StationModel(station, this.after);
            w.set(stationKey, station);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel) w.get(stationKey);
            station = new StationModel(station, this.before);
            w.set(stationKey, station);
        }

        return status;
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeProductionAtEngineShopMove) {
            ChangeProductionAtEngineShopMove arg = (ChangeProductionAtEngineShopMove)o;
	    if (! principal.equals(arg.principal)) {
		return false;
	    }

            boolean stationKeysEqual = stationKey.equals(arg.stationKey);
            boolean beforeFieldsEqual = (before == null ? arg.before == null
                                                        : before.equals(arg.before));
            boolean afterFieldsEqual = (after == null ? arg.after == null
                                                      : after.equals(arg.after));

            if (stationKeysEqual && beforeFieldsEqual && afterFieldsEqual) {
                return true;
            } 
        } 
        return false;
    }
}
