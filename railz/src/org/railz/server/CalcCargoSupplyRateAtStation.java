/*
 * Copyright (C) 2003 Scott Bennett
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
 * @author Scott Bennett
 * Created: 9th May 2003
 *
 * This class probes the tiles adjacent to a station for what cargo they supply
 * and then returns a vector of these cargo rates
 */
package org.railz.server;

import java.util.Vector;

import org.railz.controller.*;
import org.railz.world.cargo.CargoType;
import org.railz.world.station.*;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.*;
import org.railz.world.track.FreerailsTile;
import org.railz.world.track.TrackRule;

class CalcCargoSupplyRateAtStation {
    private ReadOnlyWorld w;
    private SupplyDemandViewer supplyDemandViewer;

    CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
	w = world;
	supplyDemandViewer = new SupplyDemandViewer(world);
    }

    int[] scanAdjacentTiles() {
	int nCargoTypes = w.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
	int[] supplies = new int[nCargoTypes];
	SupplyAtStation sas = supplyDemandViewer.getSupply();
	for (int i = 0; i < nCargoTypes; i++) {
	    supplies[i] = sas.getSupply(i);
	}
        return supplies;
    }

    DemandAtStation getDemand() {
        return supplyDemandViewer.getDemand();
    }

    ConvertedAtStation getConversion() {
        return supplyDemandViewer.getConversion();
    }
}
