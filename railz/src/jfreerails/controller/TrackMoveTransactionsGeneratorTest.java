/*
 * Copyright (C) 2003 Luke Lindsay
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
 * Created on 10-Aug-2003
 *
 */
package jfreerails.controller;

import java.awt.Point;
import junit.framework.TestCase;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TrackMove;
import jfreerails.world.common.*;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.*;
import jfreerails.world.player.Player;


/**
 * @author Luke Lindsay
 *
 */
public class TrackMoveTransactionsGeneratorTest extends TestCase {
    private World world;
    private TrackMoveTransactionsGenerator transactionGenerator;
    private Player player;

    protected void setUp() throws Exception {
        MapFixtureFactory mff = 
	    new MapFixtureFactory(20, 20);
	world = mff.world;
        player = new Player("test player",
                (new Player("test player")).getPublicKey(), 0);
        world.add(KEY.PLAYERS, player, Player.AUTHORITATIVE);
        transactionGenerator = new TrackMoveTransactionsGenerator(world,
                player.getPrincipal());
    }

    public void testAddTrackMove() {
        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;
        byte oldConfig;
        byte newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = CompassPoints.NORTH;
        oldTrackPiece = world.getTile(0, 0).getTrackTile();

        newTrackPiece = TrackTile.createTrackTile(world, newConfig, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), player.getPrincipal());

        Move m = transactionGenerator.addTransactions(move);
        assertNotNull(m);
    }
}
