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
package org.railz.client.common;

import java.awt.*;
import java.util.*;
import java.util.logging.*;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
/**
 * The function of this class is to store differences (executed by
 * processing moves) between this View, and the underlying world. These can be
 * removed by undoing the moves.
 */
public final class WorldOverlay implements World {
    private static final Logger logger = Logger.getLogger("global");
    private ReadOnlyWorld world;

    private HashMap tiles = new HashMap();
    private HashMap objects = new HashMap();

    /**
     * The current transaction which is being applied
     */
    private MoveState currentTransaction = new MoveState();

    /**
     * Stack of applied moves, head contains those first applied
     */
    private LinkedList appliedMoves = new LinkedList();

    private class MoveState {
	Move move;
	LinkedList tileKeys = new LinkedList();
	LinkedList objectKeys = new LinkedList();
    }

    /* temporary objects */
    private MutableObjectKey mutableObjectKey = new MutableObjectKey();
    private Point p = new Point();

    public WorldOverlay(World w) {
	world = w;
    }

    /**
     * Reset any precommitted moves
     */
    public void reset() {
	tiles.clear();
	objects.clear();
	appliedMoves.clear();
    }

    private void rollBackMove(MoveState ms) {
	while (! ms.tileKeys.isEmpty()) {
	    Point p = (Point) ms.tileKeys.removeFirst();
	    LinkedList ll = (LinkedList) tiles.get(p);
	    ll.removeLast();
	    if (ll.isEmpty())
		tiles.remove(p);
	}
	while (! ms.objectKeys.isEmpty()) {
	    ObjectKey ok = (ObjectKey) ms.objectKeys.removeFirst();
	    LinkedList ll = (LinkedList) objects.get(ok);
	    ll.removeLast();
	    if (ll.isEmpty())
		objects.remove(ok);
	}
    }

    private class WorldViewReceiver implements UncommittedMoveReceiver {
	public void undoLastMove() {
	    if (appliedMoves.isEmpty())
		return;

	    rollBackMove((MoveState) appliedMoves.removeLast());
	}

	public void processMove(Move m) {
	    MoveState ms = new MoveState();
	    // roll back any moves applied temporarily
	    rollBackMove(currentTransaction);

	    currentTransaction = ms;
	    m.doMove(WorldOverlay.this, m.getPrincipal());
	    appliedMoves.addLast(currentTransaction);
	    // create scratch MoveState for trying out moves
	    currentTransaction = new MoveState();
	}
    }

    public void integrateMove() {
	MoveState ms = (MoveState) appliedMoves.removeFirst();
	while (! ms.tileKeys.isEmpty()) {
	    Point p = (Point) ms.tileKeys.removeFirst();
	    LinkedList ll = (LinkedList) tiles.get(p);
	    ll.removeFirst();
	    if (ll.isEmpty())
		tiles.remove(p);
	}
	while (! ms.objectKeys.isEmpty()) {
	    ObjectKey ok = (ObjectKey) ms.objectKeys.removeFirst();
	    LinkedList ll = (LinkedList) objects.get(ok);
	    ll.removeFirst();
	    if (ll.isEmpty())
		objects.remove(ok);
	}
    }

    private UncommittedMoveReceiver moveReceiver = new WorldViewReceiver();

    public UncommittedMoveReceiver getMoveReceiver() {
	return moveReceiver;
    }

    /**
     * Returns the element mapped to the specified item.
     */
    public FreerailsSerializable get(ITEM item, FreerailsPrincipal p) {
	return world.get(item, p);
    }

    /**
     * Returns the element at the specified position in the specified list.
     */
    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
	LinkedList ll = (LinkedList) objects.get(new
		ObjectKey(key, p, index));
	if (ll != null)
	    return (FreerailsSerializable) ll.getLast();

	return world.get(key, index, p);
    }

    /**
     * Returns the number of elements in the specified list.
     */
    public int size(KEY key, FreerailsPrincipal p) {
	int sz = world.size(key, p);
	while (objects.containsKey(new ObjectKey(key, p, sz)) &&
		((LinkedList) objects.get(new ObjectKey(key, p, sz)))
		.getLast() != null) {
	    sz++;
	}
	return sz;
    }

    /** Returns the width of the map in tiles.
     */
    public int getMapWidth() {
	return world.getMapWidth();
    }

    /** Returns the height of the map in tiles.
     */
    public int getMapHeight() {
	return world.getMapHeight();
    }

    /**
     * Returns the tile at the specified position on the map.
     */
    public FreerailsTile getTile(int x, int y) {
	LinkedList ll = (LinkedList) tiles.get(new Point(x, y));
	if (ll != null) {
	    return (FreerailsTile) ll.getLast();
	}

	return world.getTile(x, y);
    }

    public FreerailsTile getTile(Point p) {
	return getTile(p.x, p.y);
    }

    public boolean boundsContain(int x, int y) {
	return world.boundsContain(x, y);
    }

    public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
	return (index >= 0 && index < size(k, p));
    }

    /**
     * Replaces the element mapped to the specified item with the specified
     * element.
     */
    public void set(ITEM item, FreerailsSerializable element,
        FreerailsPrincipal principal) {
	assert false;
    }

    /**
     * Replaces the element at the specified position in the specified list
     * with the specified element.
     */
    public void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal principal) {
	currentTransaction.objectKeys.addLast(new ObjectKey(key, principal,
		    index));
	LinkedList ll = (LinkedList)
	    objects.get(new ObjectKey(key, principal, index));
	if (ll == null) {
	    ll = new LinkedList();
	    objects.put(new ObjectKey(key, principal, index), ll);
	}
	ll.addLast(element);
    }

    /**
     * Appends the specified element to the end of the specifed list and
     * returns the index that can be used to retrieve it.
     */
    public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal
	    principal) {
	int index = size(key, principal);
	set(key, index, element, principal);
	return index;
    }

    /**
     * Removes the last element from the specified list.
     */
    public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal
	    principal) {
	int sz = size(key, principal);
	FreerailsSerializable fs = get(key, sz - 1, principal);
	set(key, sz - 1, null, principal);
	return fs;
    }

    /**
     * Replaces the tile at the specified position on the map with the
     * specified tile.
     */
    public void setTile(int x, int y, FreerailsTile tile) {
	Point p = new Point(x, y);
	LinkedList ll = (LinkedList) tiles.get(p);
	if (ll == null) {
	    ll = new LinkedList();
	    tiles.put(p, ll);
	}
	currentTransaction.tileKeys.addLast(p);
	ll.addLast(tile);
    }

    /**
     * @return the World object which backs this instance of WorldOverlay
     */
    public World getWorld() {
	return (World) world;
    }
}
