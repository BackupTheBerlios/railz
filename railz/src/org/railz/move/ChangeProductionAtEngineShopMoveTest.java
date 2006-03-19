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
 * Created on 28-Mar-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBundle;
import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.station.ProductionAtEngineShop;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.server.*;


/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
    private ProductionAtEngineShop before;
    private ProductionAtEngineShop after;
    private int engineType;
    private int wagonType;
    private int[] wagons;

    private ObjectKey2 stationKey1 = null;
    private ObjectKey2 stationKey2 = null;
    private ObjectKey2 stationKey3 = null;
    
    protected void setUp() {
        super.setUp();
	GameTime gt = (GameTime) getWorld().get(ITEM.TIME, Player.AUTHORITATIVE);
        CargoBundle cb = new CargoBundle();
        ObjectKey2 cbKey = new ObjectKey2(KEY.CARGO_BUNDLES, Player.NOBODY, cb.getUUID());
        StationModel stationModel1 = new StationModel(0, 0, "No name", 0, cbKey, gt);
        StationModel stationModel2 = new StationModel(0, 0, "No name", 0, cbKey, gt);
        StationModel stationModel3 = new StationModel(0, 0, "No name", 0, cbKey, gt);
        stationKey1 = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), 
                stationModel1.getUUID());
        stationKey2 = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), 
                stationModel2.getUUID());
        stationKey3 = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), 
                stationModel3.getUUID());
        getWorld().set(stationKey1, stationModel1);
        getWorld().set(stationKey2, stationModel2);
        getWorld().set(stationKey3, stationModel3);

	getWorld().add(KEY.WAGON_TYPES, new WagonType("WagonType1",
		    TransportCategory.MAIL, 10, 0, 10), Player.AUTHORITATIVE);
	getWorld().add(KEY.WAGON_TYPES, new WagonType("WagonType2",
		    TransportCategory.PASSENGER, 10, 0, 10),
		Player.AUTHORITATIVE);
	getWorld().add(KEY.WAGON_TYPES, new WagonType("WagonType3",
		    TransportCategory.FAST_FREIGHT, 10, 0, 10),
		Player.AUTHORITATIVE);
        engineType = 0;
        wagonType = 0;
        wagons = new int[] {wagonType, wagonType};
        after = new ProductionAtEngineShop(engineType, wagons);
    }

    public void testMove() {
        before = null;

        ChangeProductionAtEngineShopMove m;

        //Should fail because current production at station 1 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, stationKey1,
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should fail because this station does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 
                new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), new UUID()),
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, stationKey1,
		testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);

        //It should not be repeatable.
        assertOkButNotRepeatable(m);

        assertEqualsSurvivesSerialisation(m);
    }

    public void testProductionAtEngineShopEquals() {
        ProductionAtEngineShop a;
        ProductionAtEngineShop b;
        ProductionAtEngineShop c;
        ProductionAtEngineShop d;
        a = null;
        b = new ProductionAtEngineShop(engineType, wagons);
        c = new ProductionAtEngineShop(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}
