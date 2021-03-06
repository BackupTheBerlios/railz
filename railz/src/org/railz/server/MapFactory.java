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
 * Created on 22-Mar-2003
 *
 */
package org.railz.server;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.world.player.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.KEY;
import org.railz.world.top.WorldImpl;
import org.railz.world.track.FreerailsTile;


/**
 * This class has a static method that converts an image file into a map.
 * @author Luke
 *
 */
public class MapFactory {
    public static void setupMap(URL map_url, WorldImpl w,
        FreerailsProgressMonitor pm) {
        //Setup progress monitor..
        pm.setMessage("Setting up map.");
        pm.setValue(0);

        Image mapImage = (new javax.swing.ImageIcon(map_url)).getImage();
        Rectangle mapRect = new java.awt.Rectangle(0, 0,
                mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width,
                mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        w.setupMap(mapRect.width, mapRect.height);

        pm.setMax(mapRect.width);

        HashMap rgb2TerrainType = new HashMap();

	for (int i = 0; i < w.size(KEY.TERRAIN_TYPES, Player.AUTHORITATIVE);
		i++) {
            TerrainType tilemodel = (TerrainType)w.get(KEY.TERRAIN_TYPES, i,
		    Player.AUTHORITATIVE);
            rgb2TerrainType.put(new Integer(tilemodel.getRGB()), new Integer(i));
        }

        for (int x = 0; x < mapRect.width; x++) {
            pm.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FreerailsTile tile;
                Integer type = (Integer)rgb2TerrainType.get(new Integer(rgb));

                if (null == type) {
                    throw new NullPointerException(
			"There is no terrain type mapped to rgb value " + 
			Integer.toHexString(rgb) +
                        " at location " + x + ", " + y);
                }

                tile = new FreerailsTile(type.intValue(), null, null);
                w.setTile(x, y, tile);
            }
        }
    }
}
