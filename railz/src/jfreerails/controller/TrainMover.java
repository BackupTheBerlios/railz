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

package org.railz.controller;

import java.awt.Point;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.world.track.*;

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
public final class TrainMover {
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
	       	if (tm.getTrainMotionModel().isLost()) {
		    continue;
		}
		if (tm.getState() == TrainModel.STATE_RUNNABLE)
		    updateTrainPosition(tm);
		else if (! tm.getTrainMotionModel().isBlocked())
		   tm.releaseAllLocks(world);
	    }
	}
    }

    private void updateTrainPosition(TrainModel tm) {
	final PathLength pl = new PathLength();
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	TrainMotionModel tmm = tm.getTrainMotionModel();
	/* work out where we should be */
	/* speed in tiles per BigTick */
	float speed = ((float) ((EngineType) world.get(KEY.ENGINE_TYPES,
		    tm.getEngineType(), Player.AUTHORITATIVE)).getMaxSpeed()) /
			EngineType.TILE_HOURS_PER_MILE_BIGTICKS;
	/* speed in deltas per tick */
	speed *= TrackTile.DELTAS_PER_TILE / GameTime.TICKS_PER_BIG_TICK;
	int ticksSinceLastSync = now.getTime() -
	    tmm.getTimeOfLastSync().getTime() - tmm.getBlockedFor();
	pl.setLength(tmm.getPathToDestination().getActualLength());
	pl.add(tmm.getPathTraversedSinceLastSync().getActualLength());
	int distanceToTarget = (int) (pl.getLength() - speed *
	    ticksSinceLastSync);
	if (distanceToTarget < 0)
	    distanceToTarget = 0;

	/* can happen due to rounding */
	if (distanceToTarget > tmm.getPathToDestination().getLength())
	    return;

	assert distanceToTarget <= tmm.getPathToDestination().getLength();

	TrainPath tp =
	    tmm.getPathToDestination().truncateTail(distanceToTarget);
	if (! tmm.isBlocked())
	   tm.releaseAllLocks(world);
	TrainPath pos = tm.getPosition();
	TrainPath removed = pos.moveHeadTo(tp);
	if (! tm.acquireAllLocks(world)) {
	    tmm.getPathToDestination().append(tp);
	    pos.moveTailTo(removed);
	    tmm.block();
	    return;
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
	/* don't update the world model with the new schedule since our state
	 * is STATE_UNLOADING */
	si = si.nextOrder(world);
	// tm = new TrainModel (tm, si = si.nextOrder(world));
	// world.set(KEY.TRAINS, trainIndex, tm, trainPrincipal);
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
	if (pathToNextStation == null) {
	    /* Couldn't find a path to the next station */
	    return;
	}
	TrainPath currentPos = new TrainPath(pathToNextStation);
	currentPos.reverse();
	int trainLength = tm.getLength();
	int pathToNextStationLength = pathToNextStation.getLength() -
	    trainLength;
	if (pathToNextStationLength < 0)
	    /* XXX help! what should we do here? */
	    throw new IllegalStateException("Stations are too close to place "
		    + "train");
	currentPos.truncateTail(trainLength);
	Point tail = new Point();
	currentPos.getTail(tail);
	TrainPath pathTraversedSinceLastSync = new TrainPath(new IntLine[] 
		{new IntLine(tail.x, tail.y,
		    tail.x, tail.y) });

	tm.setPosition(currentPos);
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
