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
 * LauncherPanel1.java
 *
 * Created on 20 December 2003, 14:58
 */

package org.railz.launcher;

import java.net.InetSocketAddress;

import javax.swing.ButtonModel;
import org.railz.util.Resources;

/**
 *
 * @author rtuck99@users.sourceforge.net 
 */
final class LauncherPanel1 extends javax.swing.JPanel {
    static final int MODE_SINGLE_PLAYER = 0;
    static final int MODE_START_NETWORK_GAME = 1;
    static final int MODE_JOIN_NETWORK_GAME = 2;
    static final int MODE_SERVER_ONLY = 3;

    private Launcher owner;

    ButtonModel[] buttonModels = new ButtonModel[4];
    
    int getMode() {
	for (int i = 0; i < buttonModels.length; i++) {
	    if (buttonGroup1.getSelection() == buttonModels[i]) {
		return i;
	    }
	}
	assert false;
	return 0;
    }
    
    int getServerPort() {
	String s = serverPort.getText();
	return Integer.parseInt(s);
    }

    InetSocketAddress getRemoteServerAddress() {
	String portStr = remotePort.getText();
	if (portStr == null) {
	    return null;
	}
	int port;
	try {
	    port = Integer.parseInt(portStr);
	} catch (NumberFormatException e) {
	    return null;
	}
	InetSocketAddress address;
	try {
	    address = new InetSocketAddress
		(remoteIP.getText(), port);
	} catch (IllegalArgumentException e) {
	    return null;
	}
	return address;
    }
    
    private void validateSettings() {
	boolean isValid = false;
	String infoText = null;

	switch (getMode()) {
	    case MODE_SINGLE_PLAYER:
		isValid = true;
		break;
	    case MODE_START_NETWORK_GAME:
	    case MODE_SERVER_ONLY:
		try {
		    if (getServerPort() > 0 &&
			    getServerPort() < 65536) {
			isValid = true;
			break;
		    }
		} catch (NumberFormatException e) {
		    //ignore
		}
		infoText = Resources.get("Please enter a valid port number");
		break;
	    case MODE_JOIN_NETWORK_GAME:
		InetSocketAddress isa = getRemoteServerAddress();
		if (isa == null) {
		    infoText = Resources.get("Please enter a valid remote " +
			   "server address");
		} else if (isa.isUnresolved()) {
		    infoText = Resources.get("Couldn't resolve remote server "
			   + "address");
		} else {
		    isValid = true;
		}
		break;
	}
	owner.setInfoText(infoText);
	owner.setNextEnabled(isValid);
    }
    
