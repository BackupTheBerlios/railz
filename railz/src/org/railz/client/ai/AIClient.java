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

import java.io.*;
import java.security.*;

import org.railz.client.common.*;
import org.railz.client.top.*;
import org.railz.controller.*;
import org.railz.util.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/** Represents an instance of an AI client */
public class AIClient extends Client implements Runnable,
    ConnectionAdapterListener {
    private AIConfiguration config;
    private ClientMoveExecuter clientMoveExecuter;
    private ConnectionAdapter connectionAdapter;
    private PlayerPrincipal playerPrincipal;
    private ReadOnlyWorld world;
    private GameModel gameModel;
    private volatile boolean keepRunning = true;
    private Scheduler scheduler;

    /** The default user message logger logs to stderr */
    private UserMessageLogger userMessageLogger = new UserMessageLogger() {
	public void println(String s) {
	    System.err.println(s + "\n");
	}
    };
    
    public AIClient(AIConfiguration aic) throws IOException,
    GeneralSecurityException {
	super(aic.getPlayer());
	config = aic;
	connectionAdapter = new ConnectionAdapter(aic.getPlayer(),
		FreerailsProgressMonitor.NULL_INSTANCE,
		userMessageLogger);
	connectionAdapter.addConnectionAdapterListener(this);
	setReceiver(connectionAdapter);
	connectionAdapter.setMoveReceiver(getMoveChainFork());
	try {
	    // causes the connectionAdapter to initialize the game and call
	    // our callbacks
	    connectionAdapter.setConnection(config.getConnection());
	} catch (GeneralSecurityException e) {
	    config.getConnection().close();
	}
    }

    /** Interval in ms during which the client should just sleep */
    private static final int SLEEP_TIME = 1000;

    /**
     * Implements the main loop during which the AI client computes and
     * submits its moves.
     */
    public void run() {
	scheduler = new Scheduler(this);

	while (keepRunning) {
	    // sleep for a bit to save on CPU
	    try {
		Thread.currentThread().sleep(SLEEP_TIME);
	    } catch (InterruptedException e) {
		// ignore
	    }

	    // update the game world with any moves we may have collected
	    // whilst we were asleep
	    gameModel.update();

	    // do our stuff
	    scheduler.scheduleTasks();
	}
    }

    public void worldInitialized(ReadOnlyWorld w, ClientMoveExecuter cme) {
	world = w;
	gameModel = cme;

	/*
	 * wait until the player the client represents has been created in
	 * the model (this may not occur until we process the move creating
	 * the player from the server
	 */
	while (!world.boundsContain (KEY.PLAYERS,
		   playerPrincipal.getId(), playerPrincipal)) {
	    gameModel.update();
	}

	scheduler = new Scheduler(this);

	/* start the client main loop */
	Thread t = new Thread(this, "AI Client named " + 
		config.getPlayer().getName());
	t.start();
    }

    public void worldDisconnected() {
	keepRunning = false;
    }

    public void setPlayerPrincipal(FreerailsPrincipal p) {
	playerPrincipal = (PlayerPrincipal) p;
    }

    public FreerailsPrincipal getPlayerPrincipal() {
	return playerPrincipal;
    }

    /** Overrides superclass to make it public */
    public MoveChainFork getMoveChainFork() {
	return super.getMoveChainFork();
    }

    public ReadOnlyWorld getWorld() {
	return world;
    }

    public UntriedMoveReceiver getUntriedMoveReceiver() {
	return connectionAdapter;
    }
}
