/*
 * Copyright (C) 2004 Robert Tuck
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
/**
 * Parses the map.xml file
 */
package org.railz.server.parser;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.railz.world.top.*;
import org.railz.world.city.*;
import org.railz.world.player.*;

public class MapHandler extends DefaultHandler {
    private CitySAXParser cityParser;
    private ScriptingEventHandler scriptingEventHandler;
    private String currentElement = null;

    public MapHandler(World w) throws SAXException {
	cityParser = new CitySAXParser(w);
	scriptingEventHandler = new ScriptingEventHandler(w);
    }

    public void startElement(String namespaceURI, String sName, String qName,
        Attributes attrs) throws SAXException {
	try {
	if (currentElement == null) {
	    if (qName.equals("Cities")) {
		currentElement="Cities";
		startElement(namespaceURI, sName, qName, attrs);
	    } else if (qName.equals("Events")) {
		currentElement = qName;
		startElement(namespaceURI, sName, qName, attrs);
	    }
	    return;
	} 
	if ("Cities".equals(currentElement)) {
	    cityParser.startElement(sName, qName, attrs);
	} else if ("Events".equals(currentElement)) {
	    scriptingEventHandler.startElement(sName, qName, attrs);
	}
	} catch (Exception e) {
	    System.out.println ("startElement caught " + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void endElement(String uri, String localName, String qName) throws
	SAXException {
	    try {
	if ("Cities".equals(currentElement)) {
	    cityParser.endElement(localName, qName);
	} else if ("Events".equals(currentElement)) {
	    scriptingEventHandler.endElement(localName, qName);
	}
	if ("Cities".equals(qName) ||
		"Events".equals(qName)) {
	    currentElement = null;
	}
	    } catch (Exception e) {
		System.out.println("endElement caught exception " +
			e.getMessage());
		e.printStackTrace();
	    }
    }
}