    /** Creates new form LauncherPanel1 */
    public LauncherPanel1(Launcher owner) {
        initComponents();
	this.owner = owner;

	buttonModels[MODE_SINGLE_PLAYER] = singlePlayerButton.getModel();
	buttonModels[MODE_START_NETWORK_GAME] = startNetworkButton.getModel();
	buttonModels[MODE_JOIN_NETWORK_GAME] = joinNetworkButton.getModel();
	buttonModels[MODE_SERVER_ONLY] = serverOnlyButton.getModel();
	validateSettings();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        singlePlayerButton = new javax.swing.JRadioButton();
        startNetworkButton = new javax.swing.JRadioButton();
        joinNetworkButton = new javax.swing.JRadioButton();
        serverOnlyButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        remoteIP = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        remotePort = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        serverPort = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), org.railz.util.Resources.get("Select Game Type")));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        singlePlayerButton.setSelected(true);
        singlePlayerButton.setText(org.railz.util.Resources.get("Single-Player"));
        buttonGroup1.add(singlePlayerButton);
        singlePlayerButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                singlePlayerButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(singlePlayerButton, gridBagConstraints);

        startNetworkButton.setText(org.railz.util.Resources.get("Start a network game"));
        buttonGroup1.add(startNetworkButton);
        startNetworkButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startNetworkButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(startNetworkButton, gridBagConstraints);

        joinNetworkButton.setText(org.railz.util.Resources.get("Join a network game"));
        buttonGroup1.add(joinNetworkButton);
        joinNetworkButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                joinNetworkButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(joinNetworkButton, gridBagConstraints);

        serverOnlyButton.setText(org.railz.util.Resources.get("Server only"));
        buttonGroup1.add(serverOnlyButton);
        serverOnlyButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                serverOnlyButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(serverOnlyButton, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), org.railz.util.Resources.get("Remote server address")));
        jPanel1.setEnabled(false);
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText(org.railz.util.Resources.get("IP Address:"));
        jPanel2.add(jLabel1);

        remoteIP.setColumns(15);
        remoteIP.setText("127.0.0.1");
        remoteIP.setEnabled(false);
        remoteIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remoteIPActionPerformed(evt);
            }
        });
        remoteIP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                remoteIPFocusLost(evt);
            }
        });

        jPanel2.add(remoteIP);

        jLabel2.setText(org.railz.util.Resources.get("Port:"));
        jPanel2.add(jLabel2);

        remotePort.setColumns(5);
        remotePort.setText("55000");
        remotePort.setEnabled(false);
        remotePort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remotePortActionPerformed(evt);
            }
        });
        remotePort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                remotePortFocusLost(evt);
            }
        });

        jPanel2.add(remotePort);

        jPanel1.add(jPanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel3.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), org.railz.util.Resources.get("Server port")));
        jLabel3.setText(org.railz.util.Resources.get("Port:"));
        jPanel3.add(jLabel3);

        serverPort.setColumns(6);
        serverPort.setText("55000");
        serverPort.setEnabled(false);
        serverPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverPortActionPerformed(evt);
            }
        });
        serverPort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                serverPortFocusLost(evt);
            }
        });

        jPanel3.add(serverPort);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel3, gridBagConstraints);

    }//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
	validateSettings();
    }//GEN-LAST:event_formComponentShown

    private void serverOnlyButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_serverOnlyButtonStateChanged
	serverPort.setEnabled(serverOnlyButton.isSelected());
	validateSettings();
    }//GEN-LAST:event_serverOnlyButtonStateChanged

    private void joinNetworkButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_joinNetworkButtonStateChanged
	remoteIP.setEnabled(joinNetworkButton.isSelected());
	remotePort.setEnabled(joinNetworkButton.isSelected());
	validateSettings();
    }//GEN-LAST:event_joinNetworkButtonStateChanged

    private void startNetworkButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startNetworkButtonStateChanged
	serverPort.setEnabled(startNetworkButton.isSelected());
	validateSettings();
    }//GEN-LAST:event_startNetworkButtonStateChanged

    private void singlePlayerButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_singlePlayerButtonStateChanged
	validateSettings();
    }//GEN-LAST:event_singlePlayerButtonStateChanged

    private void serverPortFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_serverPortFocusLost
	validateSettings();
    }//GEN-LAST:event_serverPortFocusLost

    private void remotePortFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_remotePortFocusLost
	validateSettings();
    }//GEN-LAST:event_remotePortFocusLost

    private void remoteIPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_remoteIPFocusLost
	validateSettings();
    }//GEN-LAST:event_remoteIPFocusLost

    private void serverPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverPortActionPerformed
	validateSettings();
    }//GEN-LAST:event_serverPortActionPerformed

    private void remotePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remotePortActionPerformed
	validateSettings();
    }//GEN-LAST:event_remotePortActionPerformed

    private void remoteIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remoteIPActionPerformed
	validateSettings();
    }//GEN-LAST:event_remoteIPActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton joinNetworkButton;
    private javax.swing.JTextField remoteIP;
    private javax.swing.JTextField remotePort;
    private javax.swing.JRadioButton serverOnlyButton;
    private javax.swing.JTextField serverPort;
    private javax.swing.JRadioButton singlePlayerButton;
    private javax.swing.JRadioButton startNetworkButton;
    // End of variables declaration//GEN-END:variables
    
}
