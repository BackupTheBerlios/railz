/*
 * Copyright (C) Luke Lindsay
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
* ChequeredTileView.java
*
* Created on 07 July 2001, 14:25
*/
package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.railz.client.common.ImageManager;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.building.*;

/**
*
* @author  Luke Lindsay
*/
final public class ChequeredTileRenderer extends AbstractTileRenderer {
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return (x + y) % 2;
    }

    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel.getTerrainTypeName(), rgbValues, LAYER_TERRAIN);
        this.setTileIcons(new BufferedImage[2]);
        this.getTileIcons()[0] = imageManager.getImage(generateRelativeFileName(
                    0));
        this.getTileIcons()[1] = imageManager.getImage(generateRelativeFileName(
                    1));
    }

    public ChequeredTileRenderer(ImageManager imageManager, BuildingType
	    buildingType, int [] tileTypes) throws IOException {
	super(buildingType.getName(), tileTypes, LAYER_BUILDING);
	BufferedImage images[] = new BufferedImage[2];
	for (int i = 0; i < images.length; i++) {
	    images[i] = imageManager.getImage(generateRelativeFileName(i));
	}
	setTileIcons(images);
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}
