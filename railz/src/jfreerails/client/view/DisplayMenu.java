package jfreerails.client.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jfreerails.util.Resources;

public class DisplayMenu extends JMenu {
    private GUIRoot guiRoot;

    public DisplayMenu(GUIRoot gcf) {
        super(Resources.get("Display"));
	guiRoot = gcf;
        setMnemonic(68);

        JMenuItem trainOrdersJMenuItem = new JMenuItem
	    (Resources.get("Train Orders"));
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainOrders();
                }
            });

        JMenuItem stationInfoJMenuItem = new JMenuItem
	    (Resources.get("Station Info"));
        stationInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showStationInfo(0);
                }
            });

        JMenuItem trainListJMenuItem = new JMenuItem
	    (Resources.get("Train List"));
        trainListJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainList();
                }
            });

        JMenuItem profitLossJMenuItem = new JMenuItem
	    (Resources.get("Profit and Loss Statement"));
        profitLossJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showProfitLoss();
                }
            });

        JMenuItem balanceSheetJMenuItem = new JMenuItem
	    (Resources.get("Balance Sheet"));
        balanceSheetJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showBalanceSheet();
                }
            });

        add(trainOrdersJMenuItem);
        add(stationInfoJMenuItem);
        add(trainListJMenuItem);
	add(profitLossJMenuItem);
	add(balanceSheetJMenuItem);
    }

}
