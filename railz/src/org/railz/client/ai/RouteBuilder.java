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

import java.awt.*;
import java.util.*;

import org.railz.world.accounts.*;
import org.railz.world.city.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * Contains AI for constructing routes between destinations.
 */
class RouteBuilder implements TaskPlanner {
    /**
     * ArrayList of CityEntry storing inter-city distances, sorted by
     * ascending order of distance.
     */
    private ArrayList cityDistances = new ArrayList();

    private static final long MIN_BALANCE = 60000L;

    private final AIClient aiClient;

    public RouteBuilder (AIClient aic) {
	aiClient = aic;
    }

    private boolean isInitialised = false;

    private class CityEntry implements Comparable {
	/** index to CITIES table */
	int city1;
	/** index to CITIES table */
	int city2;
	/** Distance between city1 and city2 */
	PathLength distance;

	public CityEntry(int c1, int c2, PathLength d) {
	    city1 = c1;
	    city2 = c2;
	    distance = new PathLength(d);
	}

	public int compareTo(Object o) {
	    return distance.compareTo(o);
	}
    }

    private void initialise() {
	if (isInitialised)
	    return;

	// create a list of city-city entries, sorted by distance
	NonNullElements i = new NonNullElements(KEY.CITIES,
		aiClient.getWorld(), Player.AUTHORITATIVE);
	while (i.next()) {
	    NonNullElements j = new NonNullElements(KEY.CITIES,
		    aiClient.getWorld(), Player.AUTHORITATIVE);
	    j.gotoIndex(i.getIndex());
	    while (j.next()) {
		CityModel cm1 = (CityModel) i.getElement();
		CityModel cm2 = (CityModel) j.getElement();
		CityEntry ce = new CityEntry(i.getIndex(), j.getIndex(),
			new PathLength(cm1.getCityX(), cm1.getCityY(),
			    cm2.getCityX(), cm2.getCityY()));
		cityDistances.add(ce);
	    }
	}

	Collections.sort(cityDistances);
    }

    /**
     * Produce plans for constructing the best route. It is judged by the
     * following criteria:
     * <ul>
     * <li>There is a constructable route between the destinations.
     * <li>We have sufficient funds to build it immediately.
     * <li>It provides the highest return on investment (as an annual 
     * percentage of initial outlay), <b>OR</b> it fulfills a scenario objective
     * (TODO).
     * <li>There is no existing route between the two destinations.
     * <li>If we have at least one station, then all new routes must connect
     * to an existing station.
     * </ul>
     * 
     * In order to determine the route as computationally cheaply as possible,
     * we first discard all city combinations using the following criteria
     * applied in order:
     * <ul>
     * <li>If we have at least one station, then discard all routes which do
     * not have one and only one end-point at an existing station.
     * <li>Discard all routes which can never be constructed (determined in a
     * previous invocation of this method).
     * <li>A cheap estimation is used to determine whether the shortest
     * possible route between the destinations cannot possibly be afforded.
     * <li>Determine the top speed of the fastest affordable engine using a
     * given load. Use this to determine the time taken for a return journey
     * between each pair of stations.
     * <li>Determine the supply and demand between the station pairs. Using
     * the information in the previous step, calculate the operating profit
     * for each route (revenue per year, less train running costs per year)
     * <li>Determine the return on investment using the above operating profit
     * calculation, and the construction cost already calculated.
     * </ul>
     *
     * After this step, now calculate the profitability of the best route
     * calculated in the above sequence using a more detailed route analysis.
     * Since these above calculations are based on ideal scenarios, they could
     * be cached for some time. In addition, any more detailed analysis we
     * perform will come up with a worse result. Therefore if we perform a
     * detailed analysis and the new result is still the best route, then we
     * know we have found the optimum route.
     * 
     * <ul>
     * <li>If one city has an existing station, then use this station as
     * an endpoint. Otherwise the following analysis is performed for both
     * stations.
     * <li>For the remaining city(ies), we must determine the optimum point to
     * position the station.
     * <ul><li>Pick a spot on the periphery of the city, on the side nearest
     * to the other destination.
     * <li>Determine the optimum router to this point from the far station.
     * If there is no route, then we mark this city combination as never being
     * reachable, and abandon planning.
     * <li>Define a grid around the city and determine all the places where a
     * station may be placed. Each position must be reachable from the point
     * considered in the previous step.
     * <li>Perform the supply/demand analysis to determine the best place to
     * position the station.
     * </ul>
     * </ul>
     * The route to build is thus the route between the station, the point on
     * the city periphery, and the far destination (which may be a point on
     * the other citys periphery, and then a route to the other station from
     * that point.
     */
    public boolean planTask() {
	ReadOnlyWorld w = aiClient.getWorld();

	// don't bother to do anything at all unless we have at least
	// MIN_BALANCE in the bank
	BankAccount ba = (BankAccount) w.get(KEY.BANK_ACCOUNTS, 0,
	       	aiClient.getPlayerPrincipal());
	if (ba.getCurrentBalance() < MIN_BALANCE) 
	    return false;
	
	initialise();
	
	// if we have at least 1 station, then determine which station has
	// greatest supply and demand and choose this station
	
	/* TODO */
	return false;
    }

    /** computes a score based on the potential supply of a given location */
    private int computeSupplyScore(Point p) {
	/* TODO */
	return 0;
    }

    /** computes a score based on the potential demand of a given location */
    private int computeDemandScore(Point p) {
	/* TODO */
	return 0;
    }

    /** computes a score based on the supply and demand of both points */
    private int computeCombinedScore(Point p1, Point p2) {
	/* TODO */
	return 0;
    }

    /** compute a score indicating the priority of building the most favoured
     * route. This can be compared against the priority of other tasks (e.g.
     * purchasing trains, station upgrades etc.) before doing the task in
     * order to determine which activity to undertake first, in the event that
     * there are insufficient resources to perform all tasks. */
    public int getTaskPriority() {
	/* TODO */
	return 0;
    }

    public void doTask() {
	/* TODO */
    }

    public long getTaskCost() {
	/* TODO */
	return 0L;
    }
}
