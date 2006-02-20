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
package org.railz.controller;

import java.util.*;
import java.util.logging.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.track.*;
import org.railz.world.top.*;

public class RouteBuilderPathExplorerTest extends PathFinderTest {
    private FreerailsPrincipal p;
    private World w;
    
    public PathFinder getPathFinder() {
	MapFixtureFactory mff = new MapFixtureFactory();
	mff.setupCalendar();
	mff.setupEconomy();
	mff.setupEngineTypes();
	mff.setupStationImprovements();
        mff.setupBuildingTypes();
	p = mff.addPlayer("Test Player", 0);
	WorldConstants.init(mff.world);
	RouteBuilderPathExplorer.RouteBuilderPathExplorerSettings s = new
	    RouteBuilderPathExplorer.RouteBuilderPathExplorerSettings
	    (mff.world, WorldConstants.get().TR_STANDARD_TRACK, p, 0, 200);
	RouteBuilderPathExplorer pe = new RouteBuilderPathExplorer(mff.world,
		1, 1, CompassPoints.EAST, s);
	w = mff.world;
	return new PathFinder(pe, 9, 2, 10, 10000);
    }

    public void testPathFinder() {
	PathFinder pf = getPathFinder();
	LinkedList ll = pf.explore();
	String s = "";
	Logger l = Logger.getLogger("global");
	for (int i = 0; i < ll.size(); i++) {
	    PathExplorer pe = (PathExplorer) ll.get(i);
	    s += "x=" + pe.getX() + " y=" + pe.getY() + ", ";
	}
	l.log(Level.INFO, s);
	assertEquals (9, ll.size());
    }
    
    /* Test that pathfinder can find way round obstacles */
    public void testPathFinder2() {
	PathFinder pf = getPathFinder();
	w.setTile(5, 1, new FreerailsTile(MapFixtureFactory.TT_OCEAN, 
		    null, null));
	LinkedList ll = pf.explore();
	String s = "";
	Logger l = Logger.getLogger("global");
	for (int i = 0; i < ll.size(); i++) {
	    PathExplorer pe = (PathExplorer) ll.get(i);
	    s += "x=" + pe.getX() + " y=" + pe.getY() + ", ";
	}
	l.log(Level.INFO, s);
	assertEquals (9, ll.size());
    }
}
