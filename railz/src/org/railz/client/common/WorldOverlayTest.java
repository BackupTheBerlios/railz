/*
 * Copyright (C) 2005 Robert Tuck
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
package org.railz.client.common;

import org.railz.world.top.*;
import org.railz.world.player.*;
public class WorldOverlayTest extends WorldTest {
    protected World getWorld(int width, int height) {
	World w = new WorldImpl(width, height);
	w.add(KEY.PLAYERS, pl1, Player.AUTHORITATIVE);
	w.add(KEY.PLAYERS, pl2, Player.AUTHORITATIVE);
	return new WorldOverlay(w);
    }

    public void testItemStorage() {
	/* This class doesn't support setting ITEMs */
    }

    /*
    public void testNoHashMasking() {
	World w = getWorld(10, 10);
	World underlyingWorld = ((WorldOverlay) w).getWorld();
	underlyingWorld.add(KEY.STATIONS, o1, p1);
	underlyingWorld.setTile(0, 0, t1);

	// overlay objects on world
	w.set(KEY.STATIONS, 0, o2, p1);
	assertEquals(o2, w.get(KEY.STATIONS, 0, p1));
	w.setTile(0, 0, t2);
	assertEquals(t2, w.getTile(0, 0));

	// change underlying world
	underlyingWorld.set(KEY.STATIONS, 0, o3, p1);
	assertEquals(o3, w.get(KEY.STATIONS, 0, p1));
	underlyingWorld.setTile(0, 0, t3);
	assertEquals(t3, w.getTile(0, 0));

	// change overlay to same as underlying, removing the hash
	w.set(KEY.STATIONS, 0, o3, p1);
	assertEquals(o3, w.get(KEY.STATIONS, 0, p1));
	w.setTile(0, 0, t3);
	assertEquals(t3, w.getTile(0, 0));

	// change underlying world
	underlyingWorld.set(KEY.STATIONS, 0, o2, p1);
	assertEquals(o2, w.get(KEY.STATIONS, 0, p1));
	underlyingWorld.setTile(0, 0, t2);
	assertEquals(t2, w.getTile(0, 0));
    }

    public void testRemoveHash() {
	World w = getWorld(10, 10);
	World underlyingWorld = ((WorldOverlay) w).getWorld();
	underlyingWorld.add(KEY.STATIONS, o1, p1);
	underlyingWorld.setTile(0, 0, t1);
	assertEquals(o1, w.get(KEY.STATIONS, 0, p1));

	// set the overlay
	w.set(KEY.STATIONS, 0, o2, p1);
	assertEquals(o2, w.get(KEY.STATIONS, 0, p1));
	w.setTile(0, 0, t2);
	assertEquals(t2, w.getTile(0, 0));
	// set it back, thus deleting the key
	w.set(KEY.STATIONS, 0, o1, p1);
	assertEquals(o1, w.get(KEY.STATIONS, 0, p1));
	w.setTile(0, 0, t1);
	assertEquals(t1, w.getTile(0, 0));

	// check that when the underlying world is updated, this is reflected
	// in the overlay
	underlyingWorld.set(KEY.STATIONS, 0, o2, p1);
	assertEquals(o2, w.get(KEY.STATIONS, 0, p1));
	underlyingWorld.setTile(0, 0, t2);
	assertEquals(t2, w.getTile(0, 0));
    }
    */
}
