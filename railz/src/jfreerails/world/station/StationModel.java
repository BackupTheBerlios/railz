package jfreerails.world.station;

import jfreerails.world.common.*;
/**
 * This class represents a station.
 *
 * @author Luke
 *
 */
public class StationModel implements FreerailsSerializable {
    public final int x;
    public final int y;
    private final String name;
    private final SupplyAtStation supply;
    private final DemandAtStation demand;
    private final ConvertedAtStation converted;
    private final int cargoBundleNumber;
    private final GameTime creationDate;

    /** What this station is building. */
    private final ProductionAtEngineShop production;

    public ConvertedAtStation getConverted() {
        return converted;
    }

    public StationModel(StationModel s, ConvertedAtStation converted) {
        this.converted = converted;

        this.cargoBundleNumber = s.cargoBundleNumber;

        this.demand = s.demand;
        this.name = s.name;
        this.production = s.production;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
	creationDate = s.creationDate;
    }

    public StationModel(int x, int y, String stationName,
        int numberOfCargoTypes, int cargoBundle, GameTime now) {
        this.name = stationName;
        this.x = x;
        this.y = y;
        production = null;
	creationDate = now;

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new DemandAtStation(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
    }

    public String getStationName() {
        return name;
    }

    public int getStationX() {
        return x;
    }

    public int getStationY() {
        return y;
    }

    public ProductionAtEngineShop getProduction() {
        return production;
    }

    public StationModel(StationModel s, ProductionAtEngineShop production) {
        this.production = production;
        this.demand = s.demand;
        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;
        this.name = s.name;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
	creationDate = s.creationDate;
    }

    public DemandAtStation getDemand() {
        return demand;
    }

    public SupplyAtStation getSupply() {
        return supply;
    }

    public StationModel(StationModel s, DemandAtStation demand) {
        this.demand = demand;

        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;

        this.name = s.name;
        this.production = s.production;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
	creationDate = s.creationDate;
    }

    public StationModel(StationModel s, SupplyAtStation supply) {
        this.supply = supply;
        this.demand = s.demand;

        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;
        this.name = s.name;
        this.production = s.production;
        this.x = s.x;
        this.y = s.y;
	creationDate = s.creationDate;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public boolean equals(Object o) {
        if (o instanceof StationModel) {
            StationModel test = (StationModel)o;

	    if (cargoBundleNumber == test.cargoBundleNumber &&
		    x == test.x &&
		    y == test.y &&
		    demand.equals(test.demand) &&
		    converted.equals(test.converted) &&
		    name.equals(test.name) &&
		    (production == null ? test.production == null :
		     this.production.equals(test.production)) &&
		    supply.equals(test.supply) &&
		    creationDate.equals(test.creationDate)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * @return the date at which this station was constructed.
     */
    public GameTime getCreationDate() {
	return creationDate;
    }
}
