/*
 * Copyright (C) 2005 Robert Tuck
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
package org.railz.client.ai.tasks;

import java.awt.*;
import java.util.*;
import java.util.logging.*;

import org.railz.client.ai.*;
import org.railz.client.top.*;
import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.common.*;
/**
 * A factory for generating the required moves to lay tracks and purchase an
 * engine. 
 */
final class RouteBuilderMoveFactory {
    private ClientDataProvider aiClient;
    private CityEntry plannedRoute;
    private Logger logger = Logger.getLogger("ai");

    /* Start & end points of route */
    private Point startP = null;
    private Point endP = null;

    public RouteBuilderMoveFactory(ClientDataProvider aic) {
	aiClient = aic;
    }

    /**
     * Generate and submit for processing the moves to build the route and
     * create the engine
     * @param ce the planned route to build
     */ 
    public void processPlannedRoute(CityEntry ce) {
	plannedRoute = ce;
	ArrayList moves = new ArrayList();

	addBuildTrackMoves(moves);
	addBuildStationMoves(moves);
	addBuildStationImprovementMoves(moves);
	addPurchaseEngineMoves(moves);
	addScheduleMoves(moves);
	processMoves(moves);
    }

    private void addBuildTrackMoves(ArrayList moves) {
	TrackMoveProducer tmp = new TrackMoveProducer(aiClient.getWorld(),
		aiClient.getReceiver(),
		aiClient.getPlayerPrincipal());
	tmp.setTrackRule(WorldConstants.get().TR_STANDARD_TRACK);
	tmp.setTrackBuilderMode(TrackMoveProducer.BUILD_TRACK);

	Point oldP = new Point();
	Point newP = new Point(); 
	if (plannedRoute.plannedRoute.size() < 2) {
	    logger.log(Level.WARNING, "Can't build route shorter than 2" +
		    " tiles.");
	    return;
	}

	Iterator i = plannedRoute.plannedRoute.listIterator(0);
	PathExplorer pe = null;
	while (i.hasNext()) {
	    pe = (PathExplorer) i.next();
	    oldP.setLocation(newP);
	    newP.setLocation(pe.getX(), pe.getY());
	    if (startP == null) {
		startP = new Point(pe.getX(), pe.getY());
		continue;
	    }
	    byte d = CompassPoints.unitDeltasToDirection(newP.x - oldP.x,
		    newP.y - oldP.y);
	    MoveStatus ms = tmp.buildTrack(oldP, d);

	    if (ms != MoveStatus.MOVE_OK)
		logger.log(Level.WARNING, "Couldn't build track from " +
			oldP + " to " + newP);
	}
	endP = new Point(pe.getX(), pe.getY());
    }

    private void addBuildStationMoves(ArrayList moves) {
    }

    private void addBuildStationImprovementMoves(ArrayList moves) {
    }

    private void addPurchaseEngineMoves(ArrayList moves) {
    }
    
    private void addScheduleMoves(ArrayList moves) {
    }

    private void processMoves(ArrayList moves) {
	Move m = new CompositeMove(moves);
	aiClient.getReceiver().processMove(m);
    }
}

