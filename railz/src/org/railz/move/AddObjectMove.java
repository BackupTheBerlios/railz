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
 * Supersedes AddItemToListMove
 * @author bob
 */
class AddObjectMove implements ObjectMove {
    
    private final ObjectKey2 objectKey;
    private final WorldObject item;    

    public FreerailsPrincipal getPrincipal() {
	return objectKey.principal;
    }

    protected AddObjectMove(ObjectKey2 key, WorldObject item) {
        objectKey = key;
        this.item = item;	
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (! objectKey.principal.equals(Player.NOBODY) &&                 
                (! p.equals(Player.AUTHORITATIVE) 
            && ! p.equals(objectKey.principal))) {
            return MoveStatus.moveFailed("No permissions");
        }
        if (w.contains(objectKey)) {        
            return MoveStatus.moveFailed("Object " + item + " already exists.");
	}

        return MoveStatus.MOVE_OK;
    }
	
    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (! objectKey.principal.equals(Player.NOBODY) &&
                (! p.equals(Player.AUTHORITATIVE) 
            && ! p.equals(objectKey.principal))) {
            return MoveStatus.moveFailed("No permissions");
        }
        if (! item.equals(w.get(objectKey))) {        
            return MoveStatus.moveFailed("Object " + item + " does not exist.");
	}
        
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
	    w.set(objectKey, item);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
	    w.remove(objectKey);
        }

        return ms;
    }

    public boolean equals(Object o) {
        if (o instanceof AddObjectMove) {
            AddObjectMove test = (AddObjectMove) o;

            if (!this.item.equals(test.getAfter())) {
                return false;
            }

            if (! objectKey.equals(test.objectKey)) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public WorldObject getBefore() {
        return null;
    }

    public WorldObject getAfter() {
        return item;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append("\nlist=");
        sb.append(objectKey.toString());
        sb.append("\n item =");
        sb.append(this.item.toString());

        return sb.toString();
    }

    public ObjectKey2 getKeyBefore() {
        return null;
    }

    public ObjectKey2 getKeyAfter() {
        return objectKey;
    }    

    public int hashCode() {
        return objectKey.hashCode();
    }
}
