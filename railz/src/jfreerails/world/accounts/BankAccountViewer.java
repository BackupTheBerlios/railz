/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.accounts;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
public class BankAccountViewer {
    ReadOnlyWorld world;
    BankAccount bankAccount;
    Economy economy;

    public BankAccountViewer(ReadOnlyWorld w) {
	world = w;
	economy = (Economy) world.get(ITEM.ECONOMY, Player.AUTHORITATIVE);
    }

    public void setBankAccount(BankAccount ba) {
	bankAccount = ba;
    }

    /**
     * @return the cumulative amount of outside investment.
     */
    public long getOutsideInvestment() {
	int size = bankAccount.size();
	long total = 0;
	for (int i = 0; i < size; i++) {
	    Transaction t = bankAccount.getTransaction(i);
	    if (t.getCategory() == Transaction.CATEGORY_OUTSIDE_INVESTMENT) {
		total += t.getValue();
	    }
	}
	return total;
    }

    /**
     * @return the outstanding income tax liability
     */
    public long getIncomeTaxLiability() {
	int size = bankAccount.size();
	long income = 0;
	for (int i = size - 1; i >= 0; i--) {
	    Transaction t = bankAccount.getTransaction(i);
	    switch (t.getCategory()) {
		// expenses are -ve and deducted from total income
		case Transaction.CATEGORY_REVENUE:
		case Transaction.CATEGORY_COST_OF_SALES:
		case Transaction.CATEGORY_OPERATING_EXPENSE:
		case Transaction.CATEGORY_INTEREST:
		case Transaction.CATEGORY_CAPITAL_GAIN:
		    income += t.getValue();
		    break;
		case Transaction.CATEGORY_TAX:
		    i = 0;
		    break;
		default:
		    // ignore
	    }
	}
	if (income < 0) {
	    // no need to deduct tax
	    return 0;
	}
	return (income * economy.getIncomeTaxRate() / 100);
    }
}
