package jfreerails.world.terrain;

import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;

/**
 * Defines the interface of a terrain tile.
 */
public interface TerrainTile {
    /**
     * @return an index into the TERRAIN_TYPES table
     */
    int getTerrainTypeNumber();
}
