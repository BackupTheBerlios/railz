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
package org.railz.controller;

import java.util.logging.*;

import org.railz.world.station.*;
import org.railz.world.terrain.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.player.*;

/**
 * This class provides access to "Special Values" which should not be changed.
 * These special values are e.g. particular terrain types or buildings which
 * are used by the server and AI Clients.
 */
public final class WorldConstants {
    /** The station improvement that represents a water tower */
    public final int SI_WATER_TOWER;
    /** The terrain type that represents the Clear terrain type */
    public final int TT_CLEAR;
    /** The track rule that represents standard single, unelectrified  track */
    public final int TR_STANDARD_TRACK;

    private static WorldConstants instance = null;
    private static ReadOnlyWorld world;

    private static final Logger logger = Logger.getLogger("global");

    private WorldConstants(ReadOnlyWorld w) {
	world = w;
	NonNullElements i = new NonNullElements(KEY.STATION_IMPROVEMENTS, w,
		Player.AUTHORITATIVE);
	int tmp = -1;
	while (i.next()) {
	    StationImprovement si = (StationImprovement) i.getElement();
	    if (si.getName().equals("WaterTower")) {
		tmp = i.getIndex();
	    }
	}
	SI_WATER_TOWER = tmp;

	i = new NonNullElements(KEY.TERRAIN_TYPES, w, Player.AUTHORITATIVE);
	tmp = -1;
	while (i.next()) {
	    TerrainType tt = (TerrainType) i.getElement();
	    if ("Clear".equals(tt.getTerrainTypeName())) {
		tmp = i.getIndex();
	    }
	}
	TT_CLEAR = tmp;

	i = new NonNullElements(KEY.TRACK_RULES, w, Player.AUTHORITATIVE);
	tmp = -1;
	while (i.next()) {
	    TrackRule tr = (TrackRule) i.getElement();
	    if ("standard track".equals(tr.toString())) {
		tmp = i.getIndex();
	    }
	}
	TR_STANDARD_TRACK = tmp;

	if (SI_WATER_TOWER == -1 || TT_CLEAR == -1 || TR_STANDARD_TRACK == -1) {
	    logger.log(Level.SEVERE, "A special configuration value " + 
		    "needed to function could not be found");
	    System.exit(1);
	}
    }

    public static WorldConstants init(ReadOnlyWorld w) {
	if (world != w)
	    instance = new WorldConstants(w);

	return instance;
    }

    public static WorldConstants get() {
	return instance;
    }
}
