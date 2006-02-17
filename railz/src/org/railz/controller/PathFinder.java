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
    /**
     * List of unexplored PathExplorers, ordered by increasing estimated cost
     * to target.
     */
    private OpenList open = new OpenList();

    /**
     * HashMap of explored PathExplorer.
     */
    private HashMap closed = new HashMap();

    private class OpenList {
	private static final int FAST_SIZE = 25;

	/**
	 * HashSet of PathExplorer
	 */
	private HashSet openSet = new HashSet();

	/**
	 * LinkedList of PathExplorer, in order of ascending size
	 */
	private LinkedList fastList = new LinkedList();

	/**
	 * LinkedList of PathExplorer
	 */
	private LinkedList slowList = new LinkedList();

	public boolean isEmpty() {
	    return fastList.isEmpty() && slowList.isEmpty();
	}

	public PathExplorer removeFirst() {
	    if (fastList.isEmpty()) {
		// populate the fastList from the slowList
		logger.log(Level.FINE, "Rebuilding fastList");
		ListIterator i = slowList.listIterator();
		int maxFastCost = Integer.MAX_VALUE;
		while (i.hasNext()) {
		    PathExplorer pe = (PathExplorer) i.next();
		    int cost = pe.getCumulativeCost() +
			pe.getEstimatedCost(target);
		    if (cost < maxFastCost) {
			i.remove();
			if (fastList.size() >= FAST_SIZE) {
			    PathExplorer pe2 = (PathExplorer)
				fastList.removeLast();
			    i.add(pe2);
			    maxFastCost = pe2.getCumulativeCost() +
				pe2.getEstimatedCost(target);
			}
			// add to fast list
			ListIterator j = fastList.listIterator();
			while (j.hasNext()) {
			    PathExplorer pe2 = (PathExplorer) j.next();
			    if (pe2.getCumulativeCost() +
				    pe2.getEstimatedCost(target) > cost) {
				j.previous();
				break;
			    }
			}
			j.add(pe);
		    }
		}
	    }
	    PathExplorer pe = (PathExplorer) fastList.removeFirst();
	    openSet.remove(pe);
	    return pe;
	}

	public void remove(PathExplorer pathExplorer) {
	    if (! fastList.remove(pathExplorer))
		slowList.remove(pathExplorer);

	    openSet.remove(pathExplorer);
	}

	/**
	 * Add the PathExplorer at a position in order of ascending cost to
	 * target.
	 */
	public boolean add(PathExplorer pathExplorer) {
	    int pos = 0;
	    int cost = pathExplorer.getEstimatedCost(target) +
		pathExplorer.getCumulativeCost();
	    PathExplorer pe;
	    openSet.add(pathExplorer);
	    ListIterator i = fastList.listIterator();
	    while (i.hasNext()) {
		pos++;
		pe = (PathExplorer) i.next();
		if (pe.getEstimatedCost(target) + pe.getCumulativeCost() >=
			cost) {
		    i.previous();
		   // logger.log(Level.FINEST, "Adding " + pathExplorer + 
		//	    " at cost " + cost + ", pos " + pos);
		    i.add(pathExplorer);
		    if (fastList.size() > FAST_SIZE) {
			slowList.addFirst(fastList.removeLast());
		    }
		    return true;
		}
	    }

	    slowList.add(pathExplorer);
	    return true;
	}

	public boolean contains(PathExplorer pe) {
	    return openSet.contains(pe);
	}
	
	public int size() {
	    return slowList.size() + fastList.size();
	}
    }

    private Point start;
    private Point target;
    private int maxCost;
    private int minCost;
    private PathExplorer startExplorer;
    private int bestEstimate;
            
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
     * @return a linkedList of PathExplorers from the origin to reach this
     * point
     */
    private LinkedList getBestPath(PathExplorer pe) {
	LinkedList ll = new LinkedList();
	while (pe != null) {
	    ll.addFirst(pe);
	    pe = pe.getParent();
	}
	return ll;
    }

    /**
     * Don't re-open closed nodes unless new route is less than this fraction
     * of current best cost to the node
     */
    private static final float TOLERANCE = 0.9f;

    /** @return the lowest estimated remaining cost to the target achieved 
     * during exploration (i.e. how close did we get to the target before
     * we gave up) */
    public int getBestEstimatedCost()
    {
        return bestEstimate;
    }
    
    /**
     * Perform the exploration.
     * @return a LinkedList of PathExplorer objects. The first object in the
     * link is at the starting point, and the last contains the destination.
     * Return null if no path could be found.
     */
    public LinkedList explore() {
        bestEstimate = Integer.MAX_VALUE;
	int iterations = 0;
	logger.log(Level.FINE, "exploring from " + start + " to " + target);
	logger.log(Level.FINE, "best minCost=" + minCost + ", maxCost=" +
	       maxCost);
	
	PathExplorer pe = startExplorer;
	
	open.add(pe);
	int cost = 0;
	while (! open.isEmpty()) {
	    pe = (PathExplorer) open.removeFirst();
	    logger.log(Level.FINEST, "fetched tile " +
		    pe.getLocation());
	    if (iterations % 2000 == 0) {
		logger.log(Level.FINE, "current path cost: " +
			(pe.getEstimatedCost(target) + pe.getCumulativeCost())
			+ " iterations: " + iterations);
		logger.log(Level.FINE, "open size=" + open.size() +
		       	", closed size=" + closed.size() + " efficiency: " +
			((closed.size() * 100) / 
			 (iterations == 0 ? 1 : iterations)) );
	    }
	    if (pe.getLocation().equals(target)) {
		logger.log(Level.INFO, "found target " + target + 
			" after " + iterations + " iterations");
		return getBestPath(pe);
	    }

	    while (pe.hasNextDirection()) {
		PathExplorer neighbour = (PathExplorer) pe.exploreNewTile();
	//	logger.log(Level.FINEST, "testing neighbour " +
	//		neighbour.getLocation());
		boolean isOpen = open.contains(neighbour);
		PathExplorer closedPathExplorer = (PathExplorer)
		    closed.get(neighbour);
		boolean isClosed = (closedPathExplorer != null);		
		if (isClosed) {
                    if (neighbour.getCumulativeCost() < (int) 
			 (closedPathExplorer.getCumulativeCost() * TOLERANCE)) {
                        // we found a better path to this tile
                        closed.remove(neighbour);
                        isClosed = false;
                    }
		}
		if (!isClosed && !isOpen) {
                    int estimate = neighbour.getEstimatedCost(target);
                    int neighbourCost = neighbour.getCumulativeCost() +
                        estimate;                    
		    if (neighbourCost <= maxCost)
			open.add(neighbour);
                    else if (estimate <= bestEstimate)
                        bestEstimate = estimate;
		}
	    }
	    closed.put(pe, pe);
	    iterations++;
	}

	// no path was found
	return null;
    }
}
