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
 * TrainDetailsJPanel.java
 *
 * Created on 16 June 2030, 20:03
 */

package org.railz.client.view;

import java.awt.Graphics;
import javax.swing.border.TitledBorder;

import org.railz.client.model.ModelRoot;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldListListener;
import org.railz.world.train.TrainModel;
/**
 * This JPanel displays a side-on view of a train and a summary of the
 * cargo that it is carrying.
 *
 * @author  Luke Lindsay
 */
public class TrainDetailsJPanel extends javax.swing.JPanel implements WorldListListener {
    private ModelRoot modelRoot;

    private ReadOnlyWorld w;    
    
    private int trainNumber = -1;
    
    /** The id of the bundle of cargo that the train is carrying - we need to update
     * the view when the bundle is updated.
     */
    private int bundleID = -1;
    
    /** Creates new form TrainDetailsJPanel */
    public TrainDetailsJPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        trainViewJPanel1 = new org.railz.client.view.TrainViewJPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(trainViewJPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    public void setup(ModelRoot mr,
	    java.awt.event.ActionListener submitButtonCallBack) {
	modelRoot = mr;
	ReadOnlyWorld w = mr.getWorld();
	org.railz.client.renderer.ViewLists vl = mr.getViewLists();
        this.trainViewJPanel1.setup(mr, submitButtonCallBack);
        trainViewJPanel1.setHeight(20);
         trainViewJPanel1.setCenterTrain(true);
        this.w = w;
	modelRoot.getMoveChainFork().addListListener(this);
    }    
    
    public void displayTrain(int trainNumber){
    	this.trainNumber = trainNumber;
        
        trainViewJPanel1.display(trainNumber);
	String s;
	if (trainNumber >= 0) {
	    TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		    modelRoot.getPlayerPrincipal());

	    this.bundleID = train.getCargoBundleNumber();

	    CargoBundle cb = (CargoBundle)w.get(KEY.CARGO_BUNDLES,
		    train.getCargoBundleNumber());
	    s="Train #"+trainNumber+": ";
	} else {
	    s = "No trains to display";
	}
	((TitledBorder) getBorder()).setTitle(s);
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
    private org.railz.client.view.TrainViewJPanel trainViewJPanel1;
    // End of variables declaration//GEN-END:variables
    

    public void paint(Graphics g) {
	super.paint(g);
    }
}