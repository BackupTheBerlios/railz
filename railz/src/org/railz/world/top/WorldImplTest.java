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
 * Created on 20-Mar-2003
 *
 */
package org.railz.world.top;

import org.railz.world.common.FreerailsSerializable;
import junit.framework.TestCase;
import org.railz.world.player.*;

/**
 * @author Luke
 *
 */
public class WorldImplTest extends WorldTest {
    protected  World getWorld(int width, int height) {
	World w = new WorldImpl(width, height);
	w.add(KEY.PLAYERS, pl1, Player.AUTHORITATIVE);
	w.add(KEY.PLAYERS, pl2, Player.AUTHORITATIVE);
	return w;
    }
}
