/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import java.util.Arrays;
import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;
import jfreerails.world.player.Player;

/**
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGeneratorTest extends TestCase {
    private World w;
    private Player player;

    protected void setUp() throws Exception {
	Player p = new Player("testPlayer");
	p = new Player("testPlayer", p.getPublicKey(), 0);
	player = p;
        w = new WorldImpl(20, 20);
	w.add(KEY.PLAYERS, p, Player.AUTHORITATIVE);
        w.add(KEY.BANK_ACCOUNTS, new BankAccount(), p.getPrincipal());
        MapFixtureFactory.generateTrackRuleList(w);
    }

    public void testGenerateMove() {
        addTrack(0, 50);

        /* The maintenace cost of track type 0 is 10 (see MapFixtureFactory), so
        * the cost of maintaining 50 units is 500. */
	AddTransactionMove[] moves =
	    TrackMaintenanceMoveGenerator.generateMove(w);
	AddTransactionMove m = moves[0];
        Transaction t = m.getTransaction();
        long expected = -500;
        long actual = t.getValue();
        assertTrue(expected == actual);
    }

    public void testCalulateNumberOfEachTrackType() {
        int[] actual;
        int[] expected;
	actual =
	    TrackMaintenanceMoveGenerator.calulateNumberOfEachTrackType(w,
		    player.getPrincipal());
        expected = new int[] {0, 0, 0}; //No track has been built yet.
        assertTrue(Arrays.equals(expected, actual));

        int quantity = 10;

        addTrack(0, 10);

	actual = TrackMaintenanceMoveGenerator.calulateNumberOfEachTrackType(w,
		player.getPrincipal());
        expected = new int[] {10, 0, 0};
        assertTrue(Arrays.equals(expected, actual));

        addTrack(2, 20);

	actual = TrackMaintenanceMoveGenerator.calulateNumberOfEachTrackType(w,
		player.getPrincipal());
        expected = new int[] {10, 0, 20};
        assertTrue(Arrays.equals(expected, actual));
    }

    /**
     * Utility method to add the specifed number of units of the specified
     * track type.
     */
    private BankAccount addTrack(int trackType, int quantity) {
	BankAccount account = (BankAccount)w.get(KEY.BANK_ACCOUNTS, 0,
		player.getPrincipal());
	GameTime now = (GameTime) w.get(ITEM.TIME, player.getPrincipal());
	AddItemTransaction t = new AddItemTransaction(now,
		AddItemTransaction.TRACK,
                trackType, quantity, trackType);
        account.addTransaction(t);

        return account;
    }
}
