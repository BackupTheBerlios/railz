/*
 * Copyright (C) 2002 Luke Lindsay
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
 * SelectWagonsJPanel.java
 *
 * Created on 29 December 2002, 16:54
 */

package org.railz.client.view;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.TrainImages;
import org.railz.client.renderer.ViewLists;
import org.railz.world.cargo.CargoType;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
/**
 * This JPanel lets the user add wagons to a train.
 * 
 * @author  lindsal8
 *
 */
public class SelectWagonsJPanel extends javax.swing.JPanel {

	private GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	Image stationView;

	private ArrayList wagons = new ArrayList();


    private int engineType = 0;

	private ViewLists viewLists;

    /** Creates new form SelectWagonsJPanel */
	public SelectWagonsJPanel() {
		initComponents();
		URL url = SelectWagonsJPanel.class.getResource("/org/railz/data/station.gif");
		Image tempImage = (new javax.swing.ImageIcon(url)).getImage();
		
		stationView = defaultConfiguration.createCompatibleImage(tempImage.getWidth(null), tempImage.getHeight(null), Transparency.BITMASK);

		Graphics g = stationView.getGraphics();

		g.drawImage(tempImage, 0, 0, null);			
	}
	
	public void resetSelectedWagons(){
		this.wagons.clear();
	}

    /** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the FormEditor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        wagonTypesJList = new javax.swing.JList();
        okjButton = new javax.swing.JButton();
        clearjButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBackground(new java.awt.Color(0, 255, 51));
        setPreferredSize(new java.awt.Dimension(620, 380));
        setMinimumSize(new java.awt.Dimension(640, 400));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanel1.setMinimumSize(new java.awt.Dimension(200, 250));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 1000));
        wagonTypesJList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                wagonTypesJListKeyTyped(evt);
            }
        });
        wagonTypesJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wagonTypesJListMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(wagonTypesJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        okjButton.setText("OK");
        okjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonAction(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(okjButton, gridBagConstraints);

        clearjButton.setText("Clear");
        clearjButton.setActionCommand("clear");
        clearjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(clearjButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 70;
        gridBagConstraints.ipady = -720;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(40, 430, 0, 0);
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    private void okButtonAction(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_okButtonAction
	// Add your handling code here:


    } //GEN-LAST:event_okButtonAction

    private void wagonTypesJListMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_wagonTypesJListMouseClicked
	// Add your handling code here:
	addwagon();
    } //GEN-LAST:event_wagonTypesJListMouseClicked

    private void wagonTypesJListKeyTyped(java.awt.event.KeyEvent evt) { //GEN-FIRST:event_wagonTypesJListKeyTyped
	// Add your handling code here:
	if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
	    addwagon();
	} else {

	}

    } //GEN-LAST:event_wagonTypesJListKeyTyped

    //Adds the wagon selected in the list to the train consist.
    private void addwagon() {
	int type = wagonTypesJList.getSelectedIndex();	
	wagons.add(new Integer(type));
	this.repaint();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
	// Add your handling code here:
	wagons.clear();
	this.repaint();
    } //GEN-LAST:event_jButton1ActionPerformed

    public void paint(Graphics g) {
	//paint the background
	g.drawImage(this.stationView, 0, 0, null);

	int x = 0;

	int y = 330;



	final int SCALED_IMAGE_HEIGHT = 50;
	//paint the wagons
	for (int i = this.wagons.size()-1; i >= 0; i--) {  //Count down so we paint the wagon at the end of the train first. 

	    Integer type = (Integer)wagons.get(i);
	    Image image = viewLists.getTrainImages().getSideOnWagonImage(type.intValue());
	    int scaledWidth = image.getWidth(null) * SCALED_IMAGE_HEIGHT / image.getHeight(null);

	    g.drawImage(image, x, y, scaledWidth, SCALED_IMAGE_HEIGHT, null);
	    x += scaledWidth;			
	}

	//paint the engine		
	if(-1 != this.engineType){ //If an engine is selected.
		Image image = viewLists.getTrainImages().getSideOnEngineImage(this.engineType);
		int scaledWidth = (image.getWidth(null) * SCALED_IMAGE_HEIGHT) / image.getHeight(null);			
		g.drawImage(image, x, y, scaledWidth, SCALED_IMAGE_HEIGHT, null);
	}	
		
	this.paintChildren(g);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearjButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okjButton;
    private javax.swing.JList wagonTypesJList;
    // End of variables declaration//GEN-END:variables
    final private class WagonCellRenderer implements ListCellRenderer {
	private Component[] labels;

        TrainImages trainImages;

	public WagonCellRenderer(World2ListModelAdapter w2lma, TrainImages s) {
	    trainImages = s;

	    labels = new Component[w2lma.getSize()];
	    for (int i = 0; i < w2lma.getSize(); i++) {
		JLabel label = new JLabel();
		label.setFont(new java.awt.Font("Dialog", 0, 12));
		Image image = trainImages.getSideOnWagonImage(i);
		int height = image.getHeight(null);
		int width = image.getWidth(null);
		int scale = height/10;
		
		ImageIcon icon = new
		    ImageIcon(image.getScaledInstance(width/scale,
				height/scale, Image.SCALE_FAST));			
		label.setIcon(icon);
		labels[i] = label;
	    }
	}

	public Component getListCellRendererComponent(JList list, Object value, /* value to display*/
		int index, /* cell index*/
		boolean isSelected, /* is the cell selected*/
		boolean cellHasFocus) /* the list and the cell have the focus*/ {
	    if (index >= 0 && index < labels.length) {
		CargoType cargoType = (CargoType) value;
		String text = "<html><body>" + (isSelected ? "<strong>" : "") + cargoType.getDisplayName() + (isSelected ? "</strong>" : "&nbsp;&nbsp;&nbsp;&nbsp;"/*padding to stop word wrap due to greater wodth of strong font*/) + "</body></html>";
		((JLabel) labels[index]).setText(text);
		return labels[index];
	    }
	    return null;
	}
    }

    public void setup(ModelRoot mr, ActionListener submitButtonCallBack) {
        this.viewLists = mr.getViewLists();
	World2ListModelAdapter w2lma = new World2ListModelAdapter(mr.getWorld(),
		KEY.CARGO_TYPES, mr.getPlayerPrincipal()); 
	this.wagonTypesJList.setModel(w2lma);
	TrainImages trainImages = viewLists.getTrainImages();
	WagonCellRenderer wagonCellRenderer = new WagonCellRenderer(w2lma,
		trainImages);
	this.wagonTypesJList.setCellRenderer(wagonCellRenderer);
	this.okjButton.addActionListener(submitButtonCallBack);
    }

    public int[] getWagons(){
		int [] wagonsArray = new int[wagons.size()];
		for(int i=0; i<wagons.size(); i++){
			Integer type = (Integer)wagons.get(i);
			wagonsArray[i]=type.intValue();
		}
		return wagonsArray;		
	}

	public void setEngineType(int engineType) {
		this.engineType = engineType;
	}

}
