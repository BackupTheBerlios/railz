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
 * BalanceSheet.java
 *
 * Created on 27 March 2004, 20:17
 */

package jfreerails.client.view;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import javax.swing.JLabel;
import javax.swing.UIManager;

import jfreerails.client.model.*;
import jfreerails.util.Resources;
import jfreerails.world.accounts.*;
import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;

/**
 *
 * @author  rtuck99@users.berlios.de
 */
class BalanceSheetJPanel extends javax.swing.JPanel {
    private BalanceSheetModel balanceSheetModel;
    private ModelRoot modelRoot;
    private int currentYear;
    private int startYear;
    private int displayedYear;
    
    /** Creates new form BalanceSheet */
    public BalanceSheetJPanel(ModelRoot mr) {
	modelRoot = mr;
        initComponents();
	GameCalendar gc = (GameCalendar) mr.getWorld().get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	GameTime gt = (GameTime) mr.getWorld().get(ITEM.TIME,
		Player.AUTHORITATIVE);
	startYear = gc.getStartYear();
	currentYear = gc.getCalendar(gt).get(GregorianCalendar.YEAR);
	displayedYear = currentYear;
	nextJButton.setEnabled(false);
	prevJButton.setEnabled(displayedYear != startYear);
	updateDisplay();
    }
    
    private void setLabelAmount(NumberFormat nf, JLabel label, long amount) {
	label.setText("$" + nf.format(amount));
    }

