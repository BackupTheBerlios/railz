/*
 * Copyright (C) Robert Tuck
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

/**
 * @author rtuck99@users.berlios.de
 */
package org.railz.world.terrain;

import org.railz.world.player.Player;
import org.railz.world.track.*;
import org.railz.world.top.*;
import org.railz.world.common.*;

/**
 * This class provides methods describing properties of the tile which require
 * context from the game world.
 */
public class TerrainTileViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private FreerailsTile tile;
    private int x, y;
    private static final double ROOT_TWO = Math.sqrt(2);

    public TerrainTileViewer(ReadOnlyWorld w) {
       world = w;
    }       

    public void setFreerailsTile(int x, int y) {
	tile = world.getTile(x, y);
	this.x = x;
	this.y = y;
    }

    /**
     * @return the asset value of a tile, excluding any buildings or track.
     */
    public long getBookValue() {
	return getTerrainValue();
    }

    /**
     * Calculates the value of the tile based on the base value of this tile,
     * adjusted by an aaverage of the values of the surrounding tiles.
     * TODO perform the averaging...
     * @return the purchase value of a tile, excluding any buildings or track.
     */
    public long getTerrainValue() {
	TerrainType t = (TerrainType) world.get(KEY.TERRAIN_TYPES,
		tile.getTerrainTypeNumber(), Player.AUTHORITATIVE);

	return t.getBaseValue();
    }

    /**
     * @param x2 the x coord of the tile we are traversing to.
     * @param y2 the y coord of the tile we are traversing to.
     * @return the effective incline in % when traversing between the two
     * neighbouring tiles. 
     */
    public int getEffectiveIncline(int x2, int y2) {
	FreerailsTile ft = world.getTile(x, y);
	int ttn = ft.getTerrainTypeNumber();
	TerrainType tt1 = (TerrainType)
	    world.get(KEY.TERRAIN_TYPES, ttn, Player.AUTHORITATIVE);
	ft = world.getTile(x2, y2);
	ttn = ft.getTerrainTypeNumber();
	TerrainType tt2 = (TerrainType) world.get(KEY.TERRAIN_TYPES, ttn,
		Player.AUTHORITATIVE);
	// check the track type to see whether it is a tunnel
	int effectiveIncline;
	TrackRule trackType = null;
	if (ft.getTrackTile() != null) {
	    trackType = (TrackRule) world.get(KEY.TRACK_RULES,
		    ft.getTrackRule(), Player.AUTHORITATIVE);
	}
	if (trackType != null && trackType.isTunnel()) {
	    // if we are in a tunnel, then assume it's level
	    effectiveIncline = 0;
	} else {
	    effectiveIncline = tt2.getElevation() -
		    tt1.getElevation(); 
	    // if we are on a diagonal then our incline is divided by root 2
	    if (x != x2 && y != y2) {
		effectiveIncline = (int) ((double) effectiveIncline / ROOT_TWO);
	    }
	    effectiveIncline += (tt1.getRoughness() +
			tt2.getRoughness()) / 2;
	}
	return effectiveIncline;
    }
}
