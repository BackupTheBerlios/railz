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
 * File:           RulesHandler.java
 * Date:           27 April 2003  21:34
 *
 * @author  Luke
 * @version generated by NetBeans XML module
 */
package org.railz.server.parser;

import org.xml.sax.*;

import java.util.*;

import org.railz.world.cargo.CargoType;
import org.railz.world.building.*;
import org.railz.world.building.BuildingType.DistributionParams;
import org.railz.world.common.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.*;
import org.railz.world.train.TransportCategory;
import org.railz.world.player.Player;

class CargoAndTerrainHandler {
    private final World world;

    //ArrayList cargoTypes = new ArrayList();
    //ArrayList terrainTypes = new ArrayList();
    HashMap cargoName2cargoTypeNumber = new HashMap();
    HashSet rgbValuesAlreadyUsed = new HashSet();

    long tileBaseValue;
    String tileID;
    int tileCategory;
    int tileRGB;
    private int tileElevation;
    private int tileRoughness;

    ArrayList typeConsumes = new ArrayList();
    ArrayList typeProduces = new ArrayList();
    ArrayList typeConverts = new ArrayList();
    ArrayList trackTemplates = new ArrayList();

    /** Array of TerrainTypeElement */
    private ArrayList neighbouringTerrainTypes = new ArrayList();
    /** Array of TerrainTypeElement */
    private ArrayList acceptableTerrainTypes = new ArrayList();
    /** Array of TerrainTypeElement */
    private ArrayList terrainTypes;
    boolean isStation;
    long maintenance;
    int stationRadius;

    /** See comments in DTD for meanings of a, b, c */
    private class TerrainTypeElement {
	/** Index into TERRAIN_TYPES table */
	public int terrainType;
	public DistributionParams distributionParams;

	public TerrainTypeElement(int terrainType, double a, double b, double
		c) {
	    this.terrainType = terrainType;
	    distributionParams = new DistributionParams(a, b, c);
	}
    }

    CargoAndTerrainHandler(World w) {
        world = w;
    }

    private void handle_Converts(final Attributes meta)
        throws SAXException {
        String inputCargo = meta.getValue("input");
        String outputCargo = meta.getValue("output");

        int input = string2CargoNumber(inputCargo);
        int output = string2CargoNumber(outputCargo);
        Conversion conversion = new Conversion(input, output);
        typeConverts.add(conversion);
    }

    private void start_Tile(final Attributes meta) throws SAXException {
        tileID = meta.getValue("id");
        String category = meta.getValue("Category");
	if ("River".equals(category))
	    tileCategory = TerrainType.CATEGORY_RIVER;
	else if ("Ocean".equals(category))
	    tileCategory = TerrainType.CATEGORY_OCEAN;
	else if ("Hill".equals(category))
	    tileCategory = TerrainType.CATEGORY_HILL;
	else if ("Country".equals(category))
	    tileCategory = TerrainType.CATEGORY_COUNTRY;
	else
	    throw new SAXException("Unknown terrain type");

        String rgbString = meta.getValue("rgb");
        tileRGB = string2RGBValue(rgbString);

	String baseValue = meta.getValue("baseValue");
	tileBaseValue = Long.parseLong(baseValue);
	tileElevation = Integer.parseInt(meta.getValue("elevation"));
	tileRoughness = Integer.parseInt(meta.getValue("roughness"));

        //Check if another type is already using this rgb value..
        Integer rgbInteger = new Integer(tileRGB);

        if (rgbValuesAlreadyUsed.contains(rgbInteger)) {
            throw new SAXException(tileID + " can't using rgb value " +
                rgbString + " because it is being used by another tile type!");
        } else {
            rgbValuesAlreadyUsed.add(rgbInteger);
        }
    }

    private void end_Tile() throws SAXException {
	TerrainType terrainType = new TerrainType(tileRGB, tileCategory,
		tileID, tileBaseValue, tileElevation, tileRoughness);

        world.add(KEY.TERRAIN_TYPES, terrainType, Player.AUTHORITATIVE);
    }

    private void handle_Cargo(final Attributes meta) throws SAXException {
        String cargoID = meta.getValue("id");
        String cargoCategory = meta.getValue("Category");
	long baseValue = Long.parseLong(meta.getValue("baseValue"));
	int halfLife = Integer.parseInt(meta.getValue("halfLife"));
	int expiryTime = Integer.parseInt(meta.getValue("expiryTime"));

	cargoCategory = cargoCategory.replace('_', ' ');
	CargoType cargoType = new CargoType(cargoID,
		TransportCategory.parseString(cargoCategory), baseValue,
		halfLife, expiryTime);

        int cargoNumber = world.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
        cargoName2cargoTypeNumber.put(cargoID, new Integer(cargoNumber));
        world.add(KEY.CARGO_TYPES, cargoType, Player.AUTHORITATIVE);
    }

