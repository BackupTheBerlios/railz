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
 * File:           Track_TilesParser.java
 * Date:           21 January 2002  18:00
 *
 * @author  lindsal
 * @version generated by FFJ XML module
 */
package jfreerails.server.parser;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into Track_TilesHandler events.
 * <p>Usage sample:
 * <pre>
 *    Track_TilesParser parser = new Track_TilesParser(...);
 *    parser.parse(new InputSource("..."));
 * </pre>
 * <p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>
 */
final public class Track_TilesParser implements org.xml.sax.ContentHandler {
    private java.lang.StringBuffer buffer;
    private Track_TilesParslet parslet;
    private Track_TilesHandler handler;

    /**
     *  Object
     */
    private java.util.Stack context;

    public Track_TilesParser(final Track_TilesHandler handler,
        final Track_TilesParslet parslet) {
        this.parslet = parslet;
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new java.util.Stack();
    }

    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(java.lang.String ns, java.lang.String name,
        java.lang.String qname, org.xml.sax.Attributes attrs)
        throws SAXException {
        dispatch(true);
        context.push(new Object[] {
                qname, new org.xml.sax.helpers.AttributesImpl(attrs)
            });

        if ("CanOnlyBuildOnTheseTerrainTypes".equals(qname)) {
            handler.start_CanOnlyBuildOnTheseTerrainTypes(attrs);
        } else if ("ListOfTrackPieceTemplates".equals(qname)) {
            handler.start_ListOfTrackPieceTemplates(attrs);
        } else if ("TrackType".equals(qname)) {
            handler.start_TrackType(attrs);
        } else if ("TerrainType".equals(qname)) {
            handler.handle_TerrainType(attrs);
        } else if ("Tiles".equals(qname)) {
            handler.start_Tiles(attrs);
        } else if ("TrackPieceTemplate".equals(qname)) {
            handler.start_TrackPieceTemplate(attrs);
        }
    }

    public void endElement(java.lang.String ns, java.lang.String name,
        java.lang.String qname) throws SAXException {
        dispatch(false);
        context.pop();

        if ("CanOnlyBuildOnTheseTerrainTypes".equals(qname)) {
            handler.end_CanOnlyBuildOnTheseTerrainTypes();
        } else if ("ListOfTrackPieceTemplates".equals(qname)) {
            handler.end_ListOfTrackPieceTemplates();
        } else if ("TrackType".equals(qname)) {
            handler.end_TrackType();
        } else if ("Tiles".equals(qname)) {
            handler.end_Tiles();
        } else if ("TrackPieceTemplate".equals(qname)) {
            handler.end_TrackPieceTemplate();
        }
    }

    public void characters(char[] chars, int start, int len)
        throws SAXException {
        buffer.append(chars, start, len);
    }

    public void ignorableWhitespace(char[] chars, int start, int len)
        throws SAXException {
    }

    public void processingInstruction(java.lang.String target,
        java.lang.String data) throws SAXException {
    }

    public void startPrefixMapping(final java.lang.String prefix,
        final java.lang.String uri) throws SAXException {
    }

    public void endPrefixMapping(final java.lang.String prefix)
        throws SAXException {
    }

    public void skippedEntity(java.lang.String name) throws SAXException {
    }

    private void dispatch(final boolean fireOnlyIfMixed)
        throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) {
            return; //skip it
        }

        Object[] ctx = (Object[])context.peek();
        String here = (String)ctx[0];
        org.xml.sax.Attributes attrs = (org.xml.sax.Attributes)ctx[1];
        buffer.delete(0, buffer.length());
    }

    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final InputSource input)
        throws SAXException, ParserConfigurationException, IOException {
        parse(input, this);
    }

    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final java.net.URL url)
        throws SAXException, ParserConfigurationException, IOException {
        parse(new InputSource(url.toExternalForm()), this);
    }

    /**
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final InputSource input,
        final Track_TilesHandler handler, final Track_TilesParslet parslet)
        throws SAXException, ParserConfigurationException, IOException {
        parse(input, new Track_TilesParser(handler, parslet));
    }

    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final java.net.URL url,
        final Track_TilesHandler handler, final Track_TilesParslet parslet)
        throws SAXException, ParserConfigurationException, IOException {
        parse(new InputSource(url.toExternalForm()), handler, parslet);
    }

    private static void parse(final InputSource input,
        final Track_TilesParser recognizer)
        throws SAXException, ParserConfigurationException, IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(true); //the code was generated according DTD
        factory.setNamespaceAware(false); //the code was generated according DTD

        org.xml.sax.XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setContentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());
        parser.parse(input);
    }

    private org.xml.sax.ErrorHandler getDefaultErrorHandler() {
        return new org.xml.sax.ErrorHandler() {
                public void error(org.xml.sax.SAXParseException ex)
                    throws SAXException {
                    if (context.isEmpty()) {
                        System.err.println("Missing DOCTYPE.");
                    }

                    throw ex;
                }

                public void fatalError(org.xml.sax.SAXParseException ex)
                    throws SAXException {
                    throw ex;
                }

                public void warning(org.xml.sax.SAXParseException ex)
                    throws SAXException {
                    // ignore
                }
            };
    }
}
