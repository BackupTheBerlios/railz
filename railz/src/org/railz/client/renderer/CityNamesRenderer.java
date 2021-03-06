/*
 * Copyright (C) Scott Bennett
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

/**@author Scott Bennett
 * Date: 3rd April 2003
 *
 * Class to render the city names on the game map. Names are retrieved
 * from the KEY.CITIES object.
 */
package org.railz.client.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import org.railz.client.common.Painter;
import org.railz.world.player.*;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.city.CityModel;


public class CityNamesRenderer implements Painter {
    private ReadOnlyWorld w;

    public CityNamesRenderer(ReadOnlyWorld world) {
        this.w = world;
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", 0, 20));

        //draw city names onto map
        for (int i = 0; i < w.size(KEY.CITIES, Player.AUTHORITATIVE); i++) {
            CityModel tempCity = (CityModel)w.get(KEY.CITIES, i,
		    Player.AUTHORITATIVE);
            g.drawString(tempCity.getCityName(), tempCity.getCityX() *
		    TileRenderer.TILE_SIZE.width,
                tempCity.getCityY() * TileRenderer.TILE_SIZE.height + 10);
        }
    }
}
