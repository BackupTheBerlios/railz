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

package org.railz.client.top;

import java.awt.DisplayMode;
import java.io.IOException;
import java.net.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.*;
import javax.swing.JFrame;

import org.railz.client.common.ScreenHandler;
import org.railz.client.common.SynchronizedEventQueue;
import org.railz.client.common.UpdatedComponent;
import org.railz.client.view.GUIRoot;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.*;
import org.railz.controller.*;
import org.railz.util.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * This class implements a GUI-driven client to be used by human players.
 *
 * XXX How should the server be controlled from the client? (loading, saving of
 * maps etc?). Currently we will do this over the local connection only, by
 * the client having access to a ServerControlInterface object
 */
public class GUIClient extends Client implements ConnectionAdapterListener {
    private String title;
    private ModelRoot modelRoot;
    private GUIRoot guiRoot;
    private ConnectionToServer connection;
    private FreerailsProgressMonitor progressMonitor;
    private ConnectionAdapter connectionAdapter;

    private GUIClient(ConnectionToServer server, int mode, DisplayMode dm,
        String title, FreerailsProgressMonitor pm, Player player, ModelRoot mr)
        throws IOException, GeneralSecurityException {
        super(player);
	connection = server;
	progressMonitor = pm;
        modelRoot = mr;
        this.title = title;
	
        modelRoot.setMoveFork(getMoveChainFork());

	/* create the GUIRoot */
	guiRoot = new
	    GUIRoot(modelRoot);

	connectionAdapter = new ConnectionAdapter(player, pm,
		modelRoot.getUserMessageLogger());
	connectionAdapter.addConnectionAdapterListener(this);
	setReceiver(connectionAdapter);
        modelRoot.setMoveReceiver(getReceiver());
        getReceiver().setMoveReceiver(getMoveChainFork());

        //We want to setup the screen handler before creating the view lists
        //since the ViewListsImpl creates images that are compatible with
        //the current display settings and the screen handler may change the
        //display settings.
	guiRoot.setScreenHandler(new
		ScreenHandler(guiRoot.getClientJFrame(),
		    (UpdatedComponent) guiRoot.getClientJFrame(),
		    mode, dm));

        try {
            /* this causes the world to be loaded and the ViewLists to be
             * initialised */
            getReceiver().setConnection(server);
        } catch (GeneralSecurityException e) {
            server.close();
            throw e;
        }
    }

    /**
     * Start a client with an internet connection to a server
     */
    public GUIClient(InetSocketAddress server, int mode, DisplayMode dm,
        String title, FreerailsProgressMonitor pm, Player player)
        throws IOException, GeneralSecurityException {
        this(new InetConnection(server), mode, dm, title, pm, player,
            new ModelRoot());
    }

    /**
     * sets up a connnection with a local server. Currently this is the only
     * form of connection supported.
     * @throws java.io.IOException if the connection could not be opened
     */
    public GUIClient(ServerControlInterface controls, LocalConnection server,
        int mode, DisplayMode dm, String title, FreerailsProgressMonitor pm,
        Player player) throws IOException, GeneralSecurityException {
        this((ConnectionToServer)new LocalConnection(server), mode, dm, title,
            pm, player, new ModelRoot());
        modelRoot.setServerControls(controls);
    }

    public String getTitle() {
        return title;
    }

    public GUIRoot getGUIRoot() {
	return guiRoot;
    }

    /**
     * The GameLoop providing the move execution thread for the
     * ConnectionAdapter's Move Executer
     */
    private GameLoop gameLoop;

    public void worldInitialized(ReadOnlyWorld world, ClientMoveExecuter
	    moveExecuter) {
        SynchronizedEventQueue.use(world);

	if (moveExecuter instanceof NonAuthoritativeMoveExecuter) {
	    ((NonAuthoritativeMoveExecuter) moveExecuter)
		.setModelRoot(modelRoot);
	}

        /* start a new game loop */
	gameLoop = new GameLoop(guiRoot.getScreenHandler(),
		moveExecuter);

	/* send a command to set up server-specific resources */
	connection.sendCommand(new ResourceBundleManager.GetResourceCommand
		("org.railz.data.l10n.server", Locale.getDefault()));

        try {
            /* create the models */
            assert world != null;

            modelRoot.setWorld(world);
	    ViewLists viewLists = new ViewListsImpl(modelRoot,
		    guiRoot, progressMonitor);

            if (!viewLists.validate(world)) {
		/* most likely reason for failure is that the server's object
		 * set is different to what the client is expecting */
                modelRoot.getUserMessageLogger().println
		    (Resources.get("Your client is not compatible with " +
				   "the server."));
            }

            /*
             * wait until the player the client represents has been created in
             * the model (this may not occur until we process the move creating
             * the player from the server
             */
            while (!world.boundsContain (KEY.PLAYERS,
		       	((PlayerPrincipal) modelRoot.getPlayerPrincipal())
			.getId(), modelRoot.getPlayerPrincipal())) {
                moveExecuter.update();
            }

            modelRoot.setWorld(connectionAdapter, viewLists);

            /* start the game loop */
            String threadName = "Railz client: " + getTitle();
            Thread t = new Thread(gameLoop, threadName);
            t.start();
        } catch (IOException e) {
	    String s = Resources.get
		("There was a problem reading in the graphics "
			       + "data");
            modelRoot.getUserMessageLogger().println
		(s);
	    Logger.getLogger("global").log(Level.WARNING, s, e);
        }
    }

    public void worldDisconnected() {
        if (gameLoop != null) {
            gameLoop.stop();
	    SynchronizedEventQueue.stopUse();
	    gameLoop = null;
        }
    }

    public void setPlayerPrincipal(FreerailsPrincipal p) {
	modelRoot.setPlayerPrincipal(p);
    }
}
