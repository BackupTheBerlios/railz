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
package jfreerails.move;

import java.awt.Point;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
import jfreerails.world.train.*;

/**
 * @author rtuck99@users.berlios.de
 */
public abstract class TrainMove implements Move {
    protected ObjectKey trainKey;
    protected TrainPath pathFromLastSync;
    protected GameTime timeOfLastSync;

    protected TrainMove(ObjectKey train, GameTime timeOfLastSync, TrainPath
	    pathFromLastSync) {
	trainKey = train;
	this.pathFromLastSync = pathFromLastSync;
	this.timeOfLastSync = timeOfLastSync;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	if (w.size(KEY.TRAINS, p) <= trainKey.index)
	    return MoveStatus.moveFailed("Index out of bounds");

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	if (tm == null)
	    return MoveStatus.moveFailed("The train does not exist.");

	TrainMotionModel tmm = tm.getTrainMotionModel();

	if ((pathFromLastSync == null &&
		    tmm.getPathTraversedSinceLastSync() == null) ||
		((pathFromLastSync != null) && pathFromLastSync.equals
		 (tmm.getPathTraversedSinceLastSync()) &&
		tmm.getTimeOfLastSync().equals(timeOfLastSync)))
	    return MoveStatus.MOVE_OK;

	System.err.println("Failing TrainMove: before = " + tmm + 
		" after: pathSinceSync = " + pathFromLastSync + 
		", timeOfSync = " + timeOfLastSync + " in " +
		Thread.currentThread().getName()); 
		
	return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	if (w.size(KEY.TRAINS, p) <= trainKey.index)
	    return MoveStatus.moveFailed("Index out of bounds");

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	if (tm == null)
	    return MoveStatus.moveFailed("The train does not exist.");
	if (pathFromLastSync.equals(tm.getTrainMotionModel().
		    getPathTraversedSinceLastSync()))
	    return MoveStatus.MOVE_OK;

	return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	GameTime now = (GameTime) w.get(ITEM.TIME, p);
	Point head = new Point();
	pathFromLastSync.getHead(head);
	((TrainModel) w.get(KEY.TRAINS, trainKey.index, p))
	    .sync(now);
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	((TrainModel) w.get(KEY.TRAINS, trainKey.index, p))
	    .sync(timeOfLastSync);
	return MoveStatus.MOVE_OK;
    }
}
