/*
 * Copyright (C) 2005 Robert Tuck
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
package org.railz.client.ai.tasks;

import java.util.*;

import org.railz.client.ai.*;

/** Extended by each class which implements the AI for a given type of
 * task, e.g. Building track, buying trains, upgrading stations, changing
 * schedules. Provides an interface which enables a generic scheduler to
 * prioritise tasks according to "cost/benefit".
 */
public abstract class TaskPlanner implements Comparable {
    /**
     * Plan the most favourable task (or set of tasks) of this type.
     * This causes the cost and priority of these tasks to be computed. This
     * information can then be used by a scheduler to determine which tasks to
     * do, given limited resources.
     * @return true if these plans can be performed and a plan was made.
     */
    public abstract boolean planTask();

    /**
     * @return the relative priority of the task(s) planned in planTask().
     * The higher this value, the more important (greater benefit). This
     * should ideally be an estimate of the gross profit measured in $ per
     * year. A priority of 0 indicates this task should not be performed.
     */
    public abstract int getTaskPriority();

    /**
     * @return the monetary cost of carrying out the task(s) planned in
     * planTask(). Obviously, if we return 0 here, our scheduler should order
     * it to be done, no matter what priority.
     */
    public abstract long getTaskCost();

    /**
     * Carry out the task planned in planTask()
     */
    public abstract void doTask();

    /**
     * Order first by ascending order of priority,
     * then by descending cost to do the task.
     */
    public int compareTo(Object o) {
	TaskPlanner tp = (TaskPlanner) o;
	int diff = getTaskPriority() - tp.getTaskPriority();
	if (diff != 0)
	    return diff;
	diff = (int) (tp.getTaskCost() - getTaskCost());
	if (diff != 0)
	    return diff;

	return 0;
    }

    public boolean equals(Object o) {
	if (!(o instanceof TaskPlanner))
	    return false;

	return compareTo(o) == 0;
    }

    public int hashCode() {
	return getTaskPriority() ^ (int) getTaskCost();
    }

    public static ArrayList createTaskPlanners(AIClient aic) {
	ArrayList al = new ArrayList();
	al.add(new RouteBuilder(aic));
	return al;
    }

    /**
     * Prevent classes outside this package extending TaskPlanner.
     */
    TaskPlanner() {
    }
}

