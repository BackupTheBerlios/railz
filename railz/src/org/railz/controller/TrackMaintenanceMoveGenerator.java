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
 * Created on 11-Aug-2003
 *
 */
package org.railz.controller;

import java.util.*;

import org.railz.move.AddTransactionMove;
import org.railz.move.Move;
import org.railz.world.accounts.AddItemTransaction;
import org.railz.world.accounts.BankAccount;
import org.railz.world.accounts.Bill;
import org.railz.world.accounts.Transaction;
import org.railz.world.common.GameTime;
import org.railz.world.top.KEY;
import org.railz.world.top.ITEM;
import org.railz.world.top.World;
import org.railz.world.track.TrackRule;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * This class iterates over the entries in the BankAccount
 * and counts the number of units of each track type, then
 * calculates the cost of maintenance.
 *
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;
    private ReadOnlyWorld world;

    /** The number of each type of track */
    private int[] track;

    public TrackMaintenanceMoveGenerator(ReadOnlyWorld w, MoveReceiver mr) {
        this.moveReceiver = mr;
	world = w;
	track = new int[world.size(KEY.TRACK_RULES, Player.AUTHORITATIVE)];
    }

    private AddTransactionMove[] generateMove() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	AddTransactionMove[] moves = new AddTransactionMove[i.size()];
	int j = 0;
	while (i.next()) {
	    moves[j++] = generateMoveImpl
		(((Player) i.getElement()).getPrincipal());
	}
	return moves;
    }
    
    /**
     * @param t array indexed by TRACK_RULE indicating number of units of each
     * track that should be maintained.
     */
    public void setTrack(int[] t) {
	track = (int[]) t.clone();
    }

    /**
     * Reset the amount of track calculated to 0
     */
    public void reset() {
	Arrays.fill(track, 0);
    }

    /**
     * @return the monthly bill for track maintenance on the calculated amount
     * of track
     */
    public Transaction getTransaction() {
	long amount = 0;

	for (int i = 0; i < track.length; i++) {
	    TrackRule trackRule = (TrackRule)world.get(KEY.TRACK_RULES, i,
		    Player.AUTHORITATIVE);
	    long maintenanceCost = trackRule.getMaintenanceCost();

	    if (track[i] > 0) {
		amount += maintenanceCost * track[i];
	    }
	}
	amount /= 12;

	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
        return new Bill(now, amount, Bill.TRACK_MAINTENANCE);
    }

    private AddTransactionMove generateMoveImpl(FreerailsPrincipal p) {
        track = calculateNumberOfEachTrackType(p);

        return new AddTransactionMove(0, getTransaction(), p);
    }

    /**
     * determine the quantity of track this player owns by examining the
     * players account and adding up how many he has purchased.
     */
    private int[] calculateNumberOfEachTrackType(FreerailsPrincipal p) {
        int[] unitsOfTrack = new int[world.size(KEY.TRACK_RULES,
		Player.AUTHORITATIVE)];
        BankAccount account = (BankAccount)world.get(KEY.BANK_ACCOUNTS, 0, p);

        for (int i = 0; i < account.size(); i++) {
            Transaction t = account.getTransaction(i);

            if (t instanceof AddItemTransaction) {
                AddItemTransaction addItemTransaction = (AddItemTransaction)t;

                if (AddItemTransaction.TRACK ==
		       	addItemTransaction.getSubcategory()) {
		    unitsOfTrack[addItemTransaction.getType()] +=
			addItemTransaction.getQuantity();
                }
            }
        }
        return unitsOfTrack;
    }

    public void update() {
        Move[] m = generateMove();
	for (int i = 0; i < m.length; i++) {
	    moveReceiver.processMove(m[i]);
	}
    }
}
