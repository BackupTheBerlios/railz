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

import org.railz.controller.*;
import org.railz.world.building.*;
import org.railz.world.city.*;
import org.railz.world.player.*;
import org.railz.world.track.*;
import org.railz.world.top.*;
public class RouteBuilderTest extends TaskPlannerTest {
    protected TaskPlanner getTaskPlanner() {
	setupWorld();
	mff.setupCalendar();
	mff.setupEconomy();
	mff.setupCargoTypes();
	mff.setupBuildingTypes();
	mff.setupStationImprovements();
	mff.setupEngineTypes();
	// add two cities
	w.add(KEY.CITIES, new CityModel("city1", 1, 1),
		Player.NOBODY);
	w.add(KEY.CITIES, new CityModel("city2", 8, 8),
		Player.NOBODY);
	// add some buildings
	FreerailsTile ft = w.getTile(1,1);
	w.setTile(1,1, new FreerailsTile(ft, 
		    new BuildingTile(MapFixtureFactory.BT_CITY)));
	ft = w.getTile(8,8);
	w.setTile(8,8, new FreerailsTile(ft,
		    new BuildingTile(MapFixtureFactory.BT_CITY)));

	TaskPlanner tp = new RouteBuilder(dc);
	return tp;
    }
}
