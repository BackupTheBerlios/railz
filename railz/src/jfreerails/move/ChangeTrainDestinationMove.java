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

import jfreerails.world.common.*;
import jfreerails.world.train.*;
import jfreerails.world.top.*;
import jfreerails.world.player.*;

/**
 * Issued to set a new destination for the train.
 * @author rtuck99@users.berlios.de
 */
public class ChangeTrainDestinationMove extends TrainMove {
    private TrainModel oldTrain;
    private TrainModel newTrain;
    private TrainPath oldPathToDestination;

    public static ChangeTrainDestinationMove generateMove(ReadOnlyWorld w,
	    ObjectKey train, ScheduleIterator newIterator) {
	TrainModel oldTm = (TrainModel) w.get(train.key, train.index,
		train.principal);
	TrainModel newTm = new TrainModel(oldTm, newIterator);
	TrainMotionModel tmm = oldTm.getTrainMotionModel();
	return new ChangeTrainDestinationMove(train, oldTm, newTm,
		tmm);
    }

    private ChangeTrainDestinationMove(ObjectKey trainKey, TrainModel before,
	    TrainModel after, TrainMotionModel tmm) {
	super(trainKey, tmm.getTimeOfLastSync(),
		tmm.getPathTraversedSinceLastSync());
	oldTrain = before;
	newTrain = after;
	oldPathToDestination = tmm.getPathToDestination();
    }
    
    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	if (w.size(trainKey.key, trainKey.principal) <= trainKey.index) {
	    return MoveStatus.moveFailed("Train does not exist");
	}

	TrainModel tm = (TrainModel) w.get(trainKey.key, trainKey.index,
		trainKey.principal);
	if ((tm == null && oldTrain != null) ||
		(tm != null && !tm.equals(oldTrain))) {
	    System.out.println(" old = " + oldTrain + " current = " + tm);
	    return MoveStatus.moveFailed("Train has changed since last sync");
	}
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	if (w.size(trainKey.key, trainKey.principal) <= trainKey.index) {
	    return MoveStatus.moveFailed("Train does not exist");
	}

	TrainModel tm = (TrainModel) w.get(trainKey.key, trainKey.index,
		trainKey.principal);
	if ((tm == null && newTrain != null) ||
		(tm != null && !tm.equals(newTrain))) {
	    return MoveStatus.moveFailed("Train has changed since last sync");
	}
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryDoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;
	
	super.doMove(w, p);

	TrainMotionModel oldModel = ((TrainModel) w.get(trainKey.key,
		    trainKey.index, p)).getTrainMotionModel();
	w.set(trainKey.key, trainKey.index, newTrain, p);
	newTrain.setTrainMotionModel(oldModel);
	oldModel.setPathToDestination(null);

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryUndoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	TrainMotionModel oldModel = ((TrainModel) w.get(trainKey.key,
		    trainKey.index, p)).getTrainMotionModel();
	w.set(trainKey.key, trainKey.index, oldTrain, p);
	oldTrain.setTrainMotionModel(oldModel);
	oldModel.setPathToDestination(oldPathToDestination);

	super.undoMove(w, p);

	return MoveStatus.MOVE_OK;
    }

    public FreerailsPrincipal getPrincipal() {
	return trainKey.principal;
    }
}
