/*
 * Copyright (C) Robert Tuck
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
package jfreerails.controller;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import jfreerails.world.common.*;
import jfreerails.world.top.*;
import jfreerails.world.track.*;
import jfreerails.world.train.*;

public class TrainPathFinder {
    private ReadOnlyWorld world;

    private LinkedList bestPath;
    private int bestCost;

    private LinkedList currentPath;
    /**
     * track layout at the starting tile
     */
    private byte startTrackLayout;
    /**
     * current direction from the start
     */
    private byte startDirection;

    /**
     * A HashSet of Point instances which describe the track tiles which have
     * already been explored.
     */
    private HashSet exploredTiles;

    public TrainPathFinder(ReadOnlyWorld w) {
	world = w;
    }

    private void nextElement(Point currentPosition) {
	PathElement pe = (PathElement) currentPath.getLast();
	while (!pe.nextDirection()) {
	    exploredTiles.add(new Point(pe.x, pe.y));
	    currentPath.removeLast();
	    if (currentPath.size() == 1)
		break;
	    pe = (PathElement) currentPath.getLast();
	}
	currentPosition.x = pe.x;
	currentPosition.y = pe.y;
    }

    /**
     * TODO calculate cost from terrain and track layout.
     */
    public TrainPath findPath(Point start, Point target) {
	/* "Stupidity" filter... */
	if (start.equals(target))
	    return new TrainPath(new IntLine[0]);

	startDirection = CompassPoints.NORTH;
	startTrackLayout = world.getTile(start.x,
		start.y).getTrackConfiguration();
	currentPath = new LinkedList();
	bestPath = new LinkedList();
	bestCost = Integer.MAX_VALUE;
	do {
	    startDirection = CompassPoints.rotateClockwise(startDirection);
	    currentPath.clear();
	    currentPath.add(new IntLine(start.x /
			TrackTile.DELTAS_PER_TILE, start.y /
			TrackTile.DELTAS_PER_TILE,
			startTrackLayout,
			CompassPoints.getLength(startDirection) / 2));
	    PathElement currentElement = (PathElement) currentPath.getFirst();
	    Point currentPosition = new Point(currentElement.x,
		    currentElement.y);
	    Point oldPosition = new Point();
	    do {
		/* move to next tile along current direction */
		oldPosition.x = currentPosition.x;
		oldPosition.y = currentPosition.y;
		advanceOneTile(currentPosition, currentElement.direction);

		/* is this tile already explored ? */
		if (exploredTiles.contains(currentPosition)) {
		    /* go back to old tile */
		    currentPosition.x = oldPosition.x;
		    currentPosition.y = oldPosition.y;
		    nextElement(currentPosition);
		    continue;
		}

		/* have we doubled back on ourselves ? */
		Iterator i = currentPath.iterator();
		boolean flag = false;
		do {
		    PathElement p = (PathElement) i.next();
		    if (currentPosition.x == p.x &&
			    currentPosition.y == p.y) {
			currentPosition.x = oldPosition.x;
			currentPosition.y = oldPosition.y;
			flag = true;
			break;
		    }
		} while (i.hasNext());
		if (flag) {
		    nextElement(currentPosition);
		    continue;
		}

		/* add the current position to the current path */
		byte trackLayout = world.getTile(currentPosition.x,
			currentPosition.y).getTrackConfiguration();
		byte initialDirection =
		    CompassPoints.invert(currentElement.direction);

		currentElement = new PathElement(currentPosition.x,
			currentPosition.y, initialDirection, trackLayout, 1);
		currentPath.add(currentElement);

		/* have we reached the target ? */
		if (currentPosition.equals(target)) {
		    i = currentPath.iterator();
		    int cost = 0;
		    do {
			cost += ((PathElement) i.next()).getCost();
		    } while (i.hasNext());
		    if (cost < bestCost) {
			/* this is the new best route */
			bestCost = cost;
			bestPath.clear();
		       	i = currentPath.iterator();
			do {
			    bestPath.add(new PathElement((PathElement)
					i.next()));
		       	} while (i.hasNext());
		    }
		}
		nextElement(currentPosition);
	    } while (currentPath.size() > 1);
	} while (startDirection != CompassPoints.NORTH);
	/* all possible paths have been explored */
	if (bestPath.isEmpty())
	    return new TrainPath(new IntLine[0]);

	/* convert the ArrayList to a TrainPath. bestPath always contains the
	 * target tile and the start tile */
	Point oldp = new Point();
	Point newp = new Point();
	byte oldDirection;
	PathElement p = (PathElement) bestPath.removeFirst();
	LinkedList intLines = new LinkedList();
	boolean firstP = true;
	while (!bestPath.isEmpty()) {
	    oldp.x = p.x;
	    oldp.y = p.y;
	    oldDirection = p.direction;
	    while (p.direction == oldDirection) {
		p = (PathElement) bestPath.removeFirst();
		newp.x = p.x;
		newp.y = p.y;
	    }
	    if (firstP) {
		oldp.x = start.x;
		oldp.y = start.y;
		firstP = false;
	    } else {
		oldp = TrackTile.tileCoordsToDeltas(oldp);
	    }
	    newp = TrackTile.tileCoordsToDeltas(newp);
	    IntLine l = new IntLine(oldp.x, oldp.y, newp.x, newp.y);
	    intLines.add(l);
	}
	return new TrainPath((IntLine[]) intLines.toArray(new
		    IntLine[intLines.size()]));
    }

    private static class PathElement {
	public int x;
	public int y;
	/**
	 * Current direction to advance from this map location
	 */
	public  byte direction;
	private byte initialDirection;
	/**
	 * copy of the track layout at this point
	 */
	private byte trackLayout;

	private int cost;

	public PathElement(PathElement p) {
	    x = p.x;
	    y = p.y;
	    direction = p.direction;
	    initialDirection = p.initialDirection;
	    trackLayout = p.trackLayout;
	    cost = p.cost;
	}

	/**
	 * @param cost cost factor to traverse this tile
	 */
	public PathElement(int x, int y, byte direction, byte trackLayout, int
		cost) {
	    this.x = x;
	    this.y = y;
	    this.cost = cost;
	    this.trackLayout = trackLayout;
	    initialDirection = direction;
	    this.direction = direction;
	}

	/**
	 * @return true if there is another direction to explore
	 */
	public boolean nextDirection() {
	    do {
		direction = CompassPoints.rotateClockwise(direction);
		if (direction == initialDirection)
		    return false;
	    } while ((direction & trackLayout) == 0);
	    return true;
	}

	public int getCost() {
	    return cost * CompassPoints.getLength(direction);
	}
    }

    private static void advanceOneTile(Point p, byte direction) {
	switch(direction) {
	    case CompassPoints.NORTH:
		p.y--;
		return;
	    case CompassPoints.NORTHEAST:
		p.y--;
	    case CompassPoints.EAST:
		p.x++;
		return;
	    case CompassPoints.SOUTHEAST:
		p.y++;
		p.x++;
		return;
	    case CompassPoints.SOUTH:
		p.y++;
		return;
	    case CompassPoints.SOUTHWEST:
		p.x--;
		p.y++;
		return;
	    case CompassPoints.WEST:
		p.x--;
		return;
	    case CompassPoints.NORTHWEST:
		p.x--;
		p.y--;
		return;
	    default:
		throw new IllegalArgumentException();
	}
    }
}
