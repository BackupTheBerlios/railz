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

package jfreerails.client.view;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import jfreerails.client.model.ModelRoot;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddItemToListMove;
import jfreerails.move.ListMove;
import jfreerails.move.Move;
import jfreerails.util.*;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.player.*;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.*;
import jfreerails.world.train.WagonType;

/** 
 * This JPanel displays the supply and demand at a station.
 *
 * @author  Luke
 */
public class StationInfoJPanel extends javax.swing.JPanel
implements MoveReceiver {
    private static final int ICON_HEIGHT = 12;
    private ModelRoot modelRoot;
    private WorldIterator wi;
    private boolean ignoreMoves = true;
    private ReadOnlyWorld world;
    private StationTableModel stationTableModel;
    
    /**
     * The index of the cargoBundle associated with this station
     */
    private int cargoBundleIndex;
    
    private ImageIcon getWagonImage(int cargoType) {
	for (int i = 0; i < world.size(KEY.WAGON_TYPES,
		    Player.AUTHORITATIVE); i++) {
	    if (((WagonType) world.get(KEY.WAGON_TYPES, i,
			    Player.AUTHORITATIVE)).getCargoType()
		    == cargoType) {
		Image icon =
		    modelRoot.getViewLists().getTrainImages().
		    getSideOnWagonImage(i, ICON_HEIGHT);
		return new ImageIcon(icon);
	    }
	}
	throw new IllegalArgumentException();
    }

    private class StationTableCellRenderer implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object
		value, boolean isSelected, boolean hasFocus, int row, int
		column) {
	    JLabel label = new JLabel();
	    switch (column) {
		case 0:
		    int cargoType = ((Integer) value).intValue();
		    label.setIcon(getWagonImage(cargoType));
		    CargoType ct = (CargoType) world.get(KEY.CARGO_TYPES,
			    cargoType, Player.AUTHORITATIVE);
		    label.setToolTipText(ct.getDisplayName());
		    return label;
		case 1:
		case 2:
		    label.setText(((Integer) value).toString());
		    return label;
	    }
	    throw new IllegalArgumentException();
	}
    }

    private class StationTableRow {
	public final int cargoType;
	public final int supplyRate;
	public final int cargoWaiting;

	public StationTableRow(int ct, int sr, int cw) {
	    cargoType = ct;
	    supplyRate = sr;
	    cargoWaiting = cw;
	}
    }

    private class StationTableModel extends AbstractTableModel {
	/**
	 * Array of StationTableRow
	 */
	private ArrayList rows = new ArrayList();

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
	    if (wi.getIndex() == WorldIterator.BEFORE_FIRST) {
		fireTableDataChanged();
		return;
	    }

	    StationModel station = (StationModel) world.get(KEY.STATIONS,
		    wi.getIndex(), modelRoot.getPlayerPrincipal());
	    CargoBundle cb = (CargoBundle) world.get(KEY.CARGO_BUNDLES,
		    station.getCargoBundleNumber(), Player.AUTHORITATIVE);
	    for (int i = 0; i < world.size(KEY.CARGO_TYPES); i++) {
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

    /** Creates new form StationInfoJPanel */
    public StationInfoJPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        nextStation = new javax.swing.JButton();
        previousStation = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Station Name");
        jLabel1.setAlignmentY(0.0F);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 4);
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        nextStation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/next.png")));
        nextStation.setToolTipText(jfreerails.util.Resources.get("Next Station"));
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
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 4, 4);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(nextStation, gridBagConstraints);

        previousStation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/previous.png")));
        previousStation.setToolTipText(jfreerails.util.Resources.get("Previous Station"));
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
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 4, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
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
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), jfreerails.util.Resources.get("Cargo Types in Demand")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void previousStationActionPerformed(
    java.awt.event.ActionEvent evt) {
		//GEN-FIRST:event_previousStationActionPerformed
        // Add your handling code here:
        if (wi.previous()) {
            Point p =
            new Point(
            ((StationModel) wi.getElement()).getStationX(),
            ((StationModel) wi.getElement()).getStationY());
            modelRoot.getCursor().tryMoveCursor(p);
            
            display();
        } else {
            throw new IllegalStateException();
        }
	} //GEN-LAST:event_previousStationActionPerformed
    
    private void nextStationActionPerformed(
    java.awt.event.ActionEvent evt) {
		//GEN-FIRST:event_nextStationActionPerformed
        // Add your handling code here:
        if (wi.next()) {
            Point p =
            new Point(
            ((StationModel) wi.getElement()).getStationX(),
            ((StationModel) wi.getElement()).getStationY());
            modelRoot.getCursor().tryMoveCursor(p);
            display();
        } else {
            throw new IllegalStateException();
        }
        
	} //GEN-LAST:event_nextStationActionPerformed
    
    public void setup(ModelRoot mr) {
	modelRoot = mr;
	this.wi = new NonNullElements(KEY.STATIONS, modelRoot.getWorld(),
		modelRoot.getPlayerPrincipal());
        addComponentListener(componentListener);
	modelRoot.getMoveChainFork().addSplitMoveReceiver(this);
	world = modelRoot.getWorld();
	stationTableModel = new StationTableModel();
	jTable1.setModel(stationTableModel);
	jTable1.getColumnModel().getColumn(0).
	    setCellRenderer(new StationTableCellRenderer());
    }
    
    public void setStation(int stationNumber) {
        this.wi.gotoIndex(stationNumber);
        display();
    }
    
    public void display() {
        
        if (wi.getRowNumber() > 0) {
            this.previousStation.setEnabled(true);
        } else {
            this.previousStation.setEnabled(false);
        }
        
        if (wi.getRowNumber() < (wi.size() - 1)) {
            this.nextStation.setEnabled(true);
        } else {
            this.nextStation.setEnabled(false);
        }
        stationTableModel.updateModel();
	jPanel1.removeAll();

        int stationNumber = wi.getIndex();
        String label;
        if (stationNumber != WorldIterator.BEFORE_FIRST) {
            StationModel station =
	    (StationModel) world.get(KEY.STATIONS, stationNumber,
				     modelRoot.getPlayerPrincipal());
            FreerailsTile tile = world.getTile(station.x, station.y);
	    String stationTypeName = ((TrackRule) world.get(KEY.TRACK_RULES,
			tile.getTrackRule(),
			Player.AUTHORITATIVE)).toString();
            cargoBundleIndex = station.getCargoBundleNumber();
            CargoBundle cargoWaiting = (CargoBundle) world.get
		(KEY.CARGO_BUNDLES, station.getCargoBundleNumber());
            String title = station.getStationName()
		+ " (" + stationTypeName + ")";
            for (int i = 0; i < world.size(KEY.CARGO_TYPES); i++) {
                //get the values
		boolean isDemanded = station.getDemand().isCargoDemanded(i);
		if (! isDemanded)
		    continue;
		JLabel jl = new JLabel(getWagonImage(i));
		CargoType ct = (CargoType) world.get(KEY.CARGO_TYPES,
			i, Player.AUTHORITATIVE);
		jl.setToolTipText(ct.getDisplayName());
		jPanel1.add(jl);
            }
            label = title;
        } else {
            cargoBundleIndex = WorldIterator.BEFORE_FIRST;
            label = Resources.get("No Station");
        }
        jLabel1.setText(label);
        this.repaint();
    }
    
    ComponentAdapter componentListener = new ComponentAdapter() {
        public void componentHidden(ComponentEvent e) {
            ignoreMoves = true;
        }
        
        public void componentShown(ComponentEvent e) {
            ignoreMoves = false;
            int i = wi.getIndex();
            wi.reset();
            if (i != WorldIterator.BEFORE_FIRST) {
                wi.gotoIndex(i);
            }
            display();
        }
    };
    
    public void processMove(Move move) {
        if(ignoreMoves){
            return;
        }
        if (!(move instanceof ListMove)) {
            return;
        }
        ListMove lm = (ListMove) move;
        int currentIndex = wi.getIndex();
        int changedIndex = lm.getIndex();
        KEY key = lm.getKey();
        if (key == KEY.CARGO_BUNDLES) {
            if (changedIndex == cargoBundleIndex) {
                /* update our cargo bundle */
                display();
                return;
            }
        } else if (key == KEY.STATIONS) {
            wi.reset();
            if (currentIndex != WorldIterator.BEFORE_FIRST) {
                wi.gotoIndex(currentIndex);
            }
            if (lm instanceof AddItemToListMove
            && wi.getIndex() == WorldIterator.BEFORE_FIRST) {
                if (wi.next()) {
                    display();
                }
            }
            if (changedIndex < currentIndex) {
                previousStation.setEnabled(lm.getBefore() != null);
            } else if (changedIndex > currentIndex) {
                nextStation.setEnabled(lm.getAfter() != null);
            } else {
                display();
            }
        }
        return;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton nextStation;
    private javax.swing.JButton previousStation;
    // End of variables declaration//GEN-END:variables
}
