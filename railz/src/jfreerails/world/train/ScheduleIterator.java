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

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
/**
 * @author rtuck99@users.berlios.de
 */
public class ScheduleIterator implements FreerailsSerializable {
    private int currentOrder;
    private int scheduleId;
    private TrainOrdersModel priorityOrder;

    public String toString() {
	return "currentOrder = " + currentOrder + ", scheduleId = " +
	    scheduleId + ", priorityOrder = " + priorityOrder;
    }

    public ScheduleIterator(int scheduleId, int currentOrder) {
	this.scheduleId = scheduleId;
	this.currentOrder = currentOrder;
    }

    /**
     * @return the next regular scheduled order index
     */
    public int getCurrentOrderIndex() {
	return  currentOrder;
    }

    /**
     * @return true if a priority order is scheduled
     */
    public boolean hasPriorityOrder() {
	return (priorityOrder != null);
    }

    public TrainOrdersModel getCurrentOrder(ReadOnlyWorld w) {
	if (priorityOrder != null) {
	    return priorityOrder;
	}
	Schedule s = (Schedule) w.get(KEY.TRAIN_SCHEDULES, scheduleId,
		Player.AUTHORITATIVE);
	return s.getOrder(currentOrder);
    }

    public ScheduleIterator(ScheduleIterator i, TrainOrdersModel
	    priorityOrder) {
	currentOrder = i.currentOrder;
	scheduleId = i.scheduleId;
	this.priorityOrder = priorityOrder;
    }

    public ScheduleIterator nextOrder(ReadOnlyWorld w) {
	Thread.currentThread().dumpStack();
	if (priorityOrder != null) {
	    return new ScheduleIterator(this, null);
	} else {
	    Schedule s = (Schedule) w.get(KEY.TRAIN_SCHEDULES, scheduleId,
		    Player.AUTHORITATIVE);
	    if (s.getNumOrders() <= currentOrder + 1) {
		return new ScheduleIterator(scheduleId, 0);
	    }
	    return new ScheduleIterator(scheduleId, currentOrder + 1);
	}
    }

    public int getScheduleId() {
	return scheduleId;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof ScheduleIterator))
	    return false;

	ScheduleIterator i = (ScheduleIterator) o;

	return currentOrder == i.currentOrder &&
	    scheduleId == i.scheduleId &&
	    (priorityOrder == null) ? (i.priorityOrder == null) :
	    (priorityOrder.equals(i.priorityOrder));
    }

    public int hashCode() {
	return currentOrder ^ scheduleId;
    }
}
