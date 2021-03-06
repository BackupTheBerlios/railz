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
 * Created on 28-Jun-2003
 *
 */
package org.railz.server;

import java.util.Iterator;
import junit.framework.TestCase;
import java.util.logging.*;

import org.railz.controller.*;
import org.railz.move.Move;
import org.railz.move.MoveStatus;
import org.railz.world.accounts.BankAccount;
import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.cargo.MutableCargoBundle;
import org.railz.world.common.GameTime;
import org.railz.world.station.DemandAtStation;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;
import org.railz.world.train.TransportCategory;
import org.railz.world.train.WagonType;
import org.railz.world.train.TrainModel;
import org.railz.world.player.Player;


/** This Junit TestCase tests whether a train picks up and drops off the right cargo at a station.
 * @author Luke Lindsay
 *
 */
public class DropOffAndPickupCargoMoveGeneratorTest extends TestCase {
    private World w;
    private MoveReceiver moveReceiver;
    Logger logger = Logger.getLogger("global");
    
    private CargoBatch cargoType0FromStation2 = null;
    private CargoBatch cargoType1FromStation2 = null;
    private CargoBatch cargoType0FromStation0 = null;
    private CargoBundle emptyCargoBundle;

    private ObjectKey2 station0Key = null;
    private ObjectKey2 station2Key = null;
    
    private Player testPlayer = new Player ("test player", (new Player ("test"
		    + " player")).getPublicKey(), 0);

    protected void setUp() throws Exception {
        //Set up an empty cargobundle
        emptyCargoBundle = new CargoBundle();

        //Set up the world object with three cargo types, one station, and one train.		
        w = new WorldImpl();

	w.add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);

        //set up the cargo types.
        w.add(KEY.CARGO_TYPES, new CargoType("Mail", TransportCategory.MAIL,
		    100, 100, 100), Player.NOBODY);
        w.add(KEY.CARGO_TYPES, new CargoType("Passengers",
		   TransportCategory.PASSENGER, 100, 100, 100),
		Player.NOBODY);
        w.add(KEY.CARGO_TYPES, new CargoType("Goods",
		   TransportCategory.FAST_FREIGHT, 100, 100, 100),
		Player.NOBODY);

       w.add(KEY.WAGON_TYPES, new WagonType("Mail Wagon",
                   TransportCategory.MAIL, 40, 0, 10), Player.NOBODY);
       w.add(KEY.WAGON_TYPES, new WagonType("1at class carriage",
		   TransportCategory.PASSENGER, 40, 1, 10),
	       Player.NOBODY);
       w.add(KEY.WAGON_TYPES, new WagonType("Goods van",
		   TransportCategory.FAST_FREIGHT, 40, 1, 10),
	       Player.NOBODY);
       w.set(ITEM.TIME, new GameTime(0), Player.NOBODY);


        //Set up station
        int x = 10;
        int y = 10;
        CargoBundle stationCB = new CargoBundle();
        ObjectKey2 stationCBKey = new ObjectKey2(KEY.CARGO_BUNDLES, Player.NOBODY,
                stationCB.getUUID());
        w.set(stationCBKey, stationCB);
        
