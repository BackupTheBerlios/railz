/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.move;

import jfreerails.world.accounts.BalanceSheet;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.*;
public class AddBalanceSheetMove extends AddItemToListMove {
    public AddBalanceSheetMove(ReadOnlyWorld w, BalanceSheet bs,
	    FreerailsPrincipal p) {
	super(KEY.BALANCE_SHEETS, w.size(KEY.BALANCE_SHEETS, p), bs, p);
    }
}
