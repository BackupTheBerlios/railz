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
package org.railz.world.track;

import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Provides information about a given track tile in an abstract fashion 
 * (we may not have placed it on the map yet, or determined specifically which
 * tile it is)
 */
public class TrackTileViewer {
    private ReadOnlyWorld world;
    private TrackTile trackTile;

    public TrackTileViewer(ReadOnlyWorld w) {
	world = w;
    }

    public void setTrackTile(TrackTile tt) {
	trackTile = tt;
    }

    /**
     * @param newTile The tile we wish to upgrade to
     * @return get the cost to convert this TrackTile to the specified
     * TrackTile. This calculation ignores "environmental" factors as we do
     * not know where this tile is.
     * TODO calculate on basis of individual sections changed.
     * XXX Cannot change these calculations until {@link
     * org.railz.controller.TrackMoveTransactionsGenerator} is updated to use
     * this class.
     */
    public long getConstructionCost(TrackTile newTile) {
	long cost = 0;
	if (trackTile != null &&
		(newTile.getTrackRule() != trackTile.getTrackRule())) {
	    // if we are upgrading from another track type, calculate cost to
	    // upgrade existing track sections to new type

	    /* for now, removal of track to be upgraded is charged at 50% of
	       original track type base cost */
	    cost += ((TrackRule) world.get(KEY.TRACK_RULES,
			trackTile.getTrackRule(),
			Player.AUTHORITATIVE)).getPrice() / 2;
	}
	// for now, new track is charged at 100% of new track type base cost
	cost += ((TrackRule) world.get(KEY.TRACK_RULES,
		    newTile.getTrackRule(), Player.AUTHORITATIVE)).getPrice();
	return cost;
    }
    
    /**
     * Track is valued at 25% of initial cost, irrespective of age.
     */
    public long getBookValue() {
	return (long) (((TrackRule) world.get(KEY.TRACK_RULES,
			trackTile.getTrackRule(),
			Player.AUTHORITATIVE)).getPrice() * 0.25);
    }
}
