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
package jfreerails.world.building;

import java.awt.Point;

import jfreerails.world.common.*;
import jfreerails.world.top.*;
/**
 * Defines a type of building.
 * @author rtuck99@users.berlios.de
 */
public class BuildingType implements FreerailsSerializable {
    private final boolean[] validTrackLayouts = new boolean[256];
    private final Production[] production;
    private final Consumption[] consumption;
    private final Conversion[] conversion;
    private final long baseValue;
    private final String name;
    private final int stationRadius;
    private final int category;
    private final boolean[] acceptableTerrainTypes;
    private final boolean[] neighbouringTerrainTypes;

    public static final int CATEGORY_INDUSTRY = 0;
    public static final int CATEGORY_RESOURCE = 1;
    public static final int CATEGORY_URBAN = 2;
    public static final int CATEGORY_STATION = 3;

    /**
     * @return true if the terrain types on the squares near this tile are OK
     * to build on.
     * @param p position at which to build
     */
    public boolean canBuildHere(World w, Point p) {
	int terrainType = w.getTile(p).getTerrainTypeNumber();
	if (! acceptableTerrainTypes[terrainType])
	    return false;

	int xmin = (p.x == 0) ? 0 : p.x - 1;
	int ymin = (p.y == 0) ? 0 : p.y - 1;
	int xmax = (p.x == w.getMapWidth()) ? w.getMapWidth() : p.x + 1;
	int ymax = (p.y == w.getMapHeight()) ? w.getMapHeight() : p.y + 1;
	for (int x = xmin; x <= xmax; x++) {
	    for (int y = ymin; y <= ymax; y++) {
		if (x == p.x && y == p.y)
		    continue;
		terrainType = w.getTile(x, y).getTerrainTypeNumber();
		if (neighbouringTerrainTypes[terrainType])
		    return true;
	    }
	}
	return false;
    }

    private void setTrackLayouts(byte[] trackLayouts) {
	for (int i = 0; i < trackLayouts.length; i++) {
	    int layout = trackLayouts[i];
	    for (int j = 0; j < 8; j++) {
		validTrackLayouts[layout] = true;
		layout = CompassPoints.rotateClockwise((byte) layout) & 0xFF;
	    }
	}
    }

    public BuildingType(String name, long baseValue, int stationRadius, byte[]
	    validTrackLayouts, boolean[] acceptableTerrainTypes, boolean[]
	    neighbouringTerrainTypes) {
	this.name = name;
	this.baseValue = baseValue;
	this.stationRadius = stationRadius;
	production = new Production[0];
	consumption = new Consumption[0];
	conversion = new Conversion[0];
	category = CATEGORY_STATION;
	setTrackLayouts(validTrackLayouts);
	this.neighbouringTerrainTypes = neighbouringTerrainTypes;
	this.acceptableTerrainTypes = acceptableTerrainTypes;
    }

    public BuildingType(String name, Production[] production, Consumption[]
	    consumption, Conversion[] conversion, long baseValue, int
	    category, byte[] validTrackLayouts, boolean[]
	    acceptableTerrainTypes,  boolean[]
	    neighbouringTerrainTypes) {
	this.name = name;
	this.production = production;
	this.consumption = consumption;
	this.conversion = conversion;
	this.baseValue = baseValue;
	this.category = category;
	stationRadius = 0;
	setTrackLayouts(validTrackLayouts);
	this.neighbouringTerrainTypes = neighbouringTerrainTypes;
	this.acceptableTerrainTypes = acceptableTerrainTypes;
    }

    public Production[] getProduction() {
	return production;
    }

    public Consumption[] getConsumption() {
	return consumption;
    }

    public Conversion[] getConversion() {
	return conversion;
    }

    public long getBaseValue() {
	return baseValue;
    }

    public String getName() {
	return name;
    }

    public int getCategory() {
	return category;
    }

    public int getStationRadius() {
	return stationRadius;
    }

    public boolean isTrackLayoutValid(byte trackLayout) {
	return validTrackLayouts[trackLayout & 0xFF];
    }
}
