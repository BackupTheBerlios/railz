/*
 * Copyright (C) 2003 Scott Bennett
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

package org.railz.server;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.*;

import org.railz.controller.*;
import org.railz.move.AddTransactionMove;
import org.railz.move.ChangeCargoBundleMove;
import org.railz.move.Move;
import org.railz.move.TransferCargoAtStationMove;
import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.MutableCargoBundle;
import org.railz.world.common.*;
import org.railz.world.station.ConvertedAtStation;
import org.railz.world.station.DemandAtStation;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.world.player.*;

/**
 * This class generates moves that transfer cargo between train and the
 * stations it stops at - it also handles cargo converions that occur when
 * cargo is dropped off.
 * 
 * @author Scott Bennett
 * Date Created: 4 June 2003
 *
 */
class DropOffAndPickupCargoMoveGenerator {
    private ReadOnlyWorld w;
    private MoveReceiver moveReceiver;
    private static final Logger logger = Logger.getLogger("global");

    /**
     * Cargo on board the train is unloaded and sold.
     */
    public void unloadTrain(ObjectKey trainKey, ObjectKey2 stationKey) {
	TrainModel train = (TrainModel)w.get(trainKey.key, trainKey.index,
		trainKey.principal);

	CargoBundle trainBefore = (CargoBundle) w.get(train.getCargoBundle());
	
        // trainBefore = trainBefore.getCopy();
	// CargoBundle trainAfter = trainBefore.getCopy();

	StationModel station = (StationModel) w.get(stationKey);
	CargoBundle stationBefore = (CargoBundle) w.get(station.getCargoBundle());
	// stationBefore = stationBefore.getCopy();
	// CargoBundle stationAfter = stationBefore.getCopy();

        MutableCargoBundle mutableTrainCB = new MutableCargoBundle(trainBefore);
        MutableCargoBundle mutableStationCB = new MutableCargoBundle(stationBefore);
        Iterator batches = mutableTrainCB.cargoBatchIterator();
        MutableCargoBundle cargoDroppedOff = new MutableCargoBundle();

        //Unload the cargo that the station demands
        while (batches.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) batches.next()).getKey();

            //if the cargo is demanded.
            DemandAtStation demand = station.getDemand();
            int cargoType = cb.getCargoType();
	    logger.log(Level.INFO, "Unloading cargo " + cargoType);

            if (demand.isCargoDemanded(cargoType)) {
                int amount = mutableTrainCB.getAmount(cb);
		cargoDroppedOff.addCargo(cb, amount);

                //Now perform any conversions..
                ConvertedAtStation converted = station.getConverted();

                if (converted.isCargoConverted(cargoType)) {
                    int newCargoType = converted.getConversion(cargoType);
		    GameTime now = (GameTime) w.get(ITEM.TIME,
			    Player.AUTHORITATIVE);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType,
                            station.x, station.y, now.getTime(),
			    stationKey);
                    mutableStationCB.addCargo(newCargoBatch, amount);
                }

