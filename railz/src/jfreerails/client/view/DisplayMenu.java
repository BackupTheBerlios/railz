package jfreerails.client.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jfreerails.client.top.GUIComponentFactoryImpl;

public class DisplayMenu extends JMenu {
    private GUIComponentFactoryImpl guiComponentFactory;

    public DisplayMenu(GUIComponentFactoryImpl gcf) {
        super("Display");
	guiComponentFactory = gcf;
        setMnemonic(68);

        JMenuItem trainOrdersJMenuItem = new JMenuItem("Train Orders");
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiComponentFactory.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainOrders();
                }
            });

        JMenuItem stationInfoJMenuItem = new JMenuItem("Station Info");
        stationInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiComponentFactory.getDialogueBoxController();
		    if (dbc != null)
			dbc.showStationInfo(0);
                }
            });

        JMenuItem trainListJMenuItem = new JMenuItem("Train List");
        trainListJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiComponentFactory.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainList();
                }
            });

        add(trainOrdersJMenuItem);
        add(stationInfoJMenuItem);
        add(trainListJMenuItem);
    }

}
