/*
 * Copyright (C) 2004 Robert Tuck
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

package jfreerails.world.train;

import java.awt.Point;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashMap;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.track.*;

/**
 * Describes a continuous sequence of straight-line segments.
 * @author rtuck99@users.berlios.de
 */
public class TrainPath implements FreerailsSerializable {
    /**
     * A linked list of type IntLine
     */
    private LinkedList segments = new LinkedList();

    /* we store the path length in order to avoid changes in total length
     * caused by rounding errors when summing calculated diagonals */
    private int length;

    public TrainPath (Point[] points) {
	if (points.length < 2) {
	    throw new IllegalArgumentException();
	}
	double length = 0;
	for (int i = 1; i < points.length; i++) {
	    IntLine l = new IntLine(points[i - 1], points[i]);
	    segments.add(l);
	    length += l.getLength();
	}
	this.length = (int) length;
    }

    public TrainPath (IntLine[] lines) {
	if (lines.length != 0) {
	    for (int i = 0; i < lines.length; i++)
		segments.add(lines[i]);
	}
    }

    public TrainPath(TrainPath p) {
	length = p.length;
	ListIterator i = p.segments.listIterator(0);
	while (i.hasNext()) {
	    segments.add(new IntLine((IntLine) i.next()));
	}
    }

    public int getLength() {
	return length;
    }

    public void getHead(Point p) {
	IntLine l = (IntLine) segments.getFirst();
	p.x = l.x1;
	p.y = l.y1;
    }

    public void getTail(Point p) {
	IntLine l = (IntLine) segments.getLast();
	p.x = l.x2;
	p.y = l.y2;
    }

    public IntLine getFirstSegment() {
	return (IntLine) segments.getFirst();
    }

    public IntLine getLastSegment() {
	return (IntLine) segments.getLast();
    }

    /**
     * Append the specified path to the tail
     */
    public void append(TrainPath tp) {
	IntLine tpHead = (IntLine) tp.segments.getFirst();
	IntLine tail = (IntLine) segments.getLast();
	if (tpHead.x2 != tail.x1 ||
		tpHead.y2 != tail.y1)
	    throw new IllegalArgumentException();
	if (tpHead.getDirection() == tail.getDirection()) {
	    tail.append(tpHead);
	} else {
	    segments.add(tpHead);
	}
	ListIterator i = tp.segments.listIterator(1);
	while (i.hasNext()) {
	    segments.add(i.next());
	}
	length += tp.getLength();
    }

    /**
     * Truncate this TrainPath by removing a portion from the tail.
     * @param newLength the new length of the path
     * @return the portion which was removed
     */
    public TrainPath truncateTail (int newLength) {
	ListIterator i = segments.listIterator(0);
	double l = 0;
	IntLine line = null;
	LinkedList removedSegments = new LinkedList(); 
	while (i.hasNext() && l < newLength) {
	    line = (IntLine) i.next();
	    l += line.getLength();
	}
	while (i.hasNext()) {
	    removedSegments.add(i.next());
	    i.remove();
	}
	if (l > newLength) {
	    int oldX2 = line.x2;
	    int oldY2 = line.y2;
	    line.setLength(line.getLength() - (l - newLength));
	    removedSegments.addFirst(new IntLine(line.x2, line.y2, oldX2,
		       oldY2));
	}
	length = newLength;
	return new TrainPath((IntLine[]) removedSegments.toArray(new
		    IntLine[removedSegments.size()]));
    }
    
    /**
     * Prepend the specified path to this TrainPath. The tail of
     * additionalPath is equal to the head of this path
     */
    public void prepend(TrainPath additionalPath) {
	int l = additionalPath.length;
	IntLine tail = (IntLine) additionalPath.segments.removeLast();
	IntLine head = getFirstSegment();
	if (tail.getDirection() == head.getDirection()) {
	    head.prepend(tail);
	} else {
	    segments.addFirst(tail);
	}
	/* add the rest of the path */
	while (! additionalPath.segments.isEmpty()) {
	    segments.addFirst(additionalPath.segments.removeLast());
	}
	length += l;
    }

    /**
     * Moves the head to the corresponding location, and advances the tail by
     * the same amount, keeping the length the same.
     * @param additionalPath the path to add. The tail of additionalPath is
     * equal to the head of this path.
     * @return the portion of the tail removed to maintain constant length
     */
    public TrainPath moveHeadTo(TrainPath additionalPath) {
	prepend(additionalPath);
	return truncateTail(length);
    }
    
