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
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.top.UUID;

/**
 *
 * @author bob
 */
abstract class BaseCargoBundle implements FreerailsSerializable {
    protected final HashMap hashMap;
    protected final UUID uuid;
    
    /** Creates a new instance of BaseCargoBundle */
    protected BaseCargoBundle(BaseCargoBundle cb) {
        hashMap = (HashMap) cb.hashMap.clone();
        uuid = cb.getUUID();
    }
    
    protected BaseCargoBundle() {
        uuid = new UUID();
        hashMap = new HashMap();
    }
    
    public int getAmount(int cargoType) {
        Iterator it = cargoBatchIterator();
        int amount = 0;

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) it.next()).getKey();

            if (cb.getCargoType() == cargoType) {
                amount += getAmount(cb);
            }
        }

        return amount;
    }

    public int getAmount(CargoBatch cb) {
        if (contains(cb)) {
            Integer i = (Integer)hashMap.get(cb);

            return i.intValue();
        } else {
            return 0;
        }
    }

    public boolean contains(CargoBatch cb) {
        return hashMap.containsKey(cb);
    }

    public Iterator cargoBatchIterator() {
        return hashMap.entrySet().iterator();
    }

    public UUID getUUID() {
        return uuid;
    }
}
