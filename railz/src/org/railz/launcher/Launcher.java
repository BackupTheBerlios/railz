/*
 * Copyright (C) 2003 Robert Tuck
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
 * Launcher.java
 *
 * Created on 20 December 2003, 16:05
 */

package org.railz.launcher;

import java.awt.CardLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.util.logging.*;

import org.railz.client.ai.*;
import org.railz.client.common.FileUtils;
import org.railz.client.common.ScreenHandler;
import org.railz.client.top.GUIClient;
import org.railz.controller.*;
import org.railz.server.GameServer;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.util.Resources;
import org.railz.world.player.Player;

/**
 * Launcher GUI for both the server and/or client.
 *
 * @author  rtuck99@users.sourceforge.net
 */
public class Launcher extends javax.swing.JFrame implements
FreerailsProgressMonitor {
    private static final int TAB_MAIN_MENU = 0;
    private static final int TAB_MAP_SELECTION = 1;
    private static final int TAB_CLIENT_OPTIONS = 2;
    private static final int TAB_CONNECTION_STATUS = 3;

    private static final int GAME_SPEED_PAUSED = 0;
    private static final int GAME_SPEED_SLOW = 10;
    private static final Logger logger = Logger.getLogger("global");
    
    private Component[] wizardPages = new Component[4];
    int currentPage = TAB_MAIN_MENU;

    public void setMessage(String s) {
	setInfoText(s);
    }

    public void setValue(int i) {
	jProgressBar1.setValue(i);
    }

    public void setMax(int max) {
	jProgressBar1.setMaximum(max);
    }
    
    void setInfoText(String text) {
	infoLabel.setText(text);
    }

    void setNextEnabled(boolean enabled) {
	nextButton.setEnabled(enabled);
	if (nextIsStart) {
	    nextButton.setText(Resources.get("Start"));
	} else {
	    nextButton.setText(Resources.get("Next..."));
	}
    }

    private boolean nextIsStart = false;
    
    private ServerControlInterface sci;

    private void startAIClients(ServerControlInterface sci) throws
	IOException, GeneralSecurityException {
	MapSelectionPanel msp = (MapSelectionPanel)
	    wizardPages[TAB_MAP_SELECTION];
	
	for (int i = 0; i < msp.getNumAIPlayers(); i++) {
	    AIConfiguration aiConfig = new AIConfiguration
		(getNewConnection(sci),
		 getPlayer("AI Player " + String.valueOf(i + 1)));
	    // starts the AI Player
	    new AIClient(aiConfig);
	}
    }

    private ConnectionToServer getNewConnection(ServerControlInterface sci)
	throws IOException {
	LauncherPanel1 lp = (LauncherPanel1) wizardPages[TAB_MAIN_MENU];
	if (sci != null && 
		(lp.getMode() == LauncherPanel1.MODE_SINGLE_PLAYER ||
		lp.getMode() == LauncherPanel1.MODE_START_NETWORK_GAME ||
		lp.getMode() == LauncherPanel1.MODE_SERVER_ONLY)) {
	    return new LocalConnection(sci.getLocalConnection());
	} else {
	    // client only - connect to remote server
	    return new InetConnection(lp.getRemoteServerAddress());
	}
    }

    /** TODO handle loading errors gracefully */
    private void startGame() {
	jProgressBar1.setVisible(true);
	setNextEnabled(false);
	LauncherPanel1 lp = (LauncherPanel1) wizardPages[TAB_MAIN_MENU];
	MapSelectionPanel msp = (MapSelectionPanel)
	    wizardPages[TAB_MAP_SELECTION];
	ClientOptionsJPanel cop = (ClientOptionsJPanel)
	    wizardPages[TAB_CLIENT_OPTIONS];
	ServerStatusPanel ssp = (ServerStatusPanel)
	    wizardPages[TAB_CONNECTION_STATUS];

	boolean recover = true;
	int port, mode;
	GameServer gs = new GameServer();
	GUIClient gc;
	Player p;
	CardLayout cl = (CardLayout) jPanel1.getLayout();
	switch (lp.getMode()) {
	    case LauncherPanel1.MODE_SINGLE_PLAYER:
		if (msp.getMapAction() == MapSelectionPanel.START_NEW_MAP) {
		    sci = gs.getNewGame(msp.getMapName(), this, 0,
			    msp.getScenario());
		} else {
		    sci = gs.getSavedGame(this, 0, msp.getLoadFilename());
		}
		//Set initial game speed to paused.
		sci.setTargetTicksPerSecond(GAME_SPEED_PAUSED);
		mode = cop.isWindowed() ? ScreenHandler.WINDOWED_MODE :
		    ScreenHandler.FULL_SCREEN;
		try {
		    p = getPlayer(cop.getPlayerName());
		    /* hide the form whilst the mode is changed  */
		    if (mode == ScreenHandler.FULL_SCREEN)
			hide();
		    gc = new GUIClient(sci, sci.getLocalConnection(), mode,
			    cop.getDisplayMode(), Resources.get
			    ("Railz Client"), this, p);
		    startAIClients(sci);
		    recover = false;
		} catch (IOException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught IOException:", e);
		} catch (GeneralSecurityException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught " +
			    "GeneralSecurityException", e);
		} catch (Exception e) {
                    logger.log(Level.SEVERE, "Caught unexpected exception", e);
                } finally {                
		    if (recover) {
			sci.quitGame();
			cop.setControlsEnabled(true);
			prevButton.setEnabled(true);
			setNextEnabled(true);
			return;
		    }
		}
		hide();
		break;
	    case LauncherPanel1.MODE_START_NETWORK_GAME:
		if (msp.getMapAction() == MapSelectionPanel.START_NEW_MAP) {
		    sci = gs.getNewGame(msp.getMapName(), this,
			    lp.getServerPort(), msp.getScenario());
		} else {
		    sci = gs.getSavedGame(this, lp.getServerPort(),
			    msp.getLoadFilename());
		}
		mode = cop.isWindowed() ? ScreenHandler.WINDOWED_MODE :
		    ScreenHandler.FULL_SCREEN;
		try {
		    p = getPlayer(cop.getPlayerName());
		    if (mode == ScreenHandler.FULL_SCREEN)
			hide();
		    gc = new GUIClient(sci, sci.getLocalConnection(), mode,
			    cop.getDisplayMode(), Resources.get
			    ("Railz Client"), this, p);
		    if (mode == ScreenHandler.FULL_SCREEN)
			gc.getGUIRoot().getDialogueBoxController().
			    createDialog(contentJPanel, Resources.get
				    ("Railz Launcher"));
		    startAIClients(sci);
		    recover = false;
		} catch (IOException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught IOException", e);
		} catch (GeneralSecurityException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught " + 
			    "GeneralSecurityException", e);
		} catch (Exception e) {
		    logger.log(Level.SEVERE, "Caught " + 
			    "Exception", e);
		}finally {
		    if (recover) {
			sci.quitGame();
			cop.setControlsEnabled(true);
			prevButton.setEnabled(true);
			setNextEnabled(true);
			return;
		    }
		}
		ssp.setTableModel(sci.getClientConnectionTableModel());
		/* show the connection status screen */
		currentPage = TAB_CONNECTION_STATUS;
		showTab(currentPage);
		setNextEnabled(false);
		break;
	    case LauncherPanel1.MODE_JOIN_NETWORK_GAME:
		mode = cop.isWindowed() ? ScreenHandler.WINDOWED_MODE :
		    ScreenHandler.FULL_SCREEN;
		try {
		    p = getPlayer(cop.getPlayerName());
		    if (mode == ScreenHandler.FULL_SCREEN)
			hide();
		    gc = new GUIClient(lp.getRemoteServerAddress(),
				mode, cop.getDisplayMode(), Resources.get
				("Railz Client"), this, p);
		    startAIClients(null);
		    recover = false;
		} catch (IOException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught IOException", e);
		} catch (GeneralSecurityException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught " +
			    "GeneralSecurityException", e);
		} finally {
		    if (recover) {
			cop.setControlsEnabled(true);
			prevButton.setEnabled(true);
			setNextEnabled(true);
			return;
		    }
		}
		hide();
		break;
	    case LauncherPanel1.MODE_SERVER_ONLY:
		if (msp.getMapAction() == MapSelectionPanel.START_NEW_MAP) {
		    sci = gs.getNewGame(msp.getMapName(), this,
			    lp.getServerPort(), msp.getScenario());
		} else {
		    sci = gs.getSavedGame(this, lp.getServerPort(),
			    msp.getLoadFilename());
		}
		try {
		    startAIClients(sci);
		    /* TODO additional server control screen including game
		     * speed controls */
		} catch (IOException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught IOException", e);
		    return;
		} catch (GeneralSecurityException e) {
		    setInfoText(e.getMessage());
		    logger.log(Level.SEVERE, "Caught " + 
			    "GeneralSecurityException", e);
		    return;
		}
		ssp.setTableModel(sci.getClientConnectionTableModel());
		nextIsStart = true;
		setNextEnabled(true);
		currentPage = TAB_CONNECTION_STATUS;
		showTab(currentPage);
	}
    }

    private Player getPlayer(String name) throws IOException {
	Player p;

	try {
	    FileInputStream fis =
		FileUtils.openForReading(FileUtils.DATA_TYPE_PLAYER_SPECIFIC,
			name, "keyPair");
	    setInfoText(Resources.get("Loading saved player keys"));
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    p = (Player) ois.readObject();
	    p.loadSession(ois);
	} catch (FileNotFoundException e) {
	    p = new Player(name);
	    // save both public and private key for future use
	    FileOutputStream fos =
		FileUtils.openForWriting(FileUtils.DATA_TYPE_PLAYER_SPECIFIC,
			name, "keyPair");
	    setInfoText(Resources.get("Saving player keys"));
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(p);
	    p.saveSession(oos);
	} catch (ClassNotFoundException e) {
	    setInfoText(Resources.get("Player KeyPair was corrupted!"));
	    throw new IOException (e.getMessage());
	} catch (IOException e) {
	    setInfoText(Resources.get("Player KeyPair was corrupted!"));
	    throw e;
	}
	return p;
    }

    /**
     * Runs the game.
     */
    public static void main (String args[]) {
	// configure the logging properties
	LogManager lm = LogManager.getLogManager();
	try {
	    lm.readConfiguration(Launcher.class.getResourceAsStream
		    ("/org/railz/util/logging.properties"));
	} catch (IOException e) {
	    System.err.println("Couldn't open logging properties" +
		    " due to IOException" + e.getMessage());
	} catch (SecurityException e) {
	    System.err.println("Couldn't open logging configuration " + 
		    "due to SecurityException:" + e.getMessage());
	}
	Logger l = Logger.getLogger("global");
	Level lev;
	while ((lev = l.getLevel()) == null && l.getParent() != null)
	    l = l.getParent();
	if (System.getProperty(".level") != null)
	    l.setLevel(Level.parse(System.getProperty(".level")));
	Logger.getLogger("global").log(Level.INFO, "Logging enabled at level "
		+ l.getLevel());
	
	Launcher launcher = new Launcher();
	launcher.show();
    }

    /** Creates new form Launcher */
    public Launcher() {
        initComponents();

	/*
	 * Add the necessary wizard panes
	 */
	CardLayout cl = (CardLayout) jPanel1.getLayout();
	wizardPages[TAB_MAIN_MENU] = new LauncherPanel1(this);
	wizardPages[TAB_MAP_SELECTION] = new MapSelectionPanel(this);
	wizardPages[TAB_CLIENT_OPTIONS] = new ClientOptionsJPanel(this);
	wizardPages[TAB_CONNECTION_STATUS] = new ServerStatusPanel(this);

	jPanel1.add(wizardPages[TAB_MAIN_MENU], String.valueOf(TAB_MAIN_MENU));
	jPanel1.add(wizardPages[TAB_MAP_SELECTION],
		String.valueOf(TAB_MAP_SELECTION));
	jPanel1.add(wizardPages[TAB_CLIENT_OPTIONS],
		String.valueOf(TAB_CLIENT_OPTIONS));
	jPanel1.add(wizardPages[TAB_CONNECTION_STATUS],
		String.valueOf(TAB_CONNECTION_STATUS));
	pack();

	/* hide the progress bar until needed */
	jProgressBar1.setVisible(false);

	showTab(TAB_MAIN_MENU);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        contentJPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        nextButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        prevButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        contentJPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.CardLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(jSeparator1, gridBagConstraints);

        nextButton.setText(org.railz.util.Resources.get("Next..."));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(jProgressBar1, gridBagConstraints);

        prevButton.setText(org.railz.util.Resources.get("Back..."));
        prevButton.setEnabled(false);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(prevButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentJPanel.add(infoLabel, gridBagConstraints);

        getContentPane().add(contentJPanel, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
	CardLayout cl = (CardLayout) jPanel1.getLayout();
	nextIsStart = false;
	switch (currentPage) {
	    case TAB_MAP_SELECTION:
		currentPage--;
		showTab(currentPage);
		prevButton.setEnabled(false);
		break;
	    case TAB_CLIENT_OPTIONS:
		LauncherPanel1 panel = (LauncherPanel1)
		    wizardPages[TAB_MAIN_MENU];
		if (panel.getMode() == LauncherPanel1.MODE_JOIN_NETWORK_GAME) {
		    currentPage = TAB_MAIN_MENU;
		    showTab(currentPage);
		    prevButton.setEnabled(false);
		} else {
		    currentPage--;
		    showTab(currentPage);
		}
	}
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
	CardLayout cl = (CardLayout) jPanel1.getLayout();
	LauncherPanel1 panel = (LauncherPanel1) wizardPages[TAB_MAIN_MENU];
	switch (currentPage) {
	    case TAB_MAIN_MENU:
		/* Initial game selection page */
		switch (panel.getMode()) {
		    case LauncherPanel1.MODE_SERVER_ONLY:
		    case LauncherPanel1.MODE_SINGLE_PLAYER:
		    case LauncherPanel1.MODE_START_NETWORK_GAME:
			/* go to map selection screen */
			currentPage++;
			showTab(currentPage);
			break;
		    case LauncherPanel1.MODE_JOIN_NETWORK_GAME:
			/* client display options */
			nextIsStart = true;
			currentPage = TAB_CLIENT_OPTIONS;
			showTab(currentPage);
			break;
		}
		prevButton.setEnabled(true);
		break;
	    case TAB_MAP_SELECTION:
		((MapSelectionPanel)
		 wizardPages[TAB_MAP_SELECTION]).submitScenarioSettings();
		/* map selection page */
		if (panel.getMode() == LauncherPanel1.MODE_SERVER_ONLY) {
		    prevButton.setEnabled(false);
		    startGame();
		} else {
		    nextIsStart = true;
		    prevButton.setEnabled(true);
		    setNextEnabled(true);
		    currentPage++;
		    showTab(currentPage);
		}
		break;
	    case TAB_CLIENT_OPTIONS:
		/* display mode selection */
		prevButton.setEnabled(false);
		((ClientOptionsJPanel) wizardPages[TAB_CLIENT_OPTIONS])
		    .setControlsEnabled(false);
		startGame();
		break;
	    case TAB_CONNECTION_STATUS:
		/* Connection status screen */
		prevButton.setEnabled(false);
		sci.setTargetTicksPerSecond(GAME_SPEED_SLOW);
		setMessage(Resources.get("Game started."));
		setNextEnabled(false);
	}
    }//GEN-LAST:event_nextButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentJPanel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    // End of variables declaration//GEN-END:variables
    
    private void showTab(int tab) {
	((CardLayout) jPanel1.getLayout()).show(jPanel1, String.valueOf(tab));
	switch (tab) {
	    case TAB_MAIN_MENU:
		setTitle(Resources.get("Launcher Main Menu"));
		break;
	    case TAB_MAP_SELECTION:
		setTitle(Resources.get("Server Options"));
		break;
	    case TAB_CLIENT_OPTIONS:
		setTitle(Resources.get("Client Options"));
		break;
	    case TAB_CONNECTION_STATUS:
		setTitle(Resources.get("Connection Status"));
		break;
	}
    }
}
