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

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.*;
import jfreerails.world.train.*;

/**
 * This Move can change a train's engine and wagons.
 *
 * @author Luke Lindsay
 *
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    protected ChangeTrainMove(int index, TrainModel before,
        TrainModel after, FreerailsPrincipal p) {
        super(KEY.TRAINS, index, before, after, p);
    }

    public static ChangeTrainMove generateMove(int id, FreerailsPrincipal p,
	    TrainModel before, int newEngine, int[] newWagons) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, p);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	TrainMotionModel oldModel = ((TrainModel) w.get(listKey, index,
		    p)).getTrainMotionModel();
	MoveStatus ms = super.doMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	((TrainModel) w.get(listKey, index, p)).setTrainMotionModel(oldModel);

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	TrainMotionModel oldModel = ((TrainModel) w.get(listKey, index,
		    p)).getTrainMotionModel();
	MoveStatus ms = super.undoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	((TrainModel) w.get(listKey, index, p)).setTrainMotionModel(oldModel);

	return MoveStatus.MOVE_OK;
    }
}
