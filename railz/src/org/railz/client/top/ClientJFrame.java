/*
 * Copyright (C) 2003 Luke Lindsay
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
 * ClientJFrame.java
 *
 * Created on 01 June 2003, 15:56
 */

package org.railz.client.top;

import java.awt.event.KeyEvent;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.railz.client.common.*;
import org.railz.client.view.MapViewJComponent;
import org.railz.client.view.MapViewJComponentConcrete;
import org.railz.client.view.CashJLabel;
import org.railz.client.view.DateJLabel;
import org.railz.client.view.GUIRoot;
import org.railz.client.view.HelpMenu;
import org.railz.client.view.DisplayMenu;
import org.railz.client.view.GameMenu;
import org.railz.client.view.DebugMenu;
import org.railz.client.view.OverviewMapJComponent;
import org.railz.client.view.TrainsJTabPane;
import org.railz.client.model.ModelRoot;
import org.railz.world.top.KEY;
import org.railz.world.player.Player;
import org.railz.world.player.PlayerPrincipal;

/**
 *
 * @author  Luke
 */
public class ClientJFrame extends javax.swing.JFrame implements
UpdatedComponent {
    private boolean debugMenuEnabled = 
	(System.getProperty("DEBUG") != null);
    
    private GUIRoot guiRoot;
    private ModelRoot modelRoot;
    private MapViewJComponentConcrete mapViewJComponent;
    private int frameCount = 0;
    
    public ClientJFrame(GUIRoot gr, ModelRoot mr) {
	modelRoot = mr;
	guiRoot = gr;
	mapViewJComponent = new MapViewJComponentConcrete();
	initComponents();
	if (debugMenuEnabled) {
	    jMenuBar1.add(new DebugMenu(modelRoot));
	}

	jSplitPane1.revalidate();
	jSplitPane1.resetToPreferredSizes();

	/* Hack to stop F8 grabbing the focus of the SplitPane (see javadoc
	 * for JSplitPane Key assignments */
	InputMap im = jSplitPane1.getInputMap
	    (JSplitPane.WHEN_IN_FOCUSED_WINDOW);
	im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
	jSplitPane1.setInputMap(JSplitPane.WHEN_IN_FOCUSED_WINDOW, im);
	im = jSplitPane1.getInputMap
	    (JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);   
	jSplitPane1.setInputMap(JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
		null);
	jSplitPane1.revalidate();
	jSplitPane1.resetToPreferredSizes();
	
    }

    private Runnable setupImpl = new Runnable() {
	/**
	 * Execute from the swing event thread to avoid any threading problems
	 */
	public void run() {
	    mainMapView.setViewportView(mapViewJComponent);
	    mapViewJComponent.setup(guiRoot, modelRoot);
	    ((OverviewMapJComponent) mapOverview).setup(modelRoot);
	    ((DateJLabel) datejLabel).setup(modelRoot);
	    ((CashJLabel) cashjLabel).setup(modelRoot);
	    ((TrainsJTabPane) trainsJTabPane1).setup(modelRoot, guiRoot);
	    ((BuildMenu) buildMenu).setup(modelRoot);
	    setTitle("Railz Client");
	    guiRoot.getMapMediator().setMainMap
		(mainMapView.getViewport(), mapViewJComponent);
	    ((GameMenu) gameMenu).setup();
	    if (guiRoot.getScreenHandler().getMode() ==
		    ScreenHandler.WINDOWED_MODE)
		setSize(800, 550);

	    /* needed on Mac OS X */
	    show();
	}
    };

    /**
     * Calles when the world model has changed and the ViewLists are updated
     */
    public void setup() {
	SwingUtilities.invokeLater(setupImpl);
    }
    
    public void doFrameUpdate(Graphics g) {
	Container cp = getContentPane();
	g.translate(cp.getX() + jSplitPane1.getX() +
	       	lhsjPanel.getX() + mainMapView.getX(),
	       	cp.getY() + jSplitPane1.getY() +
	       	lhsjPanel.getY() + mainMapView.getY());
	JViewport vp = mainMapView.getViewport();
	Point vpLocation = vp.getLocation();
	g.setClip(vpLocation.x, vpLocation.y, vp.getWidth(), vp.getHeight() );
	g.translate(vpLocation.x, vpLocation.y);
	mapViewJComponent.doFrameUpdate(g);

	RepaintManager repaintManager = RepaintManager.currentManager
	    (mainMapView);
	repaintManager.markCompletelyClean(mainMapView);
	if (frameCount % 4 == 0) {
	    g.translate(- vpLocation.x - mainMapView.getX() +
		    statusjPanel.getX(),
		    - vpLocation.y - mainMapView.getY() +
		   statusjPanel.getY());
	    g.setClip(0, 0, statusjPanel.getWidth(), statusjPanel.getHeight());
	    statusjPanel.paint(g);
	    RepaintManager.currentManager(statusjPanel).
		markCompletelyClean(statusjPanel);
	}
	if (++frameCount == 4)
	    frameCount = 0;
    }

    public boolean isDoubleBuffered() {
	return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        rhsjPanel = new javax.swing.JPanel();
        mapOverview = new OverviewMapJComponent(guiRoot);
        trainsJTabPane1 = new TrainsJTabPane();
        lhsjPanel = new javax.swing.JPanel();
        mainMapView = new javax.swing.JScrollPane();
        statusjPanel = new javax.swing.JPanel();
        datejLabel = new DateJLabel();
        cashjLabel = new CashJLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        gameMenu = new GameMenu(modelRoot, guiRoot);
        buildMenu = new BuildMenu();
        displayMenu = new DisplayMenu(guiRoot, modelRoot);
        helpMenu = new HelpMenu(guiRoot);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jSplitPane1.setResizeWeight(0.8);
        rhsjPanel.setLayout(new java.awt.GridBagLayout());

        rhsjPanel.add(mapOverview, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rhsjPanel.add(trainsJTabPane1, gridBagConstraints);

        jSplitPane1.setRightComponent(rhsjPanel);

        lhsjPanel.setLayout(new java.awt.GridBagLayout());

        mainMapView.setAlignmentX(0.0F);
        mainMapView.setAlignmentY(0.0F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        lhsjPanel.add(mainMapView, gridBagConstraints);

        statusjPanel.add(datejLabel);

        statusjPanel.add(cashjLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        lhsjPanel.add(statusjPanel, gridBagConstraints);

        jSplitPane1.setLeftComponent(lhsjPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSplitPane1, gridBagConstraints);

        gameMenu.setText("Game");
        jMenuBar1.add(gameMenu);

        buildMenu.setText("Build");
        jMenuBar1.add(buildMenu);

        displayMenu.setText("Display");
        jMenuBar1.add(displayMenu);

        helpMenu.setText("Help");
        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    private javax.swing.JMenu debugMenu;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu buildMenu;
    private javax.swing.JLabel cashjLabel;
    private javax.swing.JLabel datejLabel;
    private javax.swing.JMenu displayMenu;
    private javax.swing.JMenu gameMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel lhsjPanel;
    private javax.swing.JScrollPane mainMapView;
    private javax.swing.JPanel mapOverview;
    private javax.swing.JPanel rhsjPanel;
    private javax.swing.JPanel statusjPanel;
    private javax.swing.JTabbedPane trainsJTabPane1;
    // End of variables declaration//GEN-END:variables
    
}
