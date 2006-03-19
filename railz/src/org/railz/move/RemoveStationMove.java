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
 * Created on 15-Apr-2003
 *
 */
package org.railz.move;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import org.railz.world.station.StationModel;
import org.railz.world.player.*;
import org.railz.world.top.KEY;
import org.railz.world.top.ObjectKey;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ObjectKey2;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.UUID;
import org.railz.world.top.WorldIterator;
import org.railz.world.train.*;

/**
 * This Move removes a station from the station list and from the map.
 * @author Luke
 *
 */
public class RemoveStationMove extends CompositeMove implements TrackMove {
    private static boolean stopsAtStation(Schedule s, ObjectKey2 station) {
	for (int i = 0; i < s.getNumOrders(); i++) {
	    if (s.getOrder(i).getStation().equals(station))
		return true;
	}
	return false;
    }

    private RemoveStationMove(ArrayList moves) {
        super(moves);
    }

    static RemoveStationMove getInstance(ReadOnlyWorld w,
        ChangeTrackPieceMove removeTrackMove, FreerailsPrincipal p) {
        Iterator wi = w.getIterator(KEY.STATIONS, p);
        UUID stationId = null;

        StationModel station = null;
        while (wi.hasNext()) {
            station = (StationModel) wi.next();

            if (station.x == removeTrackMove.getLocation().x &&
                    station.y == removeTrackMove.getLocation().y) {
                //We have found the station!
                stationId = station.getUUID();

                break;
            }
        }

        if (stationId == null) {
            throw new IllegalArgumentException("Could find a station at " +
                removeTrackMove.getLocation().x + ", " +
                removeTrackMove.getLocation().y);
        }

        StationModel station2remove = station;
        ArrayList moves = new ArrayList();
        moves.add(removeTrackMove);
        moves.add(new RemoveObjectMove(new ObjectKey2(KEY.STATIONS, p, station.getUUID()),
                station2remove));

        //Now update any train schedules that include this station.
        WorldIterator schedules = new NonNullElements(KEY.TRAIN_SCHEDULES, w,
		p);

        while (schedules.next()) {
            ImmutableSchedule schedule = (ImmutableSchedule)schedules.getElement();

	    ObjectKey2 stationKey = new ObjectKey2(KEY.STATIONS, p,
		    stationId);
            if (stopsAtStation(schedule, stationKey)) {
                MutableSchedule mutableSchedule = new MutableSchedule(schedule);
                mutableSchedule.removeAllStopsAtStation(stationKey);

                Move changeScheduleMove = new ChangeTrainScheduleMove
		    (KEY.TRAIN_SCHEDULES, schedules.getIndex(),
                        schedule, mutableSchedule.toImmutableSchedule(),
			p);
                moves.add(changeScheduleMove);
            }
        }

        return new RemoveStationMove(moves);
    }

    public Rectangle getUpdatedTiles() {
        TrackMove tm = (TrackMove)getMove(0);

        return tm.getUpdatedTiles();
    }
}
