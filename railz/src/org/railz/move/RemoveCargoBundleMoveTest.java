/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 26-May-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.MutableCargoBundle;
import org.railz.world.common.GameTime;
import org.railz.world.player.*;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.ObjectKey2;


/**
 * @author Luke
 *
 */
public class RemoveCargoBundleMoveTest extends AbstractMoveTestCase {
    public ObjectKey2 stationKey;
    
    public void setUp() {
        super.setUp();
        CargoBundle cb = new CargoBundle();
        ObjectKey2 cbKey = new ObjectKey2(KEY.CARGO_BUNDLES, Player.NOBODY, 
                cb.getUUID());
        getWorld().set(cbKey, cb);
        StationModel sm =  new StationModel(0, 0, "Test station",
                getWorld().size(KEY.CARGO_TYPES, Player.AUTHORITATIVE),
                cbKey,
                (GameTime) getWorld().get(ITEM.TIME, Player.AUTHORITATIVE));        
        stationKey = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), 
                sm.getUUID());
        getWorld().set(stationKey, sm);        
    }

     public void testMove() {
        MutableCargoBundle m1 = new MutableCargoBundle();
        m1.setAmount(new CargoBatch(1, 2, 3, 4, stationKey), 5);
        CargoBundle bundleA = new CargoBundle(m1);
        MutableCargoBundle m2 = new MutableCargoBundle(bundleA);
        m2.setAmount(new CargoBatch(1, 2, 3, 4, stationKey), 5);
        CargoBundle bundleB = new CargoBundle(m2);
        assertEquals(bundleA, bundleB);

        ObjectKey2 key = new ObjectKey2(KEY.CARGO_BUNDLES, Player.NOBODY, 
                bundleA.getUUID());
        Move m = new RemoveCargoBundleMove(key, bundleB);
        assertEqualsSurvivesSerialisation(m);

        assertTryMoveFails(m);
        getWorld().set(key, bundleA);
        assertTryUndoMoveFails(m);
        assertTryMoveIsOk(m);

        assertOkButNotRepeatable(m);
    }
}
