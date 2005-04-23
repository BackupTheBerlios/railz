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

package org.railz;

import java.util.logging.*;

import junit.framework.*;

public class Tests {
    public static Test suite() {
	/* set up logging */
	Logger l = Logger.getLogger("global");
	Level lev;
	while ((lev = l.getLevel()) == null && l.getParent() != null)
	    l = l.getParent();
	String newLevel = System.getProperty(".level");
	if (newLevel == null) 
	    newLevel = "INFO";

	l.setLevel(Level.parse(newLevel));
	while (l.getHandlers().length > 0)
	    l.removeHandler(l.getHandlers()[0]);

	try {
	    Handler h = new FileHandler("tests.log");
	    h.setFormatter(new SimpleFormatter());
	    l.addHandler(h);
	} catch (Exception e) {
	    System.err.println("Couldn't open tests.log:" + e);
	}
	Logger.getLogger("global").log(Level.parse(newLevel),
	       	"Logging enabled at level " + l.getLevel());
	
	TestSuite ts = new TestSuite();
	ts.addTestSuite(org.railz.controller.RouteBuilderPathExplorerTest.class);
	ts.addTestSuite(org.railz.client.ai.tasks.RouteBuilderTest.class);
	ts.addTestSuite(org.railz.client.common.BinaryNumberFormatterTest.class);
	ts.addTestSuite(org.railz.client.common.WorldOverlayTest.class);
	ts.addTestSuite(org.railz.client.top.OverlayMoveExecuterTest.class);
	ts.addTestSuite(org.railz.controller.TrackMoveTransactionsGeneratorTest.class);
	ts.addTestSuite(org.railz.move.AddCargoBundleMoveTest.class);
	ts.addTestSuite(org.railz.move.AddTrainMoveTest.class);
	ts.addTestSuite(org.railz.move.AddTransactionMoveTest.class);
	ts.addTestSuite(org.railz.move.ChangeCargoBundleMoveTest.class);
	ts.addTestSuite(org.railz.move.ChangeProductionAtEngineShopMoveTest.class);
	ts.addTestSuite(org.railz.move.ChangeTrackPieceCompositeMoveTest.class);
	ts.addTestSuite(org.railz.move.ChangeTrackPieceMoveTest.class);
	ts.addTestSuite(org.railz.move.CompositeMoveTest.class);
	ts.addTestSuite(org.railz.move.RemoveCargoBundleMoveTest.class);
	ts.addTestSuite(org.railz.server.DropOffAndPickupCargoMoveGeneratorTest.class);
	ts.addTestSuite(org.railz.world.top.KEYTest.class);
	ts.addTestSuite(org.railz.world.top.NonNullElementsTest.class);
	ts.addTestSuite(org.railz.world.top.WorldImplTest.class);
	ts.addTestSuite(org.railz.world.train.IntLineTest.class);
	ts.addTestSuite(org.railz.world.train.TrainModelTest.class);
	ts.addTestSuite(org.railz.world.train.TrainPathTest.class);
	return ts;
    }
}
