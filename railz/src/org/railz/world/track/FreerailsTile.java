/*
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

package org.railz.world.track;

import org.railz.world.building.*;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.terrain.TerrainTile;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;

/**
 * Encapsulates all information about a map tile
 */
public class FreerailsTile implements TerrainTile,
    FreerailsSerializable {
    static final long serialVersionUID = -6626612368478492123L;

    private final TrackTile trackTile;
    private final int terrainType;
    private final BuildingTile buildingTile;
    private FreerailsPrincipal owner;

    public FreerailsTile(int terrain, TrackTile track, BuildingTile
	    building) {
	this(terrain, track, building, Player.AUTHORITATIVE);
    }

    private FreerailsTile(int terrain, TrackTile track, BuildingTile
	    building, FreerailsPrincipal owner) {
	terrainType = terrain;
	trackTile = track;
	buildingTile = building;
	this.owner = owner;
    }

    public FreerailsTile(FreerailsTile tile, TrackTile track) {
	this(tile.terrainType, track, tile.buildingTile, tile.owner);
    }

    public FreerailsTile(FreerailsTile tile, BuildingTile building) {
	this(tile.terrainType, tile.trackTile, building, tile.owner);
    }

    public FreerailsTile(FreerailsTile tile, FreerailsPrincipal o) {
	this(tile.terrainType, tile.trackTile, tile.buildingTile, o);
    }

    /*
     * @see TrackTile#getTrackRule()
     */
    public int getTrackRule() {
        return trackTile.getTrackRule();
    }

    /*
     * @see TrackPiece#getTrackConfiguration()
     */
    public byte getTrackConfiguration() {
        return trackTile.getTrackConfiguration();
    }

    /**
     * TODO better hashCode
     */
    public int hashCode() {
	return terrainType;
    }

    public boolean equals(Object o) {
        if (o instanceof FreerailsTile) {
            FreerailsTile test = (FreerailsTile)o;

            return (terrainType == test.terrainType &&
		    (buildingTile == null ? test.buildingTile == null :
		    (buildingTile.equals(test.buildingTile))) &&
		    (trackTile == null ? test.trackTile == null :
		    (trackTile.equals(test.trackTile))) &&
		     owner.equals(test.owner));
        } else {
            return false;
        }
    }

    public int getTerrainTypeNumber() {
        return terrainType;
    }

    public TrackTile getTrackTile() {
        return trackTile;
    }

    public BuildingTile getBuildingTile() {
	return buildingTile;
    }

    public FreerailsPrincipal getOwner() {
	return owner;
    }

    public String toString() {
	return "FreerailsTile: terrainType=" + terrainType +
	    ", owner=" + owner;
    }
}
