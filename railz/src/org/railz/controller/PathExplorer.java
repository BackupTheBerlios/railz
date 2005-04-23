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

/**
 * Represents the internal state of a single tile in the exploration of the
 * map by the PathFinder.
 * This class should implement equals() and hashCode() such that two equal
 * PathExplorers provide identical neighbour tiles when iterated.
 */
public interface PathExplorer {
    /**
     * @return a new PathExplorer and update the state of this tile. If this
     * method returns null then we have already explored all the accesible
     * tiles from this point.
     */
    public PathExplorer exploreNewTile();

    /**
     * @return whether there is a new tile to explore from this point
     */ 
    public boolean hasNextDirection();

    public Point getLocation();

    public int getX();

    public int getY();

    /**
     * @return the cost of traversing from the centre of the previous tile to
     * the centre of this tile.
     */
    public int getCost();

    /**
     * @return the cumulative cost of traversing from the start to this point.
     */
    public int getCumulativeCost(); 

    /**
     * @return a copy of this PathExplorer. This is used for saving the state
     * of the explorer so that the discovered path can be preserved.
     */
    public PathExplorer getCopy();

    /**
     * @return the direction from the tile traversed to reach
     * this tile (the parent tile), to this tile.
     */
    public byte getDirection();

    /**
     * @return the estimated cost from the location of this tile to the
     * specified location
     */
    public int getEstimatedCost(Point p);

    /**
     * Reset the iterator provided by the exploreNewTile() and
     * hasNextDirection() methods.
     */
    public void reset();

    /**
     * @return the tile traversed to reach this tile.
     */
    public PathExplorer getParent();
}
