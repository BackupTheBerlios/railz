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
import java.util.logging.*;

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
    
    private static Logger logger = Logger.getLogger("global");

    private static class ExplorerList extends LinkedList {
	private int cost = 0;
	private HashSet locations = new HashSet(); 
	private final Point tmpP = new Point();

	public boolean add(Object o) {
	    super.add(o);
	    PathExplorer pe = (PathExplorer) o;
	    cost += pe.getCost();
	    locations.add(new Point(pe.getX(), pe.getY()));

	    return true;
	}

	public Object removeLast() {
	    PathExplorer pe = (PathExplorer) super.removeLast();
	    cost -= pe.getCost();
	    tmpP.setLocation(pe.getX(), pe.getY());
	    locations.remove(tmpP);
	    return pe;
	}

	public int getCost() {
	    return cost;
	}

	public void clear() {
	    super.clear();
	    locations.clear();
	    cost = 0;
	}

	public boolean haveLocation(int x, int y) {
	    tmpP.setLocation(x, y);
	    return locations.contains(tmpP);
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
	start = new Point(initialExplorer.getX(), initialExplorer.getY());
	startExplorer = initialExplorer;
	target = new Point(destX, destY);
    }

    /**
     * Perform the exploration.
     * @return a LinkedList of PathExplorer objects. The first object in the
     * link is at the starting point, and the last contains the destination.
     * Return null if no path could be found.
     */
    public LinkedList explore() {
	logger.log(Level.FINE, "exploring from " + start + " to " + target);
	logger.log(Level.FINE, "best minCost=" + minCost + ", maxCost=" +
	       maxCost);
	currentPath = new ExplorerList();
	bestPath = null;
	bestCost = maxCost;
	
	/* "Stupidity" filter... */
	if (start.equals(target))
	    return currentPath;

	PathExplorer pe = startExplorer;
	Point currentPos = new Point();
	exploredTiles.clear();
	do {
	    currentPath.add(pe);

	    pe = pe.exploreNewTile();

	    if (pe == null) {
		if (currentPath.size() == 1) {
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
	     * Have we explored further than our current bestCost? */
	    currentPos.setLocation(pe.getX(), pe.getY());
	    if (exploredTiles.contains(currentPos) ||
		    currentPath.getCost() > bestCost) {
		/* go back to old tile */
		pe = (PathExplorer) currentPath.removeLast();
		continue;
	    }

	    /* have we doubled back on ourselves ? */
	    /*if (currentPath.haveLocation(currentPos.x, currentPos.y)) {
		    pe = (PathExplorer) currentPath.removeLast();
		    continue;
	    }*/

	    /* have we reached the target ? */
	    if (currentPos.equals(target)) {
		Iterator i = currentPath.iterator();
		int cost = 0;
		do {
		    cost += ((PathExplorer) i.next()).getCost();
		} while (i.hasNext());
		if (cost < bestCost) {
		    /* this is the new best route */
		    logger.log(Level.FINE, "Found new route cost " + 
			    cost);
		    bestCost = cost;
		    if (bestPath == null)
			bestPath = new LinkedList();
		    bestPath.clear();
		    i = currentPath.iterator();
		    do {
			bestPath.add(((PathExplorer) i.next()).getCopy());
		    } while (i.hasNext());
		}
		// remove the current tile, and continue searching with
		// the previous tile.
		// currentPath.removeLast();
		pe = (PathExplorer) currentPath.removeLast();
		continue;
	    }

	    exploredTiles.add(new Point(pe.getX(), pe.getY()));

	} while (bestCost > minCost);

	return bestPath;
    }
}
