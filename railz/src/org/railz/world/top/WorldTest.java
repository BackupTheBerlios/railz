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
package org.railz.world.top;

import java.security.*;
import junit.framework.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.track.*;
/**
 * Tests an implementation of World
 */
public abstract class WorldTest extends TestCase {
    private static Player getPlayer(String name, int id) {
	Player p = new Player(name);
	PublicKey pk = p.getPublicKey();
	p = new Player(name, pk, id);
	return p;
    }

    /**
     * Retrieve an implementation of World, with a map of the specified size,
     * and containing players pl1 and pl2
     */
    protected abstract World getWorld(int width, int height);

    protected Player pl1 = getPlayer("pl1", 0);
    protected Player pl2 = getPlayer("pl2", 1);
    protected FreerailsPrincipal p1 = pl1.getPrincipal();
    protected FreerailsPrincipal p2 = pl2.getPrincipal();
    protected TestWorldObject o1 = new TestWorldObject(1);
    protected TestWorldObject o2 = new TestWorldObject(2);
    protected TestWorldObject o3 = new TestWorldObject(3);
    protected FreerailsTile t1 = new FreerailsTile(0, null, null);
    protected FreerailsTile t2 = new FreerailsTile(1, null, null);
    protected FreerailsTile t3 = new FreerailsTile(2, null, null);

    public void testWorldObjectStorage() {
        World w = getWorld(10, 10);
        System.out.println("class of w is " + w.getClass().getName());
        TestWorldObject2 two1 = new TestWorldObject2();
        TestWorldObject2 two2 = new TestWorldObject2();
        ObjectKey2 two1Key = new ObjectKey2(KEY.STATIONS, p1, two1.getUUID());
        ObjectKey2 two2Key = new ObjectKey2(KEY.STATIONS, p1, two2.getUUID());
        
        assertEquals(0, w.size(KEY.STATIONS, p1));
        w.set(two1Key, two1);
        assertEquals(two1, w.get(two1Key));
        assertTrue(w.contains(two1Key));
        assertEquals(1, w.size(KEY.STATIONS, p1));
        w.set(two2Key, two2);
        assertEquals(2, w.size(KEY.STATIONS, p1));
        assertEquals(two1, w.get(two1Key));
        assertEquals(two2, w.get(two2Key));
        w.remove(two1Key);
        assertEquals(1, w.size(KEY.STATIONS, p1));
        assertFalse(w.contains(two1Key));
        assertTrue(w.contains(two2Key));        
    }

    public void testObjectStorage() {
	World w = getWorld(10, 10);        
	w.add(KEY.TRAINS, o1, p1);
	assertEquals(o1, w.get(KEY.TRAINS, 0, p1));
	assertTrue(w.boundsContain(KEY.TRAINS, 0, p1));
	assertFalse(w.boundsContain(KEY.TRAINS, 1, p1));
	w.set(KEY.TRAINS, 0, o2, p1);
	assertEquals(o2, w.get(KEY.TRAINS, 0, p1));
	assertEquals(1, w.size(KEY.TRAINS, p1));
	w.add(KEY.TRAINS, o2, p1);
	assertTrue(w.boundsContain(KEY.TRAINS, 1, p1));
	assertEquals(2, w.size(KEY.TRAINS, p1));
	assertEquals(o2, w.removeLast(KEY.TRAINS, p1));
	assertEquals(1, w.size(KEY.TRAINS, p1));
    }

    public void testItemStorage() {
	World w = getWorld(10, 10);
	w.set(ITEM.TIME, o1, p1);
	assertEquals(o1, w.get(ITEM.TIME, p1));
	w.set(ITEM.TIME, o2, p1);
	assertEquals(o2, w.get(ITEM.TIME, p1));
    }

    public void testTileStorage() {
	World w = getWorld(10, 15);
	assertTrue(w.boundsContain(0, 0));
	assertTrue(w.boundsContain(9, 14));
	assertFalse(w.boundsContain(-1, 0));
	assertFalse(w.boundsContain(0, -1));
	assertFalse(w.boundsContain(10, 14));
	assertFalse(w.boundsContain(9, 15));
	assertEquals(10, w.getMapWidth());
	assertEquals(15, w.getMapHeight());

	w.setTile(0, 0, t1);
	assertEquals(t1, w.getTile(0, 0));
	w.setTile(0, 0, t2);
	assertEquals(t2, w.getTile(0, 0));
    }

    private class TestWorldObject implements FreerailsSerializable {
	private int id;
	public TestWorldObject(int id) {
	    this.id = id;
	}

	public boolean equals(Object o) {
	    if (o instanceof TestWorldObject) {
		return (((TestWorldObject) o).id == id);
	    }
	    return false;
	}

	public int hashCode() {
	    return id;
	}
    }
    
    private class TestWorldObject2 implements WorldObject {
        private UUID uuid = new UUID();
        
        public UUID getUUID() {
            return uuid;
        }        
        
        public boolean equals(Object o) {
            if (o instanceof WorldObject) {
                return uuid.equals(((WorldObject) o).getUUID());
            }
            return false;
        }
        
        public int hashCode() {
            return uuid.hashCode();
        }
    }
}
