/*
 * File:           Track_TilesParsletImpl.java
 * Date:           21 January 2002  18:00
 *
 * @author  lindsal
 * @version generated by FFJ XML module
 */
package jfreerails.server.parser;

import org.xml.sax.SAXException;


final public class Track_TilesParsletImpl implements Track_TilesParslet {
    public int template(final java.lang.String data) throws SAXException {
        try {
            return Integer.parseInt(data.trim());
        } catch (IllegalArgumentException ex) {
            throw new SAXException("template(" + data.trim() + ")", ex);
        }
    }

    public int[][] routesList(final java.lang.String data)
        throws SAXException {
        throw new SAXException("Not implemented yet.");
    }

    public int[] templatesList(final java.lang.String data)
        throws SAXException {
        throw new SAXException("Not implemented yet.");
    }
}