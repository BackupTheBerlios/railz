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
 * Date: 14th April 2003
 *
 * Class to render the station names on the game map. Names are retrieved
 * from the KEY.STATIONS object.
 */
package org.railz.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.Iterator;

import org.railz.client.common.Painter;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldIterator;


public class StationNamesRenderer implements Painter {
    private ReadOnlyWorld w;
    private int fontSize;
    private Color bgColor;
    private Color textColor;

    public StationNamesRenderer(ReadOnlyWorld world) {
        this.w = world;

        this.fontSize = 10;
        this.bgColor = Color.BLACK;
        this.textColor = Color.WHITE;
    }

    public void paint(Graphics2D g) {
        int rectWidth;
        int rectHeight;
        int rectX;
        int rectY;
        float visibleAdvance;
        float textX;
        float textY;

        StationModel tempStation;
        String stationName;
        int positionX;
        int positionY;

        Font font = new Font("Arial", 0, fontSize);
        FontRenderContext frc = g.getFontRenderContext();
        TextLayout layout;

        //draw station names onto map
	NonNullElements i = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal) ((Player)
		    i.getElement()).getPrincipal();
            Iterator wi = w.getIterator(KEY.STATIONS, p);	    

	    while (wi.hasNext()) { //loop over non null stations
		tempStation = (StationModel) wi.next();

		stationName = tempStation.getStationName();
		positionX = (tempStation.getStationX() *
			TileRenderer.TILE_SIZE.width) + 15;
		positionY = (tempStation.getStationY() *
			TileRenderer.TILE_SIZE.height) + 30;

		layout = new TextLayout(stationName, font, frc);
		visibleAdvance = layout.getVisibleAdvance();

		rectWidth = (int)(visibleAdvance * 1.2);
		rectHeight = (int)(fontSize * 1.5);
		rectX = (int)(positionX - (rectWidth / 2));
		rectY = positionY;

		g.setColor(bgColor);
		g.fillRect(rectX, rectY, rectWidth, rectHeight);

		textX = (float)(positionX - (visibleAdvance / 2));
		textY = positionY + fontSize + 1;

		g.setColor(textColor);
		layout.draw(g, textX, textY);

		g.setStroke(new BasicStroke(1.0f));
		//draw a border 1 pixel inside the edges of the rectangle
		g.draw(new Rectangle(rectX + 1, rectY + 1, rectWidth - 3,
			    rectHeight - 3));
	    }
	}
    }
    //paint method
}
