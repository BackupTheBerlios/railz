/**
 * The tabbed panel that sits in the lower right hand corner of the screen
 */

/*
 * $Id: TrainsJTabPane.java,v 1.1 2004/02/28 02:05:23 rtuck99 Exp $
 */

package jfreerails.client.view;

import java.awt.Point;

import javax.swing.JTabbedPane;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;

public class TrainsJTabPane extends JTabbedPane implements CursorEventListener {
    private TerrainInfoJPanel terrainInfoPanel;
    private StationInfoJPanel stationInfoPanel;
    private TrainDialogueJPanel trainSchedulePanel;
    private ReadOnlyWorld world;
    private BuildJPane buildJPane;

    public TrainsJTabPane() {
	/* set up trainsJTabbedPane */
        /*
	 * XXX Don't use SCROLL_TAB_LAYOUT as tooltips don't work (JDK bug)
	 * setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	 */
        
	terrainInfoPanel = new TerrainInfoJPanel();
	stationInfoPanel = new StationInfoJPanel();
	trainSchedulePanel = new TrainDialogueJPanel();
	buildJPane = new BuildJPane();
    }
    
    public void setMapCursor(MapCursor mapCursor){
		stationInfoPanel.setMapCursor(mapCursor);
    }
	
    public void setup(ReadOnlyWorld w,  ViewLists vl, ModelRoot modelRoot) {	
	world = w;
	
	addTab(null, vl.getImageIcon("terrain_info"), terrainInfoPanel, 
		"Terrain Info");
	addTab(null, vl.getImageIcon("station_info"), stationInfoPanel,
		"Station Info");
	addTab(null, vl.getImageIcon("schedule"), trainSchedulePanel,
		"Train Schedule");
 	addTab(null, vl.getImageIcon("build"), buildJPane, "Build");

	terrainInfoPanel.setup(world, vl);
	stationInfoPanel.setup(world, vl);
	trainSchedulePanel.setup(world, vl, modelRoot, modelRoot);
 	buildJPane.setup(vl, modelRoot);
        
	stationInfoPanel.display();
        	
    }

    private void updateTerrainInfo(CursorEvent e) {
        
	Point p = e.newPosition;
	terrainInfoPanel.setTerrainType(world.getTile(p.x,
		    p.y).getTerrainTypeNumber());
    }
    
    /**
     * Implements {CursorEventListener#cursorOneTileMove}
     */
    public void cursorOneTileMove(CursorEvent e) {
        updateTerrainInfo(e);
    }

    /**
     * Implements {CursorEventListener#cursorJumped}
     */
    public void cursorJumped(CursorEvent e) {
        updateTerrainInfo(e);
    }

    /**
     * Implements {CursorEventListener#cursorKeyPressed}
     */
    public void cursorKeyPressed(CursorEvent e) {
	// do nothing
    }
}

