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
package org.railz.world.train;

import java.awt.Point;
import java.util.HashMap;

import org.railz.world.common.*;
/**
 * This class encapsulates the transient (non-serializable) portion of a
 * trains position and movement.
 * @author rtuck99@users.berlios.de
 */
public final class TrainMotionModel {
    /**
     * This is the trains speed in tiles per BigTick.
     */
    private float speed;

    /**
     * Describes the current planned path to this trains destination. The head
     * of this path corresponds to the next scheduled stop of this train. The
     * tail of this path corrsponds to the head of the train.
     */
    private TrainPath pathToDestination;

    /**
     * Describes the path traversed since the last time the client
     * synchronized with the server. The tail of the path coincides with the
     * head of the train.
     */
    private TrainPath pathTraversedSinceLastSync; 
    
    private final GameTime timeOfLastSync;

    private boolean isBlocked;

    /**
     * Number of ticks during which this train has been blocked.
     */
    private int blockedFor;
    
    /**
     * copy constructor used for setting a new path to destination from a
     * given TrainModel
     */
    TrainMotionModel(TrainMotionModel trainMotionModel, TrainModel trainModel,
	    GameTime now) {
	this(now, generateTrainPath(trainModel),
		(trainModel.getPathToDestination() == null ? null : new
		 TrainPath(trainModel.getPathToDestination())));
	blockedFor = 0;
	isBlocked = trainMotionModel == null ? true :
	    trainMotionModel.isBlocked;
	speed = trainMotionModel == null ? 0 : trainMotionModel.speed;
    } 

    /**
     * copy constructor
     */
    TrainMotionModel(TrainMotionModel tmm) {
	speed = tmm.speed;
	pathToDestination = tmm.pathToDestination == null ? null :
	    new TrainPath(tmm.pathToDestination);
	pathTraversedSinceLastSync = tmm.pathTraversedSinceLastSync == null ?
	    null : new TrainPath(tmm.pathTraversedSinceLastSync);
	timeOfLastSync = tmm.timeOfLastSync;
	blockedFor = tmm.blockedFor;
	isBlocked = tmm.isBlocked;
    }

    TrainMotionModel() {
	this((GameTime) null, (TrainPath) null, (TrainPath) null);
    }

    private TrainMotionModel (GameTime syncTime, TrainPath
	    pathTraversedSinceLastSync, TrainPath pathToDestination) {
	if (pathTraversedSinceLastSync != null)
	    this.pathTraversedSinceLastSync = new
	    TrainPath(pathTraversedSinceLastSync);
	if (pathToDestination != null)
	    this.pathToDestination = new TrainPath(pathToDestination);
	speed = 0;
	blockedFor = 0;
	isBlocked = true;
	timeOfLastSync = syncTime;
    }

    private static TrainPath generateTrainPath(TrainModel trainModel) {
	Point p = new Point();
	trainModel.getPosition().getHead(p);
	return new TrainPath(new Point[] {p, p});
    }

    public int getBlockedFor() {
	return blockedFor;
    }

    /**
     * Bump the number of ticks we were blocked for
     */
    public void block() {
	blockedFor++;
    }

    public TrainPath getPathToDestination() {
	return pathToDestination;
    }

    public TrainPath getPathTraversedSinceLastSync() {
	return pathTraversedSinceLastSync;
    }

    public boolean isBlocked() {
	return isBlocked;
    }

    public void setBlocked(boolean b) {
	isBlocked = b;
    }
	
    public void setSpeed(float speed) {
	this.speed = speed;
    }

    public GameTime getTimeOfLastSync() {
	return timeOfLastSync;
    }

    /**
     * @return true if the train could not find track while traversing its path
     */
    public boolean isLost() {
	return pathToDestination == null;
    }

    public String toString() {
	return "TrainMotionModel: pathToDest = " + pathToDestination +
	    ", pathSinceSync = " + pathTraversedSinceLastSync +
	    ", timeOfSync = " + timeOfLastSync;
    }
}
