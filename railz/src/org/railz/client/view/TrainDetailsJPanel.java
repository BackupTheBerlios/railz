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

package org.railz.client.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import javax.swing.border.TitledBorder;
import javax.swing.*;

import org.railz.client.model.ModelRoot;
import org.railz.move.*;
import org.railz.util.*;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

/**
 * This JPanel displays information about a train. The information consists of:
 * <ul>
 * <li>a side-on view of a train summarising the cargo that it is carrying.
 * <li>the name of the train.
 * <li>the priority/state of the train.(express, standard, slow, stopped,
 * lost, blocked)
 * <li>route/destination name (to be implemented)
 * </li>
 *
 * **************************************
 * * (train name)               (state) *
 * *   ### ### ### ### ### ### ###      *
 * * (train route name)                 *
 * **************************************
 * @author  Luke Lindsay
 */
public class TrainDetailsJPanel extends javax.swing.JPanel implements
    WorldListListener, RefreshListener {
    private ImageIcon stoppedIcon = new ImageIcon(getClass()
		.getResource("/org/railz/client/graphics/toolbar/stop.png"));
    
    private ImageIcon slowIcon = new ImageIcon(getClass()
		.getResource("/org/railz/client/graphics/toolbar/slow.png"));

    private ImageIcon standardIcon = new ImageIcon(getClass()
		.getResource("/org/railz/client/graphics/toolbar/standard.png"));
    private ImageIcon expressIcon = new ImageIcon(getClass()
		.getResource("/org/railz/client/graphics/toolbar/express.png"));

    private ModelRoot modelRoot;

    private ReadOnlyWorld w;    
    
    private int trainNumber = -1;
    
    private TrainModelViewer trainModelViewer;

    /**
     * The id of the bundle of cargo that the train is carrying - we need to
     * update the view when the bundle is updated.
     */
    private int bundleID = -1;
    
    /** Creates new form TrainDetailsJPanel */
    public TrainDetailsJPanel() {
        initComponents();
	statusJButton.setIcon(standardIcon);
	statusJButton.addActionListener(statusButtonListener);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameJLabel = new javax.swing.JLabel();
        statusJButton = new javax.swing.JButton();
        destinationJLabel = new javax.swing.JLabel();
        waterBar = new WaterBar();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(""));
        nameJLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        nameJLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 2);
        add(nameJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 4);
        add(statusJButton, gridBagConstraints);

        destinationJLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        destinationJLabel.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(destinationJLabel, gridBagConstraints);

        waterBar.setMaximumSize(new java.awt.Dimension(32767, 4));
        waterBar.setMinimumSize(new java.awt.Dimension(10, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        add(waterBar, gridBagConstraints);

    }//GEN-END:initComponents

    public void setup(ModelRoot mr, GUIRoot gr) {
	modelRoot = mr;
	ReadOnlyWorld w = mr.getWorld();
        this.w = w;
	trainModelViewer = new TrainModelViewer(w);

	org.railz.client.renderer.ViewLists vl = mr.getViewLists();
        trainViewJPanel1 = new TrainViewJPanel(mr);
        trainViewJPanel1.setHeight(20);
	trainViewJPanel1.setCenterTrain(true);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.weightx = 1.0;
	gbc.gridx = 0;
	gbc.gridy = 2;
	gbc.gridwidth = 2;
	add(trainViewJPanel1, gbc);
	
	modelRoot.getMoveChainFork().addListListener(this);
	gr.addRefreshListener(this);
    }    
    
    public void displayTrain(int trainNumber){
    	this.trainNumber = trainNumber;

        trainViewJPanel1.display(trainNumber);
	String s;
	String destination = "";
	int priority = TrainModel.PRIORITY_NORMAL;
	int state = TrainModel.STATE_STOPPED;
	if (trainNumber >= 0) {
	    TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());

	    this.bundleID = train.getCargoBundleNumber();

	    CargoBundle cb = (CargoBundle)w.get(KEY.CARGO_BUNDLES,
		    train.getCargoBundleNumber());
	    s="Train #"+trainNumber+": ";
	    ScheduleIterator si = train.getScheduleIterator();
	    TrainOrdersModel tom = si.getCurrentOrder(w);
	    if (tom != null) {
		ObjectKey ok = tom.getStationNumber();
		StationModel station = (StationModel)
		    w.get(ok.key, ok.index, ok.principal);
		destination = station.getStationName();
	    }
	    priority = train.getPriority();
	    state = train.getState();
	} else {
	    s = "No trains to display";
	}
	setStatusButton(priority, state);
	nameJLabel.setText(s);
	destinationJLabel.setText(destination);
    }
        
    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
	if(KEY.TRAINS == key && index == trainNumber &&
		p.equals(modelRoot.getPlayerPrincipal())) {
	    //The train has been updated.
	    this.displayTrain(this.trainNumber);
	}else if(KEY.CARGO_BUNDLES == key && index == bundleID){ 
	    //The train's cargo has changed.
	    this.displayTrain(this.trainNumber);
	}			
	trainViewJPanel1.listUpdated(key, index, p);
	repaint();
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
	trainViewJPanel1.itemAdded(key, index, p);
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
	trainViewJPanel1.itemRemoved(key, index, p);
    }
                
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destinationJLabel;
    private javax.swing.JLabel nameJLabel;
    private javax.swing.JButton statusJButton;
    private javax.swing.JPanel waterBar;
    // End of variables declaration//GEN-END:variables
    
    private TrainViewJPanel trainViewJPanel1;

    public void paint(Graphics g) {
	super.paint(g);
    }

    private ActionListener statusButtonListener = new ActionListener() {
	/**
	 * Cycle through train status in the following order:
	 * Priority express, standard, slow, stopped
	 */
	public void actionPerformed(ActionEvent e) {
	    if (trainNumber == -1)
		return;

	    TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());

	    int priority = tm.getPriority();
	    int state = tm.getState();

	    if (state == TrainModel.STATE_STOPPED) {
		state = TrainModel.STATE_RUNNABLE;
		priority = TrainModel.PRIORITY_EXPRESS;
	    } else {
		switch (priority) {
		    case TrainModel.PRIORITY_EXPRESS:
			priority = TrainModel.PRIORITY_NORMAL;
			break;
		    case TrainModel.PRIORITY_NORMAL:
			priority = TrainModel.PRIORITY_SLOW;
			break;
		    case TrainModel.PRIORITY_SLOW:
			state = TrainModel.STATE_STOPPED;
			break;
		    default:
			throw new IllegalStateException();
		}
	    }
	    Move m;
	    GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	    if (tm.getState() != state) {
		m = ChangeTrainMove.generateMove(trainNumber,
			modelRoot.getPlayerPrincipal(), tm, state, now);
		modelRoot.getReceiver().processMove(m);
		tm = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());
	    }
	    if (tm.getPriority() != priority) {
		m = ChangeTrainMove.generatePriorityMove(trainNumber,
			modelRoot.getPlayerPrincipal(), tm, priority);
		modelRoot.getReceiver().processMove(m);
	    }
	}
    };

    private void setStatusButton(int priority, int state) {
	String tooltip = Resources.get("Standard Priority");
	ImageIcon icon = standardIcon;
	if (state == TrainModel.STATE_STOPPED) {
	    icon = stoppedIcon;
	    tooltip = Resources.get("Stopped");
	} else {
	    switch (priority) {
		case TrainModel.PRIORITY_EXPRESS:
		    icon = expressIcon;
		    tooltip = Resources.get("Express Priority");
		    break;
		case TrainModel.PRIORITY_NORMAL:
		    icon = standardIcon;
		    tooltip = Resources.get("Standard Priority");
		    break;
		case TrainModel.PRIORITY_SLOW:
		    icon = slowIcon;
		    tooltip = Resources.get("Slow Priority");
		    break;
		default:
		    throw new IllegalStateException();
	    }
	}
	statusJButton.setToolTipText(tooltip);
	statusJButton.setIcon(icon);
    }

    public class WaterBar extends JPanel {
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics gg = g.create();
	    if (trainNumber < 0)
		return;

	    TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());

	    trainModelViewer.setTrainModel(tm);
	    int waterRemaining = trainModelViewer.getWaterRemaining();
	    
	    int maxWater = ((EngineType) w.get(KEY.ENGINE_TYPES,
			tm.getEngineType(),
			Player.AUTHORITATIVE)).getWaterCapacity();

	    // draw a dark-blue rectangle and a light blue rectangle
	    gg.setColor(Color.BLUE);
	    gg.fillRect(0, 0, getWidth() * waterRemaining / maxWater,
		    getHeight());
	}
    }

    public void doRefresh() {
	waterBar.repaint();
    }
}
