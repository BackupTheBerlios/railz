package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;

public class FreerailsTile implements TrackPiece, TerrainTile,
    FreerailsSerializable {
    public static final FreerailsTile NULL = new FreerailsTile(0);
    private final TrackPiece trackPiece;
    private int terrainType;
    private FreerailsPrincipal owner;

    /**
     * Create a tile. Initially it is owned by the server
     */
    public FreerailsTile(int terrainType) {
	this(terrainType, NullTrackPiece.getInstance());
    }

    public FreerailsTile(int terrainType, TrackPiece trackPiece) {
	this(terrainType, trackPiece, Player.AUTHORITATIVE);
    }

    public FreerailsTile(int terrainType, TrackPiece trackPiece,
	    FreerailsPrincipal owner) {
        this.terrainType = terrainType;
        this.trackPiece = trackPiece;
	this.owner = owner;
    }

    /*
     * @see TrackPiece#getTrackGraphicNumber()
     */
    public int getTrackGraphicNumber() {
        return trackPiece.getTrackGraphicNumber();
    }

    /*
     * @see TrackPiece#getTrackRule()
     */
    public TrackRule getTrackRule() {
        return trackPiece.getTrackRule();
    }

    /*
     * @see TrackPiece#getTrackConfiguration()
     */
    public TrackConfiguration getTrackConfiguration() {
        return trackPiece.getTrackConfiguration();
    }

    public boolean equals(Object o) {
        if (o instanceof FreerailsTile) {
            FreerailsTile test = (FreerailsTile)o;

            boolean trackPieceFieldsEqual = (this.trackPiece.equals(test.trackPiece));

            boolean terrainTypeFieldsEqual = (terrainType == test.getTerrainTypeNumber());

            if (trackPieceFieldsEqual && terrainTypeFieldsEqual) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getTerrainTypeNumber() {
        return terrainType;
    }
/*
    public String toString() {
        return "trackPiece=" + trackPiece.toString() + " and terrainType is " +
        terrainType;
    }
*/
    public TrackPiece getTrackPiece() {
        return trackPiece;
    }

    public FreerailsPrincipal getOwner() {
	return owner;
    }

    public void setOwner(FreerailsPrincipal o) {
	owner = o;
    }
}
