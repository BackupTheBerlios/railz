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

package jfreerails.server;

import java.net.URL;
import jfreerails.server.parser.*;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.common.*;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import org.xml.sax.SAXException;

/**
 * This class sets up a World object. It cannot be instantiated.
 */
class WorldFactory {
    private WorldFactory() {
    }

    /**
     * TODO This would be better implemented in a config file, or better
     * still dynamically determined by scanning the directory.
     */
    public static String[] getMapNames() {
        return new String[] {"south_america", "small_south_america"};
    }

    public static World createWorldFromMapFile(String mapName,
        FreerailsProgressMonitor pm) {
        pm.setMessage("Setting up world.");
        pm.setValue(0);
        pm.setMax(5);

        int progess = 0;

        //Load the xml file specifying terrain types.
        URL tiles_xml_url = WorldFactory.class.getResource(
                "/jfreerails/data/terrain_tiles.xml");

        //	new jfreerails.TileSetFactoryImpl(tiles_xml_url);
        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        try {
            java.net.URL url = RunTypesParser.class.getResource(
                    "/jfreerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(w));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        pm.setValue(++progess);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        pm.setValue(++progess);
        wetf.addTypesToWorld(w);
        pm.setValue(++progess);

        URL track_xml_url = WorldFactory.class.getResource(
                "/jfreerails/data/track_tiles.xml");

	Track_TilesHandlerImpl trackSetFactory = new
	    Track_TilesHandlerImpl(track_xml_url, w);
        pm.setValue(++progess);

        //Load the terrain map
        URL map_url = WorldFactory.class.getResource("/jfreerails/data/" +
                mapName + ".png");
        MapFactory.setupMap(map_url, w, pm);

        //Load the city names
        URL cities_xml_url = WorldFactory.class.getResource("/jfreerails/data/" +
                mapName + "_cities.xml");

        try {
            InputCityNames r = new InputCityNames(w, cities_xml_url);
        } catch (SAXException e) {
        }

        //Randomly position the city tiles - no need to assign this object
        new BuildingTilePositioner(w);

        //Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(30, 1840));
        w.set(ITEM.TIME, new GameTime(0));

	//Create the economy
	Economy economy = new Economy();
	economy.setIncomeTaxRate(25);
	economy.setBaseInterestRate((float) 5.0);
	w.set(ITEM.ECONOMY, economy, Player.AUTHORITATIVE);

        return w;
    }
}
