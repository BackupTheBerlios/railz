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
package org.railz.world.building;

import org.railz.world.common.FreerailsSerializable;
/**
 * Represents a building in the game world.
 * @author rtuck99@users.berlios.de
 */
public class BuildingTile implements FreerailsSerializable {
    /**
     * index into the BUILDING_TYPES table
     */
    private int buildingType;

    public BuildingTile(int type) {
	buildingType = type;
    }

    public int getType() {
	return buildingType;
    }

    public boolean equals(Object o) {
	if (o != null &&
		o instanceof BuildingTile) {
	    return buildingType == ((BuildingTile) o).buildingType;
	}
	return false;
    }

    public int hashCode() {
	return buildingType;
    }
}
