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

import java.awt.*;
import java.util.*;

import org.railz.world.track.*;
/**
 * A generic PathFinder which uses PathExplorers to find the lowest cost path
 * to a given destination.
 */
public final class PathFinder {
    private LinkedList bestPath;
    private int bestCost;
    private ExplorerList currentPath;
    private Point start;
    private Point target;
    private int maxCost;
    private int minCost;
    private PathExplorer startExplorer;
    
    private class ExplorerList extends LinkedList {
	private int cost = 0;

	public boolean add(Object o) {
	    super.add(o);
	    PathExplorer pe = (PathExplorer) o;
	    cost += pe.getCost();
	    return true;
	}

	public Object removeLast() {
	    PathExplorer pe = (PathExplorer) super.removeLast();
	    cost -= pe.getCost();
	    return pe;
	}

	public int getCost() {
	    return cost;
	}
    }

    /**
     * A HashSet of Point instances which describe the track tiles which have
     * already been explored.
     */
    private HashSet exploredTiles = new HashSet();

    /**
     * @param minCost the minimum cost for this route. If we find a path
     * between the start and end points where the total path cost is less than
     * or equal to this cost, we stop searching and return the route found.
     * @param maxCost the maximum cost for this route. The path finder will
     * not explore paths that exceed this cost.
     */
    public PathFinder(PathExplorer initialExplorer, int destX, int destY,
	    int minCost, int maxCost) {
	this.minCost = minCost;
	this.maxCost = maxCost;
	startExplorer = initialExplorer;
	target = new Point(destX, destY);
    }

    /**
     * Perform the exploration.
     * @return a LinkedList of PathExplorer objects. The first object in the
     * link is at the starting point, and the last contains the destination.
     */
    public LinkedList explore() {
	currentPath = new ExplorerList();
	bestPath = new LinkedList();
	bestCost = maxCost;
	
	/* "Stupidity" filter... */
	if (start.equals(target))
	    return currentPath;

	PathExplorer pe = startExplorer;
	Point currentPos = new Point();
	exploredTiles.clear();
	currentPath.clear();
	do {
	    exploredTiles.add(new Point(pe.getX(), pe.getY()));

	    currentPath.add(pe);

	    pe = pe.exploreNewTile();

	    if (pe == null) {
		if (currentPath.size() == 0) {
		    // we have already explored all tiles
		    break;
		} else {
		    // remove the current tile, and continue searching with
		    // the previous tile.
		    currentPath.removeLast();
		    pe = (PathExplorer) currentPath.removeLast();
		    continue;
		}
	    }

	    /* Is this tile already explored?
	     * Have we explored further than our maxCost? */
	    currentPos.setLocation(pe.getX(), pe.getY());
	    if (exploredTiles.contains(currentPos) ||
		    currentPath.getCost() > maxCost) {
		/* go back to old tile */
		pe = (PathExplorer) currentPath.removeLast();
		continue;
	    }

	    /* have we doubled back on ourselves ? */
	    Iterator i = currentPath.iterator();
	    do {
		PathExplorer p = (PathExplorer) i.next();
		if (currentPos.x == p.getX() &&
			currentPos.y == p.getY()) {
		    pe = (PathExplorer) currentPath.removeLast();
		    break;
		}
	    } while (i.hasNext());

	    /* have we reached the target ? */
	    if (currentPos.equals(target)) {
		i = currentPath.iterator();
		int cost = 0;
		do {
		    cost += ((PathExplorer) i.next()).getCost();
		} while (i.hasNext());
		if (cost < bestCost) {
		    /* this is the new best route */
		    bestCost = cost;
		    bestPath.clear();
		    i = currentPath.iterator();
		    do {
			bestPath.add(((PathExplorer) i.next()).getCopy());
		    } while (i.hasNext());
		}
	    }
	} while (bestCost > minCost);

	return bestPath;
    }
}
