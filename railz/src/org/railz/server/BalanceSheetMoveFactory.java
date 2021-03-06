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
package org.railz.server;

import org.railz.move.*;
import org.railz.world.accounts.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Responsible for calculating balance sheets for all players at the year end.
 */
class BalanceSheetMoveFactory {
    AuthoritativeMoveExecuter moveExecuter;
    ReadOnlyWorld world;

    BalanceSheetMoveFactory(ReadOnlyWorld w, AuthoritativeMoveExecuter me) {
	world = w;
	moveExecuter = me;
    }

    void generateMoves() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world, 
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    BalanceSheet bs = BalanceSheet.generateBalanceSheet(world, p,
		    false);
	    AddBalanceSheetMove m = new AddBalanceSheetMove(world, bs, p);
	    moveExecuter.processMove(m, p);
	}
    }
}
