package jfreerails.move;

import java.awt.Point;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;

public class PurchaseTileMove extends CompositeMove implements Move {
    private static Move[] generateMoves(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	Money tileValue = w.getTile(location.x, location.y).getTerrainValue(w,
		location.x, location.y);
	AddItemTransaction t = new AddItemTransaction(AddItemTransaction.LAND,
		0, 1, new Money(- tileValue.getAmount()));
	return new Move[] {
	    new ChangeTileOwnershipMove(w, location, newOwner),
	    new AddTransactionMove(0, t, true, newOwner)
	};
    }
    
    public PurchaseTileMove(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	super(generateMoves(w, location, newOwner));
    }
}
