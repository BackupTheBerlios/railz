package jfreerails.client.model;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jfreerails.util.Resources;
import jfreerails.client.common.ToggleActionAdapter;

/**
 * Control models for the debug menu
 */
public class DebugModel {
    private ToggleActionAdapter frameRateDebugModel;
    private ToggleActionAdapter clientMoveDebugModel;

    private class ClientMoveDebugAction extends AbstractAction {
	public ClientMoveDebugAction() {
	    putValue(NAME, Resources.get("Client move debug"));
	    setEnabled(false);
	    putValue("Selected", Boolean.FALSE);
	}

	public void actionPerformed(ActionEvent e) {
	    if (Boolean.FALSE == getValue("Selected")) {
		putValue("Selected", Boolean.TRUE);
	    } else {
		putValue("Selected", Boolean.FALSE);
	    }
	}
    }
    
    private class FrameRateDebugAction extends AbstractAction {
	public FrameRateDebugAction() {
	    putValue(NAME, Resources.get("Show Frame Rate"));
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
	    // ignore
	}
    }

    DebugModel() {
	frameRateDebugModel = new ToggleActionAdapter
	    (new FrameRateDebugAction());
	clientMoveDebugModel = new ToggleActionAdapter(new
		ClientMoveDebugAction());
    }

    public ToggleActionAdapter getClientMoveDebugModel() {
	return clientMoveDebugModel;
    }

    public ToggleActionAdapter getFrameRateDebugModel () {
	return frameRateDebugModel;
    }
}
