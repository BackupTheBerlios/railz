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
 * Created on 26-May-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBundle;
import org.railz.world.common.WorldObject;
import org.railz.world.player.*;
import org.railz.world.top.ObjectKey2;


/**
 * This Move adds a cargo bundle to the cargo bundle list.
 *
 * @author Luke
 *
 */
public class AddCargoBundleMove extends AddObjectMove {
    public AddCargoBundleMove(ObjectKey2 key, CargoBundle item) {
        super(key, item);
    }
}
