/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.server;

import jfreerails.move.*;
import jfreerails.world.accounts.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
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
