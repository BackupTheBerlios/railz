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
import java.util.logging.*;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.util.*;
import org.railz.world.top.*;
/**
 * A dummy MoveExecuter which does nothing. This is used when the client is
 * connected via a LocalConnection and thus shares the instance of the world
 * with the server and does not have its own copy.
 */
final class DummyMoveExecuter extends ClientMoveExecuter {
    Logger logger = Logger.getLogger("global");

    /** Queue for moves from the server, add to head, remove from tail */
    private final LinkedList synchronizedQueue = new LinkedList();

    /** MoveReceiver which is where we forward moves from the server to */
    private MoveReceiver moveReceiver;

    /** MoveReceiver which is attached to the LocalConnection */
    private LocalConnection localConnection;

    private ReadOnlyWorld mutex;

    public DummyMoveExecuter(MoveReceiver mr, ReadOnlyWorld w) {
	moveReceiver = mr;
	mutex = w;
    }

    /** Queue a move from the server */
    public void processMove(Move move) {
	synchronized (synchronizedQueue) {
	    logger.log(Level.FINEST, "queueing move from server");
	    synchronizedQueue.addFirst(move);
	}
    }

    /** Process all queued moves from the server */
    public void update() {
	if (moveReceiver == null)
	    return;

	Move m;
	do {
	    synchronized (synchronizedQueue) {
		if (synchronizedQueue.isEmpty())
		    break;

		logger.log(Level.FINEST, "popping move from queue");
		m = (Move) synchronizedQueue.removeLast();
	    }

	    if (! (m instanceof RejectedMove))
		moveReceiver.processMove(m);
	} while (true);
    }
    
    public Object getMutex() {
	return mutex;
    }

    public int getNumBlockedMoves() {
	return 0;
    }

    public UncommittedMoveReceiver getUncommittedMoveReceiver() {
	return dummyReceiver;
    }

    private DummyReceiver dummyReceiver = new DummyReceiver();
    
    private class DummyReceiver implements UncommittedMoveReceiver {
	    /** forward a move to the server */
	    public void processMove(Move m) {
		logger.log(Level.FINEST, "Forwarding move to server");
		localConnection.processMove(m);
	    }

	    public void undoLastMove() {
		localConnection.undoLastMove();
	    }
	}

    public void addMoveReceiver(UncommittedMoveReceiver lc) {
	assert lc instanceof LocalConnection;
	localConnection = (LocalConnection) lc;
    }
}
