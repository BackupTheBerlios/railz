/*
 * Created on 10-Aug-2003
 *
 */
package jfreerails.controller;

import java.util.ArrayList;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackRule;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * This class calculates the cost of a series of track moves.  The
 * motivation for separating this code from the code that generates
 * track moves is that the transactions will be generated by the server
 * whereas the track moves will be generated by a client.
 *
 * @author Luke Lindsay
 *
 */
public class TrackMoveTransactionsGenerator {
    /** Number of each of the track types added. */
    private int[] trackAdded;

    /** Number of each of the track types removed. */
    private int[] trackRemoved;

    /* Note, trackAdded and trackRemoved cannot be combined, since
     * it may cost more to added a unit of track than is refunded when
     * you removed it.
     */
    private ArrayList transactions = new ArrayList();
    private ReadOnlyWorld w;
    private FreerailsPrincipal principal;

    /**
     * @param p the Principal on behalf of which this object generates
     * transactions for
     */
    public TrackMoveTransactionsGenerator(ReadOnlyWorld world,
        FreerailsPrincipal p) {
        w = world;
	principal = p;
    }

    public Move addTransactions(Move move) {
        int numberOfTrackTypes = w.size(KEY.TRACK_RULES);
        trackAdded = new int[numberOfTrackTypes];
        trackRemoved = new int[numberOfTrackTypes];

        unpackMove(move);
        generateTransactions();

        int numberOfMoves = 1 + transactions.size();
        Move[] moves = new Move[numberOfMoves];
        moves[0] = move;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = (Transaction)transactions.get(i);
            moves[i + 1] = new AddTransactionMove(0, t, true, principal);
        }

        return new CompositeMove(moves);
    }

    private void unpackMove(Move move) {
        if (move instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove tm = (ChangeTrackPieceMove)move;
            processMove(tm);
        } else if (move instanceof CompositeMove) {
            CompositeMove cm = (CompositeMove)move;
            cm.getMoves();

            Move[] moves = cm.getMoves();

            for (int i = 0; i < moves.length; i++) {
                unpackMove(moves[i]);
            }
        }
    }

    private void processMove(ChangeTrackPieceMove move) {
        final int after = move.getNewTrackPiece().getTrackRule().getRuleNumber();
        final int before = move.getOldTrackPiece().getTrackRule().getRuleNumber();

        if (after == before) {
            return;
        }

        if (after != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            trackAdded[after]++;
        }

        if (before != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            trackRemoved[before]++;
        }
    }

    private void generateTransactions() {
        transactions.clear();

        //For each track type, generate a transaction if any pieces of the type have been added or removed.
        for (int i = 0; i < trackAdded.length; i++) {
            int numberAdded = trackAdded[i];

            if (0 != numberAdded) {
                TrackRule rule = (TrackRule)w.get(KEY.TRACK_RULES, i);
                Money m = rule.getPrice();
                Money total = new Money(-m.getAmount() * numberAdded);
                Transaction t = new AddItemTransaction(AddItemTransaction.TRACK,
                        i, numberAdded, total);
                transactions.add(t);
            }

            int numberRemoved = trackRemoved[i];

            if (0 != numberRemoved) {
                TrackRule rule = (TrackRule)w.get(KEY.TRACK_RULES, i);
                Money m = rule.getPrice();

                Money total = new Money((m.getAmount() * numberRemoved) / 2);

                //You only get half the money back.
                total = new Money(total.getAmount() / 2);

                Transaction t = new AddItemTransaction(AddItemTransaction.TRACK,
                        i, -numberRemoved, total);
                transactions.add(t);
            }
        }
    }
}
