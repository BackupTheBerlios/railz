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

package org.railz.controller;

import java.util.ArrayList;
import org.railz.move.AddItemToListMove;
import org.railz.move.ChangeItemInListMove;
import org.railz.move.ObjectMove;
import org.railz.move.RemoveItemFromListMove;
import org.railz.move.CompositeMove;
import org.railz.move.Move;
import org.railz.move.UndoneMove;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.ObjectKey2;
import org.railz.world.top.WorldListListener;


/**
 * @version         1.0
 *
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
final public class MoveChainFork implements MoveReceiver {
    private final ArrayList moveReceivers = new ArrayList();
    private final ArrayList splitMoveReceivers = new ArrayList();
    private final ArrayList listListeners = new ArrayList();

    public MoveChainFork() {
        // do nothing
    }

    public void remove(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.remove(moveReceiver);
    }

    public void add(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.add(moveReceiver);
    }

    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        splitMoveReceivers.add(moveReceiver);
    }

    public void removeSplitMoveReceiver(MoveReceiver moveReceiver) {
	splitMoveReceivers.remove(moveReceiver);
    }

    public void addListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.add(listener);
    }

    public void removeListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.remove(listener);
    }

    /*
     * @see MoveReceiver#processMove(Move)
     */
    public void processMove(Move move) {
        for (int i = 0; i < moveReceivers.size(); i++) {
            MoveReceiver m = (MoveReceiver)moveReceivers.get(i);
            m.processMove(move);
        }

        splitMove(move);
    }

    private void splitMove(Move move) {
        if (move instanceof CompositeMove) {
            Move[] moves = ((CompositeMove)move).getMoves();

            for (int i = 0; i < moves.length; i++) {
                splitMove(moves[i]);
            }
        } else {
            for (int i = 0; i < splitMoveReceivers.size(); i++) {
                MoveReceiver m = (MoveReceiver)splitMoveReceivers.get(i);
                m.processMove(move);
            }

            if (move instanceof AddItemToListMove) {
                AddItemToListMove mm = (AddItemToListMove)move;
                sendItemAdded(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ChangeItemInListMove) {
                ChangeItemInListMove mm = (ChangeItemInListMove)move;
                sendListUpdated(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof RemoveItemFromListMove) {
                RemoveItemFromListMove mm = (RemoveItemFromListMove)move;
                sendItemRemoved(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ObjectMove) {
                ObjectMove om = (ObjectMove) move;
                if (om.getBefore() == null) {
                    sendObjectAdded(om.getKeyAfter());
                } else if (om.getAfter() == null) {
                    sendObjectRemoved(om.getKeyBefore());
                } else {
                    sendObjectChanged(om.getKeyBefore());
                }
            } else if (move instanceof UndoneMove) {            
                Move m = ((UndoneMove)move).getUndoneMove();

                if (m instanceof AddItemToListMove) {
                    AddItemToListMove mm = (AddItemToListMove)m;
		    sendItemRemoved(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                } else if (m instanceof RemoveItemFromListMove) {
                    RemoveItemFromListMove mm = (RemoveItemFromListMove)m;
		    sendItemAdded(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                } else if (move instanceof ChangeItemInListMove) {
                    ChangeItemInListMove mm = (ChangeItemInListMove)move;
		    sendListUpdated(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                }
            }
        }
    }

    private void sendObjectAdded(ObjectKey2 key) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemAdded(key);
        }        
    }
    
    private void sendObjectRemoved(ObjectKey2 key) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemRemoved(key);
        }
        
    }
    
    private void sendObjectChanged(ObjectKey2 key) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.listUpdated(key);
        }        
    }
    
    private void sendItemAdded(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemAdded(key, index, p);
        }
    }

    private void sendItemRemoved(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemRemoved(key, index, p);
        }
    }

    private void sendListUpdated(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.listUpdated(key, index, p);
        }
    }
}
