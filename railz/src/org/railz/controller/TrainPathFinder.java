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
package org.railz.controller;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;

public class TrainPathFinder {
    private ReadOnlyWorld world;

    public TrainPathFinder(ReadOnlyWorld w) {
	world = w;
    }

    private void fixUpPathEnds(LinkedList p, Point newStart, Point newEnd) {
	IntLine oldStartSegment = (IntLine) p.getFirst();
	IntLine oldEndSegment = (IntLine) p.getLast();
	Point oldStart = new Point(oldStartSegment.x1, oldStartSegment.y1);
	Point oldEnd = new Point(oldEndSegment.x2, oldEndSegment.y2);
	IntLine newStartSegment = new IntLine(newStart, oldStart);
	IntLine newEndSegment = new IntLine(oldEnd, newEnd);
	/* If direction of old & new lines is 180deg out, then substitute
	 * truncated path */
	if (newStartSegment.getDirection() ==
		CompassPoints.invert(oldStartSegment.getDirection())) {
	    p.removeFirst();
	    p.addFirst(new IntLine(newStart.x, newStart.y, oldStartSegment.x2,
			    oldStartSegment.y2));
	} else {
	    p.addFirst(newStartSegment);
	}
	if (newEndSegment.getDirection() ==
		CompassPoints.invert(oldEndSegment.getDirection())) {
	    p.removeLast();
	    p.addLast(new IntLine(oldEndSegment.x1, oldEndSegment.y1,
			newEnd.x, newEnd.y));
	} else {
	    p.addLast(newEndSegment);
	}
    }

    /**
     * @param start start location measured in Deltas from map origin.
     * @param dest end location measured in Deltas from map origin.
     * TODO improve efficiency of this algorithm (discard paths when the
     * possible minimum is longer than current best, compare current best
     * against theoretical minimum and accept non-optimal solutions if they
     * are within a certain %age.
     * @return the found path, or null if no path could be found.
     */
    public TrainPath findPath(Point start, Point dest) {
	Point target = new Point(dest);
	TrackTile.deltasToTileCoords(target);
	Point startCentre = new Point (start);
	TrackTile.deltasToTileCoords(startCentre);
	
	/* "Stupidity" filter... */
	if (startCentre.equals(target))
	    return new TrainPath(new Point[]{new Point(start.x, start.y),
		    TrackTile.tileCoordsToDeltas(startCentre),
		    new Point(dest.x, dest.y)});

	PathExplorer pe = new TrainPathExplorer(world, startCentre.x,
		startCentre.y, CompassPoints.NORTH);
	PathFinder pf = new PathFinder(pe, target.x, target.y, 0,
		Integer.MAX_VALUE);
	LinkedList bestPath = pf.explore();
	
	startCentre = TrackTile.tileCoordsToDeltas(startCentre);

	/* all possible paths have been explored, but no route was found */
	if (bestPath.isEmpty())
	    return null;

	/* convert the ArrayList to a TrainPath. bestPath always contains the
	 * target tile and the start tile */
	Point oldp = new Point();
	Point newp = new Point();
	byte oldDirection;
	PathExplorer p = (PathExplorer) bestPath.removeFirst();
	LinkedList intLines = new LinkedList();
	boolean firstP = true;
	while (!bestPath.isEmpty()) {
	    oldp.x = p.getX();
	    oldp.y = p.getY();
	    oldDirection = p.getDirection();
	    do {
		p = (PathExplorer) bestPath.removeFirst();
		newp.x = p.getX();
		newp.y = p.getY();
	    } while (p.getDirection() == oldDirection);
	    if (firstP) {
		oldp.x = startCentre.x;
		oldp.y = startCentre.y;
		firstP = false;
	    } else {
		oldp = TrackTile.tileCoordsToDeltas(oldp);
	    }
	    newp = TrackTile.tileCoordsToDeltas(newp);
	    IntLine l = new IntLine(oldp.x, oldp.y, newp.x, newp.y);
	    intLines.add(l);
	}
	fixUpPathEnds(intLines, start, dest);
	TrainPath retVal = new TrainPath((IntLine[]) intLines.toArray(new
		    IntLine[intLines.size()]));
	return retVal;
    }
}
