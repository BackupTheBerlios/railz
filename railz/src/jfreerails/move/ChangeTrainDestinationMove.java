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

import jfreerails.world.train.*;
import jfreerails.world.top.*;
import jfreerails.world.player.*;

/**
 * Issued to set a new destination for the train.
 * @author rtuck99@users.berlios.de
 */
public class ChangeTrainDestinationMove extends ChangeTrainMove {
    public static ChangeTrainDestinationMove generateMove(ReadOnlyWorld w,
	    ObjectKey train, ScheduleIterator newIterator) {
	TrainModel tm = (TrainModel) w.get(train.key, train.index,
		train.principal);
	TrainModel newTm = new TrainModel(tm, newIterator);
	return new ChangeTrainDestinationMove(train.index, tm, newTm,
		train.principal);
    }

    private ChangeTrainDestinationMove(int index, TrainModel before,
	    TrainModel after, FreerailsPrincipal p) {
	super(index, before, after, p);
    }
}
