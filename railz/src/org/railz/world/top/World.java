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

import org.railz.world.common.WorldObject;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.track.FreerailsTile;


/**
 * <p>This class implements methods which can be used to alter the
 * world. Notice that incontrast to, say, <CODE>java.util.List</CODE> there is
 * no remove() method that shifts any subsequent elements to the left (subtracts
 * one from their indices).
 * This means that an elements' position in a list can be used as an address
 * space independent way to reference the element.  If you want to remove an
 * element from a list, you should set it to null, e.g. <br>
 * <CODE>world.set(KEY.TRAINS, 5, null);</CODE><br>
 * Code that loops through lists should handle null values gracefully</p>
 */
public interface World extends ReadOnlyWorld {
    /**
     * Replaces the element mapped to the specified item with the specified
     * element.
     */
    void set(ITEM item, FreerailsSerializable element,
        FreerailsPrincipal principal);

    /**
     * @deprecated in favour of {@link #set(ObjectKey2 key, FreerailsSerializable object)}
     * Replaces the element at the specified position in the specified list
     * with the specified element.
     */
    void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal principal);

    /**
     * Adds or replaces the object in the game world
     */
    void set(ObjectKey2 key, WorldObject object);
    
    /**
     * @deprecated in favour of {@link #set(ObjectKey2 key, FreerailsSerializable object)}
     * Appends the specified element to the end of the specifed list and
     * returns the index that can be used to retrieve it.
     */
    int add(KEY key, FreerailsSerializable element, FreerailsPrincipal principal);

    /**
     * @deprecated in favour of {@link #remove(ObjectKey2 key)}
     * Removes the last element from the specified list.
     */
    FreerailsSerializable removeLast(KEY key, FreerailsPrincipal principal);

    /*
     * Removes the object from the game world
     */
    WorldObject remove(ObjectKey2 key);
    
    /**
     * Replaces the tile at the specified position on the map with the
     * specified tile.
     */
    void setTile(int x, int y, FreerailsTile tile);
}