                batches.remove();
            }
        }

	AddTransactionMove payment[] =
	    ProcessCargoAtStationMoveGenerator.processCargo(w,
		    new CargoBundle(cargoDroppedOff), trainKey.principal, 
                    stationKey);

	ChangeCargoBundleMove changeAtStation = new
	    ChangeCargoBundleMove(stationBefore, 
                new CargoBundle(mutableStationCB), station.getCargoBundle());

	ChangeCargoBundleMove changeOnTrain = new
	    ChangeCargoBundleMove(trainBefore, 
                new CargoBundle(mutableTrainCB), train.getCargoBundle());

	logger.log(Level.FINE, "train " + trainKey.index + ": stationAfter = "
		+ mutableStationCB + ", stationBefore = " + stationBefore +
		", trainAfter = " + mutableTrainCB + ", trainBefore = " +
		trainBefore + ", dropped off = " + cargoDroppedOff);

	logger.log(Level.FINE,"payment = " + payment);
	moveReceiver.processMove(TransferCargoAtStationMove.generateMove
		(changeAtStation, changeOnTrain, payment));
    }

    /**
     * Sell or dump all cargo which can't fit on the train
     */
    public void dumpSurplusCargo(ObjectKey trainKey, ObjectKey2 stationKey) {
	TrainModel train = (TrainModel)w.get(trainKey.key, trainKey.index,
		trainKey.principal);
	
	CargoBundle trainBefore = (CargoBundle) w.get(train.getCargoBundle());
	MutableCargoBundle mutableTrainCB = new MutableCargoBundle(trainBefore);

	StationModel station = (StationModel) w.get(stationKey);	
	CargoBundle stationBefore = (CargoBundle) w.get(station.getCargoBundle());	
	MutableCargoBundle mutableStationCB = new MutableCargoBundle(stationBefore);

        MutableCargoBundle cargoDroppedOff = new MutableCargoBundle();

	//Unload the cargo that there isn't space for on the train regardless
	//of whether the station demands it.
        int[] spaceAvailable = this.getSpaceAvailableOnTrain(train);

	boolean needToDump = false;
        for (int cargoType = 0; cargoType < spaceAvailable.length;
                cargoType++) {
            if (spaceAvailable[cargoType] < 0) {
                int amount2transfer = -spaceAvailable[cargoType];
                transferCargoToStation(cargoType, amount2transfer, mutableTrainCB,
                    mutableStationCB, cargoDroppedOff, stationKey);
		needToDump = true;
		logger.log(Level.SEVERE, "Dumping " + amount2transfer + " of " 
			+ cargoType);
            }
        }

	if (! needToDump)
	    return;

	AddTransactionMove payment[] =
	    ProcessCargoAtStationMoveGenerator.processCargo(w,
		    new CargoBundle(cargoDroppedOff), trainKey.principal, stationKey);

	ChangeCargoBundleMove changeAtStation = new
	    ChangeCargoBundleMove(stationBefore, 
                new CargoBundle(mutableStationCB), station.getCargoBundle());

	ChangeCargoBundleMove changeOnTrain = new
	    ChangeCargoBundleMove(trainBefore, new CargoBundle(mutableTrainCB),
                train.getCargoBundle());

	moveReceiver.processMove(TransferCargoAtStationMove.generateMove
		(changeAtStation, changeOnTrain, payment));
    }

    /**
     * @return true if there is enough cargo at this station to load the
     * train, false otherwise.
     */
    public boolean checkCargoAtStation(ObjectKey trainKey, ObjectKey2 stationKey)
    {
	TrainModel tm = (TrainModel) w.get(trainKey.key, trainKey.index,
		trainKey.principal);
	
	TrainOrdersModel tom = tm.getScheduleIterator().getCurrentOrder(w);
	if (tom == null)
	    return false;

	if (! tom.getWaitUntilFull())
	    return true;

	StationModel sm = (StationModel) w.get(stationKey);
	
	CargoBundle stationBundle = (CargoBundle) w.get(sm.getCargoBundle());
	
	int[] spaceLeft = getSpaceAvailableOnTrain(tm);
	for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES,
		    Player.AUTHORITATIVE);
		cargoType++) {
	    if (stationBundle.getAmount(cargoType) < spaceLeft[cargoType])
		return false;
	}

	return true;
    }

    /**
     * Load the train with as much cargo as is available and will fit on the
     * train.
     */
    public void loadTrain(ObjectKey trainKey, ObjectKey2 stationKey) {
	TrainModel train = (TrainModel)w.get(trainKey.key, trainKey.index,
		trainKey.principal);
	
	CargoBundle trainBefore = (CargoBundle) w.get(train.getCargoBundle());	
	MutableCargoBundle mutableTrainCB = new MutableCargoBundle(trainBefore);

	StationModel station = (StationModel) w.get(stationKey);	
	CargoBundle stationBefore = (CargoBundle) w.get(station.getCargoBundle());	
	MutableCargoBundle mutableStationCB = 
                new MutableCargoBundle(stationBefore);
	
        int[] spaceAvailable = getSpaceAvailableOnTrain(train);

	//Transfer cargo from the station to the train subject to the space
	//available on the train.
	for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES,
		    Player.AUTHORITATIVE);
                cargoType++) {
            int amount2transfer = Math.min(spaceAvailable[cargoType],
                    mutableStationCB.getAmount(cargoType));
	    transferCargo(cargoType, amount2transfer, mutableStationCB,
		    mutableTrainCB);
        }

	ChangeCargoBundleMove changeAtStation = new
	    ChangeCargoBundleMove(stationBefore, 
                new CargoBundle(mutableStationCB), station.getCargoBundle());

	ChangeCargoBundleMove changeOnTrain = new
	    ChangeCargoBundleMove(trainBefore, 
                new CargoBundle(mutableTrainCB), train.getCargoBundle());

	moveReceiver.processMove(TransferCargoAtStationMove.generateMove
		(changeAtStation, changeOnTrain));
    }

    /**
     * Contructor
     * @param world The world object
     */
    public DropOffAndPickupCargoMoveGenerator(ReadOnlyWorld world,
	    MoveReceiver mr) {
        w = world;
	moveReceiver = mr;
    }

    /**
     * @return array indexed by CARGO_TYPE indicating available space
     */
    private int[] getSpaceAvailableOnTrain(TrainModel train) {
        //This array will store the amount of space available on the train for each cargo type. 
	int[] spaceAvailable = new int[w.size(KEY.CARGO_TYPES,
		Player.AUTHORITATIVE)];

        //First calculate the train's total capacity.
        for (int j = 0; j < train.getNumberOfWagons(); j++) {
	    WagonType wagonType = (WagonType) w.get(KEY.WAGON_TYPES,
		    train.getWagon(j), Player.AUTHORITATIVE);
	    int cargoType = wagonType.getCargoType();

            spaceAvailable[cargoType] += wagonType.getCapacity();
        }

	CargoBundle cb = (CargoBundle) w.get(train.getCargoBundle());
        //Second, subtract the space taken up by cargo that the train is already carrying.
        for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES,
		    Player.AUTHORITATIVE);
                cargoType++) {
            spaceAvailable[cargoType] -= cb.getAmount(cargoType);
        }

        return spaceAvailable;
    }

    /**
     * @param cargoType the cargo type to be transferred.
     * @param amountToTransfer the amount in tonees to be transferred.
     * @param from the cargo bundle from which the cargo is to be transferred.
     * @param to the cargo bundle to which the cargo is to be transferred.
     * @param droppedOff the cargo for which payment should be made.
     */
    private void transferCargoToStation(int cargoType, int amountToTransfer,
	    MutableCargoBundle from, MutableCargoBundle to, MutableCargoBundle droppedOff,
	    ObjectKey2 stationKey) {
	if (0 == amountToTransfer)
	    return;

	StationModel station = (StationModel) w.get(stationKey);
	Iterator batches = from.cargoBatchIterator();
	int amountTransferedSoFar = 0;
	DemandAtStation demand = station.getDemand();
	ConvertedAtStation converted = station.getConverted();
	
	while (batches.hasNext() &&
		amountTransferedSoFar < amountToTransfer) {
	    CargoBatch cb = (CargoBatch)((Entry) batches.next()).getKey();

	    if (cb.getCargoType() == cargoType) {
		int amount = from.getAmount(cb);
		int amountOfThisBatchToTransfer;

		if (amount < amountToTransfer - amountTransferedSoFar) {
		    amountOfThisBatchToTransfer = amount;
		    batches.remove();
		} else {
		    amountOfThisBatchToTransfer = amountToTransfer -
			amountTransferedSoFar;
		    from.addCargo(cb, -amountOfThisBatchToTransfer);
		}

		if (demand.isCargoDemanded(cargoType)) {
		    // cargo is demanded, so do not add to station, but
		    // add to droppedOff for payment
		    if (droppedOff != null)
			droppedOff.addCargo(cb, amountOfThisBatchToTransfer);
		} else if (converted.isCargoConverted(cargoType)) {
		    // convert cargo to new type and add this to station
                    int newCargoType = converted.getConversion(cargoType);
		    GameTime now = (GameTime) w.get(ITEM.TIME,
			    Player.AUTHORITATIVE);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType,
                            station.x, station.y, now.getTime(),
			    stationKey);
                    to.addCargo(newCargoBatch, amountOfThisBatchToTransfer);
                } else {
		    // cargo is neither demanded nor converted, so leave it at
		    // station
		    to.addCargo(cb, amountOfThisBatchToTransfer);
		}
		amountTransferedSoFar += amountOfThisBatchToTransfer;
	    }
	}
    }

    /**
     * Move the specified quantity of the specifed cargotype from one bundle
     * to another.
     */
    private static void transferCargo(int cargoTypeToTransfer,
        int amountToTransfer, MutableCargoBundle from, MutableCargoBundle to) {
        if (0 == amountToTransfer) {
            return;
        } else {
            Iterator batches = from.cargoBatchIterator();
            int amountTransferedSoFar = 0;

            while (batches.hasNext() &&
                    amountTransferedSoFar < amountToTransfer) {
                CargoBatch cb = (CargoBatch)((Entry) batches.next()).getKey();

                if (cb.getCargoType() == cargoTypeToTransfer) {
                    int amount = from.getAmount(cb);
                    int amountOfThisBatchToTransfer;

                    if (amount < amountToTransfer - amountTransferedSoFar) {
                        amountOfThisBatchToTransfer = amount;
                        batches.remove();
                    } else {
                        amountOfThisBatchToTransfer = amountToTransfer -
                            amountTransferedSoFar;
                        from.addCargo(cb, -amountOfThisBatchToTransfer);
                    }

                    to.addCargo(cb, amountOfThisBatchToTransfer);
                    amountTransferedSoFar += amountOfThisBatchToTransfer;
                }
            }
        }
    }
}
