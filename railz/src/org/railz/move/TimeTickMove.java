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

package org.railz.move;

import java.util.logging.*;

import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.top.World;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.common.GameTime;
import org.railz.world.top.ITEM;


public class TimeTickMove implements Move {
    private static final Logger logger = Logger.getLogger("global");

    private GameTime oldTime = null;
    private GameTime newTime = null;

    public GameTime getNewTime() {
	return newTime;
    }

    public static TimeTickMove getMove(ReadOnlyWorld w) {
        TimeTickMove timeTickMove = new TimeTickMove();
        timeTickMove.oldTime = (GameTime)w.get(ITEM.TIME, Player.AUTHORITATIVE);
        timeTickMove.newTime = new GameTime(timeTickMove.oldTime.getTime() + 1);

        return timeTickMove;
    }

    public FreerailsPrincipal getPrincipal() {
	return Player.NOBODY;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	if (((GameTime)w.get(ITEM.TIME,
			Player.AUTHORITATIVE)).equals(oldTime)) {
            return MoveStatus.MOVE_OK;
        } else {
	    logger.log(Level.FINER, "oldTime = " + oldTime.getTime() +
		    " <=> " + "currentTime " +
		    ((GameTime)w.get(ITEM.TIME,
				     Player.AUTHORITATIVE)).getTime());

            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (((GameTime)w.get(ITEM.TIME, Player.AUTHORITATIVE))
		.equals(newTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        if (tryDoMove(w, p).equals(MoveStatus.MOVE_OK)) {
            w.set(ITEM.TIME, newTime, Player.AUTHORITATIVE);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        if (tryUndoMove(w, p).equals(MoveStatus.MOVE_OK)) {
            w.set(ITEM.TIME, oldTime, Player.AUTHORITATIVE);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public String toString() {
        return "TimeTickMove: " + oldTime + "=>" + newTime;
    }
}
