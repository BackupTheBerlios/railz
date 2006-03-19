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
 * Created on 30-Jul-2003
 *
 */
package org.railz.server;

import java.util.Iterator;
import java.util.Map.Entry;

import org.railz.controller.*;
import org.railz.move.AddTransactionMove;
import org.railz.world.accounts.*;
import org.railz.world.cargo.*;
import org.railz.world.common.GameTime;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.ITEM;
import org.railz.world.top.ObjectKey2;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;

/** This class generates Moves that pay the player for delivering the cargo.
 *
 * @author Luke Lindsay
 *
 */
public class ProcessCargoAtStationMoveGenerator {
    /**
     * @param tp owner of the train
     */
    public static AddTransactionMove[] processCargo(ReadOnlyWorld w,
	CargoBundle cargoBundle, FreerailsPrincipal tp, ObjectKey2 stationKey) {
	CargoPaymentCalculator cpc = new CargoPaymentCalculator(w);
	StationModel thisStation = (StationModel)w.get(stationKey);

	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	Transaction[] t = cpc.calculatePayment(cargoBundle,
		thisStation.getStationX(), thisStation.getStationY(), now);
	AddTransactionMove[] moves = new AddTransactionMove[t.length];
	for (int i = 0; i < t.length; i++)
	    moves[i] = new AddTransactionMove(0, t[i], tp);

	return moves;
    }
}
