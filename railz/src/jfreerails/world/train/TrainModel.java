package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.*;


public class TrainModel implements FreerailsSerializable {
    public static final int MAX_NUMBER_OF_WAGONS = 10;
    private int scheduleID;
    TrainPositionOnMap trainposition;
    int engineType = 0;
    final int[] wagonTypes;
    private int cargoBundleNumber;
    private GameTime creationDate;

    /**
     * copy constructor with original schedule, cargo, position, but new
     * engine and wagons
     */
    public TrainModel getNewInstance(int newEngine, int[] newWagons) {
        return new TrainModel(newEngine, newWagons, this.getPosition(),
            this.getScheduleID(), this.getCargoBundleNumber(), creationDate);
    }

    /**
     * @return the date at which the engine was created
     */
    public GameTime getCreationDate() {
	return creationDate;
    }

    /**
     * Constructor for a new train.
     * @param engine type of the engine
     * @param wagons array of indexes into the WAGON_TYPES table
     * @param p initial position of the train on the map.
     */
    public TrainModel(int engine, int[] wagons, TrainPositionOnMap p, int
	    scheduleID, int bundleId, GameTime creationDate) {
	engineType = engine;
	wagonTypes = wagons;
	trainposition = p;
	this.scheduleID = scheduleID;
	cargoBundleNumber = bundleId;
	this.creationDate = creationDate;
    }

    public TrainModel(int engine, int[] wagons, TrainPositionOnMap p,
        int scheduleID) {
        this.engineType = engine;
        this.wagonTypes = wagons;
        trainposition = p;
        this.scheduleID = scheduleID;
    }

    public int getLength() {
        return (1 + wagonTypes.length) * 32; //Engine + wagons.
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

    public TrainPositionOnMap getPosition() {
        return trainposition;
    }

    public void setPosition(TrainPositionOnMap s) {
        trainposition = s;
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

    public int getScheduleID() {
        return scheduleID;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.cargoBundleNumber == test.cargoBundleNumber &&
                this.engineType == test.engineType &&
                null == this.trainposition ? null == test.trainposition
                                           : this.trainposition.equals(test.trainposition) &&
                Arrays.equals(this.wagonTypes, test.wagonTypes) &&
                this.scheduleID == test.scheduleID;

            return b;
        } else {
            return false;
        }
    }
}
