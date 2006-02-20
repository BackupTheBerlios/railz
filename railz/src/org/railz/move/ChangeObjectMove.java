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
import org.railz.world.player.Player;
import org.railz.world.top.ObjectKey2;
import org.railz.world.top.World;

/**
 * 
 */
class ChangeObjectMove implements ObjectMove {
    private ObjectKey2 objectKey;
    private WorldObject oldValue;
    private WorldObject newValue;
    
    /** Creates a new instance of ChangeObjectMove */
    public ChangeObjectMove(ObjectKey2 key, WorldObject oldValue,
            WorldObject newValue) {
        objectKey = key;
        this.oldValue = oldValue;        
        this.newValue = newValue;        
    }

    public FreerailsPrincipal getPrincipal() {
        return objectKey.principal;
    }

    public ObjectKey2 getKeyBefore() {
        return objectKey;
    }

    public ObjectKey2 getKeyAfter() {
        return objectKey;
    }

    public WorldObject getBefore() {
        return oldValue;
    }

    public WorldObject getAfter() {
        return newValue;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ChangeObjectMove) {
            ChangeObjectMove m = (ChangeObjectMove) obj;
            return objectKey.equals(m.objectKey) &&
                    oldValue.equals(m.oldValue) &&
                    newValue.equals(m.newValue);            
        }
        return false;
    }
    
    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);
        if (ms.isOk()) {
            w.set(objectKey, newValue);
            return MoveStatus.MOVE_OK;
        }
        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);
        if (ms.isOk()) {
            w.set(objectKey, oldValue);
            return MoveStatus.MOVE_OK;
        }
        return ms;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(newValue, w, p);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(oldValue, w, p);
    }

    public int hashCode() {
        return objectKey.hashCode();
    }
    
    private MoveStatus tryMove(WorldObject fromValue,
            World w, FreerailsPrincipal p) {
        WorldObject current = w.get(objectKey);
        if (!fromValue.equals(current)) {
            return MoveStatus.moveFailed("Object initial state not equal:" + objectKey);
        }

        if (! objectKey.principal.equals(Player.NOBODY) &&
                (! objectKey.principal.equals(p)) && (! p.equals(Player.AUTHORITATIVE)))
            return MoveStatus.moveFailed("No permissions to change object");
        
        return MoveStatus.MOVE_OK;
    }
    
    public String toString() {
        return getClass().getName() + ": key=" + objectKey +
                ", before=" + oldValue + ", after=" + newValue;
    }
}
