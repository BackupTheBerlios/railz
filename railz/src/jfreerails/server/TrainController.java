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
package jfreerails.server;

import java.awt.Point;

import jfreerails.move.*;
import jfreerails.world.common.*;
import jfreerails.world.building.*;
import jfreerails.world.player.*;
import jfreerails.world.station.*;
import jfreerails.world.top.*;
import jfreerails.world.track.*;
import jfreerails.world.train.*;

/**
 * Responsible for controlling the state of trains. This package controls the
 * starting and stopping of trains at train stations, and the changing of
 * train destinations.
 */
class TrainController {
    /**
     * Time to wait in station whilst unloading cargo
     */
    private static final int UNLOADING_DELAY = 30;
    
    /**
     * Time to wait in station whilst loading cargo
     */
    private static final int LOADING_DELAY = 30;
    
    private ReadOnlyWorld world;
    private AuthoritativeMoveExecuter moveReceiver;

    public TrainController(ReadOnlyWorld w, AuthoritativeMoveExecuter mr) {
	world = w;
	moveReceiver = mr;
    }

    public void updateTrains() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    NonNullElements j = new NonNullElements(KEY.TRAINS, world, p);
	    while (j.next()) {
		TrainModel tm = (TrainModel) j.getElement();
		updateTrain(tm, now, p, j.getIndex());
	    }
	}
    }

    private void updateTrain(TrainModel tm, GameTime now, FreerailsPrincipal
	    p, int trainIndex) {
	int state = tm.getState();
	switch (state) {
	    case TrainModel.STATE_LOADING:
		if (tm.getStateLastChangedTime().getTime() + LOADING_DELAY <
			now.getTime())
		    loadTrain(tm, now, p, trainIndex);
		return;
	    case TrainModel.STATE_UNLOADING:
		if (tm.getStateLastChangedTime().getTime() + UNLOADING_DELAY <
			now.getTime())
		    unloadTrain(trainIndex, p, tm, now);
		return;
	    case TrainModel.STATE_RUNNABLE:
		/* check to see whether the train has reached its destination
		 */
		TrainPath tp =
		    tm.getTrainMotionModel().getPathToDestination();
		if (tp != null && tp.getLength() == 0) {
		    setState(trainIndex, p, TrainModel.STATE_UNLOADING);
		}
		return;
	    default:
		return;
	}
    }

    /**
     * TODO split loading and unloading into separate moves. For now do both
     * operations during load phase.
     */
    private void unloadTrain(int trainIndex, FreerailsPrincipal p,
	    TrainModel tm, GameTime now) {
	setState(trainIndex, p, TrainModel.STATE_LOADING);
    }

    private void loadTrain(TrainModel tm, GameTime now, FreerailsPrincipal p,
	    int trainIndex) {
	/* get the details of the station the train is at */
	Point point = new Point();
	tm.getPosition().getHead(point);
	TrackTile.deltasToTileCoords(point);
	FreerailsPrincipal sp = null;
	NonNullElements j = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	NonNullElements i = null;
	boolean flag = false;
	StationModel sm;
	while (j.next()) {
	    sp = ((Player) j.getElement()).getPrincipal();
	    i = new NonNullElements(KEY.STATIONS, world, sp);
	    while (i.next()) {
		sm = (StationModel) i.getElement();
		if (sm.getStationX() == point.x &&
			sm.getStationY() == point.y) {
		    flag = true;
		    break;
		}
	    }
	}
	if (flag) {
	    DropOffAndPickupCargoMoveGenerator dopucmg = new
		DropOffAndPickupCargoMoveGenerator(p, trainIndex, sp,
			i.getIndex(), world);
	    Move m = dopucmg.generateMove();
	    moveReceiver.processMove(m);
	}
	// set the trains new destination
	Move ctdm = ChangeTrainDestinationMove.generateMove(world,
		new ObjectKey(KEY.TRAINS, p, trainIndex),
		tm.getScheduleIterator().nextOrder(world));
	moveReceiver.processMove(ctdm);
	setState(trainIndex, p, TrainModel.STATE_RUNNABLE);
	return;
    }

    private void setState(int trainIndex, FreerailsPrincipal p, int newState) {
	System.out.println("setting state of train " + trainIndex + " for " +
		p + " to state " + stateToString(newState) + " at " + 
		world.get(ITEM.TIME, p));
	TrainModel tm = (TrainModel) world.get(KEY.TRAINS, trainIndex, p);
	ObjectKey key = new ObjectKey(KEY.TRAINS, p, trainIndex);
	ChangeTrainStateMove m  = new ChangeTrainStateMove(world, key,
		tm.getTrainMotionModel().getPathTraversedSinceLastSync(),
		tm.getTrainMotionModel().getTimeOfLastSync(), newState);
	moveReceiver.processMove(m);
    }

    private static String stateToString(int state) {
	switch (state) {
	    case TrainModel.STATE_RUNNABLE:
		return "Runnable";
	    case TrainModel.STATE_STOPPED:
		return "Stopped";
	    case TrainModel.STATE_LOADING:
		return "Loading";
	    case TrainModel.STATE_UNLOADING:
		return "Unloading";
	    default:
		throw new IllegalArgumentException();
	}
    }
}
