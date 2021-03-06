/*
 * Copyright (C) 2001 Luke Lindsay
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
 *  SquareTileBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:36
 */
package org.railz.client.renderer;

import java.awt.Graphics;


/**
 *  This class stores a buffer containing the terrain and track layers of
 *  current visible rectangle of the map. It is responsible of painting these
 *  layers and updating the buffer when the map scrolls or tiles are updated.
 *
 *@author     Luke Lindsay
 *     01 November 2001
 *@version    1.0
 */
final public class SquareTileBackgroundRenderer
    extends BufferedTiledBackgroundRenderer {
    private MapLayerRenderer mapView;

    protected void paintBufferRectangle(int x, int y, int width, int height) {
	if (bg == null)
	    return;

        bg.setClip(x, y, width, height);
        bg.translate(-bufferRect.x, -bufferRect.y);
        mapView.paintRect(bg, bufferRect);
        bg.translate(bufferRect.x, bufferRect.y);
    }

    public SquareTileBackgroundRenderer(MapLayerRenderer mv, float _scale) {
        this.mapView = mv;
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        mapView.paintTile(g, tileX, tileY);
    }

    public void refreshTile(int x, int y) {
	if (bg == null)
	    return;

        bg.translate(-bufferRect.x, -bufferRect.y);
        mapView.paintTile(bg, x, y);
        bg.translate(bufferRect.x, bufferRect.y);
    }
}
