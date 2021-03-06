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
 * Created on 07-Jul-2003
 *
 */
package org.railz.move;

import org.railz.world.accounts.BankAccount;
import org.railz.world.accounts.InitialDeposit;
import org.railz.world.accounts.Transaction;
import org.railz.world.accounts.AddItemTransaction;
import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.top.KEY;
import org.railz.world.top.ITEM;

/**
 * @author Luke Lindsay
 *
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(1000000L == account.getCurrentBalance());

        Transaction t = new InitialDeposit((GameTime)
		getWorld().get(ITEM.TIME, Player.AUTHORITATIVE), 100);
        Move m = new AddTransactionMove(0, t, testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
	account = (BankAccount) getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(1000100L == account.getCurrentBalance());

        Move m2 = new AddTransactionMove(5, t, testPlayer.getPrincipal());
        assertTryMoveFails(m2);
        assertEqualsSurvivesSerialisation(m);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(1000000L == account.getCurrentBalance());

	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		testPlayer.getPrincipal());
	Transaction t = new AddItemTransaction(now, AddItemTransaction.TRACK,
		0, 1, -1000100L);
        Move m = new AddTransactionMove(0, t, true, testPlayer.getPrincipal());

        //This move should fail since there is no money in the account and 
        //it is constrained is set to true.
        assertTryMoveFails(m);
    }
}
