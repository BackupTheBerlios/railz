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
import java.util.HashMap;

import jfreerails.world.common.*;
/**
 * This class encapsulates the transient (non-serializable) portion of a
 * trains position and movement.
 * @author rtuck99@users.berlios.de
 */
public class TrainMotionModel {
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
    
    private GameTime timeOfLastSync;

    public boolean isBlocked;
    
    TrainMotionModel() {
	pathTraversedSinceLastSync = null;
	pathToDestination = null;
	speed = 0;
	isBlocked = true;
    }

    public TrainPath getPathToDestination() {
	return pathToDestination;
    }

    public void setPathToDestination(TrainPath p) {
	pathToDestination = p;
    }

    public TrainPath getPathTraversedSinceLastSync() {
	return pathTraversedSinceLastSync;
    }

    public void setPathTraversedSinceLastSync(TrainPath tp) {
	pathTraversedSinceLastSync = tp;
    }

    public void sync(GameTime now, Point p) {
	timeOfLastSync = now;
	pathTraversedSinceLastSync = new TrainPath(new Point[]{p, p});
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
}
