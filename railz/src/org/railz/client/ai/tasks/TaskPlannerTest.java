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
package org.railz.client.ai.tasks;

import junit.framework.*;

import org.railz.client.top.*;
import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.player.*;

public abstract class TaskPlannerTest extends TestCase {
    protected abstract TaskPlanner getTaskPlanner();
    protected FreerailsPrincipal p1;
    protected World w;
    protected MapFixtureFactory mff;
    protected DummyClient dc;

    boolean supportsExactCostEstimation = false;
    
    protected void setupWorld() {
	mff = new MapFixtureFactory();
	p1 = mff.addPlayer("testPlayer1", 0);
	w = mff.world;
	BankAccount ba = new BankAccount();
	ba = ba.addTransaction(new InitialDeposit(new GameTime(0), 1000000L));
	w.add(KEY.BANK_ACCOUNTS, ba, p1);
	dc = new DummyClient();
    }

    public void testPlanTask() {
	TaskPlanner tp = getTaskPlanner();
	assertTrue(tp.planTask());
	assertTrue(tp.getTaskPriority() > 0);
	long cost = tp.getTaskCost();
	BankAccount ba = (BankAccount) w.get(KEY.BANK_ACCOUNTS, 0, p1);
	long initialMoney = ba.getCurrentBalance();
	tp.doTask();
	if (supportsExactCostEstimation) {
	    assertTrue(initialMoney - ba.getCurrentBalance() == cost);
	}
    }

    protected class DummyClient implements ClientDataProvider {
	private MoveChainFork mcf;
	private DummyMoveReceiver dmr;

	public DummyClient() {
	    mcf = new MoveChainFork();
	    dmr = new DummyMoveReceiver(mcf);
	}

	public FreerailsPrincipal getPlayerPrincipal() {
	    return p1;
	}

	public MoveChainFork getMoveChainFork() {
	    return mcf;
	}

	public UntriedMoveReceiver getReceiver() {
	    return dmr;
	}

	public ReadOnlyWorld getWorld() {
	    return w;
	}
    }

    private class DummyMoveReceiver implements UntriedMoveReceiver {
	MoveChainFork mcf;

	public DummyMoveReceiver(MoveChainFork mcf) {
	    this.mcf = mcf;
	}

	public MoveStatus tryDoMove(Move m) {
	    return m.tryDoMove(w, m.getPrincipal());
	}

	public MoveStatus tryUndoMove(Move m) {
	    return m.tryUndoMove(w, m.getPrincipal());
	}

	public void undoLastMove() {
	    assertTrue(false);
	}

	public void processMove(Move m) {
	    MoveStatus ms = m.doMove(w, m.getPrincipal());

	    if (ms != MoveStatus.MOVE_OK)
		m = new RejectedMove(m, ms);

	    mcf.processMove(m);
	}
    }
}


