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
package org.railz.client.ai;

import java.util.*;

import org.railz.client.ai.tasks.*;
import org.railz.client.top.*;
import org.railz.world.accounts.*;
import org.railz.world.top.*;
/**
 * A generic scheduler which schedules activities planned by TaskPlanners.
 */
final class Scheduler {
    private ArrayList taskPlanners;
    private ClientDataProvider aiClient;

    public Scheduler(ClientDataProvider aic) {
	aiClient = aic;
	taskPlanners = TaskPlanner.createTaskPlanners(aic);
    }

    /**
     * Determine the highest priority task and run it.
     */
    public void scheduleTasks() {
	for (int i = 0; i < taskPlanners.size(); i++) {
	    TaskPlanner tp = (TaskPlanner) taskPlanners.get(i);
	    tp.planTask();
	}
	/* sort by ascending order of priority */
	Collections.sort(taskPlanners);

	/* figure out how much money do we have */
	BankAccount ba = (BankAccount)
	    aiClient.getWorld().get(KEY.BANK_ACCOUNTS, 0,
		    aiClient.getPlayerPrincipal());
	long balance = ba.getCurrentBalance();

	for (int i = taskPlanners.size() - 1; i >= 0; i--) {
	    TaskPlanner tp = (TaskPlanner) taskPlanners.get(i);
	    if (balance > tp.getTaskCost() && tp.getTaskCost() > 0 &&
		    tp.getTaskPriority() > 0) {
		tp.doTask();
		// don't do any more tasks, because this action may have
		// cost calculations on any others. Also our account will
		// have gone down
		break;
	    }
	}

	// now do all our "free" tasks
	for (int i = taskPlanners.size() - 1; i >= 0; i--) {
	    TaskPlanner tp = (TaskPlanner) taskPlanners.get(i);
	    if (tp.getTaskCost() == 0 && tp.getTaskPriority() > 0) {
		tp.doTask();
	    }
	}
    }
}
