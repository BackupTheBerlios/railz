package jfreerails.client.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import jfreerails.client.renderer.BlankMapRenderer;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.top.GUIComponentFactoryImpl;

public class OverviewMapJComponent extends JPanel {
    private GUIComponentFactoryImpl guiComponentFactory;
    private MapViewMoveReceiver moveReceiver;
    MainMapAndOverviewMapMediator mediator;

	protected MapRenderer mapView=new BlankMapRenderer(0.4F);

	public OverviewMapJComponent(GUIComponentFactoryImpl gcf) {
	    this.setPreferredSize(mapView.getMapSizeInPixels());
	    guiComponentFactory = gcf;
	    mediator = guiComponentFactory.getMapMediator();
	    addComponentListener(componentListener);
	    addMouseMotionListener(mouseAdapter);
	    addMouseListener(mouseAdapter);
	    guiComponentFactory.getMapMediator().setOverviewMap(this);
	}	

	public void setup(ModelRoot mr){
	    mapView = new ZoomedOutMapRenderer(mr.getWorld());
	    this.setPreferredSize(mapView.getMapSizeInPixels());
	    this.setMinimumSize(this.getPreferredSize());
	    this.setSize(this.getPreferredSize());

	    if (moveReceiver != null) {
		mr.getMoveChainFork().removeSplitMoveReceiver
		    (moveReceiver);
	    }
	    moveReceiver = new MapViewMoveReceiver(mapView);
	    mr.getMoveChainFork().addSplitMoveReceiver(moveReceiver);

	    if(null!=this.getParent()){									
		this.getParent().validate();
	    }			
	    guiComponentFactory.getMapMediator().setOverviewMap(this);
	}

	protected void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
		java.awt.Rectangle r = this.getVisibleRect();
		// draw the overview map
		mapView.paintRect(g2, r);
		// draw the rectangle
		Rectangle mainMapVisRect = guiComponentFactory.
		    getMapMediator().getMapVisibleRectangle();
		g2.setColor(Color.WHITE);
		g2.drawRect(mainMapVisRect.x, mainMapVisRect.y,
			mainMapVisRect.width, mainMapVisRect.height);
	}
	
	public Dimension getPreferredSize() {
		return mapView.getMapSizeInPixels();
	}
    
	private ComponentListener componentListener = new ComponentAdapter() {
	    public void componentResized(java.awt.event.ComponentEvent evt) {
		guiComponentFactory.getMapMediator().updateObservedRect();
	    }
	    public void componentShown(java.awt.event.ComponentEvent evt) {
		guiComponentFactory.getMapMediator().updateObservedRect();
	    }
	};
	
	private MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
	    boolean inside = false;
	    boolean draggingAndStartedInside = false;

	    private void updateInside(MouseEvent evt) {
		Rectangle currentVisRect = mediator.getMapVisibleRectangle();
		boolean b = currentVisRect.contains(evt.getX(), evt.getY());
		if (b != inside) {
		    inside = b;
		    if (inside) {
			OverviewMapJComponent.this.setCursor
			    (new Cursor(Cursor.MOVE_CURSOR));
		    } else {
			OverviewMapJComponent.this.setCursor
			    (new Cursor(Cursor.DEFAULT_CURSOR));
		    }
		}
	    }

	    public void mouseMoved(MouseEvent evt) {
		updateInside(evt);
	    }

	    public void mousePressed(MouseEvent evt) {
		if (inside) {
		    draggingAndStartedInside = true;
		}
	    }

	    public void mouseReleased(MouseEvent evt) {
		draggingAndStartedInside = false;
	    }

	    public void mouseDragged(MouseEvent evt) {
		if (draggingAndStartedInside) {

		    mediator.setMainMapPosition(evt.getPoint());
		    updateInside(evt);
		}
	    }

	    public void mouseClicked(MouseEvent evt) {
		Rectangle r = mediator.getMapVisibleRectangle();
		Point p = new Point(evt.getX() - r.width / 2,
			evt.getY() - r.height / 2);
		mediator.setMainMapPosition(p);
		updateInside(evt);
	    }
	};
}
