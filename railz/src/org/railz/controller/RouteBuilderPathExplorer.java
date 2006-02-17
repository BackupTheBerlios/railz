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

import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.terrain.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;

/**
 * The PathExplorer for the AI client RouteBuilder. This PathExplorer
 * determines the best route for a train of a specific mass and EngineType,
 * taking into account terrain and construction cost.
 */
public final class RouteBuilderPathExplorer implements  PathExplorer {
    private final RouteBuilderPathExplorer parent;
    private final int x;
    private final int y;
    private final byte initialDirection;
    private byte direction;
    private final ReadOnlyWorld world;
    private final RouteBuilderPathExplorerSettings settings;
    private static final Logger logger = Logger.getLogger("global");
    private int cost = Integer.MIN_VALUE;

    public RouteBuilderPathExplorer(ReadOnlyWorld w, int x, int y, byte i,
	    RouteBuilderPathExplorerSettings s) {
	this(w, x, y, (byte) 0, i, s, (RouteBuilderPathExplorer) null);
    }

    private RouteBuilderPathExplorer(ReadOnlyWorld w, int x, int y, byte d,
	    byte i, RouteBuilderPathExplorerSettings s,
	    RouteBuilderPathExplorer p) {
	this.x = x;
	this.y = y;
	initialDirection = i;
	direction = d;
	world = w;
	settings = s;
	parent = p;
    }

    /**
     * @return a new PathExplorer and update the state of this tile. If this
     * method returns null then we have already explored all the accesible
     * tiles from this point.
     */
    public PathExplorer exploreNewTile() {
	byte nextDirection = getNextDirection(direction);
	if (nextDirection == 0)
	    return null;

	int newX = x + CompassPoints.getUnitDeltaX(nextDirection);
	int newY = y + CompassPoints.getUnitDeltaY(nextDirection);

	direction = nextDirection;

	return new RouteBuilderPathExplorer(world, newX, newY, (byte) 0,
		nextDirection, settings, this);
    }

    /**
     * @return whether there is a new tile to explore from this point. This
     * implementation only allows 45 degree bends in the track. We return the
     * straight-on direction first, then left and right turns.
     */ 
    public boolean hasNextDirection() {
	return getNextDirection(direction) != 0;
    }

