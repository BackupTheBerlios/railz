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

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
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
import org.railz.move.ChangeTrainScheduleMove;
import org.railz.move.Move;
import org.railz.util.Resources;
import org.railz.world.cargo.CargoType;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.ObjectKey;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldListListener;
import org.railz.world.train.*;

/**
 *  This JPanel displays a train's schedule and provides controls that let you
 *  edit it.
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
        addWagonJMenu = new javax.swing.JMenu();
        removeWagonsJMenu = new javax.swing.JMenu();
        removeLastJMenuItem = new javax.swing.JMenuItem();
        removeAllJMenuItem = new javax.swing.JMenuItem();
        changeConsistJMenu = new javax.swing.JMenu();
        noChangeJMenuItem = new javax.swing.JMenuItem();
        engineOnlyJMenuItem = new javax.swing.JMenuItem();
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

        gotoStationJMenuItem.setText("Goto station");
        gotoStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(gotoStationJMenuItem);

        changeStation.setText("Change Station");
        changeStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeStationActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(changeStation);

        removeStationJMenuItem.setText("Remove station");
        removeStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(removeStationJMenuItem);

        editOrderJPopupMenu.add(jSeparator1);

        addWagonJMenu.setText("Add Wagon");
        editOrderJPopupMenu.add(addWagonJMenu);

        removeWagonsJMenu.setText("Remove wagon(s)");
        removeLastJMenuItem.setText("Remove last");
        removeLastJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLastJMenuItemActionPerformed(evt);
            }
        });

        removeWagonsJMenu.add(removeLastJMenuItem);

        removeAllJMenuItem.setText("Remove all wagons");
        removeAllJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllJMenuItemActionPerformed(evt);
            }
        });

        removeWagonsJMenu.add(removeAllJMenuItem);

        editOrderJPopupMenu.add(removeWagonsJMenu);

        changeConsistJMenu.setText("Change consist to..");
        noChangeJMenuItem.setText("'No change'");
        noChangeJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noChangeJMenuItemActionPerformed(evt);
            }
        });

        changeConsistJMenu.add(noChangeJMenuItem);

        engineOnlyJMenuItem.setText("Engine only");
        engineOnlyJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                engineOnlyJMenuItemActionPerformed(evt);
            }
        });

        changeConsistJMenu.add(engineOnlyJMenuItem);

        editOrderJPopupMenu.add(changeConsistJMenu);

        waitJMenu.setText("Wait at station");
        dontWaitJMenuItem.setText("Don't wait");
        dontWaitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontWaitJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(dontWaitJMenuItem);

        waitUntilFullJMenuItem.setText("Wait until full");
        waitUntilFullJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waitUntilFullJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(waitUntilFullJMenuItem);

        editOrderJPopupMenu.add(waitJMenu);

        editOrderJPopupMenu.add(jSeparator2);

        pullUpJMenuItem.setText("Pull up");
        pullUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pullUpJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pullUpJMenuItem);

        pushDownJMenuItem.setText("Push down");
        pushDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pushDownJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pushDownJMenuItem);

        addStationJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/add_station.png")));
        addStationJButton.setToolTipText("Add Station");
        addStationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStationJButtonActionPerformed(evt);
            }
        });

        priorityOrdersJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/priority_orders.png")));
        priorityOrdersJButton.setToolTipText("Add Priority Orders");
        priorityOrdersJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityOrdersJButtonActionPerformed(evt);
            }
        });

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder("Schedule"));
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
    
    private void changeStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStationActionPerformed
        int orderNumber = this.orders.getSelectedIndex();
        selectStationJPanel.display(trainNumber, orderNumber);
        
        //Show the select station popup in the middle of the window.
	selectStationDialog = guiRoot.getDialogueBoxController().createDialog
	    (selectStationJPanel, Resources.get("Select a Station"));
    }//GEN-LAST:event_changeStationActionPerformed
    
    private void removeAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllJMenuItemActionPerformed
        removeAllWagons();
    }//GEN-LAST:event_removeAllJMenuItemActionPerformed
    
    private void removeLastJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLastJMenuItemActionPerformed
        removeLastWagon();
    }//GEN-LAST:event_removeLastJMenuItemActionPerformed
    
    private void waitUntilFullJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waitUntilFullJMenuItemActionPerformed
        setWaitUntilFull(true);
    }//GEN-LAST:event_waitUntilFullJMenuItemActionPerformed
    
    private void dontWaitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontWaitJMenuItemActionPerformed
        setWaitUntilFull(false);
    }//GEN-LAST:event_dontWaitJMenuItemActionPerformed
    
    private void engineOnlyJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_engineOnlyJMenuItemActionPerformed
        removeAllWagons();
    }//GEN-LAST:event_engineOnlyJMenuItemActionPerformed
    
    private void noChangeJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noChangeJMenuItemActionPerformed
        noChange();
    }//GEN-LAST:event_noChangeJMenuItemActionPerformed
    
    private void priorityOrdersJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityOrdersJButtonActionPerformed
        MutableSchedule s = getSchedule();
        s.setPriorityOrders(new TrainOrdersModel(new ObjectKey(KEY.STATIONS,
			modelRoot.getPlayerPrincipal(), 0), null, false, true,
		    true));
        sendUpdateMove(s);
    }//GEN-LAST:event_priorityOrdersJButtonActionPerformed
    
    private void addStationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStationJButtonActionPerformed
        MutableSchedule s = getSchedule();
        s.addOrder(new TrainOrdersModel(new ObjectKey(KEY.STATIONS,
			modelRoot.getPlayerPrincipal(), 0), null, false, true,
		    true));
        sendUpdateMove(s);
    }//GEN-LAST:event_addStationJButtonActionPerformed
    
    private void removeStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeStationJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.removeOrder(i);
        sendUpdateMove(s);
    }//GEN-LAST:event_removeStationJMenuItemActionPerformed
    
    private void gotoStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoStationJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.setOrderToGoto(i);
        sendUpdateMove(s);
    }//GEN-LAST:event_gotoStationJMenuItemActionPerformed
    
    private void pushDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushDownJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.pushDown(i);
        sendUpdateMove(s);
        orders.setSelectedIndex(i+1);
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
		pullUpJMenuItem.setEnabled(s.canPullUp(i));
		pushDownJMenuItem.setEnabled(s.canPushDown(i));
		gotoStationJMenuItem.setEnabled(s.canSetGotoStation(i));
		removeWagonsJMenu.setEnabled(order.orderHasWagons());
		waitJMenu.setEnabled(order.orderHasWagons());
		addWagonJMenu.setEnabled(order.hasLessThanMaxiumNumberOfWagons());
		setupWagonsPopup();
		editOrderJPopupMenu.show(e.getComponent(), e.getX(),
			e.getY());
	    }
	}
    };
    
    private void pullUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullUpJMenuItemActionPerformed
        // Add your handling code here:
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.pullUp(i);
        sendUpdateMove(s);
        orders.setSelectedIndex(i-1);
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
        addStationJButton.setEnabled(s.canAddOrder());
        
        //Only one set of prority orders are allowed.
        priorityOrdersJButton.setEnabled(!s.hasPriorityOrders());
	editOrderJPopupMenu.setEnabled(true);
    }
    
    MutableSchedule getSchedule(){
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
        ImmutableSchedule immutableSchedule = (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES, train.getScheduleIterator().getScheduleId());
        return new MutableSchedule(immutableSchedule);
    }
    
    private void setupWagonsPopup() {
        addWagonJMenu.removeAll(); //Remove existing menu items.
        NonNullElements cargoTypes = new NonNullElements(KEY.CARGO_TYPES, w);
        
        TrainImages trainImages = vl.getTrainImages();
        
        while (cargoTypes.next()) {
            final CargoType wagonType = (CargoType) cargoTypes.getElement();
            JMenuItem wagonMenuItem = new JMenuItem();
            final int wagonTypeNumber = cargoTypes.getIndex();
            wagonMenuItem.setText(wagonType.getDisplayName());
            Image image = trainImages.getSideOnWagonImage(wagonTypeNumber);
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            int scale = height/10;
            ImageIcon icon = new ImageIcon(image.getScaledInstance(width/scale,
            height/scale, Image.SCALE_FAST));
            wagonMenuItem.setIcon(icon);
            wagonMenuItem
            .addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    
                    addWagon(wagonTypeNumber);
                }
            });
            addWagonJMenu.add(wagonMenuItem);
        }
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
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void noChange(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(), null,
		false, oldOrders.loadTrain, oldOrders.unloadTrain);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void setWaitUntilFull(boolean b){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(),
		oldOrders.consist, b, oldOrders.loadTrain,
		oldOrders.unloadTrain);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void  addWagon(int wagonTypeNumber){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        int[] newConsist;
        //The consist will be null if old orders were 'no change'.
        if(null != oldOrders.consist){
            int oldLength = oldOrders.consist.length;
            newConsist = new int[oldLength+1];
            //Copy existing wagons
            for( int i = 0 ; i < oldLength ; i ++){
                newConsist[i] = oldOrders.consist[i];
            }
            //Then add specified wagon.
            newConsist[oldLength] = wagonTypeNumber;
        }else{
            newConsist = new int[]{wagonTypeNumber};
        }
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(),
		newConsist, oldOrders.getWaitUntilFull(), oldOrders.loadTrain,
		oldOrders.unloadTrain);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void removeAllWagons(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(), new
		int[0], false, oldOrders.loadTrain, oldOrders.unloadTrain);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void removeLastWagon(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        int[] oldConsist = oldOrders.consist;
        int newLength = oldConsist.length -1 ;
        if(newLength < 0){
            throw new NoSuchElementException("No wagons to remove!");
        }
        int[] newConsist = new int[newLength];
        
        //Copy existing wagons
        System.arraycopy(oldConsist, 0, newConsist, 0, newConsist.length);
	newOrders = new TrainOrdersModel(oldOrders.getStationNumber(),
		newConsist, oldOrders.waitUntilFull, oldOrders.loadTrain,
		oldOrders.unloadTrain);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    void sendUpdateMove(MutableSchedule mutableSchedule ){
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, this.trainNumber,
		modelRoot.getPlayerPrincipal());
        ScheduleIterator si = train.getScheduleIterator();
        ImmutableSchedule before = (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES, si.getScheduleId());
        ImmutableSchedule after = mutableSchedule.toImmutableSchedule();
        Move m = new ChangeTrainScheduleMove(si.getScheduleId(), before, after);
        modelRoot.getReceiver().processMove(m);
    }
    
    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if(KEY.TRAIN_SCHEDULES == key &&
	       	scheduleIterator.getScheduleId() == index){
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
    private javax.swing.JMenu addWagonJMenu;
    private javax.swing.JMenu changeConsistJMenu;
    private javax.swing.JMenuItem changeStation;
    private javax.swing.JMenuItem dontWaitJMenuItem;
    private javax.swing.JPopupMenu editOrderJPopupMenu;
    private javax.swing.JMenuItem engineOnlyJMenuItem;
    private javax.swing.JMenuItem gotoStationJMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem noChangeJMenuItem;
    private javax.swing.JList orders;
    javax.swing.JButton priorityOrdersJButton;
    private javax.swing.JMenuItem pullUpJMenuItem;
    private javax.swing.JMenuItem pushDownJMenuItem;
    private javax.swing.JMenuItem removeAllJMenuItem;
    private javax.swing.JMenuItem removeLastJMenuItem;
    private javax.swing.JMenuItem removeStationJMenuItem;
    private javax.swing.JMenu removeWagonsJMenu;
    private javax.swing.JMenu waitJMenu;
    private javax.swing.JMenuItem waitUntilFullJMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
