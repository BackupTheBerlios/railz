/**
 * @author rtuck99@users.belios.de
 */
package jfreerails.world.track;

import jfreerails.world.common.*;
import jfreerails.world.top.*;

public class TrackPieceViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private int x, y;
    private FreerailsTile tile;

    public TrackPieceViewer(ReadOnlyWorld w) {
	world = w;
    }

    public void setFreerailsTile(int x, int y) {
	tile = world.getTile(x, y);
    }

    /**
     * Track is valued at 25% of initial cost, irrespective of age.
     * TODO
     * Stations have zero value since station and track are indivisible.
     * Stations are accounted for elsewhere.
     */
    public long getBookValue() {
	if (tile.getTrackRule().isStation())
	    return 0;

	return (long) (tile.getTrackRule().getPrice() * 0.25);
    }
}
