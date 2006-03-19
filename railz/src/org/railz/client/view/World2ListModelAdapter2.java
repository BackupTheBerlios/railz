/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 23-Mar-2003
 * 
 */
package org.railz.client.view;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.util.*;
import org.railz.world.common.WorldObject;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.*;

/**
 * This class implements the GoF Adapter pattern.  It converts the
 * interface of a list on the World to a ListModel interface that can
 * be used by JLists.  Currently, change notification is <b>not</b> implemented.
 * @author Luke
 * 
 */
public class World2ListModelAdapter2 implements ListModel {
	
	private final KEY k;
	
	private final ReadOnlyWorld w;

	private final FreerailsPrincipal principal;
	
	private final WeakRefList listeners = new WeakRefList();
        
        private HashMap keys2Indices = new HashMap();
        
        private ArrayList indices2Keys = new ArrayList();

        private MoveChainFork moveChainFork;
        
        public ObjectKey2 getKey(int index) {
            return (ObjectKey2) indices2Keys.get(index);
        }
        
        /** 
         * @return the index at which the specified key may be found,
         * otherwise -1 if not found
         */
        public int getIndex(ObjectKey2 key) {
            Integer i = (Integer) keys2Indices.get(key);
            if (i == null)
                return -1;

            return i.intValue();
        }
        
	private WorldListListener worldListener = new WorldListListener() {
	    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
                // do nothing
	    }

	    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
                // do nothing
	    }

	    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
                // do nothing
	    }

            public void listUpdated(ObjectKey2 key) {
		if ((! key.key.equals(k)) || (! key.principal.equals(principal)))
		    return;

		Enumeration en = listeners.elements();
		ListDataEvent lde = new
		    ListDataEvent(World2ListModelAdapter2.this,
			    ListDataEvent.CONTENTS_CHANGED, getIndex(key), 
                        getIndex(key));
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .contentsChanged(lde);
		    }
		}
            }

            public void itemRemoved(ObjectKey2 key) {		    
		if ((! key.key.equals(k)) || (! key.principal.equals(principal)))
		    return;

                indices2Keys.remove(keys2Indices.remove(key));                
                
		Enumeration en = listeners.elements();
		ListDataEvent lde = new
		    ListDataEvent(World2ListModelAdapter2.this,
			    ListDataEvent.INTERVAL_REMOVED, getIndex(key),
                        getIndex(key));
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .intervalRemoved(lde);
		    }
		}
            }

            public void itemAdded(ObjectKey2 key) {
		if ((! key.key.equals(k)) || (! key.principal.equals(principal)))
		    return;

                keys2Indices.put(key, new Integer(indices2Keys.size()));
                indices2Keys.add(key);
                
		Enumeration en = listeners.elements();
		ListDataEvent lde = new 
		    ListDataEvent(World2ListModelAdapter2.this,
			    ListDataEvent.INTERVAL_ADDED, getIndex(key), getIndex(key));
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .intervalAdded(lde);
		    }
		}
            }
	};

	public World2ListModelAdapter2(ReadOnlyWorld world, KEY key,
		FreerailsPrincipal p){
	    this(world, key, p, null);
	}

	public World2ListModelAdapter2(ReadOnlyWorld world, KEY key,
		FreerailsPrincipal p, MoveChainFork mcf){
	    this.k = key;
	    this.w = world;
	    principal = p;
            moveChainFork = mcf;
            Iterator i = world.getIterator(key, p);
            while (i.hasNext()) {
                WorldObject o = (WorldObject) i.next();
                ObjectKey2 ok = new ObjectKey2(key, p, o.getUUID());
                keys2Indices.put(ok, new Integer(indices2Keys.size()));
                indices2Keys.add(ok);
            }
	    if (mcf != null)
		mcf.addListListener(worldListener);
	}

	public int getSize() {
		return indices2Keys.size();
	}

	public Object getElementAt(int i) {
		return w.get((ObjectKey2) indices2Keys.get(i));
	}

	public void addListDataListener(ListDataListener l) {
	    System.out.println("listener added");
	    synchronized (listeners) {
		listeners.add(l);
	    }
	}

	public void removeListDataListener(ListDataListener l) {
	    synchronized (listeners) {
		listeners.remove(l);
	    }
	}
        
        public void dispose() {
            moveChainFork.removeListListener(worldListener);
        }
}
