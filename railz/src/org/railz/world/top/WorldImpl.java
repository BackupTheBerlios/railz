/*
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

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.WorldObject;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.track.FreerailsTile;

/**
 * Implements storage for the world data. TODO At the moment all world data is
 * uniformly viewable by all parties. At some point in the future, instead of
 * a single WorldImpl accessible to all parties, different "Views" will be
 * made available for each party (server, and each connected client). Each
 * client will receive different moves according to their view. In this way,
 * no client will be able to view privileged information about other clients.
 */
public class WorldImpl implements World {
    /**
     * An array of ArrayList indexed by keyNumber.
     * If the key is shared, then the ArrayList consists of instances of the
     * class corresponding to the KEY type. Otherwise, the ArrayList is
     * indexed by Player index, and contains instances of ArrayList
     * which themselves contain instances of the class corresponding to the
     * KEY type.
     */
    private final ArrayList[] lists = new ArrayList[KEY.getNumberOfKeys()];
    
    private final FreerailsSerializable[] items = new FreerailsSerializable[ITEM.getNumberOfKeys()];
    private FreerailsTile[][] map;

    private final HashMap[] sharedObjects = new HashMap[KEY.getNumberOfKeys()];
    private final ArrayList[] playerObjects = new ArrayList[KEY.getNumberOfKeys()];
    
    public WorldImpl(int mapWidth, int mapHeight) {
        this.setupMap(mapWidth, mapHeight);
        this.setupLists();
    }

    public WorldImpl() {
	this(0, 0);
    }
    
    /**
     * Create a shallow copy of the specified world which only exposes
     * those objects which the specified player is permitted to access
     */
    private WorldImpl(WorldImpl wi, FreerailsPrincipal viewer) {
	setupLists();
	int pi = wi.getPlayerIndex(viewer);
	for (int i = 0; i < lists.length; i++) {
	    KEY k = KEY.getKey(i);
            if (! k.usesObjectKey2) {
                ArrayList al = wi.lists[i];
                for (int j = 0; j < al.size(); j++) {                
                    if (k.isPrivate && pi != j) {
                        // this player isn't allowed to see
                        lists[i].add(null);
                    } else {
                        // this player can see, make a shallow copy
                        lists[i].add(al.get(j));
                    }
                }
            } else {
                if (k.shared) {
                    sharedObjects[i] = wi.sharedObjects[i];
                } else {
                    ArrayList al = wi.playerObjects[i];
                    for (int j = 0; j < al.size(); j++) {
                        if (k.isPrivate && pi != j) {
                            playerObjects[i].add(null);
                        } else {
                            playerObjects[i].add(al.get(j));
                        }
                    }
                }
            }
	}
        // All ITEMs are accessible
	for (int i = 0; i < wi.items.length; i++)
	    items[i] = wi.items[i];
        
        // The map is viewable to all
	map = wi.map;
    }

    synchronized ReadOnlyWorld getReadOnlyView (FreerailsPrincipal viewer) {
	return new WorldImpl(this, viewer);
    }

    public void setupMap(int mapWidth, int mapHeight) {
        map = new FreerailsTile[mapWidth][mapHeight];
    }

    /**
     * Initialise the world contents (note that since there are no players,
     * only the 1st level of nesting is created.
     */
    private void setupLists() {
        for (int i = 0; i < lists.length; i++) {
            KEY k = KEY.getKey(i);
            if (k.usesObjectKey2) {
                if (k.shared) {
                    sharedObjects[i] = new HashMap();
                } else {
                    playerObjects[i] = new ArrayList();
                }
            } else {
                lists[i] = new ArrayList();
            }
        }
    }

    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
        if (key.usesObjectKey2)
            throw new IllegalArgumentException("KEY " + key + " uses ObjectKey2");
        
        if (key.shared) {
            return (FreerailsSerializable)lists[key.getKeyNumber()].get(index);
        }

