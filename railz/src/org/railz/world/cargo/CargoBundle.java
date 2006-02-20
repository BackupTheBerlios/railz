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
 * Created on 24-May-2003
 *
 */
package org.railz.world.cargo;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import org.railz.world.common.WorldObject;
import org.railz.world.top.UUID;


/**This CargoBundle uses a <code>java.util.HashMap</code> to
 * map quantities to cargo batches.
 *
 * @author Luke
 *
 */
public class CargoBundle extends BaseCargoBundle implements WorldObject {
    protected CargoBundle(CargoBundle cb) {
        super(cb);
    }
    
    public CargoBundle() {
        // call default super constructor
    }    
    
    public CargoBundle(MutableCargoBundle cb) {
        super(cb);
    }
    
    public String toString() {
        String s = "CargoBundle {\n";
        Iterator it = this.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) it.next()).getKey();
            s += this.getAmount(cb) + " units of cargo type " +
            cb.getCargoType() + "\n";
        }

        s += "}";

        return s;
    }

    public boolean equals(Object o) {
        if (o instanceof CargoBundle) {
            CargoBundle test = (CargoBundle) o;

            return uuid.equals(test.uuid) &&
                    hashMap.equals(test.hashMap);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return uuid.hashCode();
    }
    
    public Object clone() {        
        return new CargoBundle(this);
    }
}
