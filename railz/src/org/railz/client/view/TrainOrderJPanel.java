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
 * TrainOrderJPanel.java
 *
 * Created on 23 August 2003, 17:25
 */

package org.railz.client.view;

import javax.swing.*;

import org.railz.client.model.ModelRoot;
import org.railz.world.top.*;
import org.railz.world.station.*;
import java.awt.*;
/**
 *
 * @author  Luke Lindsay
 */
public class TrainOrderJPanel extends javax.swing.JPanel {
    private ModelRoot modelRoot;
    
    org.railz.world.top.ReadOnlyWorld w;
    
    ImageIcon gotoNow = new ImageIcon(TrainOrderJPanel.class.getResource("/org/railz/client/graphics/selected_arrow.png"));
    ImageIcon gotoAfterPriorityOrders = new ImageIcon(TrainOrderJPanel.class.getResource("/org/railz/client/graphics/deselected_arrow.png"));
    ImageIcon dontGoto = null;
    
    private Color backgoundColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get("List.background");
    
    private Color selectedColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get("List.selectionBackground");
    
    /** Creates new form TrainOrderJPanel */
    public TrainOrderJPanel() {
        initComponents();
        this.setBackground(backgoundColor);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        gotoIcon = new javax.swing.JLabel();
        consistChangeJPanel = new TrainViewJPanel();
        noChangeJLabel = new javax.swing.JLabel();
        stationNameJLabel = new javax.swing.JLabel();
        ordersJLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gotoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/selected_arrow.png")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(gotoIcon, gridBagConstraints);

        consistChangeJPanel.setLayout(new java.awt.GridBagLayout());

        noChangeJLabel.setText("No Change");
        consistChangeJPanel.add(noChangeJLabel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(consistChangeJPanel, gridBagConstraints);

        stationNameJLabel.setText("Some Station");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(stationNameJLabel, gridBagConstraints);

        ordersJLabel.setText("wait until full / don't wait");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 5);
        add(ordersJLabel, gridBagConstraints);

    }//GEN-END:initComponents
    
    public void setup(ModelRoot mr, java.awt.event.ActionListener submitButtonCallBack) {
	modelRoot = mr;
        w = mr.getWorld();
        TrainViewJPanel trainViewJPanel = (TrainViewJPanel)consistChangeJPanel;
        trainViewJPanel.setHeight(15);
        trainViewJPanel.setup(mr);
    }
    
    public void update(TrainOrdersListModel.TrainOrdersListElement
	    trainOrders, boolean isSelected, int index) {
        //Set station name
        int stationNumber = trainOrders.order.station.index;
	StationModel station = (StationModel)w.get(KEY.STATIONS, stationNumber,
		modelRoot.getPlayerPrincipal());
        String stationName = station.getStationName();
        this.stationNameJLabel.setText(stationName);
        
        //Set wait until full
	String waitUntilFull  = trainOrders.order.waitUntilFull ? 
	    "Wait until full" : "";
        this.ordersJLabel.setText(waitUntilFull);
        
        //Set selected
        if(isSelected){
            this.setBackground(selectedColor);
        }else{
            this.setBackground(backgoundColor);
        }
        
        //Set goto status.
        switch (trainOrders.gotoStatus){
            case TrainOrdersListModel.DONT_GOTO:
                this.gotoIcon.setIcon(this.dontGoto);
                break;
            case TrainOrdersListModel.GOTO_AFTER_PRIORITY_ORDERS:
                this.gotoIcon.setIcon(this.gotoAfterPriorityOrders);
                break;
            case TrainOrdersListModel.GOTO_NOW:
                this.gotoIcon.setIcon(this.gotoNow);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(trainOrders.gotoStatus));
        }
        this.gotoIcon.setPreferredSize(new Dimension(20, 20));
        
        //Set consist
        TrainViewJPanel trainViewJPanel = (TrainViewJPanel)consistChangeJPanel;
        trainViewJPanel.display(trainOrders.trainNumber, index);
        
        //Show priority orders.
        if(trainOrders.isPriorityOrder){
            //Write the station name in upper case
            String s = this.stationNameJLabel.getText();
            this.stationNameJLabel.setText(s + " (Priority Orders)");
        }
        
        //Check for 'No change'
        if(null == trainOrders.order.consist){
            this.noChangeJLabel.setText("No Change");
        }else{
            this.noChangeJLabel.setText(null);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel consistChangeJPanel;
    private javax.swing.JLabel gotoIcon;
    private javax.swing.JLabel noChangeJLabel;
    private javax.swing.JLabel ordersJLabel;
    private javax.swing.JLabel stationNameJLabel;
    // End of variables declaration//GEN-END:variables
    
}
