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

import java.util.*;
import java.util.logging.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.railz.client.common.*;
import org.railz.controller.*;
import org.railz.move.Move;
import org.railz.move.MoveStatus;
import org.railz.move.TimeTickMove;
import org.railz.util.*;
import org.railz.world.player.Player;
import org.railz.world.player.PlayerPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.World;

/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 */
public class ConnectionAdapter implements UntriedMoveReceiver,
    ConnectionListener {
    private ClientMoveExecuter moveExecuter;
    private Player player;
    ConnectionToServer connection;
    private Object authMutex = new Integer(1);
    private boolean authenticated;
    private static final Logger logger = Logger.getLogger("global");
    private UserMessageLogger userMessageLogger;

    /**
     * we forward outbound moves from the client to this via the
     * ClientMoveExecuter.
     */
    UncommittedMoveReceiver uncommittedReceiver;

    /**
     * Moves from the server are forwarded to this via the ClientMoveExecuter.
     */
    MoveReceiver moveReceiver;
    World world;
    private FreerailsProgressMonitor progressMonitor;

    public int getNumBlockedMoves() {
	return moveExecuter.getNumBlockedMoves();
    }

    public ConnectionAdapter(Player
	    player, FreerailsProgressMonitor pm,
	    UserMessageLogger uml) {
        this.player = player;
        this.progressMonitor = pm;
	userMessageLogger = uml;
    }

    /**
     * This class receives moves from the connection and passes them on to a
     * MoveReceiver.
     */
    public class WorldUpdater implements SourcedMoveReceiver {
        private MoveReceiver moveReceiver;

        /**
         * TODO get rid of this
         */
        public void undoLastMove() {
            // do nothing
        }

        public void processMove(Move move, ConnectionToServer c) {
            processMove(move);
        }

        /**
         * Processes inbound moves from the server
         */
        public void processMove(Move move) {
	    MoveReceiver mr;
	    synchronized (this) {
		mr = moveReceiver;
	    }

	    if (mr == null)
		return;

            if (move instanceof TimeTickMove) {
                /*
                 * flush our outgoing moves prior to receiving next tick
                 * TODO improve our buffering strategy
                 */
                connection.flush();
            }

            mr.processMove(move);
        }

        public synchronized void setMoveReceiver(MoveReceiver moveReceiver) {
            this.moveReceiver = moveReceiver;
        }
    }

    private WorldUpdater worldUpdater = new WorldUpdater();

    /**
     * Processes outbound moves to the server
     */
    public synchronized void processMove(Move move) {
	logger.log(Level.FINEST, "sending move to server");
        if (uncommittedReceiver != null) {
            uncommittedReceiver.processMove(move);
        }
    }

    public synchronized void undoLastMove() {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.undoLastMove();
        }
    }

    public synchronized MoveStatus tryDoMove(Move move) {
	return move.tryDoMove(world, move.getPrincipal());
    }

    public synchronized MoveStatus tryUndoMove(Move move) {
	return move.tryUndoMove(world, move.getPrincipal());
    }

    private void closeConnection() {
        connection.close();
        connection.removeMoveReceiver(worldUpdater);
        userMessageLogger.println
	    (Resources.get("Connection to server closed"));
    }

    public synchronized void setConnection(ConnectionToServer c)
        throws IOException, GeneralSecurityException {
        setConnectionImpl(c);
    }

    /**
     * This function may be entered from either the AWT event handler thread
     * (via a local connection when the user clicks on something), or from the
     * network connection thread, or from the initialisation thread of the
     * launcher.
     */
    private synchronized void setConnectionImpl(ConnectionToServer c)
        throws IOException, GeneralSecurityException {
        if (connection != null) {
	    connectionAdapterListener.worldDisconnected();
            closeConnection();
            connection.removeMoveReceiver(worldUpdater);
            connection.removeConnectionListener(this);
        }

	connection = c;
	connection.open();

	connection.addMoveReceiver(worldUpdater);
	connection.addConnectionListener(this);

	/* attempt to authenticate the player */
	userMessageLogger.println
	    (Resources.get("Attempting to authenticate player: ") +
	     player.getName());
	authenticated = false;
	connection.sendCommand(new AddPlayerCommand(player, player.sign()));
	synchronized (authMutex) {
	    if (!authenticated) {
		userMessageLogger.println
		    (Resources.get("Waiting for authentication"));

		try {
		    authMutex.wait();
		} catch (InterruptedException e) {
		    //ignore
		}

		if (!authenticated) {
		    throw new GeneralSecurityException("Server rejected " +
			    "attempt to authenticate");
		}
	    }
	} // synchronized (authMutex)
	    
        /* grab the lock on the WorldUpdater in order to prevent any moves from
         * the server being lost whilst we plumb it in */
	synchronized (worldUpdater) {
            world = connection.loadWorldFromServer();

            /* plumb in a new Move Executer */
	    if (connection instanceof LocalConnection) {
		moveExecuter = new DummyMoveExecuter(moveReceiver, world);
	    } else {
		moveExecuter = new NonAuthoritativeMoveExecuter(world,
			moveReceiver, userMessageLogger);
	    }
            worldUpdater.setMoveReceiver(moveExecuter);
            uncommittedReceiver = moveExecuter.getUncommittedMoveReceiver();
            moveExecuter.addMoveReceiver(connection);
        }

	connectionAdapterListener.worldInitialized(world, moveExecuter);
    }

    public void setMoveReceiver(MoveReceiver m) {
        //moveReceiver = new CompositeMoveSplitter(m);
        //I don't want moves split at this stage since I want to be able
        //to listen for composite moves.
        moveReceiver = m;
    }

    public void connectionClosed(ConnectionToServer c) {
	connectionAdapterListener.worldDisconnected();
    }

    public void connectionOpened(ConnectionToServer c) {
	// ignore
    }

    public void connectionStateChanged(ConnectionToServer c) {
        // ignore
    }

    public void processServerCommand(ConnectionToServer c, ServerCommand s) {
        if (s instanceof AddPlayerResponseCommand) {
            synchronized (authMutex) {
                authenticated = !((AddPlayerResponseCommand)s).isRejected();

                if (authenticated) {
                    userMessageLogger.println
			(Resources.get
			 ("Player was successfully authenticated"));
                    connectionAdapterListener.setPlayerPrincipal
			(((AddPlayerResponseCommand)s).getPrincipal());
                } else {
                    userMessageLogger.println
			(Resources.get("Authentication was rejected"));
                }

                authMutex.notify();
            }
        } else if (s instanceof WorldChangedCommand) {
		Runnable r = new ConnectionHelper(c);
		(new Thread(r)).start();
        } else if (s instanceof ServerMessageCommand) {
	    userMessageLogger.println
		(Resources.get(((ServerMessageCommand) s).getMessage()));
	} else if (s instanceof
		ResourceBundleManager.GetResourceResponseCommand) {
		ResourceBundleManager.GetResourceResponseCommand response =
		    (ResourceBundleManager.GetResourceResponseCommand) s;
		if (response.isSuccessful()) {
		    Resources.setExternalResourceBundle
			(response.getResourceBundle());
		} else {
		    logger.log(Level.WARNING,
			    "Couldn't get resource bundle from " +
			    "server");
		}
	}
    }

    private class ConnectionHelper implements Runnable {
	ConnectionToServer c;

	public ConnectionHelper(ConnectionToServer c) {
	    this.c = c;
	}

	public void run() {
	    try {
		setConnectionImpl(c);
	    } catch (IOException e) {
		userMessageLogger.println
		    (Resources.get("Unable to open remote connection"));
		closeConnection();
	    } catch (GeneralSecurityException e) {
		userMessageLogger.println
		    (Resources.get("Unable to authenticate with server: ")
		     + e.toString());
	    }
	}
    }

    private ConnectionAdapterListener connectionAdapterListener;

    public void addConnectionAdapterListener(ConnectionAdapterListener l) {
	connectionAdapterListener = l;
    }

    public void removeConnectionAdapterListener(ConnectionAdapterListener l) {
	connectionAdapterListener = null;
    }
    }
