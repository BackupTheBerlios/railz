/*
 *
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

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.ListCellRenderer;

import org.railz.client.common.PortablePopupAdapter;
import org.railz.client.renderer.TrainImages;
import org.railz.client.renderer.ViewLists;
import org.railz.client.model.ModelRoot;
import org.railz.client.view.TrainOrdersListModel.TrainOrdersListElement;
import org.railz.move.*;
import org.railz.util.Resources;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

/**
 * This JPanel displays a train's schedule and provides controls that let you
 * edit it.
 * @author  Luke Lindsay
 */
public class TrainScheduleJPanel extends javax.swing.JPanel implements
WorldListListener, ListCellRenderer {
    private ArrayList listCells = new ArrayList();
    private ModelRoot modelRoot;
    private GUIRoot guiRoot;

    private ReadOnlyWorld w;
    
    private ViewLists vl;
    
    private int trainNumber = -1;
    
    private ScheduleIterator scheduleIterator = null;
    
    private TrainOrdersListModel listModel;
    private Component selectStationDialog;
    private volatile Component wagonSelectionDialog;
    private WagonSelectionJPanel wagonSelectionJPanel;
    private int wagonSelectionOrderNo;
    
    /** Creates new form TrainScheduleJPanel */
    public TrainScheduleJPanel() {
        initComponents();
	orders.setCellRenderer(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        editOrderJPopupMenu = new javax.swing.JPopupMenu();
        gotoStationJMenuItem = new javax.swing.JMenuItem();
        changeStation = new javax.swing.JMenuItem();
        removeStationJMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        changeConsistJMenuItem = new javax.swing.JMenuItem();
        waitJMenu = new javax.swing.JMenu();
        dontWaitJMenuItem = new javax.swing.JMenuItem();
        waitUntilFullJMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        pullUpJMenuItem = new javax.swing.JMenuItem();
        pushDownJMenuItem = new javax.swing.JMenuItem();
        addStationJButton = new javax.swing.JButton();
        priorityOrdersJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        orders = new javax.swing.JList();
        orders.addMouseListener(ordersPopupAdapter);

        gotoStationJMenuItem.setText(org.railz.util.Resources.get("Go to station"));
        gotoStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(gotoStationJMenuItem);

        changeStation.setText(org.railz.util.Resources.get("Change station"));
        changeStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeStationActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(changeStation);

        removeStationJMenuItem.setText(org.railz.util.Resources.get("Remove station"));
        removeStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(removeStationJMenuItem);

        editOrderJPopupMenu.add(jSeparator1);

        changeConsistJMenuItem.setText(org.railz.util.Resources.get("Change Consist"));
        changeConsistJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeConsistJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(changeConsistJMenuItem);

        waitJMenu.setText("Wait at station");
        dontWaitJMenuItem.setText(org.railz.util.Resources.get("Don't wait"));
        dontWaitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontWaitJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(dontWaitJMenuItem);

        waitUntilFullJMenuItem.setText(org.railz.util.Resources.get("Wait until full"));
        waitUntilFullJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waitUntilFullJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(waitUntilFullJMenuItem);

        editOrderJPopupMenu.add(waitJMenu);

        editOrderJPopupMenu.add(jSeparator2);

        pullUpJMenuItem.setText(org.railz.util.Resources.get("Pull up"));
        pullUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pullUpJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pullUpJMenuItem);

        pushDownJMenuItem.setText(org.railz.util.Resources.get("Push down"));
        pushDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pushDownJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pushDownJMenuItem);

        addStationJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/add_station.png")));
        addStationJButton.setToolTipText(org.railz.util.Resources.get("Add station"));
        addStationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStationJButtonActionPerformed(evt);
            }
        });

        priorityOrdersJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/priority_orders.png")));
        priorityOrdersJButton.setToolTipText(org.railz.util.Resources.get("Add priority orders"));
        priorityOrdersJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityOrdersJButtonActionPerformed(evt);
            }
        });

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(org.railz.util.Resources.get("Schedule")));
        orders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(orders);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents

    private void changeConsistJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeConsistJMenuItemActionPerformed
	if (wagonSelectionDialog != null)
	    return;

	wagonSelectionOrderNo = orders.getSelectedIndex();

	wagonSelectionJPanel = new WagonSelectionJPanel();
	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());

	int[] orderWagons = ((TrainOrdersListElement)
	    listModel.getElementAt(wagonSelectionOrderNo)).order.getConsist();
	tm = tm.getNewInstance(tm.getEngineType(), orderWagons);
	wagonSelectionJPanel.setup(modelRoot, tm);
	wagonSelectionDialog =
	    guiRoot.getDialogueBoxController().createDialog(wagonSelectionJPanel,
		    Resources.get("Select new consist"));
	wagonSelectionDialog.addComponentListener(changeConsistListener);
    }//GEN-LAST:event_changeConsistJMenuItemActionPerformed
    
    private void changeStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStationActionPerformed
        int orderNumber = this.orders.getSelectedIndex();
        selectStationJPanel.display(trainNumber, orderNumber);
        
        //Show the select station popup in the middle of the window.
	selectStationDialog = guiRoot.getDialogueBoxController().createDialog
	    (selectStationJPanel, Resources.get("Select a Station"));
    }//GEN-LAST:event_changeStationActionPerformed
    
    private void waitUntilFullJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waitUntilFullJMenuItemActionPerformed
        setWaitUntilFull(true);
    }//GEN-LAST:event_waitUntilFullJMenuItemActionPerformed
    
    private void dontWaitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontWaitJMenuItemActionPerformed
        setWaitUntilFull(false);
    }//GEN-LAST:event_dontWaitJMenuItemActionPerformed
    
    private void priorityOrdersJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityOrdersJButtonActionPerformed
	TrainOrdersModel tom = new TrainOrdersModel(new ObjectKey(KEY.STATIONS,
			modelRoot.getPlayerPrincipal(), 0), null, false, true,
		    true);
	ScheduleIterator si = new ScheduleIterator(scheduleIterator, tom);
	TrainModel train = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
	Move m = ChangeTrainMove.generateMove(trainNumber,
		modelRoot.getPlayerPrincipal(), train, si,
		(GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE));
	modelRoot.getReceiver().processMove(m);
    }//GEN-LAST:event_priorityOrdersJButtonActionPerformed
    
    private void addStationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStationJButtonActionPerformed
	TrainModel tm = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
	int[] consist = new int[tm.getNumberOfWagons()];
	for (int i = 0; i < consist.length; i++)
	    consist[i] = tm.getWagon(i);
	Schedule s = (Schedule) w.get(KEY.TRAIN_SCHEDULES,
		tm.getScheduleIterator().getScheduleKey().index,
	       	tm.getScheduleIterator().getScheduleKey().principal);
        TrainOrdersModel order = new TrainOrdersModel
	    (new ObjectKey(KEY.STATIONS, modelRoot.getPlayerPrincipal(), 0),
	     consist, false, true, true);
        sendAddStationMove(s.getNumOrders(), order);
    }//GEN-LAST:event_addStationJButtonActionPerformed
    
    private void removeStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeStationJMenuItemActionPerformed
	sendRemoveStationMove(orders.getSelectedIndex());
    }//GEN-LAST:event_removeStationJMenuItemActionPerformed
    
    private void gotoStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoStationJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();

	ScheduleIterator si = new ScheduleIterator
	    (scheduleIterator.getScheduleKey(), i);
	Move m = ChangeTrainMove.generateMove(trainNumber,
		modelRoot.getPlayerPrincipal(), (TrainModel) 
		w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal()),
		si, (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE));
	modelRoot.getReceiver().processMove(m);
    }//GEN-LAST:event_gotoStationJMenuItemActionPerformed
    
    private void pushDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushDownJMenuItemActionPerformed
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
        ImmutableSchedule immutableSchedule = (ImmutableSchedule)w.get
	    (KEY.TRAIN_SCHEDULES,
	     train.getScheduleIterator().getScheduleKey().index,
	     train.getScheduleIterator().getScheduleKey().principal);
        int i = orders.getSelectedIndex();
	TrainOrdersModel tom = immutableSchedule.getOrder(i);
	sendRemoveStationMove(i);
	if (i + 1 > immutableSchedule.getNumOrders()) {
	    sendAddStationMove(0, tom);
	} else {
	    sendAddStationMove(i + 1, tom);
	}
    }//GEN-LAST:event_pushDownJMenuItemActionPerformed
    
    private PortablePopupAdapter ordersPopupAdapter = new PortablePopupAdapter()
    {
	public void triggerPopup(MouseEvent e) {
	    int i = orders.getSelectedIndex();
	    MutableSchedule s = getSchedule();
	    if(i >= s.getNumOrders()){
		//The selected index does not exist!
		//For some reason, the JList hasn't updated yet.
		i = -1;
	    }
	    if(-1 != i) {
		TrainOrdersModel order = s.getOrder(i);
		pullUpJMenuItem.setEnabled(i > 0);
		pushDownJMenuItem.setEnabled(s.getNumOrders() > i + 1);
		gotoStationJMenuItem.setEnabled(true);
		waitJMenu.setEnabled(order.orderHasWagons());
		editOrderJPopupMenu.show(e.getComponent(), e.getX(),
			e.getY());
	    }
	}
    };
    
    private void pullUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullUpJMenuItemActionPerformed
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
        ImmutableSchedule immutableSchedule = (ImmutableSchedule)w.get
	    (KEY.TRAIN_SCHEDULES,
	     train.getScheduleIterator().getScheduleKey().index,
	     train.getScheduleIterator().getScheduleKey().principal);
        int i = orders.getSelectedIndex();
	TrainOrdersModel tom = immutableSchedule.getOrder(i);
	sendRemoveStationMove(i);
	if (i == 0) {
	    sendAddStationMove(0, tom);
	} else {
	    sendAddStationMove(i - 1, tom);
	}
    }//GEN-LAST:event_pullUpJMenuItemActionPerformed
    
    public void setup(ModelRoot mr, GUIRoot gr) {
	guiRoot = gr;
	modelRoot = mr;
        this.w = mr.getWorld();
        this.vl = mr.getViewLists();
        
        //This actionListener is fired by the select station popup when a stion is selected.
        ActionListener actionListener =  new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                setStationNumber(selectStationJPanel.getSelectedStationID());
		if(selectStationDialog != null) {
		    selectStationDialog.setVisible(false);
		    selectStationDialog = null;
		}
            }
        };
	selectStationJPanel = new SelectStationJPanel();
	selectStationJPanel.addActionListener(actionListener);
        selectStationJPanel.setup(modelRoot);
    }
    
    public void display(int trainNumber){
	System.out.println("TrainScheduleJPanel display");
	if (wagonSelectionDialog != null &&
		this.trainNumber != trainNumber) {
	    /* close the dialog without saving changes */
	    wagonSelectionDialog.removeComponentListener(changeConsistListener);
	    wagonSelectionDialog.setVisible(false);
	    wagonSelectionDialog = null;
	}
        this.trainNumber = trainNumber;
	if (trainNumber >= 0) {
	    TrainModel train = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());
	    scheduleIterator = train.getScheduleIterator();
	    listModel = new TrainOrdersListModel(modelRoot, trainNumber);
	    listCells.clear();
	    for (int i = 0; i < listModel.getSize(); i++) {
		TrainOrderJPanel toj = new TrainOrderJPanel();
		toj.setup(modelRoot, null);
		listCells.add(toj);
	    }
	    orders.setModel(listModel);
	    // orders.setFixedCellWidth(250);
	    listModel.fireRefresh();
	    enableButtons();
	} else {
	    disableButtons();
	}
    }
    
    private void disableButtons() {
	addStationJButton.setEnabled(false);
	priorityOrdersJButton.setEnabled(false);
	editOrderJPopupMenu.setEnabled(false);
    }

    private void enableButtons(){
        MutableSchedule s  = getSchedule();
        addStationJButton.setEnabled(true);
        
        //Only one set of prority orders are allowed.
        priorityOrdersJButton.setEnabled(!scheduleIterator.hasPriorityOrder());
	editOrderJPopupMenu.setEnabled(true);
    }
    
    MutableSchedule getSchedule(){
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
        ImmutableSchedule immutableSchedule = (ImmutableSchedule)w.get
	    (KEY.TRAIN_SCHEDULES,
	     train.getScheduleIterator().getScheduleKey().index,
	     train.getScheduleIterator().getScheduleKey().principal);
        return new MutableSchedule(immutableSchedule);
    }
    
    private void setStationNumber(int stationIndex){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
	newOrders = new TrainOrdersModel(new ObjectKey(KEY.STATIONS,
		    modelRoot.getPlayerPrincipal(), stationIndex),
		oldOrders.getConsist(), oldOrders.getWaitUntilFull(),
		oldOrders.loadTrain, oldOrders.unloadTrain);
        sendUpdateStationMove(orderNumber, newOrders);
    }
    
    private void setWaitUntilFull(boolean b){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(),
		oldOrders.consist, b, oldOrders.loadTrain,
		oldOrders.unloadTrain);
        sendUpdateStationMove(orderNumber, newOrders);
    }
    
    void sendUpdateStationMove(int aStationIndex, TrainOrdersModel aOrder) {
	Move m = AddRemoveScheduleStationMove.generateChangeMove
	    (new ObjectKey(KEY.TRAIN_SCHEDULES, 
			   scheduleIterator.getScheduleKey().principal,
			   scheduleIterator.getScheduleKey().index),
	     aStationIndex, aOrder, w);
	modelRoot.getReceiver().processMove(m);
    }

    void sendRemoveStationMove(int aStationIndex) {
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
	Move m = AddRemoveScheduleStationMove.generateRemoveMove
	   (new ObjectKey(KEY.TRAIN_SCHEDULES,
			  train.getScheduleIterator().getScheduleKey().principal,
			  train.getScheduleIterator().getScheduleKey().index),
	    aStationIndex, w); 
	modelRoot.getReceiver().processMove(m);
    }

   void sendAddStationMove(int aStationIndex, TrainOrdersModel aOrder) {
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
       Move m = AddRemoveScheduleStationMove.generateAddMove
	   (new ObjectKey(KEY.TRAIN_SCHEDULES,
			  train.getScheduleIterator().getScheduleKey().principal,
			  train.getScheduleIterator().getScheduleKey().index),
	    aStationIndex, w, aOrder);
       modelRoot.getReceiver().processMove(m);
   } 

    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if ((KEY.TRAIN_SCHEDULES == key &&
	       	scheduleIterator.getScheduleKey().index == index &&
		scheduleIterator.getScheduleKey().principal.equals(p)) || 
	    (KEY.TRAINS == key && 
	     trainNumber == index &&
	     modelRoot.getPlayerPrincipal().equals(p))) {
            listModel.fireRefresh();
            enableButtons();
        }
    }
    
    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
        //do nothing.
    }
    
    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
        //do nothing.
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	TrainOrderJPanel toj = (TrainOrderJPanel) listCells.get(index);
	toj.update((TrainOrdersListModel.TrainOrdersListElement) value,
		isSelected, index);
	return toj;
    }

    private SelectStationJPanel selectStationJPanel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton addStationJButton;
    private javax.swing.JMenuItem changeConsistJMenuItem;
    private javax.swing.JMenuItem changeStation;
    private javax.swing.JMenuItem dontWaitJMenuItem;
    private javax.swing.JPopupMenu editOrderJPopupMenu;
    private javax.swing.JMenuItem gotoStationJMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    javax.swing.JList orders;
    javax.swing.JButton priorityOrdersJButton;
    private javax.swing.JMenuItem pullUpJMenuItem;
    private javax.swing.JMenuItem pushDownJMenuItem;
    private javax.swing.JMenuItem removeStationJMenuItem;
    private javax.swing.JMenu waitJMenu;
    private javax.swing.JMenuItem waitUntilFullJMenuItem;
    // End of variables declaration//GEN-END:variables
    
    private ComponentListener changeConsistListener  = new ComponentAdapter()
	{
	    public void componentHidden(ComponentEvent e) {
		System.out.println("Component hidden!");
		int[] wagons = wagonSelectionJPanel.getWagons();
		TrainOrdersModel oldOrder = ((TrainOrdersListElement)
		       	listModel.getElementAt(wagonSelectionOrderNo)).order;
		TrainOrdersModel tom = new TrainOrdersModel
		    (oldOrder.getStationNumber(), wagons,
		     oldOrder.getWaitUntilFull(), oldOrder.loadTrain,
		     oldOrder.unloadTrain);
		Move m = AddRemoveScheduleStationMove.generateChangeMove
		    (scheduleIterator.getScheduleKey(), wagonSelectionOrderNo,
		     tom, w);
		modelRoot.getReceiver().processMove(m);
		wagonSelectionDialog = null;
		wagonSelectionJPanel = null;
	    }
	};
}
