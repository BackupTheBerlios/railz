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
 * File:           CargoAndTerrainParser.java
 * Date:           27 April 2003  21:34
 *
 * @author  Luke
 * @version generated by NetBeans XML module
 */
package org.railz.server.parser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.railz.world.top.*;

/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into RulesHandler events.
 * <p>Usage sample:
 * <pre>
 *    RulesParser parser = new RulesParser(...);
 *    parser.parse(new InputSource("..."));
 * </pre>
 * <p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>
 *
 */
public class CargoAndTerrainParser implements ContentHandler {
    private java.lang.StringBuffer buffer;
    private CargoAndTerrainHandler handler;
    private java.util.Stack context;
    private EntityResolver resolver;
    private EngineTypesHandler engineTypesHandler;
    private String currentSection;
    private EconomyHandler economyHandler;
    private StationImprovementsHandler stationImprovementsHandler;
    private WagonTypesHandler wagonTypesHandler;

    /**
     * Creates a parser instance.
     * @param w the world model to initialise.
     * @param resolver SAX entity resolver implementation or <code>null</code>.
     * It is recommended that it could be able to resolve at least the DTD.
     */
    private CargoAndTerrainParser(World w,
        final EntityResolver resolver) {
        handler = new CargoAndTerrainHandler(w);
	engineTypesHandler = new EngineTypesHandler(w);
	economyHandler = new EconomyHandler(w);
	stationImprovementsHandler = new StationImprovementsHandler(w);
	wagonTypesHandler = new WagonTypesHandler(w);
        this.resolver = resolver;

        buffer = new StringBuffer(111);
        context = new java.util.Stack();
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void setDocumentLocator(Locator locator) {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startDocument() throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endDocument() throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startElement(java.lang.String ns, java.lang.String name,
        java.lang.String qname, Attributes attrs) throws SAXException {
        dispatch(true);
        context.push(new Object[] {
                qname, new org.xml.sax.helpers.AttributesImpl(attrs)
            });

	if ("Cargo_Types".equals(currentSection) ||
		"Terrain_Types".equals(currentSection) ||
		"Building_Types".equals(currentSection)) {
	   handler.startElement(ns, name, qname, attrs);
	} else if ("EngineTypes".equals(currentSection)) {
	    engineTypesHandler.startElement(ns, name, qname, attrs);
	} else if ("Economy".equals(currentSection)) {
	   economyHandler.startElement(ns, name, qname, attrs);
	} else if ("StationImprovements".equals(currentSection)) {
	    stationImprovementsHandler.startElement(ns, name, qname, attrs);
	} else if ("WagonTypes".equals(currentSection)) {
	    wagonTypesHandler.startElement(ns, name, qname, attrs);
	} else if ("EngineTypes".equals(name) ||
		"Economy".equals(name) ||
		"StationImprovements".equals(name) ||
		"Cargo_Types".equals(name) ||
		"Terrain_Types".equals(name) ||
		"Building_Types".equals(name) ||
		"WagonTypes".equals(name)) {
	    currentSection = name;
	    // recurse
	    startElement(ns, name, qname, attrs);
	}
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endElement(java.lang.String ns, java.lang.String name,
        java.lang.String qname) throws SAXException {
        dispatch(false);
        context.pop();

	if ("Cargo_Types".equals(currentSection) ||
		"Terrain_Types".equals(currentSection) ||
		"Building_Types".equals(currentSection)) {
	   handler.endElement(ns, name, qname);
	} else if ("EngineTypes".equals(currentSection)) {
	    engineTypesHandler.endElement(ns, name, qname);
	} else if ("Economy".equals(currentSection)) {
	    economyHandler.endElement(ns, name, qname);
	} else if ("StationImprovements".equals(currentSection)) {
	    stationImprovementsHandler.endElement(ns, name, qname);
	} else if ("WagonTypes".equals(currentSection)) {
	    wagonTypesHandler.endElement(ns, name, qname);
	}
        if ("EngineTypes".equals(name) ||
		"Economy".equals(name) ||
		"StationImprovements".equals(name) ||
		"Cargo_Types".equals(name) ||
		"Terrain_Types".equals(name) ||
		"Building_Types".equals(name) ||
		"WagonTypes".equals(name)) {
	    currentSection = null;
	}
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void characters(char[] chars, int start, int len)
        throws SAXException {
        buffer.append(chars, start, len);
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void ignorableWhitespace(char[] chars, int start, int len)
        throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void processingInstruction(java.lang.String target,
        java.lang.String data) throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startPrefixMapping(final java.lang.String prefix,
        final java.lang.String uri) throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endPrefixMapping(final java.lang.String prefix)
        throws SAXException {
    }

    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void skippedEntity(java.lang.String name)
        throws SAXException {
    }

    private void dispatch(final boolean fireOnlyIfMixed)
        throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) {
            return; //skip it
        }

        Object[] ctx = (Object[])context.peek();
        String here = (String)ctx[0];
        Attributes attrs = (Attributes)ctx[1];
        buffer.delete(0, buffer.length());
    }

    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public void parse(final InputSource input)
        throws SAXException, javax.xml.parsers.ParserConfigurationException, 
            java.io.IOException {
		    parse(input, this);
    }

    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public void parse(final java.net.URL url)
        throws SAXException, javax.xml.parsers.ParserConfigurationException, 
            java.io.IOException {
        parse(new InputSource(url.toExternalForm()), this);
    }

    /**
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public static void parse(final InputSource input,
        World w)
        throws SAXException, javax.xml.parsers.ParserConfigurationException, 
            java.io.IOException {
        parse(input, new CargoAndTerrainParser(w, null));
    }

    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public static void parse(final java.net.URL url,
        final World w)
        throws SAXException, javax.xml.parsers.ParserConfigurationException, 
            java.io.IOException {
		try {
		    parse(new InputSource(url.toExternalForm()), w);
		} catch (SAXParseException e) {
		    System.out.println("Parse exception " + e.getMessage() +
			    " at line " + e.getLineNumber());
		    if (e.getException() != null)
			e.getException().printStackTrace();
		    throw e;
		}
    }

    private static void parse(final InputSource input,
        final CargoAndTerrainParser recognizer)
        throws SAXException, javax.xml.parsers.ParserConfigurationException, 
            java.io.IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(true); //the code was generated according DTD
        factory.setNamespaceAware(true); //the code was generated according DTD

        XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setContentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());

        if (recognizer.resolver != null) {
            parser.setEntityResolver(recognizer.resolver);
        }

        parser.parse(input);
    }

    /**
     * Creates default error handler used by this parser.
     * @return org.xml.sax.ErrorHandler implementation
     *
     */
    protected ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
                public void error(SAXParseException ex)
                    throws SAXException {
                    if (context.isEmpty()) {
                        System.err.println("Missing DOCTYPE.");
                    }

                    throw ex;
                }

                public void fatalError(SAXParseException ex)
                    throws SAXException {
                    throw ex;
                }

                public void warning(SAXParseException ex)
                    throws SAXException {
                    // ignore
                }
            };
    }
}
