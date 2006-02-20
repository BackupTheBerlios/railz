/*
 * Copyright (C) 2006 Robert Tuck
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

import org.railz.world.common.WorldObject;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.ObjectKey2;

/**
 * This interface supersedes {@link ListMove}
 * @author bob
 */
public interface ObjectMove extends Move {
    /** @return null if the object was added */
    public ObjectKey2 getKeyBefore();
    
    /** @return null if the object was removed */
    public ObjectKey2 getKeyAfter();

    /** @return null if the object was added */    
    public WorldObject getBefore();
    
    /** @return null if the object was removed */
    public WorldObject getAfter();
    
    /** @return the principal on whose behalf this move is being executed */
    public FreerailsPrincipal getPrincipal();
}
