/*
 * Copyright (C) Luke Lindsay
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

package org.railz.move;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.top.World;
import org.railz.world.player.FreerailsPrincipal;

/**
 * All moves should implement this interface and obey the contract
 * described below.
 * <p>(1) They should be immutable.</P>
 * <p>(2) They should overide <code>Object.equals()</code> to test
 * for logical equality.</P>
 * <p>(3) They should store 'before' and 'after' values for
 * all properties of the world object that the move changes.
 * <p>(4) The changes they encapsulate are stored in an address space
 * independent way, so that a move generated on a client can be serialised,
 * sent over a network, and then deserialised and executed on a server. To
 * achieve this, they refer to items in the game world via either their
 *  coorinates, e.g. tile 10,50, or their position in a list, e.g. train #4.</p>
 * <p>(5) They are undoable. To achieve this, they need to store the information
 * necessary to undo the change. E.g. a change-terrain-type move might store the
 * tile coorindates, the terrain type before the change and the terrain type
 *  after the change.</p>
 *<p>(6) The tryDoMove and tryUndoMove methods test whether the move is valid
 * but leave the gameworld unchanged</p>
 *
 *
 * @author lindsal
 */
public interface Move extends FreerailsSerializable {
    /**
     * @return The principal that owns the objects this move affects. This
     * information may be used by clients when executing moves submitted by
     * other players.
     */
    FreerailsPrincipal getPrincipal();

    /** Tests whether this Move can be executed on
     * the specifed world object, this method should
     * leave the world object unchanged.
     */
    MoveStatus tryDoMove(World w, FreerailsPrincipal p);

    /** Tests whether this Move can be undone on
     * the specifed world object, this method should
     * leave the world object unchanged.
     */
    MoveStatus tryUndoMove(World w, FreerailsPrincipal p);

    /** Executes this move on the specifed world object.
     */
    MoveStatus doMove(World w, FreerailsPrincipal p);

    /** If <code>doMove</code> has just been executed on the
     * specified world object, calling this method changes the
     * state of the world object back to how it was before <code>doMove</code>
     * was called.
     */
    MoveStatus undoMove(World w, FreerailsPrincipal p);
}
