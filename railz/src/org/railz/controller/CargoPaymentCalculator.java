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

package org.railz.controller;

import java.util.*;
import java.util.Map.Entry;

import org.railz.world.accounts.*;
import org.railz.world.cargo.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
/**
 * Calculates the payment due for transporting a CargoBatch from one map tile
 * to another within a certain time.
 */
public final class CargoPaymentCalculator {
    private ReadOnlyWorld world;

    public CargoPaymentCalculator(ReadOnlyWorld w) {
	world = w;
    }

    /**
     * @return the amount(s) to be paid for delivering the CargoBundle cb
     *  to x, y at time t
     */
    public Transaction[] calculatePayment(CargoBundle cb, int
	    x, int y, GameTime t) {
	    Iterator batches = cb.cargoBatchIterator();
	    long freightAmount = 0;
	    long passengerAmount = 0;
	    MutableCargoBundle passengerBundle = new MutableCargoBundle();
	    MutableCargoBundle freightBundle = new MutableCargoBundle();
	    while (batches.hasNext()) {
		CargoBatch cBatch = (CargoBatch) ((Entry)
			batches.next()).getKey();
		int dx = (cBatch.getSourceX() - x);
		int dy = (cBatch.getSourceY() - y);
		double dist = Math.sqrt(dx*dx + dy*dy);
		int elapsedTime = t.getTime() - (int) cBatch.getTimeCreated();
		CargoType ct = (CargoType) world.get(KEY.CARGO_TYPES,
		    cBatch.getCargoType(), Player.AUTHORITATIVE);
		double amount = ((double) cb.getAmount(cBatch)) *
		    Math.log(1 + dist) *
		    (double) ct.getAgeAdjustedValue(elapsedTime);
		if (ct.getCategory() == TransportCategory.PASSENGER) {
		    passengerAmount += amount;
		    passengerBundle.addCargo(cBatch,
			    cb.getAmount(cBatch));
		} else {
		    freightAmount += amount;
		    freightBundle.addCargo(cBatch,
			    cb.getAmount(cBatch));
		}
	    }

	    Transaction[] tr = new Transaction[2];
	    tr[0] = new DeliverCargoReceipt(t, freightAmount, new CargoBundle(freightBundle),
		    DeliverCargoReceipt.SUBCATEGORY_FREIGHT);
	    tr[1] = new DeliverCargoReceipt(t, passengerAmount,
		    new CargoBundle(passengerBundle),
		    DeliverCargoReceipt.SUBCATEGORY_PASSENGERS);
	    return tr;
    }
}
