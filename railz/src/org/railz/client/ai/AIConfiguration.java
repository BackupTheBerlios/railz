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
package org.railz.client.ai;

import org.railz.controller.*;
import org.railz.world.player.*;

/**
 * This class specifies any configuration options the AI client is passed at
 * startup. (e.g. difficulty level, player name, etc.)
 */
public final class AIConfiguration {
    /** The player this acts on behalf of */
    private Player player;

    private ConnectionToServer serverConnection;

    public AIConfiguration(ConnectionToServer c, Player p) {
	player = p;
	serverConnection = c;
    }

    public Player getPlayer() {
	return player;
    }

    public ConnectionToServer getConnection() {
	return serverConnection;
    }
}
