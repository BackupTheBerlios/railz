/*
 * Copyright (C) 2002 Luke Lindsay
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

package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.station.*;
import jfreerails.world.top.*;
import jfreerails.world.train.*;
import jfreerails.world.track.*;

/**
 * Responsible for moving the trains.
 *
 * @author Luke Lindsay 27-Oct-2002
 *
 * Rewritten from scratch by Robert Tuck 12-Apr-2004
 *
 * TODO
 * Determine position based upon elapsed time since the last tile centre was
 * passed, and the desired speed at the next centre point.
 * s = ut + 0.5at^2
 * f = ma ... fd = mad ... p = mav ... p/mv = a .. p / mv = dv/dt
 * S p / mv dt = dv
 *
 * for now just use simple constant-speed model.
 */
public class TrainMover {
    private World world;
    private TrainPathFinder pathFinder;

    public TrainMover(World w) {
	world = w;
	pathFinder = new TrainPathFinder(w);
    }

    /**
     * Moves all the trains if appropriate
     */
    public void moveTrains() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    NonNullElements j = new NonNullElements(KEY.TRAINS, world, p);
	    while (j.next()) {
		TrainModel tm = (TrainModel) j.getElement();
		if (tm.getPosition() == null) {
		    setInitialPosition(tm, p, j.getIndex());
		}
		if (tm.getState() == TrainModel.STATE_RUNNABLE)
		    updateTrainPosition(tm);
	    }
	}
    }

    private void updateTrainPosition(TrainModel tm) {
	System.out.println("updating position of " + tm);
	final HashMap newMapTiles = new HashMap();
	final HashMap oldMapTiles = new HashMap();
	final HashMap undoList = new HashMap();
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	TrainMotionModel tmm = tm.getTrainMotionModel();
	if (tmm.isBlocked() &&
		! acquireAllLocks(tm))
	    return;
	/* work out where we should be */
	/* speed in tiles per BigTick */
	float speed = ((float) ((EngineType) world.get(KEY.ENGINE_TYPES,
		    tm.getEngineType(), Player.AUTHORITATIVE)).getMaxSpeed()) /
			EngineType.TILE_HOURS_PER_MILE_BIGTICKS;
	/* speed in deltas per tick */
	speed *= TrackTile.DELTAS_PER_TILE / GameTime.TICKS_PER_BIG_TICK;
	int ticksSinceLastSync = now.getTime() -
	    tmm.getTimeOfLastSync().getTime();
	int distanceToTarget = (int) (tmm.getPathToDestination().getLength() +
	    tmm.getPathTraversedSinceLastSync().getLength() - speed *
	    ticksSinceLastSync);
	if (distanceToTarget < 0)
	    distanceToTarget = 0;

	assert distanceToTarget <= tmm.getPathToDestination().getLength();

	TrainPath tp =
	    tmm.getPathToDestination().truncateTail(distanceToTarget);
	/* check to see if we have crossed a tile boundary */
	newMapTiles.clear();
	oldMapTiles.clear();
	undoList.clear();
	tp.getMapCoordsAndDirections(newMapTiles);
	Iterator i = newMapTiles.entrySet().iterator();
	boolean undoNeeded = false;
	while (i.hasNext()) {
	    Byte b;
	    byte extraLocks;
	    Entry e = (Entry) i.next();
	    if ((b = (Byte) oldMapTiles.get(e.getValue())) != null) {
		extraLocks = (byte) ((~b.byteValue()) & ((Byte)
			e.getValue()).byteValue());
	    } else {
		extraLocks = ((Byte) e.getValue()).byteValue();
	    }
	    if (extraLocks != 0) {
		TrackTile tt = world.getTile((Point)
			e.getValue()).getTrackTile();
		if (! tt.getLock(extraLocks)) {
		    undoNeeded = true;
		    break;
		}
		undoList.put(e.getKey(), new Byte(extraLocks));
	    }
	}
	if (undoNeeded) {
	    /* undo all locks, restore the pathToDestination and bail out */
	    i = undoList.entrySet().iterator();
	    while (i.hasNext()) {
		Entry e = (Entry) i.next();
		world.getTile((Point) e.getKey()).
		    getTrackTile().releaseLock
		    (((Byte) e.getValue()).byteValue());
	    }
	    tmm.getPathToDestination().append(tp);
	    return;
	}
	TrainPath pos = tm.getPosition();
	TrainPath removed = pos.moveHeadTo(tp);
	/* unlock any tiles we are no longer using */
	newMapTiles.clear();
	pos.getMapCoordsAndDirections(newMapTiles);
	removed.getMapCoordsAndDirections(oldMapTiles);
	i = oldMapTiles.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    Byte b;
	    byte locksToRemove = ((Byte) e.getValue()).byteValue();
	    if ((b = (Byte) newMapTiles.get((Point) e.getKey())) != null)
		locksToRemove &= ~b.byteValue();
	    TrackTile tt = world.getTile((Point) e.getKey()).getTrackTile();
	    tt.releaseLock(locksToRemove);
	}
	/* increment the pathTraversedSinceLastSync */
	tmm.getPathTraversedSinceLastSync().prepend(removed);
    }

    private void setInitialPosition(TrainModel tm, FreerailsPrincipal
	    trainPrincipal, int trainIndex) {
	ScheduleIterator si = tm.getScheduleIterator();
	TrainOrdersModel departsOrder = si.getCurrentOrder(world);
	StationModel departsStation = (StationModel) world.get(KEY.STATIONS,
		departsOrder.getStationNumber().index,
		departsOrder.getStationNumber().principal); 
	tm = new TrainModel (tm, si = si.nextOrder(world));
	world.set(KEY.TRAINS, trainIndex, tm, trainPrincipal);
	TrainOrdersModel arrivesOrder = si.getCurrentOrder(world);
	StationModel arrivesStation = (StationModel) world.get(KEY.STATIONS,
		arrivesOrder.getStationNumber().index,
		arrivesOrder.getStationNumber().principal);

	Point departStationCoords = new Point(departsStation.getStationX(),
		    departsStation.getStationY());
	Point arriveStationCoords = new Point(arrivesStation.getStationX(),
		    arrivesStation.getStationY());
	departStationCoords =
	    TrackTile.tileCoordsToDeltas(departStationCoords);
	arriveStationCoords =
	    TrackTile.tileCoordsToDeltas(arriveStationCoords);
	/* XXX arguments are reversed since we want the tail of the TrainPath
	 * to be the head of the train */
	TrainPath pathToNextStation = pathFinder.findPath(arriveStationCoords,
		    departStationCoords);
	TrainPath pathTraversedSinceLastSync = new TrainPath(new IntLine[] 
		{new IntLine(departStationCoords.x, departStationCoords.y,
		    departStationCoords.x, departStationCoords.y) });
	TrainMotionModel tmm = tm.getTrainMotionModel();
	tmm.setPathTraversedSinceLastSync(pathTraversedSinceLastSync);
	int trainLength = tm.getLength();
	int pathToNextStationLength = pathToNextStation.getLength() -
	    trainLength;
	if (pathToNextStationLength < 0)
	    /* XXX help! what should we do here? */
	    throw new IllegalStateException("Stations are too close to place "
		    + "train");

	TrainPath trainPos =
	    pathToNextStation.truncateTail(pathToNextStationLength);
	tmm.setPathToDestination(pathToNextStation);
	System.out.println ("setting new train position " + trainPos +
		" on model " + tm + " in " + Thread.currentThread().getName());
	tm.setPosition(trainPos);
    }

    private boolean acquireAllLocks(TrainModel tm) {
	HashMap mapCoords = new HashMap();
	tm.getPosition().getMapCoordsAndDirections(mapCoords);
	final HashMap undoList = new HashMap();
	undoList.clear();
	Iterator i = mapCoords.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    Point p = (Point) e.getKey();
	    Byte b = (Byte) e.getValue();
	    TrackTile tt = world.getTile(p).getTrackTile();
	    if (!tt.getLock(b.byteValue())) {
		i = undoList.entrySet().iterator();
		while (i.hasNext()) {
		    e = (Entry) i.next();
		    tt = world.getTile((Point) e.getKey()).getTrackTile();
		    tt.releaseLock(((Byte) e.getValue()).byteValue());
		}
		return false;
	    }
	    undoList.put(p, b);
	}
	tm.getTrainMotionModel().setBlocked(false);
	return true;
    }

    private void releaseAllLocks(TrainModel tm) {
	HashMap mapCoords = new HashMap();
	tm.getPosition().getMapCoordsAndDirections(mapCoords);
	Iterator i = mapCoords.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    world.getTile((Point) e.getKey()).getTrackTile().releaseLock
		(((Byte) e.getValue()).byteValue());
	}
	tm.getTrainMotionModel().setBlocked(true);
    }

    private static byte directionFromDelta(int dx, int dy) {
	switch (dx) {
	    case 1:
		switch (dy) {
		    case 1:
			return CompassPoints.SOUTHEAST;
		    case 0:
			return CompassPoints.EAST;
		    case -1:
			return CompassPoints.NORTHEAST;
		    default:
			throw new IllegalArgumentException();
		}
	    case 0:
		switch (dy) {
		    case 1:
			return CompassPoints.SOUTH;
		    case -1:
			return CompassPoints.NORTH;
		    default:
			throw new IllegalArgumentException();
		}
	    case -1:
		switch (dy) {
		    case -1:
			return CompassPoints.NORTHWEST;
		    case 0:
			return CompassPoints.WEST;
		    case 1:
			return CompassPoints.SOUTHWEST;
		    default:
			throw new IllegalArgumentException();
		}
	    default:
		throw new IllegalArgumentException();
	}
    }
}
