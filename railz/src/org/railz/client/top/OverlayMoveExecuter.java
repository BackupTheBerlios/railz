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

package org.railz.client.top;

import java.util.*;

import org.railz.client.common.*;
import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.top.*;
import org.railz.util.*;

/**
 * A move executer which pre-commits moves on the outward trip
 * to the server, and rolls back pre-committed moves when a response is
 * received by the server. All moves sent by the server are committed.
 *
 * @author rtuck99@users.sourceforge.net
 */
final class OverlayMoveExecuter extends ClientMoveExecuter {
    private WorldOverlay worldOverlay;
    private World world;
    private MoveReceiver moveReceiver = null;
    private MoveReceiver committedMoveReceiver;
    private LinkedList confirmedMoves = new LinkedList();

    /**
     * List of Move instances which have been temporarily committed to the
     * overlay, new moves are appended to the tail.
     */
    private LinkedList pendingMoves = new LinkedList();

    public OverlayMoveExecuter(MoveReceiver mr, WorldOverlay w) {
	worldOverlay = w;
	world = w.getWorld();
	committedMoveReceiver = mr;
    }

    public UncommittedMoveReceiver getUncommittedMoveReceiver() {
	return overlayMoveProcessor;
    }

    public int getNumBlockedMoves() {
	synchronized (pendingMoves) {
	    return pendingMoves.size();
	}
    }

    public void addMoveReceiver(UncommittedMoveReceiver mr) {
	moveReceiver = mr;
    }

    public void update() {
	Move moveFromServer = null;
	Move m = null;
	do {
	    boolean b = false;
	    synchronized (confirmedMoves) {
		if (confirmedMoves.isEmpty())
		    break;

		moveFromServer = (Move) confirmedMoves.removeFirst();
	    }

	    boolean noMovesPending;
	    synchronized (pendingMoves) {
		noMovesPending = pendingMoves.isEmpty();
		if (! noMovesPending) {
		    m = (Move) pendingMoves.getFirst();
		    b = m.equals(moveFromServer) ||
			((moveFromServer instanceof RejectedMove) &&
			 ((RejectedMove) moveFromServer).getAttemptedMove()
			 .equals(m));
		    if (b) {
			pendingMoves.removeFirst();
		    }
		}
	    }

	    if (b) {
		if (m instanceof RejectedMove) {
		    worldOverlay.reset();
		    synchronized (pendingMoves) {
			pendingMoves.clear();
		    }
		} else {
		    worldOverlay.integrateMove();
		}
	    } 
	    /* else {
		if (! (moveFromServer instanceof RejectedMove)) {
		    MoveStatus ms;
		    ms = moveFromServer.doMove(world,
			    moveFromServer.getPrincipal());
		    assert (noMovesPending ||
			    ms == MoveStatus.MOVE_OK) : ms.toString();
		} else {
		    System.out.println("Move rejected!");
		}
	    } */

	    if (committedMoveReceiver != null)
		committedMoveReceiver.processMove(moveFromServer);
	} while (true);
    }

    public Object getMutex() {
	return world;
    }

    public void processMove(Move move) {
	synchronized (confirmedMoves) {
	    confirmedMoves.addLast(move);
	}
    }

    private OverlayMoveProcessor overlayMoveProcessor = new
	OverlayMoveProcessor();

    private class OverlayMoveProcessor implements UncommittedMoveReceiver {
	public void processMove(Move m) {
	    synchronized (pendingMoves) {
		pendingMoves.addLast(m);
	    }
	    worldOverlay.getMoveReceiver().processMove(m);
	    if (moveReceiver != null)
		moveReceiver.processMove(m);
	}

	public void undoLastMove() {
	    // not supported
	    throw new IllegalStateException();
	}
    }
}

