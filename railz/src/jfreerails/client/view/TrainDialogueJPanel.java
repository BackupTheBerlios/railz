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
 * TrainDialogueJPanel.java
 *
 * Created on 24 August 2003, 17:13
 */

package org.railz.client.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.NoSuchElementException;
import javax.swing.ListCellRenderer;
import javax.swing.JList;

import org.railz.client.common.*;
import org.railz.client.model.ModelRoot;
import org.railz.util.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.train.MutableSchedule;
import org.railz.world.top.*;

/**
 *
 * @author  Luke Lindsay
 */
public class TrainDialogueJPanel extends javax.swing.JPanel implements
WorldListListener {
    
    /**
     * stores our current position in the list of trains.
     */
    private WorldIterator wi;

    private ReadOnlyWorld w;
    private ModelRoot modelRoot;
    private GUIRoot guiRoot;
    
    private ActionListener popupButtonListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    wi.gotoIndex(popupJButton.getSelectedIndex());
	    TrainDialogueJPanel.this.display();
	}
    };

    private DetailsListCellRenderer trainDetailsRenderer = new
	DetailsListCellRenderer();
   
    private void disableAllComponents(Container c) {
	Component components[] = c.getComponents();
	for (int i = 0; i < components.length; i++) {
	    if (components[i] instanceof Container) {
		disableAllComponents((Container) components[i]);
	    }
	    components[i].setEnabled(false);
	}
    }

    private class DetailsListCellRenderer implements ListCellRenderer {
	private TrainDetailsJPanel trainDetailsJPanel = new
	    TrainDetailsJPanel();
	private TrainViewJPanel trainViewJPanel;

	public Component getListCellRendererComponent(JList list, Object
		value, int index, boolean isSelected, boolean cellHasFocus) {
	    System.out.println ("getting index " + index);
	    if (index == -1) {
		trainDetailsJPanel.displayTrain(wi.getIndex());
		return trainDetailsJPanel;
	    }
	    return trainViewJPanel.getListCellRendererComponent(list, value,
		    index, isSelected, cellHasFocus);
	}
    };

    /** Creates new form TrainDialogueJPanel */
    public TrainDialogueJPanel() {
        initComponents();
	/* add the two buttons from newTrainScheduleJPanel "*/
	java.awt.GridBagConstraints gridBagConstraints;
	
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 2;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(newTrainScheduleJPanel1.addStationJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
	gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
	add(newTrainScheduleJPanel1.priorityOrdersJButton, gridBagConstraints);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        newTrainScheduleJPanel1 = new org.railz.client.view.TrainScheduleJPanel();
        jPanel1 = new javax.swing.JPanel();
        previousJButton = new javax.swing.JButton();
        nextJButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(newTrainScheduleJPanel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        previousJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/previous.png")));
        previousJButton.setToolTipText("Previous Train");
        previousJButton.setAlignmentX(1.0F);
        previousJButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        previousJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(previousJButton, gridBagConstraints);

        nextJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/railz/client/graphics/toolbar/next.png")));
        nextJButton.setToolTipText("Next Train");
        nextJButton.setAlignmentX(1.0F);
        nextJButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(nextJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void previousJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousJButtonActionPerformed
        // Add your handling code here:
        if(wi.previous()){
            display();
        }else{
            System.err.println("Couldn't get previous");
        }
    }//GEN-LAST:event_previousJButtonActionPerformed
    
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        // Add your handling code here:
        if(wi.next()){
            display();
        }else{
            System.err.println("Couldn't get next");
        }
    }//GEN-LAST:event_nextJButtonActionPerformed
    
    public void setup(ModelRoot mr, GUIRoot gr) {
	modelRoot = mr;
	guiRoot = gr;
        w = modelRoot.getWorld();
	wi = new NonNullElements(KEY.TRAINS, w, mr.getPlayerPrincipal());
        newTrainScheduleJPanel1.setup(mr, gr);
	addComponentListener(componentListener);

	trainDetailsRenderer.trainDetailsJPanel.setup(modelRoot, null);
	disableAllComponents(trainDetailsRenderer.trainDetailsJPanel);
	trainDetailsRenderer.trainViewJPanel = new TrainViewJPanel(modelRoot);

	popupJButton = new PopupJButton(new World2ListModelAdapter(w,
		    KEY.TRAINS, modelRoot.getPlayerPrincipal(),
		    modelRoot.getMoveChainFork()), trainDetailsRenderer);

	GridBagConstraints gridBagConstraints = new
	    java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(popupJButton, gridBagConstraints);

	popupJButton.addActionListener(popupButtonListener);
    }
    
    /**
     * Refreshes the component with the currently selected train
     */
    public void display(){
	System.out.println("TrainDialogueJPanel display");
	int tmp = wi.getIndex();
	try {
	    wi.reset(); // refresh the iterator code
	    wi.gotoIndex(tmp);
	} catch (NoSuchElementException e) {
	    // ignore
	}
	/* go to 1st train if current position is undefined */
	if (wi.getIndex() < 0 && wi.size() > 0) {
	    wi.next();
	}
	if (wi.getIndex() > 0) {
            this.previousJButton.setEnabled(true);
        } else {
            this.previousJButton.setEnabled(false);
        }
        
        if (wi.getIndex() >= (wi.size() - 1) || wi.getIndex() ==
		WorldIterator.BEFORE_FIRST) {
            this.nextJButton.setEnabled(false);
        } else {
            this.nextJButton.setEnabled(true);
        }
        
        newTrainScheduleJPanel1.display(wi.getIndex());
        trainDetailsRenderer.trainDetailsJPanel.displayTrain(wi.getIndex());
	repaint();
    }
    
    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        newTrainScheduleJPanel1.listUpdated(key, index, p);
        trainDetailsRenderer.trainDetailsJPanel.listUpdated(key, index, p);
	popupJButton.repaint();
    }
    
    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
	if (key == KEY.TRAINS) { 
	    display();
	}
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
	// do nothing
    }
    
    ComponentAdapter componentListener = new ComponentAdapter() {
	public void componentHidden(ComponentEvent e) {
	    modelRoot.removeListListener(TrainDialogueJPanel.this);
	}

	public void componentShown(ComponentEvent e) {
	    modelRoot.addListListener(TrainDialogueJPanel.this);
	    display();
	}
    };
     
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private org.railz.client.view.TrainScheduleJPanel newTrainScheduleJPanel1;
    private javax.swing.JButton nextJButton;
    private javax.swing.JButton previousJButton;
    // End of variables declaration//GEN-END:variables
    
    private PopupJButton popupJButton;
}