    private void start_Cargo_Types(final Attributes meta)
        throws SAXException {
        //no need to do anything here.
    }

    private void end_Cargo_Types() throws SAXException {
        //no need to do anything here.
    }

    private void start_Terrain_Types(final Attributes meta)
        throws SAXException {
        //no need to do anything here.
    }

    private void end_Terrain_Types() throws SAXException {
        //no need to do anything here.
    }

    private void handle_Building_Types(final Attributes meta) throws
	SAXException {
	    // do nothing
	}

    private void end_Building_Type() throws SAXException {
        Consumption[] consumes = new Consumption[typeConsumes.size()];

        for (int i = 0; i < typeConsumes.size(); i++) {
            consumes[i] = (Consumption)typeConsumes.get(i);
        }

        Production[] produces = new Production[typeProduces.size()];

        for (int i = 0; i < typeProduces.size(); i++) {
            produces[i] = (Production)typeProduces.get(i);
        }

        Conversion[] converts = new Conversion[typeConverts.size()];

        for (int i = 0; i < typeConverts.size(); i++) {
            converts[i] = (Conversion)typeConverts.get(i);
        }

	BuildingType buildingType;
	byte[] tts = new byte[trackTemplates.size()];
	for (int i = 0; i < trackTemplates.size(); i++) {
	    tts[i] = ((Byte) trackTemplates.get(i)).byteValue();
	}
	    
	boolean[] att = new boolean[world.size(KEY.TERRAIN_TYPES,
		Player.AUTHORITATIVE)];
	boolean[] ntt = new boolean[world.size(KEY.TERRAIN_TYPES,
	    Player.AUTHORITATIVE)];
	DistributionParams[] dp = new DistributionParams
	    [world.size(KEY.TERRAIN_TYPES, Player.AUTHORITATIVE)];

	for (int i = 0; i < acceptableTerrainTypes.size(); i++) {
	    att[((TerrainTypeElement) acceptableTerrainTypes.get(i))
		.terrainType] = true;
	    dp[((TerrainTypeElement) acceptableTerrainTypes.get(i))
		.terrainType] = ((TerrainTypeElement)
			acceptableTerrainTypes.get(i)).distributionParams;
	}

	for (int i = 0; i < neighbouringTerrainTypes.size(); i++)
	    ntt[((TerrainTypeElement) neighbouringTerrainTypes.get(i))
		.terrainType] = true;
	
	if (isStation) {
	    buildingType = new BuildingType(tileID, tileBaseValue,
		    stationRadius, tts, att, ntt, dp);
	} else {
	    buildingType = new BuildingType(tileID, produces,
		    consumes, converts, tileBaseValue, tileCategory, tts, att,
		    ntt, dp);
	}

	world.add(KEY.BUILDING_TYPES, buildingType, Player.AUTHORITATIVE);
    }

    private void start_Building_Type(final Attributes meta) throws SAXException
	{
	    typeConsumes.clear();
	    typeProduces.clear();
	    typeConverts.clear();
	    trackTemplates.clear();
	    tileID = meta.getValue("id");
	    String category = meta.getValue("category");
	    if ("Urban".equals(category))
		tileCategory = BuildingType.CATEGORY_URBAN;
	    else if ("Resource".equals(category))
		tileCategory = BuildingType.CATEGORY_RESOURCE;
	    else if ("Industry".equals(category))
		tileCategory = BuildingType.CATEGORY_INDUSTRY;
	    else if ("Station".equals(category))
		tileCategory = BuildingType.CATEGORY_STATION;
	    else 
		throw new SAXException("Unrecognised building type");
	    tileBaseValue = Long.parseLong(meta.getValue("baseValue"));
	    maintenance = Long.parseLong(meta.getValue("maintenance"));
	    isStation = "Station".equals(category);
	    if (isStation) {
		stationRadius =
		    Integer.parseInt(meta.getValue("stationRadius"));
	    }
	}

    private void start_Types(final Attributes meta) throws SAXException {
        //no need to do anything here.
    }

    private void end_Types() throws SAXException {
        //no need to do anything here.
    }

    private void handle_Consumes(final Attributes meta)
        throws SAXException {
        int cargoConsumed = string2CargoNumber(meta.getValue("Cargo"));
        String prerequisisteString = meta.getValue("Prerequisiste");

        //"Prerequisiste" is an optional attribute, so may be null. 
        int prerequisisteForConsumption = (null == prerequisisteString ? 1
                                                                       : Integer.parseInt(prerequisisteString));
        Consumption consumption = new Consumption(cargoConsumed,
                prerequisisteForConsumption);
        typeConsumes.add(consumption);
    }

    private void handle_Produces(final Attributes meta)
        throws SAXException {
        int cargoProduced = string2CargoNumber(meta.getValue("Cargo"));
        int rateOfProduction = Integer.parseInt(meta.getValue("Rate"));
        Production production = new Production(cargoProduced, rateOfProduction);
        typeProduces.add(production);
    }

