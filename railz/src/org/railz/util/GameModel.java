/*
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

package org.railz.util;

public interface GameModel {
    public static final GameModel NULL_MODEL = new GameModel() {
	private Object mutex = new Integer(1);

            public void update() {
            }

	    public Object getMutex() {
		return mutex;
	    }
        };

    /**
     * Call to update the game world with any pending moves
     */
    void update();

    /**
     * @return an object which can be used to synchronize against in order to
     * prevent changes to the game world.
     */
    public Object getMutex();
}