    /**
     * Updates the information on the display
     */
    private void updateDisplay() {
	String titleText = (displayedYear == currentYear) ? 
	    Resources.get("Balance Sheet:") + Resources.get("Pro-forma for ")
	    + currentYear : Resources.get("Balance Sheet:") + displayedYear;
	Color defaultFg =
	    UIManager.getLookAndFeelDefaults().getColor("Label.foreground");
	NumberFormat nf = NumberFormat.getInstance();
	balanceSheetModel = new BalanceSheetModel (modelRoot, displayedYear);
	BalanceSheet bs = balanceSheetModel.getBalanceSheet();
	jLabel1.setText(titleText);
	setLabelAmount(nf, assetsLessLiabilitiesJLabel,
		bs.assetsLessLiabilities);
	setLabelAmount(nf, bondsJLabel, bs.bonds);
	setLabelAmount(nf, cashJLabel, bs.cash);
	setLabelAmount(nf, incomeTaxJLabel, bs.tax);
	setLabelAmount(nf, initialInvestmentJLabel, bs.investedCapital);
	setLabelAmount(nf, interestJLabel, bs.interest);
	setLabelAmount(nf, loansJLabel, bs.loans);
	setLabelAmount(nf, overdraftJLabel, bs.overdraft);
	setLabelAmount(nf, propertyJLabel, bs.property);
	setLabelAmount(nf, retainedEarnings, bs.retainedEarnings);
	setLabelAmount(nf, rollingStockJLabel, bs.rollingStock);
	setLabelAmount(nf, stockJLabel, bs.stock);
	setLabelAmount(nf, totalAssetsJLabel, bs.totalAssets);
	setLabelAmount(nf, totalCurrentAssetsJLabel, bs.totalCurrentAssets);
	setLabelAmount(nf, totalCurrentLiabilitiesJLabel,
		bs.totalCurrentLiabilities);
	setLabelAmount(nf, totalEquityJLabel, bs.totalEquity);
	setLabelAmount(nf, totalLiabilitiesJLabel, bs.totalLiabilities);
	setLabelAmount(nf, trackMaintenanceJLabel, bs.trackMaintenance);
	setLabelAmount(nf, trainMaintenanceJLabel, bs.trainMaintenance);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        rollingStockJLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cashJLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        propertyJLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        trainMaintenanceJLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        trackMaintenanceJLabel = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        incomeTaxJLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        interestJLabel = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        overdraftJLabel = new javax.swing.JLabel();
        bondsJLabel = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        loansJLabel = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        initialInvestmentJLabel = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        stockJLabel = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        retainedEarnings = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        totalCurrentAssetsJLabel = new javax.swing.JLabel();
        totalAssetsJLabel = new javax.swing.JLabel();
        totalCurrentLiabilitiesJLabel = new javax.swing.JLabel();
        totalLiabilitiesJLabel = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        totalEquityJLabel = new javax.swing.JLabel();
        assetsLessLiabilitiesJLabel = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        prevJButton = new javax.swing.JButton();
        nextJButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(jfreerails.util.Resources.get("Balance Sheet:"));
        add(jLabel1, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 4, 4, 4)));
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 14));
        jLabel2.setText(jfreerails.util.Resources.get("Assets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(jfreerails.util.Resources.get("Rolling Stock"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 1;
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setText(jfreerails.util.Resources.get("Current Assets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel4, gridBagConstraints);

        rollingStockJLabel.setText("rollingstock");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(rollingStockJLabel, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel6.setText(jfreerails.util.Resources.get("Cash"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel6, gridBagConstraints);

        cashJLabel.setText("cash");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(cashJLabel, gridBagConstraints);

        jLabel8.setText(jfreerails.util.Resources.get("Long-term Assets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel8, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 14));
        jLabel9.setText(jfreerails.util.Resources.get("Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jLabel9, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel10.setText(jfreerails.util.Resources.get("Property"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel10, gridBagConstraints);

        propertyJLabel.setText("property");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(propertyJLabel, gridBagConstraints);

        jLabel5.setText(jfreerails.util.Resources.get("Current Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel7.setText(jfreerails.util.Resources.get("Train Maintenance"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel7, gridBagConstraints);

        trainMaintenanceJLabel.setText("jLabel11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(trainMaintenanceJLabel, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel12.setText(jfreerails.util.Resources.get("Track Maintenance"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel12, gridBagConstraints);

        trackMaintenanceJLabel.setText("jLabel13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(trackMaintenanceJLabel, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel14.setText(jfreerails.util.Resources.get("Income Tax"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel14, gridBagConstraints);

        incomeTaxJLabel.setText("jLabel15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(incomeTaxJLabel, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel16.setText(jfreerails.util.Resources.get("Interest on Loans"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel16, gridBagConstraints);

        interestJLabel.setText("jLabel17");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(interestJLabel, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel18.setText(jfreerails.util.Resources.get("Overdraft"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel18, gridBagConstraints);

        overdraftJLabel.setText("jLabel19");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(overdraftJLabel, gridBagConstraints);

        bondsJLabel.setText("jLabel11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(bondsJLabel, gridBagConstraints);

        jLabel13.setText(jfreerails.util.Resources.get("Long-Term Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel13, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel15.setText(jfreerails.util.Resources.get("Bonds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel15, gridBagConstraints);

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel17.setText(jfreerails.util.Resources.get("Loans"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel17, gridBagConstraints);

        loansJLabel.setText("jLabel19");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(loansJLabel, gridBagConstraints);

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel20.setText(jfreerails.util.Resources.get("Retained Earnings"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel20, gridBagConstraints);

        jLabel21.setText(jfreerails.util.Resources.get("Equity"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel21, gridBagConstraints);

        initialInvestmentJLabel.setText("jLabel22");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(initialInvestmentJLabel, gridBagConstraints);

        jLabel23.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel23.setText(jfreerails.util.Resources.get("Initial Investment"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel23, gridBagConstraints);

        stockJLabel.setText("jLabel24");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(stockJLabel, gridBagConstraints);

        jLabel25.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel25.setText(jfreerails.util.Resources.get("Stock"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel25, gridBagConstraints);

        retainedEarnings.setText("jLabel26");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(retainedEarnings, gridBagConstraints);

        jLabel11.setText(jfreerails.util.Resources.get("Total Current Assets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel11, gridBagConstraints);

        jLabel19.setText(jfreerails.util.Resources.get("Total Assets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel19, gridBagConstraints);

        jLabel22.setText(jfreerails.util.Resources.get("Total Current Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel22, gridBagConstraints);

        jLabel24.setText(jfreerails.util.Resources.get("Total Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel24, gridBagConstraints);

        totalCurrentAssetsJLabel.setText("jLabel26");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(totalCurrentAssetsJLabel, gridBagConstraints);

        totalAssetsJLabel.setText("jLabel27");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(totalAssetsJLabel, gridBagConstraints);

        totalCurrentLiabilitiesJLabel.setText("jLabel28");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(totalCurrentLiabilitiesJLabel, gridBagConstraints);

        totalLiabilitiesJLabel.setText("jLabel29");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(totalLiabilitiesJLabel, gridBagConstraints);

        jLabel26.setText(jfreerails.util.Resources.get("Total Equity"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel26, gridBagConstraints);

        totalEquityJLabel.setText("jLabel27");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(totalEquityJLabel, gridBagConstraints);

        assetsLessLiabilitiesJLabel.setText("jLabel27");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(assetsLessLiabilitiesJLabel, gridBagConstraints);

        jLabel28.setText(jfreerails.util.Resources.get("Assets less Liabilities"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel28, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 29;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jSeparator3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jSeparator4, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel2.add(jSeparator1);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        prevJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/previous.png")));
        prevJButton.setToolTipText(jfreerails.util.Resources.get("Previous Year"));
        prevJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevJButtonActionPerformed(evt);
            }
        });

        jPanel4.add(prevJButton);

        nextJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/toolbar/next.png")));
        nextJButton.setToolTipText(jfreerails.util.Resources.get("Next Year"));
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        jPanel4.add(nextJButton);

        jPanel2.add(jPanel4);

        add(jPanel2, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void prevJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevJButtonActionPerformed
	displayedYear--;
	if (displayedYear == startYear) {
	    prevJButton.setEnabled(false);
	}
	nextJButton.setEnabled(true);
	updateDisplay();
    }//GEN-LAST:event_prevJButtonActionPerformed

    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
	displayedYear++;
	if (displayedYear == currentYear) {
	    nextJButton.setEnabled(false);
	}
	prevJButton.setEnabled(true);
	updateDisplay();
    }//GEN-LAST:event_nextJButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel assetsLessLiabilitiesJLabel;
    private javax.swing.JLabel bondsJLabel;
    private javax.swing.JLabel cashJLabel;
    private javax.swing.JLabel incomeTaxJLabel;
    private javax.swing.JLabel initialInvestmentJLabel;
    private javax.swing.JLabel interestJLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel loansJLabel;
    private javax.swing.JButton nextJButton;
    private javax.swing.JLabel overdraftJLabel;
    private javax.swing.JButton prevJButton;
    private javax.swing.JLabel propertyJLabel;
    private javax.swing.JLabel retainedEarnings;
    private javax.swing.JLabel rollingStockJLabel;
    private javax.swing.JLabel stockJLabel;
    private javax.swing.JLabel totalAssetsJLabel;
    private javax.swing.JLabel totalCurrentAssetsJLabel;
    private javax.swing.JLabel totalCurrentLiabilitiesJLabel;
    private javax.swing.JLabel totalEquityJLabel;
    private javax.swing.JLabel totalLiabilitiesJLabel;
    private javax.swing.JLabel trackMaintenanceJLabel;
    private javax.swing.JLabel trainMaintenanceJLabel;
    // End of variables declaration//GEN-END:variables
    
}
