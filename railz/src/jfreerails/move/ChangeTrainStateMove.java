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
import jfreerails.world.player.*;
import jfreerails.world.train.*;
import jfreerails.world.top.*;

/**
 * Issued when the train should change state
 * @author rtuck99@users.berlios.de
 */
public class ChangeTrainStateMove extends TrainMove {
    private TrainPath pathFromLastSync;
    private ObjectKey trainKey;
    int oldState;
    GameTime oldStateTime;
    int newState;
    GameTime newStateTime;
    private FreerailsPrincipal principal;

    public ChangeTrainStateMove(ReadOnlyWorld w, ObjectKey train, TrainPath
	    pathFromLastSync, int newState) {
	super(train, pathFromLastSync);
	TrainModel tm = (TrainModel) w.get(train.key, train.index,
		train.principal);
	this.oldState = tm.getState();
	oldStateTime = tm.getStateLastChangedTime();
	this.newState = newState;
	newStateTime = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	principal = train.principal;
    }

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	if (super.tryDoMove(w, p) != MoveStatus.MOVE_OK)
	    return MoveStatus.MOVE_FAILED;

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	if (tm.getState() != oldState)
	    return MoveStatus.MOVE_FAILED;

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	if (super.tryUndoMove(w, p) != MoveStatus.MOVE_OK)
	    return MoveStatus.MOVE_FAILED;

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	if (tm.getState() != newState)
	    return MoveStatus.MOVE_FAILED;

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	if (tryDoMove(w, p) != MoveStatus.MOVE_OK)
	    return MoveStatus.MOVE_FAILED;

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	tm.setState(newState, newStateTime);
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	if (tryUndoMove(w, p) != MoveStatus.MOVE_OK)
	    return MoveStatus.MOVE_FAILED;

	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainKey.index, p);
	tm.setState(oldState, oldStateTime);
	return MoveStatus.MOVE_OK;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof ChangeTrainStateMove))
	    return false;

	ChangeTrainStateMove m = (ChangeTrainStateMove) o;
	return trainKey.equals(m.trainKey) &&
	    pathFromLastSync.equals(m.pathFromLastSync) &&
	    oldState == m.oldState &&
	    newState == m.newState &&
	    oldStateTime.equals(m.oldStateTime) &&
	    newStateTime.equals(m.newStateTime);
    }

    public int hashCode() {
	return trainKey.hashCode();
    }
}
