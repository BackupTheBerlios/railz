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
package org.railz.world.city;

import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
/**
 * Provides information about a given city
 */
public class CityModelViewer {
    private ReadOnlyWorld world;
    private CityModel cityModel;

    public CityModelViewer(ReadOnlyWorld w) {
	world = w;
    }

    public void setCityModel(CityModel cm) {
	cityModel = cm;
    }

    /** @return the index in the STATIONS table of the  station owned by
     * player p within the city radius, or -1 if there is no station */
    public int hasStation(FreerailsPrincipal p) {
	int xmin = cityModel.getCityX() - cityModel.getCityRadius();
	int xmax = cityModel.getCityX() + cityModel.getCityRadius();
	int ymin = cityModel.getCityY() - cityModel.getCityRadius();
	int ymax = cityModel.getCityY() + cityModel.getCityRadius();

	xmin = xmin < 0 ? 0 : xmin;
	ymin = ymin < 0 ? 0 : ymin;
	xmax = xmax >= world.getMapWidth() ? world.getMapWidth() - 1 : xmax;
	ymax = ymax >= world.getMapHeight() ? world.getMapHeight() - 1 : ymax;

	NonNullElements i = new NonNullElements(KEY.STATIONS, world, p);
	while (i.next()) {
	    StationModel sm = (StationModel) i.getElement();
	    int x = sm.getStationX();
	    int y = sm.getStationY();
	    if (x >= xmin && x <= xmax && y >= ymin && y <= ymax) {
		return i.getIndex();
	    }
	}
	return -1;
    }
}

