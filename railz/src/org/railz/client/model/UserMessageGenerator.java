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

package org.railz.client.model;

import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.railz.controller.MoveReceiver;
import org.railz.move.*;
import org.railz.util.Resources;
import org.railz.world.accounts.*;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.GameCalendar;
import org.railz.world.common.GameTime;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ObjectKey2;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.TrainModel;


/**
 * This class inspects incoming moves and generates a user message if
 * appropriate.
 * It could also be used to trigger sounds.
 *  @author Luke
 *
 * Created on Dec 13, 2003
 * 
 */
class UserMessageGenerator implements MoveReceiver {
    ModelRoot mr;
    ReadOnlyWorld world;
    DecimalFormat formatter = new DecimalFormat("#,###,###");

    UserMessageGenerator(ModelRoot mr, ReadOnlyWorld world) {
        this.mr = mr;
        this.world = world;
    }

    public void processMove(Move move) {
        //Check whether it is a train arriving at a station.
        if (move instanceof TransferCargoAtStationMove) {
	    // ignore other player's trains
	    if (! move.getPrincipal().equals(mr.getPlayerPrincipal()))
		return;

	    TransferCargoAtStationMove transferCargoAtStationMove =
		(TransferCargoAtStationMove)move;

	    ObjectKey2 trainCargoBundle =
		transferCargoAtStationMove.getChangeOnTrain()
		.getKeyAfter();
	    ObjectKey2 stationCargoBundle =
		transferCargoAtStationMove.getChangeAtStation()
		.getKeyAfter();
	    NonNullElements trains = new NonNullElements(KEY.TRAINS, world,
		    mr.getPlayerPrincipal());
	    NonNullElements players = new NonNullElements(KEY.PLAYERS,
		    world, Player.AUTHORITATIVE);

	    int trainNumber = -1;
	    String stationName = Resources.get("No station");

	    while (trains.next()) {
		TrainModel train = (TrainModel)trains.getElement();

		if (train.getCargoBundle().equals(trainCargoBundle)) {
		    trainNumber = trains.getIndex() + 1;

		    break;
		}
	    }

	    while (players.next()) {
		FreerailsPrincipal p = (FreerailsPrincipal)
		    ((Player) players.getElement()).getPrincipal();
		Iterator stations = world.getIterator(KEY.STATIONS, p);
		while (stations.hasNext()) {
		    StationModel station = (StationModel)stations.next();

		    if (station.getCargoBundle().equals(stationCargoBundle)) {
			stationName = station.getStationName();
			break;
		    }
		}
	    }

	    GameTime gt = (GameTime)world.get(ITEM.TIME, Player.AUTHORITATIVE);
	    GameCalendar gc = (GameCalendar)world.get(ITEM.CALENDAR,
		    Player.AUTHORITATIVE);
	    GregorianCalendar cal = gc.getCalendar(gt);
	    DateFormat df =
		DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			DateFormat.MEDIUM);
	    String message = df.format(cal.getTime()) + "  Train #" +
		trainNumber + " arrives at " + stationName;
	    mr.getUserMessageLogger().println(message);

	    AddTransactionMove[] addTransactionMoves =
		transferCargoAtStationMove.getPayment();

	    if (addTransactionMoves == null) {
		// nothing more to report
		return;
	    }

	    message = "";

	    for (int j = 0; j < addTransactionMoves.length; j++) {
		AddTransactionMove addTransactionMove =
		    addTransactionMoves[j];
		DeliverCargoReceipt deliverCargoReceipt = 
		    (DeliverCargoReceipt)addTransactionMove.getTransaction();
		long revenue = deliverCargoReceipt.getValue();

		if (0 < revenue) {
		    CargoBundle cb = deliverCargoReceipt.getCargoDelivered();

		    for (int i = 0; i < world.size(KEY.CARGO_TYPES,
				Player.AUTHORITATIVE); i++) {
			int amount = cb.getAmount(i);

			if (amount > 0) {
			    CargoType ct =
				(CargoType)world.get(KEY.CARGO_TYPES, i,
						     Player.AUTHORITATIVE);
			    message += amount + " " + ct.getDisplayName() +
				"\n";
			}
		    }

		    message += "$" + formatter.format(revenue) + "\n";
		}
	    }
	    mr.getUserMessageLogger().println(message);
        } else if ((move instanceof AddTransactionMove) &&
		move.getPrincipal().equals(mr.getPlayerPrincipal())) {
	    Transaction t = (Transaction) ((AddTransactionMove)
		    move).getTransaction();
	    switch (t.getCategory()) {
		case Transaction.CATEGORY_COST_OF_SALES:
		    if (t.getSubcategory() == Bill.FUEL) {
			mr.getUserMessageLogger().println(Resources.get
				("Fuel bill: $") + (-t.getValue()));
		    }
		    break;
		case Transaction.CATEGORY_TAX:
		   if (t.getSubcategory() == Bill.INCOME_TAX) {
		       mr.getUserMessageLogger().println(Resources.get
			       ("Income tax charge: $") + (-t.getValue()));
		   }
		   break;
		case Transaction.CATEGORY_OPERATING_EXPENSE:
		   switch (t.getSubcategory()) {
		       case Bill.TRACK_MAINTENANCE:
			   mr.getUserMessageLogger().println(Resources.get
				   ("Track maintenance charge: $") +
				   (-t.getValue()));
			   break;
		       case Bill.ROLLING_STOCK_MAINTENANCE:
			   mr.getUserMessageLogger().println(Resources.get
				   ("Train maintenance charge: $") + 
				   (-t.getValue()));
			   break;
		   }
		   break;
		case Transaction.CATEGORY_INTEREST:
		  switch (t.getSubcategory()) {
		      case InterestTransaction.SUBCATEGORY_OVERDRAFT:
			  mr.getUserMessageLogger().println(Resources.get
				  ("Overdraft interest charge: $") + 
				  (-t.getValue()));
			  break;
		      case InterestTransaction.SUBCATEGORY_ACCOUNT_CREDIT_INTEREST: 
			  mr.getUserMessageLogger().println(Resources.get
				  ("Account interest credited: $") +
				  (t.getValue()));
			  break;
		  }
	    }
	}
    }
}