    /**
     * Moves the tail to the corresponding location, and reverses the head by
     * the same amount, keeping the length the same.
     * @param additionalPath the path to add. The head of additionalPath is
     * equal to the tail of this path.
     * @return the position of the head prior to the path being advanced
     */
    public Point moveTailTo(TrainPath additionalPath) {
	IntLine tail = (IntLine) additionalPath.segments.removeFirst();
	IntLine head = getLastSegment();
	Point oldHeadPoint = new Point (tail.x1, tail.y1);
	if (tail.getDirection() == head.getDirection()) {
	    head.append(tail);
	} else {
	    segments.add(tail);
	}
	/* add the rest of the path */
	while (! additionalPath.segments.isEmpty()) {
	    segments.add(additionalPath.segments.removeFirst());
	}
	double l = length;
	int i = segments.size();
	do {
	    l -= ((IntLine) segments.get(i)).getLength();
	    i--;
	} while (l > 0 && i >= 0);
	while (i > 0) {
	    /* remaining segments are surplus to requirements */
	    segments.removeFirst();
	    i--;
	};	
	if (l < 0) {
	    IntLine seg = (IntLine) segments.getFirst();
	    seg.setLengthFromTail(seg.getLength() + l);
	}
	return oldHeadPoint;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof TrainPath))
	    return false;

	TrainPath tp = (TrainPath) o;
	return (tp.segments.equals(segments));
    }

    public int hashCode() {
	return segments.hashCode();
    }

    /**
     * Determines the map coordinates and directions in which the TrainPath
     * traverses.
     * @param mapCoords an empty HashMap into which the map coordinates are
     * to be put as keys, and the directions as objects.
     */
    public void getMapCoordsAndDirections(HashMap mapCoords) {
	byte direction, nextDirection;
	ListIterator i = segments.listIterator(0);
	Point map = new Point();
	final Point oldMapPoint = new Point();
	nextDirection = 0;
	while (i.hasNext()) {
	    IntLine l = (IntLine) i.next();
	    int dx = l.x2 - l.x1;
	    int dy = l.y2 - l.y1;
	    map.x = l.x1 / TrackTile.DELTAS_PER_TILE;
	    map.y = l.y1 / TrackTile.DELTAS_PER_TILE;
	    while (map.x != l.x2 / TrackTile.DELTAS_PER_TILE &&
		    map.y != l.y2 / TrackTile.DELTAS_PER_TILE) {
		direction = nextDirection;
		nextDirection = 0;
		oldMapPoint.x = map.x;
		oldMapPoint.y = map.y;
		if (dx < 0) {
		    map.x--;
		    nextDirection = (byte) (CompassPoints.NORTHWEST |
			CompassPoints.WEST |
			CompassPoints.SOUTHWEST);
		} else if (dx > 0) {
		    map.x++;
		    nextDirection = (byte) (CompassPoints.NORTHEAST |
			CompassPoints.EAST |
			CompassPoints.SOUTHEAST);
		} else {
		    nextDirection = (byte) (CompassPoints.NORTH |
			CompassPoints.SOUTH);
		}
		if (dy < 0) {
		    map.y--;
		    nextDirection &= (byte) (CompassPoints.NORTHWEST |
			    CompassPoints.NORTH | CompassPoints.NORTHEAST);
		} else if (dy > 0) {
		    map.y++;
		    nextDirection &= (byte) (CompassPoints.SOUTHWEST |
			    CompassPoints.SOUTH |
			    CompassPoints.SOUTHEAST);
		} else {
		    nextDirection &= (byte) (CompassPoints.EAST |
			    CompassPoints.WEST);
		}
		direction |= (byte) nextDirection;
		Byte oldNextDirection = (Byte) mapCoords.get(map);
		if (oldNextDirection != null)
		    nextDirection |= oldNextDirection.byteValue();
		mapCoords.put(new Point(oldMapPoint), new Byte(direction));
	    };
	}
	/* add the last point */
	mapCoords.put(new Point(map), new Byte(nextDirection));
    }

    /**
     * @return a CompassPoint indicating the direction at the specified locus
     * (traversing from head to tail)
     * @param p used to return the absolute position of the locus measured in
     * Deltas from the map origin.
     * @param distance distance of the locus from the head of the TrainPath
     */
    public byte getDirectionAtDistance(Point p, int distance) {
	double d = 0.0;
	ListIterator li = segments.listIterator(0);
	while (li.hasNext()) {
	    IntLine l = (IntLine) li.next();
	    double segLength = l.getLength();
	    if (d + segLength >= distance) {
		// locus is within this segment.
		p.x = l.x1 + (int) ((l.x2 - l.x1) * (distance - d) / segLength);
		p.y = l.y1 + (int) ((l.y2 - l.y1) * (distance - d) / segLength);
		return l.getDirection();
	    }
	    d += segLength;
	}
	// distance was bigger than length of the TrainPath
	throw new IllegalArgumentException();
    }
}
