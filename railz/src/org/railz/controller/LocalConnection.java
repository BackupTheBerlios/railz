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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.railz.move.Move;
import org.railz.world.top.World;


/**
 * This class implements a local connection to a server contained within the
 * same JVM.
 */
public class LocalConnection implements ConnectionToServer {
    private SourcedMoveReceiver moveReceiver;
    private LocalConnection peer;
    private World world;
    private ConnectionListener connectionListener;
    private ConnectionState state = ConnectionState.CLOSED;

    /**
     * Indicates whether the connection should forward moves the the remote side
     */
    private boolean sendMoves = false;

    public void flush() {
        // do nothing
    }

    public void addConnectionListener(ConnectionListener l) {
        connectionListener = l;
    }

    public void removeConnectionListener(ConnectionListener l) {
        connectionListener = null;
    }

    public void addMoveReceiver(SourcedMoveReceiver m) {
        moveReceiver = m;
    }

    public void removeMoveReceiver(SourcedMoveReceiver m) {
        moveReceiver = null;
    }

    public void processMove(Move move) {
        if (sendMoves) {
            Move m = (Move)defensiveCopy(move);
            peer.sendMove(m);
        }
    }

    /**
     * TODO get rid of this
     */
    public void undoLastMove() {
        if (sendMoves) {
            peer.sendUndoLastMove();
        }
    }

    /**
     * This constructor is called by the server
     */
    public LocalConnection() {
        setState(ConnectionState.WAITING);
    }

    /**
     * This constructor is called by the client
     */
    public LocalConnection(LocalConnection peer) {
        this.peer = peer;
    }

    protected void sendMove(Move move) {
        if (moveReceiver != null) {
            moveReceiver.processMove(move, this);
        }
    }

    /**
     * TODO get rid of this
     */
    protected void sendUndoLastMove() {
        moveReceiver.undoLastMove();
    }

    /**
     * This is called by the client connection object on the servers connection
     * object
     */
    protected boolean connect(LocalConnection peer) {
        this.peer = peer;
        sendMoves = true;

        return true;
    }

    /**
     * This is called by the client connection object on the servers connection
     * object
     */
    protected void disconnect() {
        sendMoves = false;
        this.peer = null;

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    private Serializable defensiveCopy(Serializable s) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(s);
            objectOut.flush();

            byte[] bytes = out.toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Object o = objectIn.readObject();

            return (Serializable)o;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    public World loadWorldFromServer() {
        sendMoves = true;

        /* set the state on the server connection to say that the client is
         * ready to receive moves */
        setState(ConnectionState.READY);
        peer.setState(ConnectionState.READY);

        assert peer.world != null;

        /* return the same copy of the world */
	return peer.world;
    }

    public void open() {
        peer.connect(this);
        setState(ConnectionState.WAITING);
        peer.setState(ConnectionState.WAITING);
    }

    public void close() {
        sendMoves = false;
        world = null;
        peer.disconnect();
        setState(ConnectionState.CLOSED);

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    /**
     * Called by the server
     */
    public void setWorld(World w) {
        world = w;
    }

    public ConnectionState getConnectionState() {
        return state;
    }

    private void setState(ConnectionState s) {
        state = s;

        if (connectionListener != null) {
            connectionListener.connectionStateChanged(this);
        }
    }

    private void sendServerCommand(ServerCommand s) {
        if (connectionListener != null) {
            System.out.println("Sending " + s);
            connectionListener.processServerCommand(this, s);
        }
    }

    /**
     * send a server command to the remote peer
     */
    public void sendCommand(ServerCommand s) {
	if (peer != null)
	    peer.sendServerCommand(s);
    }
}