    private int string2RGBValue(String temp_number) {
        int rgb = (int)Integer.parseInt(temp_number, 16);

        /*
        *  We need to change the format of the rgb value to the same one as used
        *  by the the BufferedImage that stores the map.  See org.railz.common.Map
        */
        rgb = new java.awt.Color(rgb).getRGB();

        return rgb;
    }

    /** Returns the index number of the cargo with the specified name. */
    private int string2CargoNumber(String cargoName) throws SAXException {
        if (cargoName2cargoTypeNumber.containsKey(cargoName)) {
            Integer integer = (Integer)cargoName2cargoTypeNumber.get(cargoName);

            return integer.intValue();
        } else {
            throw new SAXException("Unknown cargo type: " + cargoName);
        }
    }
    
    private void handle_trackPieceTemplate(final Attributes meta) throws
	SAXException {
	    String trackTemplate = meta.getValue("trackTemplate");
	    trackTemplates.add(new
		    Byte(CompassPoints.nineBitToEightBit(trackTemplate)));
	}

    private void start_NeighbouringTerrainTypes(final Attributes meta) throws
	SAXException {
	    neighbouringTerrainTypes.clear();
	    terrainTypes = neighbouringTerrainTypes;
	}

    private void end_NeighbouringTerrainTypes() throws
	SAXException {
	    // do nothing
	}

    private void start_AcceptableTerrainTypes (final Attributes meta) throws
	SAXException {
	    acceptableTerrainTypes.clear();
	    terrainTypes = acceptableTerrainTypes;
	}

    private void end_AcceptableTerrainTypes () throws SAXException {
	// do nothing
    }

    private void handle_AllTerrainTypes(final Attributes meta) throws
	SAXException {
	    int n = world.size(KEY.TERRAIN_TYPES, Player.AUTHORITATIVE);
	    for (int i = 0; i < n; i++) {
		terrainTypes.add(new TerrainTypeElement(i, 0, 1, 0));
	    }
	}

    private void handle_TerrainType(final Attributes meta) throws SAXException {
	int n = world.size(KEY.TERRAIN_TYPES, Player.AUTHORITATIVE);
	String type = meta.getValue("terrainType");
	double a = Double.parseDouble(meta.getValue("a"));
	double b = Double.parseDouble(meta.getValue("b"));
	double c = Double.parseDouble(meta.getValue("c"));
	for (int i = 0; i < n; i++) {
	    if (((TerrainType) world.get(KEY.TERRAIN_TYPES, i,
			    Player.AUTHORITATIVE)).getTerrainTypeName().
		    equals(type)) {
		terrainTypes.add(new TerrainTypeElement(i, a, b, c));
		break;
	    }
	}
    }
    
    public void startElement(String ns, String name, String qname, Attributes
	    attrs) throws SAXException {
	if ("Converts".equals(name)) {
	    handle_Converts(attrs);
	} else if ("Tile".equals(name)) {
	    start_Tile(attrs);
	} else if ("Cargo".equals(name)) {
	    handle_Cargo(attrs);
	} else if ("Cargo_Types".equals(name)) {
	    start_Cargo_Types(attrs);
	} else if ("Terrain_Types".equals(name)) {
	    start_Terrain_Types(attrs);
	} else if ("Types".equals(name)) {
	    start_Types(attrs);
	} else if ("Consumes".equals(name)) {
	    handle_Consumes(attrs);
	} else if ("Produces".equals(name)) {
	    handle_Produces(attrs);
	} else if ("Building_Types".equals(name)) {
	    handle_Building_Types(attrs);
	} else if ("Building_Type".equals(name)) {
	    start_Building_Type(attrs);
	} else if ("TrackPieceTemplate".equals(name)) {
	    handle_trackPieceTemplate(attrs);
	} else if ("NeighbouringTerrainTypes".equals(name)) {
	    start_NeighbouringTerrainTypes(attrs);
	} else if ("AcceptableTerrainTypes".equals(name)) {
	    start_AcceptableTerrainTypes(attrs);
	} else if ("TerrainType".equals(name)) {
	    handle_TerrainType(attrs);
	} else if ("AllTerrainTypes".equals(name)) {
	    handle_AllTerrainTypes(attrs);
	}
    }

    public void endElement(String ns, String name, String qname) throws
	SAXException {
	    if ("Tile".equals(name)) {
		end_Tile();
	    } else if ("Cargo_Types".equals(name)) {
		end_Cargo_Types();
	    } else if ("Terrain_Types".equals(name)) {
		end_Terrain_Types();
	    } else if ("Types".equals(name)) {
		end_Types();
	    } else if ("Building_Type".equals(name)) {
		end_Building_Type();
	    } else if ("NeighbouringTerrainTypes".equals(name)) {
		end_NeighbouringTerrainTypes();
	    } else if ("AcceptableTerrainTypes".equals(name)) {
		end_AcceptableTerrainTypes();
	    }
	}
}