        return (FreerailsSerializable)((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(
                p))).get(index);
    }

    public WorldObject get(ObjectKey2 key) {
        if (key.key.shared)
            return (WorldObject) sharedObjects[key.key.getKeyNumber()].get(key.uuid);
        else {
            int playerId = getPlayerIndex(key.principal);
            return (WorldObject) 
                ((HashMap) 
                    ((ArrayList) playerObjects[key.key.getKeyNumber()]).get(playerId))
                .get(key.uuid);
        }
    }
    
    public void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal p) {
        if (key.usesObjectKey2)
            throw new IllegalArgumentException("KEY " + key + " uses ObjectKey2");
        
        if (key.shared) {
            lists[key.getKeyNumber()].set(index, element);

            return;
        }

        ((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(p))).set(index,
            element);
    }

    public void set(ObjectKey2 key, WorldObject object) {
        if (key.key.shared) {
            HashMap m = sharedObjects[key.key.getKeyNumber()];
            m.put(key.uuid, object);
        } else {
            ArrayList l = playerObjects[key.key.getKeyNumber()];
            HashMap m = (HashMap) l.get(getPlayerIndex(key.principal));
            m.put(key.uuid, object);
        }
    }
        
    public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal p) {
        if (key.usesObjectKey2)
            throw new IllegalArgumentException("KEY " + key + " uses ObjectKey2");
        
        if (key == KEY.PLAYERS) {
            return addPlayer((Player)element, p);
        }

        if (key.shared) {
            lists[key.getKeyNumber()].add(element);

            return size(key, Player.NOBODY) - 1;
        }

        ((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(p))).add(element);

        return size(key, p) - 1;
    }

    public int size(KEY key, FreerailsPrincipal p) {
        if (key.usesObjectKey2) {
            if (key.shared) {
                return sharedObjects[key.getKeyNumber()].size();
            } else {
                ArrayList l = playerObjects[key.getKeyNumber()];
                return ((HashMap) l.get(getPlayerIndex(p))).size();
            }
        }
        
        if (key.shared) {
            return lists[key.getKeyNumber()].size();
        }

        return ((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(p))).size();
    }

    public int getMapWidth() {
        return map.length;
    }

    public int getMapHeight() {
        if (map.length == 0) {
            //When the map size is 0*0 we get a java.lang.ArrayIndexOutOfBoundsException: 0
            // if we don't have check above.			 	
            return 0;
        } else {
            return map[0].length;
        }
    }

    public void setTile(int x, int y, FreerailsTile element) {
	// System.out.println("setTile:" + element);
        map[x][y] = element;
    }

    public FreerailsTile getTile(int x, int y) {
        return map[x][y];
    }

    public FreerailsTile getTile(Point p) {
	return map[p.x][p.y];
    }

    public boolean boundsContain(int x, int y) {
        return x >= 0 && x < map.length && y >= 0 && y < map[0].length;
    }

    public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
        if (k.usesObjectKey2)
            throw new IllegalArgumentException("KEY " + k + " uses ObjectKey2");
        
        return index >= 0 && index < this.size(k, p);
    }

    public boolean contains(ObjectKey2 key) {
        if (key.key.shared) {
            return sharedObjects[key.key.getKeyNumber()].containsKey(key.uuid);            
        } else {
            ArrayList l = playerObjects[key.key.getKeyNumber()];
            HashMap m = (HashMap) l.get(getPlayerIndex(key.principal));
            return m.containsKey(key.uuid);
        }
    }
    
    public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal p) {
        if (key.usesObjectKey2)
            throw new IllegalArgumentException("KEY " + key + " uses ObjectKey2");
        
        int size;

        if (key.shared) {
            size = lists[key.getKeyNumber()].size();
        } else {
            size = ((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(p))).size();
        }

        int index = size - 1;

        if (key.shared) {
            return (FreerailsSerializable)lists[key.getKeyNumber()].remove(index);
        }

        return (FreerailsSerializable)((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(
                p))).remove(index);
    }

    public WorldObject remove(ObjectKey2 key) {
        if (key.key.shared) {
            return (WorldObject) sharedObjects[key.key.getKeyNumber()].remove(key.uuid);
        } else {
            ArrayList l = playerObjects[key.key.getKeyNumber()];
            return (WorldObject) ((HashMap) l.get(getPlayerIndex(key.principal))).remove(key.uuid);
        }
    }
    
    public boolean equals(Object o) {
        if (o instanceof WorldImpl) {
            WorldImpl test = (WorldImpl)o;
            
            if (lists.length != test.lists.length) {
                return false;
            } else {
                for (int i = 0; i < lists.length; i++) {
                    if (lists[i] != null && !lists[i].equals(test.lists[i])) {
                        return false;
                    }
                }
            }

            if (! (Arrays.equals(sharedObjects, test.sharedObjects)
                && Arrays.equals(playerObjects, test.playerObjects))) {                
                return false;
            }
            
            if ((this.getMapWidth() != test.getMapWidth()) ||
                    (this.getMapHeight() != test.getMapHeight())) {
                return false;
            } else {
                for (int x = 0; x < this.getMapWidth(); x++) {
                    for (int y = 0; y < this.getMapHeight(); y++) {
                        if (!getTile(x, y).equals(test.getTile(x, y))) {
                            return false;
                        }
                    }
                }
            }

            if (this.items.length != test.items.length) {
                return false;
            } else {
                for (int i = 0; i < this.items.length; i++) {
                    //Some of the elements in the items array might be null, so we check for this before
                    //calling equals to avoid NullPointerExceptions.
                    if (!(null == items[i] ? null == test.items[i]
                                               : items[i].equals(test.items[i]))) {
                        return false;
                    }
                }
            }

            //phew!
            return true;
        } else {
            return false;
        }
    }

    public FreerailsSerializable get(ITEM item, FreerailsPrincipal p) {
        return items[item.getKeyNumber()];
    }

    public void set(ITEM item, FreerailsSerializable element,
        FreerailsPrincipal p) {
        items[item.getKeyNumber()] = element;
    }

    /**
     * @param player Player to add
     * @param p principal who is adding
     * @return index of the player
     */
    private int addPlayer(Player player, FreerailsPrincipal p) {
	if (p.equals(Player.NOBODY)) {
	    // Player Nobody attempted to add a player
	    return -1;
	}

        lists[KEY.PLAYERS.getKeyNumber()].add(player);

        int index = size(KEY.PLAYERS, Player.NOBODY) - 1;

        for (int i = 0; i < KEY.getNumberOfKeys(); i++) {
            KEY key = KEY.getKey(i);

            if (key.shared != true) {
                if (key.usesObjectKey2) {
                    while (playerObjects[i].size() <= index) {
                        playerObjects[i].add(new HashMap());
                    }                    
                } else {
                    while (lists[i].size() <= index) {
                        lists[i].add(new ArrayList());
                    }
                }
            }
        }

        return index;
    }

    private static final int playerKey = KEY.PLAYERS.getKeyNumber();

    private int getPlayerIndex(FreerailsPrincipal p) {
        for (int i = lists[playerKey].size() - 1; i >= 0; --i) {
            if (p.equals(((Player)(lists[playerKey].get(i))).getPrincipal())) {
                return i;
            }
        }

        throw new ArrayIndexOutOfBoundsException("No matching principal for " +
            p.toString());
    }

    private synchronized void writeObject(ObjectOutputStream out) throws
	IOException {
	    out.defaultWriteObject();
	}

    private synchronized void readObject(ObjectInputStream in) throws
	IOException, ClassNotFoundException {
	    in.defaultReadObject();
	}

    public Iterator getIterator(KEY k) {
        return new WorldIteratorImpl
                (sharedObjects[k.getKeyNumber()].values().iterator());
    }

    public Iterator getIterator(KEY k, FreerailsPrincipal p) {
        if (k.shared)
            return getIterator(k);
        
        return new WorldIteratorImpl(((HashMap) playerObjects[k.getKeyNumber()]
                .get(getPlayerIndex(p))).values().iterator());
    }        
    
    public Iterator getObjectKey2Iterator(KEY k, FreerailsPrincipal p) {
        if (k.shared)
            return getObjectKey2Iterator(k);
        
        return new WorldObjectKey2Iterator(k, p,
                ((HashMap) playerObjects[k.getKeyNumber()]
                .get(getPlayerIndex(p))).keySet().iterator());
    }
    
    public Iterator getObjectKey2Iterator(KEY k) {
        return new WorldObjectKey2Iterator
                (k, null, sharedObjects[k.getKeyNumber()].keySet().iterator());        
    }
    
    /** Enforces read-only semantics on an iterator */
    private static class WorldIteratorImpl implements Iterator {
        private Iterator i;
        
        public WorldIteratorImpl(Iterator i) {
            this.i = i;
        }

        public boolean hasNext() {
            return i.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Object next() {
            return i.next();
        }                
    }
    
    private static class WorldObjectKey2Iterator extends WorldIteratorImpl {
        private final KEY key;
        
        private final FreerailsPrincipal principal;
        
        public WorldObjectKey2Iterator(KEY k, FreerailsPrincipal p, Iterator i) {
            super(i);
            key = k;
            if (p != null) {
                principal = p;
            } else {
                principal = Player.NOBODY;
            }
        }
        
        public Object next() {
            return new ObjectKey2(key, principal, 
                    ((UUID) super.next()));
        }
    }
}
