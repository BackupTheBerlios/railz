/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.terrain;

import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.top.*;
import jfreerails.world.common.*;

/**
 * This class provides methods describing properties of the tile which require
 * context from the game world.
 */
public class TerrainTileViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private FreerailsTile tile;
    private int x, y;

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
}
