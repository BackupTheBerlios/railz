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
 * Created on 31-May-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBundle;
import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;


/**
 * @author Luke
 *
 */
public class CompositeMoveTest extends AbstractMoveTestCase {
    StationModel station1;
    StationModel station2;
    StationModel station3;
    StationModel station4;

    private ObjectKey2 getNewCBKey() {
        CargoBundle cb = new CargoBundle();
        ObjectKey2 cbKey = new ObjectKey2(KEY.CARGO_TYPES, Player.NOBODY, cb.getUUID());
        return cbKey;
    }
    
    public void setUp() {
	super.setUp();
	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		Player.AUTHORITATIVE);
	station1 = new StationModel(1, 1, "station1", 10, getNewCBKey(), now);
	station2 = new StationModel(2, 3, "station2", 10, getNewCBKey(), now);
	station3 = new StationModel(3, 3, "station3", 10, getNewCBKey(), now);
	station4 = new StationModel(4, 4, "station4", 10, getNewCBKey(), now);
    }

    public void testMove() {
        Move[] moves = new Move[4];
        moves[0] = new AddObjectMove(new ObjectKey2(KEY.STATIONS, 
                testPlayer.getPrincipal(), station1.getUUID()), station1);
        moves[1] = new AddObjectMove(new ObjectKey2(KEY.STATIONS, 
                testPlayer.getPrincipal(), station2.getUUID()), station2);
        moves[2] = new AddObjectMove(new ObjectKey2(KEY.STATIONS, 
                testPlayer.getPrincipal(), station3.getUUID()), station3);
        moves[3] = new AddObjectMove(new ObjectKey2(KEY.STATIONS, 
                testPlayer.getPrincipal(), station4.getUUID()), station4);

        Move compositeMove = new CompositeMove(moves);
        assertEqualsSurvivesSerialisation(compositeMove);
        assertTryMoveIsOk(compositeMove);
        assertEquals("The stations should not have been add yet.", 0,
            getWorld().size(KEY.STATIONS, testPlayer.getPrincipal()));
        assertDoMoveIsOk(compositeMove);
        assertEquals("The stations should have been add now.", 4,
            getWorld().size(KEY.STATIONS, testPlayer.getPrincipal()));
        assertTryUndoMoveIsOk(compositeMove);
        assertUndoMoveIsOk(compositeMove);

        assertOkButNotRepeatable(compositeMove);
    }
}
