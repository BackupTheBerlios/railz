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
 * Created on Dec 13, 2003
 */
package org.railz.world.accounts;

import org.railz.world.cargo.CargoBundle;
import org.railz.world.common.GameTime;

/**
 *
 *  @author Luke
 *
 */
public final class DeliverCargoReceipt extends Transaction {
    public static final int SUBCATEGORY_FREIGHT = 1;
    public static final int SUBCATEGORY_PASSENGERS = 2;

    private final CargoBundle cargoDelivered;
    private final int subcategory;

    public DeliverCargoReceipt(GameTime time, long value, CargoBundle cb,
	    int subcat) {
        super(time, value);
        cargoDelivered = cb;
	if (subcat != SUBCATEGORY_FREIGHT && subcat != SUBCATEGORY_PASSENGERS)
	    throw new IllegalArgumentException();

	subcategory = subcat;
    }

    public CargoBundle getCargoDelivered() {
        return cargoDelivered;
    }
    
    public int getCategory() {
	return CATEGORY_REVENUE;
    }

    public int getSubcategory() {
	return subcategory;
    }
}
