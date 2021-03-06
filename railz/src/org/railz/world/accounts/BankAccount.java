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
 * Created on 24-Jun-2003
 *
 */
package org.railz.world.accounts;

import java.io.*;
import java.util.ArrayList;

import org.railz.world.common.FreerailsSerializable;

/**
 * @author Luke Lindsay
 *
 */
public class BankAccount implements FreerailsSerializable {
    static final long serialVersionUID = 4209030709047745616L;

    private ArrayList transactions = new ArrayList();
    private long currentBalance = 0;

    public BankAccount() {
    }

    private BankAccount(BankAccount b) {
	currentBalance = b.currentBalance;
	transactions = (ArrayList) b.transactions.clone();
    }

    public synchronized long getCurrentBalance() {
        return currentBalance;
    }

    public synchronized int size() {
        return transactions.size();
    }

    public synchronized BankAccount addTransaction(Transaction t) {
	BankAccount b = new BankAccount(this);
        b.transactions.add(t);
        b.currentBalance += t.getValue();
	return b;
    }

    public synchronized BankAccount removeLastTransaction() {
	BankAccount b = new BankAccount(this);
        int last = b.transactions.size() - 1;
        Transaction t = (Transaction) b.transactions.remove(last);
        b.currentBalance -= t.getValue();
        return b;
    }

    public synchronized Transaction getTransaction(int i) {
        return (Transaction)transactions.get(i);
    }

    public synchronized int hashCode() {
	return (int) (((currentBalance & 0xffff0000L) >> 32) ^
	       	(currentBalance & 0xffff));
    }

    public synchronized boolean equals(Object o) {
        if (o instanceof BankAccount) {
            BankAccount test = (BankAccount)o;

            return this.transactions.equals(test.transactions);
            //No need to look at the current balance field since it 
            //can be calculated by looking at the transactions.
        } else {
            return false;
        }
    }
}
