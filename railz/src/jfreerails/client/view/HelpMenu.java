package jfreerails.client.view;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jfreerails.client.top.GUIComponentFactoryImpl;

public class HelpMenu extends JMenu {
    GUIComponentFactoryImpl guiComponentFactory;

    public HelpMenu (GUIComponentFactoryImpl gcf) {
        super("Help");
	guiComponentFactory = gcf;

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
		    guiComponentFactory.getDialogueBoxController();
		    if (dbc != null)
			dbc.showAbout();
                }
            });

        JMenuItem how2play = new JMenuItem("Getting started");
        how2play.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiComponentFactory.getDialogueBoxController();
			if (dbc != null)
			    dbc.showHow2Play();
                }
            });

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiComponentFactory.getDialogueBoxController();
		    if (dbc != null)
			dbc.showGameControls();
                }
            });

        add(showControls);
        add(how2play);
        add(about);
    }

}

