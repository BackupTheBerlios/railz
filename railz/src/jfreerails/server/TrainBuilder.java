/*
 * Copyright (C) 2002 Luke Lindsay
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

package jfreerails.server;

import java.awt.Point;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddCargoBundleMove;
import jfreerails.move.AddTrainMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.common.*;
import jfreerails.world.top.*;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;
import jfreerails.world.train.*;
import jfreerails.world.player.Player;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * This class generates the move that adds a train to the game world and sets
 * its initial position.  Note, the client should not use this class to build
 * trains, instead it should request that a train gets built by setting
 * production at an engine shop.
 *
 * @author Luke Lindsay 13-Oct-2002
 *
 */
class TrainBuilder {
    private World world;
    private MoveReceiver moveReceiver;

    public TrainBuilder(World w, MoveReceiver mr) {
        this.world = w;
        moveReceiver = mr;

        if (null == mr) {
            throw new NullPointerException();
        }
    }

    /**
     * Warning, this method assumes that no other threads are accessing the
     * world object!
     *
     * @param engineTypeNumber type of the engine
     * @param wagons array of wagon types
     * @param p point at which to add train on map.
     * @param tp Principal which will own the train
     */
    public void buildTrain(int engineTypeNumber, int[] wagons, Point p,
	    FreerailsPrincipal tp) {
        FreerailsTile tile = (FreerailsTile)world.getTile(p.x, p.y);

        if (null != tile.getTrackTile()) {
            //Add train to train list.

            CargoBundle cb = new CargoBundleImpl();
            int cargoBundleNumber = world.size(KEY.CARGO_BUNDLES);
            Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber,
                    cb);
            int scheduleNumber = world.size(KEY.TRAIN_SCHEDULES);

	    GameTime now = (GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE);
            TrainModel train = new TrainModel(engineTypeNumber, wagons,
                    cargoBundleNumber, now);

            EngineType engineType = (EngineType)world.get(KEY.ENGINE_TYPES,
                    engineTypeNumber);
            int trainNumber = world.size(KEY.TRAINS, tp);

            WorldIterator wi = new NonNullElements(KEY.STATIONS, world, tp);

            MutableSchedule s = new MutableSchedule();

	    /*
	     * Add upto 4 stations to the schedule. Stations are selected from
	     * those owned by the train owner.
	     */
            while (wi.next() && s.getNumOrders() < 5) {
                TrainOrdersModel orders = new TrainOrdersModel
		    (new ObjectKey(KEY.STATIONS, tp, wi.getIndex()),
                        null, false, true, true);
                s.addOrder(orders);
            }

	    train = new TrainModel(train, new ScheduleIterator(scheduleNumber,
			0));

            ImmutableSchedule is = s.toImmutableSchedule();

	    AddTrainMove addTrainMove = AddTrainMove.generateMove(world,
		    trainNumber,
		    train, engineType.getPrice(), is, tp);

            Move compositeMove = new CompositeMove(new Move[] {
                        addCargoBundleMove, addTrainMove });

            moveReceiver.processMove(compositeMove);
        } else {
            throw new IllegalArgumentException("No track here (" + p.x + ", " +
                p.y + ") so cannot build train");
        }
    }
}
