/**
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.common;

/**
 * Implemented by a class which is a fixed asset
 */
public interface FixedAsset {
    /**
     * @return the current book value of the item
     */
    public long getBookValue();
}
