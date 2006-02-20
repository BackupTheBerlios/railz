/*
 * Copyright (C) 2006 Robert Tuck
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

package org.railz.world.cargo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.railz.world.top.UUID;

/**
 *
 * @author bob
 */
public class MutableCargoBundle extends BaseCargoBundle {
    public MutableCargoBundle() {
        // default super constructor
    }
    
    /** Creates a new instance of MutableCargoBundle */
    public MutableCargoBundle(CargoBundle cb) {
        super(cb);
    }
    
    public void setAmount(CargoBatch cb, int amount) {
        if (0 == amount) {
            hashMap.remove(cb);
        } else {
            hashMap.put(cb, new Integer(amount));
        }
    }

    public void addCargo(CargoBatch cb, int amount) {
        int amountAlready = this.getAmount(cb);
        this.setAmount(cb, amount + amountAlready);
    }
}
