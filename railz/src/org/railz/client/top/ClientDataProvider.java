/*
 * Copyright (C) 2005 Robert Tuck
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
package org.railz.client.top;

import org.railz.controller.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Provides a central point for providing access to information about the
 * clients World Model
 */
public interface ClientDataProvider {
    /**
     * @return the principal that this client acts on behalf of.
     */
    public FreerailsPrincipal getPlayerPrincipal();

    /**
     * @return a MoveChainFork for subscribing to received moves.
     */
    public MoveChainFork getMoveChainFork();

    /**
     * @return an UntriedMoveReceiver to submit moves to.
     */
    public UntriedMoveReceiver getReceiver();

    /**
     * @return a read-only copy of the gameworld state.
     */
    public ReadOnlyWorld getWorld();
}