        String stationName = "Station 1";
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.NOBODY);
        StationModel station = new StationModel(x, y, stationName,
		w.size(KEY.CARGO_TYPES, Player.NOBODY),
		stationCBKey, now);
        station0Key = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(), 
                station.getUUID());
        w.set(station0Key, station);

        cargoType0FromStation0 = new CargoBatch(0, 0, 0, 0, station0Key);
        
        CargoBundle station2cb = new CargoBundle();
        ObjectKey2 station2cbKey = new ObjectKey2(KEY.CARGO_BUNDLES, 
                Player.NOBODY, station2cb.getUUID());
        w.set(station2cbKey, station2cb);        
        StationModel station2 = new StationModel(0, 0, "Station 2",
                w.size(KEY.CARGO_TYPES, Player.NOBODY),
                station2cbKey, now);
        station2Key = new ObjectKey2(KEY.STATIONS, testPlayer.getPrincipal(),
                station2.getUUID());        
        w.set(station2Key, station2);
        
        cargoType1FromStation2 = new CargoBatch(1, 0, 0, 0, station2Key);
        cargoType0FromStation2 = new CargoBatch(0, 0, 0, 0, station2Key);
        
        //Set up train
        CargoBundle trainCB = new CargoBundle();
        ObjectKey2 trainCBKey = new ObjectKey2(KEY.CARGO_BUNDLES, Player.NOBODY,
                trainCB.getUUID());
	w.set(trainCBKey, trainCB);

        //3 wagons to carry cargo type 0.
        int[] wagons = new int[] {0, 0, 0};
        TrainModel train = new TrainModel(0, wagons, trainCBKey, now);
        w.add(KEY.TRAINS, train, testPlayer.getPrincipal());

        w.add(KEY.BANK_ACCOUNTS, new BankAccount(), testPlayer.getPrincipal());
	moveReceiver = new AuthoritativeMoveExecuter(w, null);
    }

    private CargoBundle emptyCargoBundle(CargoBundle cb) {
        MutableCargoBundle mcb = new MutableCargoBundle(cb);
        Iterator i = mcb.cargoBatchIterator();
        while (i.hasNext()) {
            CargoBatch batch = (CargoBatch) i.next();            
            mcb.setAmount(batch, 0);
        }
        return new CargoBundle(mcb);
    }
    
    /** Tests picking up cargo from a station. */
    public void testPickUpCargo1() {
        //Set up the variables for this test.		
        MutableCargoBundle cargoBundleWith2CarloadsOfCargo0 = 
                new MutableCargoBundle(getCargoAtStation());

        //cargoBundleWith2CarloadsOfCargo0.setAmount(cargoType0FromStation2, 2);
        cargoBundleWith2CarloadsOfCargo0.setAmount(cargoType0FromStation2, 80);

        //Get the station from the world object.
	StationModel station = (StationModel)w.get(station0Key);

        CargoBundle emptyTrainCargoBundle = emptyCargoBundle(getCargoOnTrain());
        CargoBundle emptyStationCargoBundle = emptyCargoBundle(getCargoAtStation());
        assertEquals("There shouldn't be any cargo at the station yet",
            emptyStationCargoBundle, getCargoAtStation());
        assertEquals("There shouldn't be any cargo on the train yet",
            emptyTrainCargoBundle, getCargoOnTrain());

        //Now add 2 carloads of cargo type 0 to the station.				
        MutableCargoBundle newCB = new MutableCargoBundle
                (getCargoAtStation());
        newCB.setAmount(cargoType0FromStation2, 80);
        w.set(station.getCargoBundle(), new CargoBundle(newCB));        

        //The train should pick up this cargo, since it has three wagons capable of carrying cargo type 0.
        stopAtStation();

        //The train should now have the two car loads of cargo and there should be no cargo at the station.
        assertEquals("There should no longer be any cargo at the station",
            emptyStationCargoBundle, getCargoAtStation());
                
        assertEquals("The train should now have the two car loads of cargo",
            80, getCargoOnTrain().getAmount(cargoType0FromStation2));
    }

    /** Tests picking up cargo when the there is too much cargo at the station for the train to carry.*/
    public void testPickUpCargo2() {
        
        setCargoAtStation(this.cargoType0FromStation2, 200);        

        stopAtStation();
        
        //Test the expected values against the actuals..
        //The train has 3 wagons, each wagon carries 40 units of cargo, so
        //the train should pickup 120 units of cargo.
        assertEquals(120, getCargoOnTrain().getAmount(cargoType0FromStation2));
        //The remaining 80 units of cargo should be left at the station.
        assertEquals(80, getCargoAtStation().getAmount(cargoType0FromStation2));
    }

    /** Tests that a train takes into account how much cargo it
     * already has and the type of wagons it has when it is picking up cargo.
     */
    public void testPickUpCargo3() {
        //Set wagons on train.	
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, 0,
	       	testPlayer.getPrincipal());
        int[] wagons = new int[] {0, 0, 2, 2};

        //2 wagons for cargo type 0; 2 wagons for cargo type 2.
        train = addWagons(wagons);

        //Set cargo on train.
        setCargoOnTrain(this.cargoType0FromStation2, 30);

        //Set cargo at station.
        setCargoAtStation(this.cargoType0FromStation0, 110);

        //Check that station does not demand cargo type 0.
	StationModel station = (StationModel)w.get(station0Key);
        assertFalse(station.getDemand().isCargoDemanded(0));

        //Stop at station.
        stopAtStation();

        assertEquals(60, getCargoAtStation().getAmount(cargoType0FromStation0));
        /* The train has 2 wagons for cargo type 0 but had 30 units of
         * cargo type 0 before stopping so it can only pick up 50 units.
         */
        assertEquals(30, getCargoOnTrain().getAmount(cargoType0FromStation2));
        assertEquals(50, getCargoOnTrain().getAmount(cargoType0FromStation0));
    }

    /** Tests that a train drops of cargo that a station demands and does not drop off cargo that is not
     * demanded unless it has to.
     */
    public void testDropOffCargo() {
        //Set the station to demand cargo type 0.
	StationModel station = (StationModel)w.get(station0Key);
        DemandAtStation demand = new DemandAtStation(new boolean[] {
                    true, false, false, false
                });
        station = new StationModel(station, demand);
        w.set(station0Key, station);

        //Check that the station demadns what we think it does.		
        assertTrue("The station should demand cargo type 0.",
            station.getDemand().isCargoDemanded(0));
        assertFalse("The station shouldn't demand cargo type 1.",
            station.getDemand().isCargoDemanded(1));

        //Add 2 wagons for cargo type 0 and 1 for cargo type 1 to train.
        int[] wagons = new int[] {0, 0, 1, 1};
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());
        train = addWagons(wagons);

        //Add quantities of cargo type 0 and 2 to the train.
        setCargoOnTrain(this.cargoType0FromStation2, 50);
        setCargoOnTrain(this.cargoType1FromStation2, 40);

        stopAtStation();

        /* The train should have dropped of the 50 units cargo of type 0 since the
         * station demands it but not the 40 units of cargo type 1 which is does not
         * demand.
         */        
        assertEquals(40, getCargoOnTrain().getAmount(cargoType1FromStation2));
        assertEquals(0, getCargoAtStation().getAmount(cargoType1FromStation2));
        assertEquals(0, getCargoAtStation().getAmount(cargoType0FromStation2));

        //Now remove the wagons from the train.
        removeAllWagonsFromTrain();
	logger.log(Level.SEVERE, "stopping at station");
        stopAtStation();
	logger.log(Level.SEVERE, "stopped at station");

        /*This time the train has no wagons, so has to drop the 40 units
         * of cargo type 1 even though the station does not demand it.  Since
         * ths station does not demand it, it is added to the cargo waiting at
         * the station.
         */
        assertEquals(40, getCargoAtStation().getAmount(cargoType1FromStation2));
        assertEquals(0, getCargoOnTrain().getAmount(cargoType1FromStation2));
    }

    /** Tests that a train does not drop cargo off at its station of origin unless it has to*/
    public void testDontDropOffCargo() {
        //Set station to 
        setCargoOnTrain(cargoType0FromStation0, 50);
        setCargoOnTrain(cargoType0FromStation2, 50);

        stopAtStation();

        //The train shouldn't have dropped anything off.
        assertEquals(50, getCargoOnTrain().getAmount(cargoType0FromStation0));
        assertEquals(50, getCargoOnTrain().getAmount(cargoType0FromStation2));
        assertEquals(0, getCargoAtStation().getAmount(cargoType0FromStation0));
        assertEquals(0, getCargoAtStation().getAmount(cargoType0FromStation2));

        //Now remove the wagons from the train.
        removeAllWagonsFromTrain();

        stopAtStation();

        /*The train now has no wagons, so must drop off the cargo whether the
        station demands it or not.  Since the station does not demand it, the
        cargo should get added to the cargo waiting at the station. */
        assertEquals(50, getCargoAtStation().getAmount(cargoType0FromStation0));
        assertEquals(50, getCargoAtStation().getAmount(cargoType0FromStation2));
        assertEquals(0, getCargoOnTrain().getAmount(cargoType0FromStation0));
        assertEquals(0, getCargoOnTrain().getAmount(cargoType0FromStation2));
    }

    /**  Tests that a train drops off any cargo before picking
     * up cargo.
     */
    public void testPickUpAndDropOffSameCargoType() {
        //Set cargo at station and on train.
        setCargoOnTrain(this.cargoType0FromStation2, 120);
        setCargoAtStation(this.cargoType0FromStation0, 200);

        //Set station to demand cargo 0.
	StationModel station = (StationModel)w.get(station0Key);
        DemandAtStation demand = new DemandAtStation(new boolean[] {
                    true, false, false, false
                });
        station = new StationModel(station, demand);
        w.set(station0Key, station);

        assertTrue(station.getDemand().isCargoDemanded(0));
        stopAtStation();

        assertEquals(120, getCargoOnTrain().getAmount(cargoType0FromStation0));
        assertEquals(80, getCargoAtStation().getAmount(cargoType0FromStation0));
    }

    private void removeAllWagonsFromTrain() {
        addWagons(new int[] {});
    }

    private TrainModel addWagons(int[] wagons) {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());
        TrainModel newTrain = train.getNewInstance(train.getEngineType(), wagons);
        w.set(KEY.TRAINS, 0, newTrain, testPlayer.getPrincipal());

        return newTrain;
    }

    private void stopAtStation() {
	ObjectKey trainKey = new ObjectKey(KEY.TRAINS,
		testPlayer.getPrincipal(), 0);

        DropOffAndPickupCargoMoveGenerator moveGenerator = new
	    DropOffAndPickupCargoMoveGenerator(w, moveReceiver);
	moveGenerator.unloadTrain(trainKey, station0Key);
	moveGenerator.dumpSurplusCargo(trainKey, station0Key);
	moveGenerator.loadTrain(trainKey, station0Key);
    }

    private void setCargoAtStation(CargoBatch batch, int amount) {
        StationModel sm = (StationModel) w.get(station0Key); 
        CargoBundle cb = (CargoBundle) w.get(sm.getCargoBundle());
        MutableCargoBundle mcb = new MutableCargoBundle(cb);
        mcb.setAmount(batch, amount);
        w.set(sm.getCargoBundle(), new CargoBundle(mcb));        
    }

    private void setCargoOnTrain(CargoBatch batch, int amount) {
        TrainModel tm = (TrainModel) w.get(KEY.TRAINS, 0, testPlayer.getPrincipal()); 
        CargoBundle cb = (CargoBundle) w.get(tm.getCargoBundle());
        MutableCargoBundle mcb = new MutableCargoBundle(cb);
        mcb.setAmount(batch, amount);
        w.set(tm.getCargoBundle(), new CargoBundle(mcb));        
    }
    

    /** Retrieves the cargo bundle that is waiting at the station from the world object.*/
    private CargoBundle getCargoAtStation() {
	StationModel station = (StationModel)w.get(station0Key);
        CargoBundle cargoAtStation = (CargoBundle)w.get(station.getCargoBundle());

        return cargoAtStation;
    }

    /** Retrieves the cargo bundle that the train is carrying from the world object */
    private CargoBundle getCargoOnTrain() {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());
        CargoBundle cargoOnTrain = (CargoBundle)w.get(train.getCargoBundle());

        return cargoOnTrain;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite testSuite = new junit.framework.TestSuite(DropOffAndPickupCargoMoveGeneratorTest.class);

        return testSuite;
    }
}
