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
import org.railz.util.*;
/**
 * This class is a base class from which MoveExecuters which handle the
 * coordination of moves sent by the client and received from the server are
 * subclassed.
 */
public abstract class ClientMoveExecuter implements MoveReceiver, GameModel {
    /**
     * @return an UncommittedMoveReceiver implemented by this
     * ClientMoveExecuter which can be used by client components to submit moves
     * to the server.
     */
    public abstract UncommittedMoveReceiver getUncommittedMoveReceiver();

    /**
     * @return the number of moves which have been sent by the server but not
     * committed to this clients world copy, due to an incorrect move being
     * precommitted by the ClientMoveExecuter.
     */
    public abstract int getNumBlockedMoves();

    /**
     * Set the UncommittedMoveReceiver to which this ClientMoveExecuter
     * forwards moves.
     */
    public abstract void addMoveReceiver(UncommittedMoveReceiver mr);
}
