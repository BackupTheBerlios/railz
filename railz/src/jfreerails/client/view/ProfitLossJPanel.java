/*
 * Copyright (C) 2004 Robert Tuck
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
 * ProfitLossJPanel.java
 *
 * Created on 16 March 2004, 22:50
 */

package jfreerails.client.view;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Calendar;
import javax.swing.UIManager;

import jfreerails.client.model.ModelRoot;
import jfreerails.client.model.ProfitLossModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.GameCalendar;
import jfreerails.util.Resources;

/**
 *
 * @author  rtuck99@users.berlios.de
 */
class ProfitLossJPanel extends javax.swing.JPanel {
    
    /** Creates new form ProfitLossJPanel */
    ProfitLossJPanel() {
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
        jPanel2 = new javax.swing.JPanel();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        taxRateJLabel = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        totalProfitJLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        trackMaintenanceJLabel = new javax.swing.JLabel();
        rollingStockMaintenanceJLabel = new javax.swing.JLabel();
        interestPayableJLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        preTaxProfitJLabel = new javax.swing.JLabel();
        incomeTaxLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        freightRevenueJLabel = new javax.swing.JLabel();
        passengerRevenueJLabel = new javax.swing.JLabel();
        fuelExpenseJLabel = new javax.swing.JLabel();
        grossProfitJLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        totalRevenueJLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        totalExpensesJLabel = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14));
        jLabel1.setText(jfreerails.util.Resources.get("Profit and Loss"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jLabel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/previous.png")));
        prevButton.setToolTipText(jfreerails.util.Resources.get("Previous Year"));
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        jPanel2.add(prevButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/next.png")));
        nextButton.setToolTipText(jfreerails.util.Resources.get("Next Year"));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        jPanel2.add(nextButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 4, 0);
        add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel14.setText(jfreerails.util.Resources.get("Income Tax @"));
        jPanel1.add(jLabel14);

        taxRateJLabel.setText("jLabel18");
        jPanel1.add(taxRateJLabel);

        jLabel19.setText("%");
        jPanel1.add(jLabel19);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jPanel1, gridBagConstraints);

        totalProfitJLabel.setText("jLabel17");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(totalProfitJLabel, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel9.setText(jfreerails.util.Resources.get("Track Maintenance"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel9, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel10.setText(jfreerails.util.Resources.get("Rolling Stock Maintenance"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel10, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel11.setText(jfreerails.util.Resources.get("Interest Payable"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel11, gridBagConstraints);

        trackMaintenanceJLabel.setText("jLabel12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(trackMaintenanceJLabel, gridBagConstraints);

        rollingStockMaintenanceJLabel.setText("jLabel13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(rollingStockMaintenanceJLabel, gridBagConstraints);

        interestPayableJLabel.setText("jLabel14");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(interestPayableJLabel, gridBagConstraints);

        jLabel12.setText(jfreerails.util.Resources.get("Profit Before Tax"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel12, gridBagConstraints);

        preTaxProfitJLabel.setText("jLabel13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(preTaxProfitJLabel, gridBagConstraints);

        incomeTaxLabel.setText("jLabel15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(incomeTaxLabel, gridBagConstraints);

        jLabel16.setText(jfreerails.util.Resources.get("Total Profit"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel16, gridBagConstraints);

        jLabel2.setText(jfreerails.util.Resources.get("Revenue"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(jfreerails.util.Resources.get("Freight Haulage"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel4.setText(jfreerails.util.Resources.get("Passenger Ticket Sales"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel4, gridBagConstraints);

        jLabel5.setText(jfreerails.util.Resources.get("Expenses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel6.setText(jfreerails.util.Resources.get("Fuel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel6, gridBagConstraints);

        jLabel7.setText(jfreerails.util.Resources.get("Gross Profit"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel7, gridBagConstraints);

        jLabel8.setText(jfreerails.util.Resources.get("Operating Expenses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel8, gridBagConstraints);

        freightRevenueJLabel.setText("jLabel9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(freightRevenueJLabel, gridBagConstraints);

        passengerRevenueJLabel.setText("jLabel10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(passengerRevenueJLabel, gridBagConstraints);

        fuelExpenseJLabel.setText("jLabel11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(fuelExpenseJLabel, gridBagConstraints);

        grossProfitJLabel.setText("jLabel12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(grossProfitJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jSeparator3, gridBagConstraints);

        totalRevenueJLabel.setText("jLabel13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(totalRevenueJLabel, gridBagConstraints);

        jLabel15.setText(jfreerails.util.Resources.get("Total Revenue"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel15, gridBagConstraints);

        totalExpensesJLabel.setText("jLabel17");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(totalExpensesJLabel, gridBagConstraints);

        jLabel18.setText(jfreerails.util.Resources.get("Total Expenses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabel18, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jSeparator4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 4);
        add(jPanel3, gridBagConstraints);

    }//GEN-END:initComponents

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
	displayedYear--;
	updateDisplay(new ProfitLossModel(modelRoot, displayedYear));
	if (displayedYear == baseYear) {
	    prevButton.setEnabled(false);
	}
	nextButton.setEnabled(true);
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
	displayedYear++;
	updateDisplay(new ProfitLossModel(modelRoot, displayedYear));
	if (displayedYear == currentYear) {
	    nextButton.setEnabled(false);
	}
	prevButton.setEnabled(true);
    }//GEN-LAST:event_nextButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel freightRevenueJLabel;
    private javax.swing.JLabel fuelExpenseJLabel;
    private javax.swing.JLabel grossProfitJLabel;
    private javax.swing.JLabel incomeTaxLabel;
    private javax.swing.JLabel interestPayableJLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel passengerRevenueJLabel;
    private javax.swing.JLabel preTaxProfitJLabel;
    private javax.swing.JButton prevButton;
    private javax.swing.JLabel rollingStockMaintenanceJLabel;
    private javax.swing.JLabel taxRateJLabel;
    private javax.swing.JLabel totalExpensesJLabel;
    private javax.swing.JLabel totalProfitJLabel;
    private javax.swing.JLabel totalRevenueJLabel;
    private javax.swing.JLabel trackMaintenanceJLabel;
    // End of variables declaration//GEN-END:variables
    
    private ModelRoot modelRoot;

    private int currentYear;
    private int baseYear;
    private int displayedYear;

    void setup(ModelRoot mr) {
	modelRoot = mr;
	GameTime time = (GameTime) modelRoot.getWorld().get(ITEM.TIME,
		modelRoot.getPlayerPrincipal());
	GameCalendar cal = (GameCalendar)
	    modelRoot.getWorld().get(ITEM.CALENDAR,
		    modelRoot.getPlayerPrincipal());
	currentYear = cal.getCalendar(time).get(Calendar.YEAR);
	displayedYear = currentYear;

	baseYear = cal.getStartYear();

	nextButton.setEnabled(false);
	prevButton.setEnabled(currentYear > baseYear);
	updateDisplay(new ProfitLossModel(modelRoot, currentYear));
    }

    private void updateDisplay(ProfitLossModel model) {
	String yearText = (displayedYear == currentYear) ?
	    Resources.get("Year to date") : String.valueOf(displayedYear);
	jLabel1.setText(Resources.get("Profit and Loss Statement - ") +
		    yearText);
	Color defaultFg =
	    UIManager.getLookAndFeelDefaults().getColor("Label.foreground");
	NumberFormat nf = NumberFormat.getInstance();
	freightRevenueJLabel.setText("$" + nf.format(model.freightRevenue));
	totalRevenueJLabel.setText("$" + nf.format(model.totalRevenue));
	fuelExpenseJLabel.setText("$" + nf.format(model.fuelExpenses));
	grossProfitJLabel.setText("$" + nf.format(model.grossProfit));
	incomeTaxLabel.setText("$" + nf.format(model.incomeTax));
	interestPayableJLabel.setText("$" +
		nf.format(model.interestPayableExpense));
	passengerRevenueJLabel.setText("$" +
		nf.format(model.passengerRevenue));
	totalExpensesJLabel.setText("$" + nf.format(model.totalExpenses));
	preTaxProfitJLabel.setText("$" + nf.format(model.profitBeforeTax));
	if (model.profitBeforeTax < 0)
	    preTaxProfitJLabel.setForeground(Color.RED);
	else
	    preTaxProfitJLabel.setForeground(defaultFg);

	rollingStockMaintenanceJLabel.setText("$" +
		nf.format(model.rollingStockMaintenanceExpense));
	taxRateJLabel.setText(String.valueOf(model.incomeTaxRatePercent));
	totalProfitJLabel.setText("$" + nf.format(model.profitAfterTax));
	if (model.profitAfterTax < 0)
	    totalProfitJLabel.setForeground(Color.RED);
	else
	    totalProfitJLabel.setForeground(defaultFg);

	trackMaintenanceJLabel.setText("$" +
		nf.format(model.trackMaintenanceExpense));
    }
}
