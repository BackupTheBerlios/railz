package jfreerails.client.top;

import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.view.DetailMapView;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MainMapAndOverviewMapMediator;
import jfreerails.client.view.MapCursor;
import jfreerails.client.view.MapViewJComponentConcrete;
import jfreerails.client.view.MapViewMoveReceiver;
import jfreerails.client.view.ModelRoot;
import jfreerails.client.view.ModelRootListener;
import jfreerails.client.view.OverviewMapJComponent;
import jfreerails.client.view.ServerControlModel;
import jfreerails.client.view.StationPlacementCursor;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.StationBuilder;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * A central point for coordinating GUI components.
 */
public class GUIComponentFactoryImpl implements ModelRootListener {
    private ModelRoot modelRoot;

    private DialogueBoxController dialogueBoxController;
    private ReadOnlyWorld world;
    private MapViewJComponentConcrete mapViewJComponent;
    DetailMapView mainMap;
    ClientJFrame clientJFrame;
    UserMessageGenerator userMessageGenerator;
    private MainMapAndOverviewMapMediator mediator;
    private ScreenHandler screenHandler;

    public GUIComponentFactoryImpl(ModelRoot mr) {
        modelRoot = mr;

        modelRoot.addModelRootListener(this);
        mediator = new MainMapAndOverviewMapMediator();
        clientJFrame = new ClientJFrame(this, modelRoot);
        dialogueBoxController = new DialogueBoxController(clientJFrame,
                modelRoot, this);
    }

    private void setup() {
        ViewLists viewLists = modelRoot.getViewLists();
        world = modelRoot.getWorld();

        if (!viewLists.validate(world)) {
            throw new IllegalArgumentException("The specified" +
                " ViewLists are not comaptible with the clients world!");
        }

        dialogueBoxController.setup();

        clientJFrame.setup();

        userMessageGenerator = new UserMessageGenerator(this.modelRoot, world);
        modelRoot.getMoveChainFork().add(userMessageGenerator);
    }

    public ScreenHandler getScreenHandler() {
	return screenHandler;
    }

    public void setScreenHandler(ScreenHandler sh) {
	screenHandler = sh;
    }

    public DialogueBoxController getDialogueBoxController() {
	return dialogueBoxController;
    }

    public MainMapAndOverviewMapMediator getMapMediator() {
	return mediator;
    }

    public void setMapMediator(MainMapAndOverviewMapMediator m) {
	mediator = m;
    }

    public MapViewJComponentConcrete getMapViewJComponent() {
	return mapViewJComponent;
    }

    public void setMapViewJComponent(MapViewJComponentConcrete mapView) {
	mapViewJComponent = mapView;
    }

    public JFrame getClientJFrame() {
        return clientJFrame;
    }

    public void modelRootChanged() {
        setup();
    }

    public void update() {
    }
}