    private byte getNextDirection(byte d) {
	byte nextDirection = 0;
	if (d == 0) {
	    nextDirection = initialDirection;
	} else if (parent == null) {
	    // the root tile can rotate in all directions
	    if (d == CompassPoints.rotateAnticlockwise(initialDirection))
		return 0;

	    nextDirection = CompassPoints.rotateClockwise(d);
	} else if (d == initialDirection) {
	    nextDirection = CompassPoints.rotateAnticlockwise(initialDirection);
	} else {
	    byte rotCw;
	    if (d == 
		    (rotCw = CompassPoints.rotateClockwise(initialDirection))) {
		return 0;
	    }
	    nextDirection = rotCw;
	}

	// validate this direction against map position
	int newX = x + CompassPoints.getUnitDeltaX(nextDirection);
	if (newX < 0 || newX >= world.getMapWidth())
	    return getNextDirection(nextDirection);
	int newY = y + CompassPoints.getUnitDeltaY(nextDirection);
	if (newY < 0 || newY >= world.getMapHeight())
	    return getNextDirection(nextDirection);

	// check to see if there is an obstruction
	FreerailsTile ft = world.getTile(newX, newY);
	if (! (Player.AUTHORITATIVE.equals(ft.getOwner()) ||
		    settings.owner.equals(ft.getOwner())) ||
		(ft.getBuildingTile() != null && ft.getTrackTile() == null))
	    return getNextDirection(nextDirection);

	// can we build here?
	TerrainType tt = (TerrainType) world.get(KEY.TERRAIN_TYPES,
		ft.getTerrainTypeNumber(), Player.AUTHORITATIVE);
	if (! settings.trackRule.canBuildOnThisTerrainType
		(tt.getTerrainCategory()))
	    return getNextDirection(nextDirection);
	
	return nextDirection;
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

    /**
     * @return the estimated cost of traversing from the centre of this tile
     * to the specified tile
     */
    public int getEstimatedCost(Point p) {
	int anglePenalty = 0;
	byte d1 = initialDirection;
	byte d2 = CompassPoints.deltasToDirection(p.x - x, p.y - y);

	if (d2 == 0) {
	    // already at p!
	    return 0;
	}

	while (d1 != d2) {
	    d1 = CompassPoints.rotateClockwise(d1);
	    anglePenalty++;
	}
	if (anglePenalty > 4)
	    anglePenalty = 8 - anglePenalty;
	// 1st turn is "free"
	if (anglePenalty > 0)
	    anglePenalty--;

	return (int) (1.1f * getCost() * (anglePenalty + 
	    ((int) new PathLength(x, y, p.x, p.y).getLength())));
    }

    private int cumulativeCost = Integer.MIN_VALUE;
    
    public int getCumulativeCost() {
	if (cumulativeCost != Integer.MIN_VALUE)
	    return cumulativeCost;

	int c = getCost();
	if (parent != null) {
	    c += parent.getCumulativeCost();
	}
	cumulativeCost = c;
	return c;
    }

    /**
     * @return the cost of traversing from the centre of the previous tile to
     * the centre of this tile.
     */
    public int getCost() {
        // return the cached cost if we already calculated it
	if (cost != Integer.MIN_VALUE) {
	    return cost;
	}
        
	cost = 0;
	boolean nodebug = ! logger.isLoggable(Level.FINEST);
	if (parent != null) 
	    cost = parent.getIncrementalCost(initialDirection);
	logger.log (Level.FINEST, nodebug ? "" : "Incremental cost:" + cost);

	// calculate the cost of purchasing the tile, if required.
	FreerailsTile ft = world.getTile(x, y);
	if (! settings.owner.equals(ft.getOwner())) {
	    settings.terrainTileViewer.setFreerailsTile(x, y);
	    cost += settings.terrainTileViewer.getTerrainValue() / 100;
	}
	logger.log (Level.FINEST, nodebug ? "" : "After tile purchase cost:" + cost);

	/* These calculations should produce 0 cost if we already have
	 * existing track */
	byte config1 = 0;
        if (ft.getTrackTile() != null)
	    config1 = ft.getTrackTile().getTrackConfiguration();
	byte config2 = (byte) (config1 | direction);
	TrackTile tt1 = TrackTile.createTrackTile(world, config1,
		settings.trackRuleIndex);
	TrackTile tt2 = TrackTile.createTrackTile(world, config2,
		settings.trackRuleIndex);
	settings.transactionsGenerator.resetTransactions();
	settings.transactionsGenerator.addTrackChange(ft, tt1, tt2);
        cost += settings.transactionsGenerator.getTotalCost() / 100;
	logger.log (Level.FINEST, nodebug ? "" : "After track laying cost:" + cost);

	// calculate penalties / bonus due to terrain
	if (parent != null) 
	    settings.terrainTileViewer.setFreerailsTile(parent.x, parent.y);
	int effectiveIncline = settings.terrainTileViewer
	    .getEffectiveIncline(x, y);
	cost += settings.getTerrainBonus(effectiveIncline);
	logger.log (Level.FINER, nodebug ? "" : "Total track laying cost:" + cost);

	return cost;
    }

    private TerrainType getTerrainType() {
	return (TerrainType) world.get(KEY.TERRAIN_TYPES,
	       	world.getTile(x, y).getTerrainTypeNumber(),
		Player.AUTHORITATIVE);
    }

    /**
     * @return a copy of this PathExplorer. This is used for saving the state
     * of the explorer so that the discovered path can be preserved.
     */
    public PathExplorer getCopy() {
	RouteBuilderPathExplorer pe = new RouteBuilderPathExplorer(world, x,
		y, direction, initialDirection, settings, parent);
	pe.cost = cost;
	return pe;
    }

    /**
     * @return the direction from the tile traversed to reach
     * this tile (the parent tile), to this tile.
     */
    public byte getDirection() {
	return initialDirection;
    }

    public static final class RouteBuilderPathExplorerSettings {
	private FreerailsPrincipal owner;
	private TrackRule trackRule;
	private int trackRuleIndex;
	private TrackMoveTransactionsGenerator transactionsGenerator;
	private TerrainTileViewer terrainTileViewer;
	private EngineType engineType;
	private HashMap bonusCache = new HashMap();
	private int mass;

	/**
	 * Get the terrain bonus in $100 units, these values are cached since
	 * calculating max speed is expensive.
	 * XXX This method contains "fudge" factors. How should the cost be
	 * calculated for delay due to track tile?
	 */
	int getTerrainBonus(int effectiveIncline) {
	    Integer i = new Integer(effectiveIncline);
	    Integer bonus = (Integer) bonusCache.get(i);
	    if (bonus == null) {
		float maxSpeed = engineType.getMaxSpeed(effectiveIncline,
			mass);
		if (maxSpeed < 0.2f) {
		    // penalty for really steep/impassable gradients is 
		    // $10Million. That should stop them getting built:)
		    bonus = new Integer(100000);
		} else {
		    bonus = new Integer((int) (30 - 10 * maxSpeed));
		}
		bonusCache.put(i, bonus);
	    }
	    return bonus.intValue();
	}

	/**
	 * @param dtt index into the TRACK_RULES table for the default track
	 * type to build.
	 * @param owner the principal on whose behalf we are exploring.
	 * @param et the index into the ENGINE_TYPES table
	 * @param m mass of the train in tonnes
	 */
	public RouteBuilderPathExplorerSettings(ReadOnlyWorld w,
		int dtt, FreerailsPrincipal p, int et, int m) {
	    trackRuleIndex = dtt;
	    trackRule = (TrackRule) w.get(KEY.TRACK_RULES, dtt,
		    Player.AUTHORITATIVE);
	    owner = p;
	    transactionsGenerator = new TrackMoveTransactionsGenerator
		(w, p);
	    terrainTileViewer = new TerrainTileViewer(w);
	    engineType = (EngineType) w.get(KEY.ENGINE_TYPES, et,
		    Player.AUTHORITATIVE);
	    mass = m;
	}
    }

    /**
     * @return the incremental cost in 100s of $ of building track from the
     * centre of this tile to the edge of the last tile returned.
     */
    private int getIncrementalCost(byte d) {
	if (d == 0)
	    return 0;
	FreerailsTile ft = world.getTile(x, y);
	// byte config1 = CompassPoints.invert(initialDirection);
        byte config1 = initialDirection;
        if (ft.getTrackTile() != null) 
	    config1 |= ft.getTrackTile().getTrackConfiguration();
	byte config2 = (byte) (config1 | d);
	TrackTile tt1 = TrackTile.createTrackTile(world, config1,
		settings.trackRuleIndex);
	TrackTile tt2 = TrackTile.createTrackTile(world, config2,
		settings.trackRuleIndex);
	settings.transactionsGenerator.resetTransactions();
	settings.transactionsGenerator.addTrackChange(ft, tt1, tt2);        
        long cost = settings.transactionsGenerator.getTotalCost();        
	return (int) (cost / 100);
    }

    public void reset() {
	direction = 0;
    }

    public boolean equals(Object o) {
	if (o instanceof RouteBuilderPathExplorer) {
	    RouteBuilderPathExplorer pe = (RouteBuilderPathExplorer) o;
	    return x == pe.x && y == pe.y;
	       //	&& initialDirection ==
		// pe.initialDirection;
	}
       return false;	
    }

    public int hashCode() {         
	return x ^ (y << 16);
       //	^ initialDirection;
    }

    public PathExplorer getParent() {
	return parent;
    }

    public String toString() {
	return "RouteBuilderPathExplorer: x=" + x + ", y=" + y;
    }
}

