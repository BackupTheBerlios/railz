/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 25-Aug-2003
 *
  */
package jfreerails.move;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
import jfreerails.world.train.*;

/**
 * This Move can change a train's engine and wagons.
 *
 * @author Luke Lindsay
 *
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    private GameTime oldSyncTime;

    private ChangeTrainMove(int id, TrainModel before, TrainModel
	    after, FreerailsPrincipal p) {
	super(KEY.TRAINS, id, before, after, p);
	oldSyncTime = before.getTrainMotionModel().getTimeOfLastSync();
    }

    /**
     * Change trains scheduled stop
     */
    public static ChangeTrainMove generateMove(int id,
	    FreerailsPrincipal p, TrainModel before, ScheduleIterator
	    newScheduleIterator) {
	TrainModel after = new TrainModel(before, newScheduleIterator);
	return  new ChangeTrainMove(id, before, after, p);
    }

    /**
     * Change trains state
     */
    public static ChangeTrainMove generateMove(int id, FreerailsPrincipal p,
	    TrainModel before, int newState, GameTime now) {
	TrainModel after = new TrainModel(before, now, newState);
	return new ChangeTrainMove(id, before, after, p);
    }

    /**
     * Change path to destination.
     */
    public static ChangeTrainMove generateMove(int id, FreerailsPrincipal p,
	    TrainModel before, TrainPath pathToDestination, GameTime now) {
	TrainModel after = new TrainModel(before, pathToDestination, now);
	return new ChangeTrainMove(id, before, after, p);
    }

    /**
     * Change engine and wagons
     */
    public static ChangeTrainMove generateMove(int id, FreerailsPrincipal p,
	    TrainModel before, int newEngine, int[] newWagons) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, p);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	TrainModel tm = ((TrainModel) w.get(listKey, index, p));
	if (!tm.getTrainMotionModel().isBlocked())
	    tm.releaseAllLocks(w);
	
	MoveStatus ms = super.doMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	tm = ((TrainModel) w.get(listKey, index, p));
	tm.sync(now);

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	TrainModel tm = (TrainModel) w.get(listKey, index, p);
	if (!tm.getTrainMotionModel().isBlocked())
	    tm.releaseAllLocks(w);

	MoveStatus ms = super.undoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	((TrainModel) w.get(listKey, index, p)).sync(oldSyncTime);

	return MoveStatus.MOVE_OK;
    }
}
