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
import org.railz.world.common.FreerailsSerializable;
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

    public WorldImpl(int mapWidth, int mapHeight) {
        this.setupMap(mapWidth, mapHeight);
        this.setupLists();
    }

    public WorldImpl() {
	this(0, 0);
    }
    
    private WorldImpl(WorldImpl wi, FreerailsPrincipal viewer) {
	setupLists();
	int pi = wi.getPlayerIndex(viewer);
	for (int i = 0; i < lists.length; i++) {
	    ArrayList al = wi.lists[i];
	    KEY k = KEY.getKey(i);
	    for (int j = 0; j < al.size(); j++) {
		if (k.isPrivate && pi != j) {
		    lists[i].add(null);
		} else {
		    lists[i].add(al.get(j));
		}
	    }
	}
	for (int i = 0; i < wi.items.length; i++)
	    items[i] = wi.items[i];
	map = wi.map;
    }

    synchronized ReadOnlyWorld getReadOnlyView (FreerailsPrincipal viewer) {
	return new WorldImpl(this, viewer);
    }

    public void setupMap(int mapWidth, int mapHeight) {
        map = new FreerailsTile[mapWidth][mapHeight];
    }

    private void setupLists() {
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new ArrayList();
        }
    }

    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
        if (key.shared) {
            return (FreerailsSerializable)lists[key.getKeyNumber()].get(index);
        }

        return (FreerailsSerializable)((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(
                p))).get(index);
    }

    public void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal p) {

        if (key.shared) {
            lists[key.getKeyNumber()].set(index, element);

            return;
        }

        ((ArrayList)lists[key.getKeyNumber()].get(getPlayerIndex(p))).set(index,
            element);
    }

    public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal p) {
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
        return index >= 0 && index < this.size(k, p);
    }

    public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal p) {
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

    public boolean equals(Object o) {
        if (o instanceof WorldImpl) {
            WorldImpl test = (WorldImpl)o;

            if (lists.length != test.lists.length) {
                return false;
            } else {
                for (int i = 0; i < lists.length; i++) {
                    if (!lists[i].equals(test.lists[i])) {
                        return false;
                    }
                }
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
                while (lists[i].size() <= index) {
                    lists[i].add(new ArrayList());
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
}
