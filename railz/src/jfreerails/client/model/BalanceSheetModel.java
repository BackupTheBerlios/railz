/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.client.model;

import java.util.GregorianCalendar;

import jfreerails.world.accounts.*;
import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
public class BalanceSheetModel {
    private final BalanceSheet balanceSheet;

    public BalanceSheet getBalanceSheet() {
	return balanceSheet;
    }

    private ModelRoot modelRoot;

    public BalanceSheetModel(ModelRoot mr, int year) {
	modelRoot = mr;
	ReadOnlyWorld w = mr.getWorld();
	GameCalendar gc = (GameCalendar) w.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	int thisYear = gc.getCalendar(now).get(GregorianCalendar.YEAR);
	if (year == thisYear) {
	    /* create a pro-forma balance sheet */
	    balanceSheet = BalanceSheet.generateBalanceSheet(w,
		    modelRoot.getPlayerPrincipal(), true);
	} else {
	    /* retreive historical balance sheet */
	    balanceSheet = (BalanceSheet) w.get(KEY.BALANCE_SHEETS, year -
		    gc.getStartYear(), modelRoot.getPlayerPrincipal()); 
	}
    }
}
