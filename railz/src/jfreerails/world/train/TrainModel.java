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

package jfreerails.world.train;

import java.util.Arrays;
import java.io.ObjectInputStream;
import java.io.IOException;

import jfreerails.world.common.*;
import jfreerails.world.track.*;

public class TrainModel implements FreerailsSerializable {
    /**
     * The time at which the state of the train was last changed.
     */
    private GameTime stateLastChanged;

    /**
     * The train is in transit between stops. (but may be blocked by other
     * trains on the track.
     */
    public static final int STATE_RUNNABLE = 0;
    /**
     * The train is stopped by the user.
     */
    public static final int STATE_STOPPED = 1;
    /**
     * The train is loading cargo at a station.
     */
    public static final int STATE_LOADING = 2;
    /**
     * The train is unloading cargo at a station
     */
    public static final int STATE_UNLOADING = 3;

    public static final int MAX_NUMBER_OF_WAGONS = 10;
    private ScheduleIterator scheduleIterator;

    /**
     * Describes the current position of the train. The head of the path
     * coincides with the head of the train. The tail of the path coincides
     * with the end of the train.
     */
    private TrainPath trainPath;

    private transient TrainMotionModel trainMotionModel;

    private int engineType = 0;
    private final int[] wagonTypes;
    private int cargoBundleNumber;
    private final GameTime creationDate;

    private int state;

    /**
     * copy constructor with original schedule, cargo, position, but new
     * engine and wagons
     */
    public TrainModel getNewInstance(int newEngine, int[] newWagons) {
        return new TrainModel(newEngine, newWagons, this.getPosition(),
	    this.getCargoBundleNumber(),
	    creationDate, state, scheduleIterator, trainMotionModel,
	    stateLastChanged);
    }

    /**
     * @return the date at which the engine was created
     */
    public GameTime getCreationDate() {
	return creationDate;
    }

    private TrainModel(int engine, int[] wagons, TrainPath currentP,
	    int bundleId, GameTime creationDate,
	    int state, ScheduleIterator
	    scheduleIterator, TrainMotionModel motionModel, GameTime
	    stateLastChanged) {
	engineType = engine;
	wagonTypes = wagons;
	trainPath = currentP;
	cargoBundleNumber = bundleId;
	this.creationDate = creationDate;
	this.state = state;
	this.stateLastChanged = stateLastChanged;
	this.scheduleIterator = scheduleIterator;
	trainMotionModel = motionModel;
    }

    /**
     * Constructor for a new train.
     * @param engine type of the engine
     * @param wagons array of indexes into the WAGON_TYPES table
     * @param p initial position of the train on the map.
     */
    public TrainModel(int engine, int[] wagons, int bundleId,
	    GameTime creationDate) {
	this(engine, wagons, null, bundleId, creationDate,
		STATE_UNLOADING, null, new TrainMotionModel(), creationDate);
    }

    /**
     * @return train length in Deltas
     */
    public int getLength() {
       	//Engine + wagons.
        return (1 + wagonTypes.length) * TrackTile.DELTAS_PER_TILE;
    }

    public boolean canAddWagon() {
        return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return wagonTypes.length;
    }

    /**
     * @return Index into WAGON_TYPES table of the ith wagon in the train
     */
    public int getWagon(int i) {
        return wagonTypes[i];
    }

    public TrainPath getPosition() {
        return trainPath;
    }

    public void setPosition(TrainPath s) {
        trainPath = s;
    }

    /**
     * @return an index into the ENGINE_TYPES database
     */
    public int getEngineType() {
        return engineType;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.cargoBundleNumber == test.cargoBundleNumber &&
                this.engineType == test.engineType &&
                null == this.trainPath ? null == test.trainPath :
		    this.trainPath.equals(test.trainPath) &&
                Arrays.equals(this.wagonTypes, test.wagonTypes) &&
                this.scheduleIterator == test.scheduleIterator;

            return b;
        } else {
            return false;
        }
    }

    public int getState() {
	return state;
    }

    public GameTime getStateLastChangedTime() {
	return stateLastChanged;
    }

    public void setState(int s, GameTime now) {
	state = s;
	stateLastChanged = now;
    }

    public ScheduleIterator getScheduleIterator() {
	return scheduleIterator;
    }

    public TrainModel (TrainModel tm, ScheduleIterator si) {
	this(tm.engineType, tm.wagonTypes, tm.trainPath, tm.cargoBundleNumber,
		tm.creationDate, tm.state, si, tm.trainMotionModel,
		tm.stateLastChanged);
    }

    public void setTrainMotionModel(TrainMotionModel tmm) {
	trainMotionModel = tmm;
    }

    public TrainMotionModel getTrainMotionModel() {
	return trainMotionModel;
    }

    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
	in.defaultReadObject();
	trainMotionModel = new TrainMotionModel();
    }
}
