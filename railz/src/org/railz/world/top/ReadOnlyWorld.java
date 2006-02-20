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
import java.util.Iterator;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.WorldObject;
import org.railz.world.track.FreerailsTile;
import org.railz.world.player.FreerailsPrincipal;


/** <p>This interface defines a unified set of methods to access the elements
 * that make up the game world.  One can think of it as a specific-purpose
 * Collection.  Game world elements are either placed on a 2D grid - the map -
 * or in one of a number of lists.  The lists are accessed using keys defined in
 * the class {@link KEY}.</p>
 *
 * <p>Example: the following code gets train #5.<br>
 * <CODE> TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5);</CODE></p>
 *
 * <p>The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added.  Instead one can just add a new entry to the class KEY.</p>
 *
 * Code that loops through lists should handle null values gracefully</p>
 *
 * TODO prune this interface...
 *
 */
public interface ReadOnlyWorld extends FreerailsSerializable {
    /**
     * Returns the element mapped to the specified item.
     */
    FreerailsSerializable get(ITEM item, FreerailsPrincipal p);

    /**
     * @deprecated in favour of {@link #get(ObjectKey2)}
     * Returns the element at the specified position in the specified list.
     */
    FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p);

    WorldObject get(ObjectKey2 key);
    
    /**
     * Returns the number of elements in the specified list.
     */
    int size(KEY key, FreerailsPrincipal p);

    /** Returns the width of the map in tiles.
     */
    int getMapWidth();

    /** Returns the height of the map in tiles.
     */
    int getMapHeight();

    /**
     * Returns the tile at the specified position on the map.
     */
    FreerailsTile getTile(int x, int y);

    FreerailsTile getTile(Point p);

    boolean boundsContain(int x, int y);

    /**
     * @deprecated in favour of {@link #contains(ObjectKey2)}
     */
    boolean boundsContain(KEY k, int index, FreerailsPrincipal p);
    
    boolean contains(ObjectKey2 object);
    
    /** @return a read-only iterator (no remove) */
    Iterator getIterator(KEY k, FreerailsPrincipal p);

    /** @return a read-only iterator (no remove) */
    Iterator getIterator(KEY k);
}
