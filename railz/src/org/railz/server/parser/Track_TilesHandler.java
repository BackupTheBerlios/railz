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
 * File:           Track_TilesHandler.java
 * Date:           21 January 2002  18:00
 *
 * @author  lindsal
 * @version generated by FFJ XML module
 */
package org.railz.server.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public interface Track_TilesHandler {
    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    void start_CanOnlyBuildOnTheseTerrainTypes(final Attributes meta)
        throws SAXException;

    /**
     * A container element end event handling method.
     */
    void end_CanOnlyBuildOnTheseTerrainTypes() throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    void start_ListOfTrackPieceTemplates(final Attributes meta)
        throws SAXException;

    /**
     * A container element end event handling method.
     */
    void end_ListOfTrackPieceTemplates() throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    void start_TrackType(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     */
    void end_TrackType() throws SAXException;

    /**
     * An empty element event handling method.
     * @param meta attributes for the TerrainType element
     */
    void handle_TerrainType(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    void start_Tiles(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     */
    void end_Tiles() throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    void start_TrackPieceTemplate(final Attributes meta)
        throws SAXException;

    /**
     * A container element end event handling method.
     */
    void end_TrackPieceTemplate() throws SAXException;
}
