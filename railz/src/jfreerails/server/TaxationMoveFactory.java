package jfreerails.server;

import java.util.Date;
import java.util.GregorianCalendar;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.*;
import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
/**
 * Generates taxation moves
 *
 * @author rtuck99@users.berlios.de
 */
class TaxationMoveFactory {
    private ReadOnlyWorld world;
    private MoveReceiver moveReceiver;

    TaxationMoveFactory(ReadOnlyWorld w, MoveReceiver mr) {
	moveReceiver = mr;
	world = w;
    }

    private void generateMovesForPlayer(long startTicks, long endTicks,
	    FreerailsPrincipal p, Economy economy, GameTime now) {
	BankAccount account = (BankAccount) (world.get(KEY.BANK_ACCOUNTS, 0,
		    p));
	BankAccountViewer bav = new BankAccountViewer(world);
	bav.setBankAccount(account);
	long taxDue = bav.getIncomeTaxLiability();
	if (taxDue < 0)
	    taxDue = 0;
	Bill taxBill = new Bill(now, taxDue, Bill.INCOME_TAX);
	AddTransactionMove taxMove = new AddTransactionMove(0, taxBill, false,
		p);
	moveReceiver.processMove(taxMove);
    }

    /**
     * Generates taxation moves for all players for the previous year.
     * @param year year for which to generate taxes.
     */
    void generateMoves(int year) {
	GregorianCalendar yearStart = new GregorianCalendar(year, 0, 1);
	GregorianCalendar yearEnd = new GregorianCalendar(year + 1, 0, 1);
	Economy economy = (Economy) world.get(ITEM.ECONOMY,
		Player.AUTHORITATIVE);
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	GameCalendar gameCalendar = (GameCalendar) world.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	long baseMillis = (new GregorianCalendar(gameCalendar.getStartYear(),
		    0, 1)).getTimeInMillis();
	long startTicks = (yearStart.getTimeInMillis() - baseMillis) / (1000 *
		60 * 60 * 24) * gameCalendar.getTicksPerDay();
	long endTicks = (yearEnd.getTimeInMillis() - baseMillis)  / (1000 * 60
		* 60 * 24) * gameCalendar.getTicksPerDay();

	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    generateMovesForPlayer(startTicks, endTicks, p, economy, now);
	}
    }
}
