/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.station;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
import jfreerails.world.track.FreerailsTile;
public class StationModelViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private StationModel stationModel;
    private GameCalendar calendar;

    public StationModelViewer(ReadOnlyWorld w) {
	world = w;
	calendar = (GameCalendar) world.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
    }

    public void setStationModel(StationModel sm) {
	stationModel = sm;
    }

    /**
     * Stations depreciate from their initial value over 25 years to 50% of
     * their initial value.
     */
    public long getBookValue() {
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	long nowMillis = calendar.getCalendar(now).getTimeInMillis();
	long creationMillis = calendar.getCalendar
	    (stationModel.getCreationDate()).getTimeInMillis();
	/* assume years are 365 days long for simplicity */
	int elapsedYears = (int)
	    (creationMillis - nowMillis) / (1000 * 60 * 60 * 24 * 365);
	FreerailsTile tile = world.getTile(stationModel.getStationX(),
		stationModel.getStationY());

	long initialPrice = tile.getTrackRule().getPrice();
	if (elapsedYears >= 25) {
	    return (long) (initialPrice * 0.50);
	}
	return (long) (initialPrice * (1.0 - (elapsedYears * 0.5 / 25)));
    }
}
