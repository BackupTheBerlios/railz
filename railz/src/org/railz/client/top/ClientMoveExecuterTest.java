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

import java.awt.*;
import junit.framework.*;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.player.*;

/**
 * Tests an implementation of ClientMoveExecuter
 */
public abstract class ClientMoveExecuterTest extends TestCase {
    /**
     * @return an initialised ClientMoveExecuter connected to the TestServer
     * and TestClient instances. The underlying world instance should be
     * initialised to contain player pl1, and serverWorld should be set to
     * this. clientWorld should be set to the clients copy of the world.
     */
    public abstract ClientMoveExecuter getCME();

    protected boolean isPrecommitSupported = true;

    protected World serverWorld;
    protected World clientWorld;
    protected MoveReceiver testClient = new TestClient();
    protected TestServer testServer = new TestServer();
    protected FreerailsSerializable o1 = new TestObject(1);
    protected FreerailsSerializable o2 = new TestObject(2);
    protected Player pl1 = getPlayer("p1", 0);
    protected FreerailsPrincipal p1 = pl1.getPrincipal();

    protected static Player getPlayer(String name, int index) {
	Player p = new Player(name);
	p = new Player(name, p.getPublicKey(), index);
	return p;
    }

    public void testAddMove() {
	ClientMoveExecuter cme = getCME();
	TestMove2 tm = new TestMove2(KEY.TRAINS, 0, p1, new TestObject(0));
	cme.getUncommittedMoveReceiver().processMove(tm);
	if (isPrecommitSupported) {
	    assertEquals(1, clientWorld.size(KEY.TRAINS, p1));
	    assertEquals(new TestObject(0), clientWorld.get(KEY.TRAINS, 0,
			p1));
	}
	testServer.checkMoves(1);
	assertEquals(tm, testServer.receivedMove);
	tm.doMove(serverWorld, tm.getPrincipal());
	assertEquals(new TestObject(0), serverWorld.get(KEY.TRAINS, 0, p1));
	cme.processMove(tm);
	cme.update();
	assertEquals(new TestObject(0), clientWorld.get(KEY.TRAINS, 0, p1));
	assertEquals(1, clientWorld.size(KEY.TRAINS, p1));
    }

    public void testPrecommit() {
	if (!isPrecommitSupported)
	    return;

	ClientMoveExecuter cme = getCME();
	serverWorld.add(KEY.TRAINS, new TestObject(0), p1);
	assertEquals(new TestObject(0), clientWorld.get(KEY.TRAINS, 0,
		    p1));
	Move m = new TestMove(KEY.TRAINS, 0, p1, new TestObject(0), new
		TestObject(1));

	UncommittedMoveReceiver umr = cme.getUncommittedMoveReceiver();
	umr.processMove(m);
	assertEquals(1, cme.getNumBlockedMoves());
	assertEquals(new TestObject(1), clientWorld.get(KEY.TRAINS, 0,
		    p1));
	testServer.checkMoves(1);
	assertEquals(m, testServer.receivedMove);
	m.doMove(serverWorld, m.getPrincipal());
	assertEquals(new TestObject(1), serverWorld.get(KEY.TRAINS, 0,
		    p1));

	cme.processMove(m);
	cme.update();
	assertEquals(new TestObject(1), clientWorld.get(KEY.TRAINS, 0,
		    p1));
	assertEquals(0, cme.getNumBlockedMoves());

	((TestClient) testClient).checkMoves(1);
	assertEquals(m, ((TestClient) testClient).receivedMove);

    }

    public void testTileMove() {
	ClientMoveExecuter cme = getCME();
	serverWorld.setTile(0, 0, new FreerailsTile(0, null, null));
	TestMove3 tm = new TestMove3(clientWorld, new Point(0, 0),
		p1);
	cme.getUncommittedMoveReceiver().processMove(tm);
	FreerailsTile newTile = new FreerailsTile
	    (new FreerailsTile(0, null, null), p1);
	if (isPrecommitSupported) {
	    assertEquals(newTile, clientWorld.getTile(0, 0));
	    assertFalse(newTile.equals(serverWorld.getTile(0,0)));
	    assertEquals(1, cme.getNumBlockedMoves());
	}
	tm.doMove(serverWorld, tm.getPrincipal());
	assertEquals(newTile, serverWorld.getTile(0, 0));
	cme.processMove(tm);
	cme.update();
	assertEquals(newTile, clientWorld.getTile(0, 0));

	tm = new TestMove3(clientWorld, new Point(0, 0),
		Player.AUTHORITATIVE);
	cme.getUncommittedMoveReceiver().processMove(tm);
	newTile = new FreerailsTile(new FreerailsTile(0, null, null),
		Player.AUTHORITATIVE);
	tm.doMove(serverWorld, tm.getPrincipal());
	assertEquals(newTile, serverWorld.getTile(0,0));
	cme.processMove(tm);
	cme.update();
	assertEquals(newTile, clientWorld.getTile(0, 0));
    }
    
    private class TestClient implements MoveReceiver {
	private Move receivedMove;
	private int movesReceived;

	public void processMove(Move m) {
	    movesReceived++;
	    receivedMove = m;
	}

	public void checkMoves(int expectedMoves) {
	    assertEquals(expectedMoves, movesReceived);
	    movesReceived = 0;
	}
    }

    private class TestServer implements UncommittedMoveReceiver {
	private int movesReceived = 0;
	private Move receivedMove;

	public void checkMoves(int expectedMoves) {
	    assertEquals(expectedMoves, movesReceived);
	    movesReceived = 0;
	}

	public void undoLastMove() {
	}

	public void processMove(Move move) {
	    movesReceived++;
	    receivedMove = move;
	}
    }

    private class TestObject implements FreerailsSerializable {
	int id;

	public String toString() {
	    return "TestObject: id = " + id;
	}

	public TestObject(int i) {
	    id = i;
	}

	public boolean equals(Object o) {
	    if (o instanceof TestObject) {
		return ((TestObject) o).id == id;
	    }
	    return false;
	}

	public int hashCode() {
	    return id;
	}
    }

    private class TestMove extends ChangeItemInListMove {
	public TestMove(KEY k, int i, FreerailsPrincipal p,
		FreerailsSerializable o1, FreerailsSerializable o2) {
	    super(k, i, o1, o2, p);
	}
    }

    private class TestMove2 extends AddItemToListMove {
	public TestMove2(KEY k, int i,  FreerailsPrincipal p,
		FreerailsSerializable item) {
	    super (k, i, item, p);
	}
    }

    private class TestMove3 extends ChangeTileOwnershipMove {
	public TestMove3(ReadOnlyWorld w, Point p, FreerailsPrincipal
		newOwner) {
	    super(w, p, newOwner);
	}
    }
}
