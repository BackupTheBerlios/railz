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

import java.awt.*;
import java.util.*;

import org.railz.client.ai.*;
import org.railz.client.top.*;
import org.railz.world.common.*;
import org.railz.world.city.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Represents a planned route to be built between two destinations, which may
 * be cities, existing stations, or specific locations, depending upon the
 * state of planning.
 */
final class CityEntry implements Comparable {
    /** index to CITIES table */
    int city1;
    /** index to CITIES table */
    int city2;
    /** index to STATIONS table */
    int station1 = -1;
    /** index to STATIONS table */
    int station2 = -1;

    /** Distance between city1 and city2 */
    PathLength distance;
    /** construction cost estimate */
    long constructionEstimate;
    /** annnual revenue estimate */
    long annualRevenueEstimate;
    /** annual cost estimate */
    long annualCostEstimate;


    /** whether we have performed the detailed estimate, or just the easy
     * one */
    boolean detailedEstimate;

    /** Location of final "best" site for station */
    Point site1;
    /** Location of final "best" site for station */
    Point site2;

    /** LinkedList of PathExplorers */
    LinkedList plannedRoute;

    private ClientDataProvider aiClient;

    public CityEntry(ClientDataProvider aic, int c1, int c2, PathLength d) {
	city1 = c1;
	city2 = c2;
	distance = new PathLength(d);
	aiClient = aic;
    }

    /**
     * @return the annual return as a fraction of the construction cost
     */
    public float getAnnualReturn() {
	return ((float) annualRevenueEstimate - annualCostEstimate)
	    / constructionEstimate;
    }

    public int hashCode() {
	return Float.floatToRawIntBits(getAnnualReturn());
    }

    public boolean equals(Object o) {
	if (! (o instanceof CityEntry))
	    return false;

	return compareTo(o) == 0;
    }

    public int compareTo(Object o) {
	CityEntry ce = (CityEntry) o;
	if (constructionEstimate == 0)
	    return distance.compareTo(ce.distance);

	float annualReturn = getAnnualReturn();
	float ceAnnualReturn = ce.getAnnualReturn();
	// highest annual return is smallest
	if (annualReturn > ceAnnualReturn) {
	    return -1;
	} else if (annualReturn < ceAnnualReturn) {
	    return 1;
	}
	return 0;
    }

    public String toString() {
	CityModel cm1 = (CityModel) aiClient.getWorld().get
	    (KEY.CITIES, city1, Player.AUTHORITATIVE);
	CityModel cm2 = (CityModel) aiClient.getWorld().get
	    (KEY.CITIES, city2, Player.AUTHORITATIVE);
	return cm1.getCityName() + " <=> " + cm2.getCityName() + ", c=" +
	    constructionEstimate + ", ar= " + annualRevenueEstimate +
	    ", ac= " + annualCostEstimate;
    }
}

