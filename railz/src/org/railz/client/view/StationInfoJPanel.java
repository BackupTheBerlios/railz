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
 * StationInfoJPanel.java
 *
 * Created on 04 May 2003, 18:56
 */

package org.railz.client.view;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.railz.client.model.ModelRoot;
import org.railz.controller.MoveReceiver;
import org.railz.move.AddItemToListMove;
import org.railz.move.ListMove;
import org.railz.move.Move;
import org.railz.move.ObjectMove;
import org.railz.util.*;
import org.railz.world.building.*;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.player.*;
import org.railz.world.station.StationModel;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.WagonType;

/** 
 * This JPanel displays the supply and demand at a station.
 *
 * @author  Luke
 */
public class StationInfoJPanel extends javax.swing.JPanel
implements MoveReceiver {
    private static final int ICON_HEIGHT = 12;
    private ModelRoot modelRoot;
    private GUIRoot guiRoot;
    private World2ListModelAdapter2 stationsListModel;
    private ObjectKey2 currentStation;
    private boolean ignoreMoves = true;
    private ReadOnlyWorld world;
    private StationTableModel stationTableModel;
    private StationTableCellRenderer stationTableCellRenderer;
    private ModdableResourceFinder graphicsResourceFinder;
    private StationsListener stationsListener;
    
    /**
     * The index of the cargoBundle associated with this station
     */
    private ObjectKey2 cargoBundleKey;
    
    private class StationTableCellRenderer implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object
		value, boolean isSelected, boolean hasFocus, int row, int
		column) {
	    StationTableRow str = stationTableModel.getStationTableRow(row);
	    switch (column) {
		case 0:
		    return str.cargoTypeLabel;
		case 1:
		    return str.supplyRateLabel;
		case 2:
		    return str.cargoWaitingLabel;
	    }
	    throw new IllegalArgumentException();
	}
    }

    private class StationTableRow {
	public final JLabel cargoTypeLabel;
	public final JLabel supplyRateLabel;
	public final JLabel cargoWaitingLabel;
	public final int cargoType;
	public final int supplyRate;
	public final int cargoWaiting;

	public StationTableRow(int ct, int sr, int cw) {
	    cargoType = ct;
	    supplyRate = sr;
	    cargoWaiting = cw;

	    cargoTypeLabel = new JLabel();
	    cargoTypeLabel.setIcon(modelRoot.getViewLists().getTrainImages()
		    .getWagonImage(cargoType, ICON_HEIGHT));
	    CargoType cType = (CargoType) world.get(KEY.CARGO_TYPES,
		    cargoType, Player.AUTHORITATIVE);
	    cargoTypeLabel.setToolTipText(cType.getDisplayName());

	    supplyRateLabel = new JLabel(String.valueOf(supplyRate));
	    cargoWaitingLabel = new JLabel(String.valueOf(cargoWaiting));
	}
    }

    /**
     * The table model for the supply and demand at this station
     */
    private class StationTableModel extends AbstractTableModel {
	/**
	 * Array of StationTableRow
	 */
	private ArrayList rows = new ArrayList();

	public StationTableRow getStationTableRow(int row) {
	    return (StationTableRow) rows.get(row);
	}

	public String getColumnName(int column) {
	    switch (column) {
		case 0:
		    return Resources.get("Cargo");
		case 1:
		    return Resources.get("Supply rate");
		case 2:
		    return Resources.get("Cargo Waiting");
	    }
	    throw new IllegalArgumentException();
	}

	public StationTableModel() {
	    updateModel();
	}

	public void updateModel() {
	    rows.clear();
	    if (currentStation == null) {
		fireTableDataChanged();
		return;
	    }

	    StationModel station = (StationModel) world.get(currentStation);
	    CargoBundle cb = (CargoBundle) 
                world.get(station.getCargoBundle());
	    for (int i = 0; i < world.size(KEY.CARGO_TYPES,
			Player.AUTHORITATIVE); i++) {
		if (station.getSupply().getSupply(i) == 0)
		    continue;

		int supplyRate = station.getSupply().getSupply(i);
		int waiting = cb.getAmount(i);
		rows.add(new StationTableRow(i, supplyRate, waiting));
	    }
	    fireTableDataChanged();
	}

	public int getRowCount() {
	    return rows.size();
	}

	public int getColumnCount() {
	    return 3;
	}

	public Object getValueAt(int row, int column) {
	    StationTableRow stm = (StationTableRow) rows.get(row);
	    switch (column) {
		case 0:
		    return new Integer(stm.cargoType);
		case 1:
		    return new Integer(stm.supplyRate);
		case 2:
		    return new Integer(stm.cargoWaiting);
	    }
	    throw new IllegalArgumentException();
	}
    }

    ActionListener infoJButtonListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    StationJPanel sjp = new StationJPanel();
	    sjp.setup(modelRoot, currentStation);
	    guiRoot.getDialogueBoxController().showContent(sjp);
	}
    };

    /** Creates new form StationInfoJPanel */
    public StationInfoJPanel(GUIRoot gr) {
        guiRoot = gr;
        graphicsResourceFinder = gr.getGraphicsResourceFinder();
        initComponents();
	jButton1.addActionListener(infoJButtonListener);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nextStation = new javax.swing.JButton();
        previousStation = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        nextStation.setIcon(new ImageIcon(graphicsResourceFinder.getURLForReading("toolbar/next.png")));
        nextStation.setToolTipText(org.railz.util.Resources.get("Next Station"));
        nextStation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nextStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextStationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 4, 4);
        add(nextStation, gridBagConstraints);

        previousStation.setIcon(new ImageIcon(graphicsResourceFinder.getURLForReading("toolbar/previous.png")));
        previousStation.setToolTipText(org.railz.util.Resources.get("Previous Station"));
        previousStation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        previousStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousStationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 4, 2);
        add(previousStation, gridBagConstraints);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Cargo", "Supply Rate", "Waiting"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), org.railz.util.Resources.get("Cargo Types in Demand")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Station Name");
        jLabel1.setAlignmentY(0.0F);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 4);
        jPanel2.add(jLabel1, gridBagConstraints);

        jButton1.setIcon(new ImageIcon(graphicsResourceFinder.getURLForReading("toolbar/info.png")));
        jPanel2.add(jButton1, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 4);
        add(jPanel2, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void previousStationActionPerformed(
    java.awt.event.ActionEvent evt) {
		//GEN-FIRST:event_previousStationActionPerformed
        // Add your handling code here:
        int currentIndex = stationsListModel.getIndex(currentStation);
        if (currentIndex > 0)
        {
            currentStation = stationsListModel.getKey(currentIndex - 1);
            StationModel sm = (StationModel) stationsListModel.getElementAt
                    (currentIndex - 1);
            Point p = new Point(sm.getStationX(), sm.getStationY());
            modelRoot.getCursor().tryMoveCursor(p);
            
            display();
        } else {
            throw new IllegalStateException();
        }
	}//GEN-LAST:event_previousStationActionPerformed
    
    private void nextStationActionPerformed(
    java.awt.event.ActionEvent evt) {
		//GEN-FIRST:event_nextStationActionPerformed
        // Add your handling code here:
        int currentIndex = stationsListModel.getIndex(currentStation);
        if (currentIndex >= 0 && currentIndex + 1 < stationsListModel.getSize()) {
            currentStation = stationsListModel.getKey(currentIndex + 1);
            StationModel sm = (StationModel) 
                stationsListModel.getElementAt(currentIndex + 1);
            Point p = new Point(sm.getStationX(), sm.getStationY());
            modelRoot.getCursor().tryMoveCursor(p);
            display();
        } else {
            throw new IllegalStateException();
        }
        
	}//GEN-LAST:event_nextStationActionPerformed
    
    public void setup(ModelRoot mr) {
	modelRoot = mr;
	setListModel(new World2ListModelAdapter2(modelRoot.getWorld(), 
                KEY.STATIONS, 
		modelRoot.getPlayerPrincipal(), 
                modelRoot.getMoveChainFork()));
        addComponentListener(componentListener);
	modelRoot.getMoveChainFork().addSplitMoveReceiver(this);
	world = modelRoot.getWorld();
	stationTableModel = new StationTableModel();
	jTable1.setModel(stationTableModel);
	stationTableCellRenderer = new StationTableCellRenderer();
	jTable1.getColumnModel().getColumn(0).
	    setCellRenderer(stationTableCellRenderer);
    }
    
    public void setStation(ObjectKey2 key) {
        currentStation = key;
        display();
    }
    
    public void display() {
        int currentIndex = -1;
        if (currentStation != null) {
            currentIndex = stationsListModel.getIndex(currentStation);
            jButton1.setEnabled(true);
        }
        
        if (currentStation != null && currentIndex > 0) {
            this.previousStation.setEnabled(true);
        } else {
            this.previousStation.setEnabled(false);
        }
        
        if (currentStation != null &&
                currentIndex < (stationsListModel.getSize() - 1)) {
            this.nextStation.setEnabled(true);
        } else {
            this.nextStation.setEnabled(false);
        }
        stationTableModel.updateModel();
	jPanel1.removeAll();
        
        String label;
        if (currentStation != null) {
            StationModel station =
                (StationModel) world.get(currentStation);
            FreerailsTile tile = world.getTile(station.x, station.y);
	    String stationTypeName = ((BuildingType)
		    world.get(KEY.BUILDING_TYPES,
			tile.getBuildingTile().getType(),
			Player.AUTHORITATIVE)).getName();
            cargoBundleKey = station.getCargoBundle();
            CargoBundle cargoWaiting = (CargoBundle) world.get
		(cargoBundleKey);
            String title = station.getStationName()
		+ " (" + stationTypeName + ")";
            for (int i = 0; i < world.size(KEY.CARGO_TYPES,
			Player.AUTHORITATIVE); i++) {
                //get the values
		boolean isDemanded = station.getDemand().isCargoDemanded(i);
		if (! isDemanded)
		    continue;
		JLabel jl = new JLabel(modelRoot.getViewLists()
			.getTrainImages().getWagonImage(i, ICON_HEIGHT));
		CargoType ct = (CargoType) world.get(KEY.CARGO_TYPES,
			i, Player.AUTHORITATIVE);
		jl.setToolTipText(ct.getDisplayName());
		jPanel1.add(jl);
            }
            label = title;
        } else {
            cargoBundleKey = null;
            label = Resources.get("No Station");
        }
        jLabel1.setText(label);
        this.repaint();
    }
    
    ComponentAdapter componentListener = new ComponentAdapter() {
        public void componentHidden(ComponentEvent e) {
            stationsListModel.dispose();            
        }
        
        public void componentShown(ComponentEvent e) {
            setListModel(new World2ListModelAdapter2(modelRoot.getWorld(),
                    KEY.STATIONS, modelRoot.getPlayerPrincipal(),
                    modelRoot.getMoveChainFork()));
            if (currentStation != null &&
                    ! modelRoot.getWorld().contains(currentStation))
                currentStation = null;
            display();
        }
    };
    
    public void processMove(Move move) {
        if(ignoreMoves){
            return;
        }
        if (move instanceof ObjectMove) {
            ObjectMove om = (ObjectMove) move;
            ObjectKey2 key = (om.getKeyAfter() == null) ? om.getKeyBefore() : om.getKeyAfter();
            if (key.equals(cargoBundleKey)) {
                /* update our cargo bundle */
                display();
                return;        
            }
        }         
    }
    
    private void setListModel(World2ListModelAdapter2 w2lma2) {
        if (stationsListener != null) {
            // unregister the old listeners
            stationsListModel.removeListDataListener(stationsListener);
            stationsListener = null;
        }
        
        stationsListModel = w2lma2;
        if (stationsListModel != null)
            stationsListModel.addListDataListener(new StationsListener());
    }
    
    private class StationsListener implements ListDataListener {
        public void contentsChanged(ListDataEvent e) {
            if (stationsListModel.getIndex(currentStation) >= e.getIndex0() &&
                    stationsListModel.getIndex(currentStation) <= e.getIndex1()) {
                display();
            }
        }

        public void intervalRemoved(ListDataEvent e) {
            if (stationsListModel.getIndex(currentStation) < 0) {
                // the station was removed
                if (stationsListModel.getSize() > 0) {
                    currentStation = stationsListModel.getKey(0);
                } else {
                    currentStation = null;
                }
                display();
            }
        }

        public void intervalAdded(ListDataEvent e) {
            if (currentStation == null) {
                currentStation = stationsListModel.getKey(0);
            }
            display();
        }        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton nextStation;
    private javax.swing.JButton previousStation;
    // End of variables declaration//GEN-END:variables
}
