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

import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
/**
 * The PathExplorer for finding the path for a train between two stops.
 * TODO calculate cost from terrain and track layout.
 * Also it may be more efficient to scan from 45deg A/C from target, rather
 * than 180..)
 */
public class TrainPathExplorer implements PathExplorer {
    private ReadOnlyWorld world;

    private int x, y;

    private byte initialDirection;

    private int parentCost;

    private TrainPathExplorer parent;

    /**
     * Direction of last returned tile, or 0 if no tile returned
     */
    private byte direction;

    private TrainPathExplorer(TrainPathExplorer tpe) {
	x = tpe.x;
	y = tpe.y;
	world = tpe.world;
	initialDirection = tpe.initialDirection;
	direction = tpe.direction;
	parent = tpe.parent;
    }

    public PathExplorer getCopy() {
	return new TrainPathExplorer (this);
    }

    public PathExplorer exploreNewTile() {
	int newX = x;
	int newY = y;
	byte newDirection = getNextDirection();
	if (newDirection == 0)
	    return null;

	direction = newDirection;

	switch(direction) {
	    case CompassPoints.NORTH:
		newY--;
		break;
	    case CompassPoints.NORTHEAST:
		newY--;
	    case CompassPoints.EAST:
		newX++;
		break;
	    case CompassPoints.SOUTHEAST:
		newY++;
		newX++;
		break;
	    case CompassPoints.SOUTH:
		newY++;
		break;
	    case CompassPoints.SOUTHWEST:
		newX--;
		newY++;
		break;
	    case CompassPoints.WEST:
		newX--;
		break;
	    case CompassPoints.NORTHWEST:
		newX--;
		newY--;
		break;
	    default:
		throw new IllegalArgumentException();
	}
	return new TrainPathExplorer(this, newX, newY);
    }

    public TrainPathExplorer (ReadOnlyWorld w, int x, int y, byte
	    initialDirection) {
	this.x = x;
	this.y = y;
	this.initialDirection = initialDirection;
	direction = initialDirection;
	world = w;
	parent = null;
    }

    private TrainPathExplorer (TrainPathExplorer pe, int x, int y) {
	this.x = x;
	this.y = y;
	world = pe.world;
	initialDirection = CompassPoints.invert(pe.direction);
	direction = initialDirection;
	parent = pe;
    }

    public boolean hasNextDirection() {
	return getNextDirection() != 0;
    }

    private byte getNextDirection() {
	byte trackConfig = world.getTile(x, y).getTrackConfiguration();
	byte newDirection = direction;
	byte dirty = initialDirection;
	do {
	    if (newDirection == 0) {
	       	newDirection = initialDirection;
		dirty = 0;
	    } else {
		dirty |= newDirection;
		newDirection = CompassPoints.rotateClockwise(newDirection);
	    }
	    if ((newDirection & trackConfig) != 0 &&
		    ((newDirection & dirty) == 0)) {
		return newDirection;
	    }
	} while ((newDirection & dirty) == 0);
	return 0;
    }

    public Point getLocation() {
	return new Point(x, y);
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public int getCost() {
	return CompassPoints.getLength(initialDirection);
    }

    public int getCumulativeCost() {
	TrainPathExplorer tpe = this;
	int cost = 0;
	while (tpe != null) {
	    cost += tpe.getCost();
	    tpe = tpe.parent;
	}

	return cost;
    }

    public byte getDirection() {
	return CompassPoints.invert(initialDirection);
    }

    public int getEstimatedCost(Point p) {
	PathLength pl = new PathLength(x, y, p.x, p.y);
	return (int) (pl.getLength() * TrackTile.DELTAS_PER_TILE);
    }

    public void reset() {
	direction = initialDirection;
    }

    public boolean equals(Object o) {
	if (o instanceof TrainPathExplorer) {
	    TrainPathExplorer tpe = (TrainPathExplorer) o;
	    return x == tpe.x && y == tpe.y &&
	       	initialDirection == tpe.initialDirection;
	}
	return false;
    }

    public int hashCode() {
	return x ^ y;
    }

    public PathExplorer getParent() {
	return parent;
    }
}
