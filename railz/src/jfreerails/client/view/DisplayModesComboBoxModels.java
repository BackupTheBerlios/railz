/*
 * Copyright (C) 2003 Luke Lindsay
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
 * DisplayModesComboBoxModel.java
 *
 * Created on 30 August 2003, 23:39
 */

package jfreerails.client.view;

import java.awt.*;
import java.util.ArrayList;
/**
 *
 * @author  Luke Lindsay
 */
public class DisplayModesComboBoxModels implements javax.swing.ComboBoxModel {
    
     private GraphicsConfiguration defaultConfiguration =
			GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice()
				.getDefaultConfiguration();
     
   
     MyDisplayMode[] modes;
     
     MyDisplayMode selection;
     
     public DisplayModesComboBoxModels(){
	 ArrayList acceptableModes = new ArrayList();
         DisplayMode currentMode = defaultConfiguration.getDevice().getDisplayMode();
         selection = new MyDisplayMode(currentMode);
         DisplayMode[]  displayModes  = defaultConfiguration.getDevice().getDisplayModes();
         for (int i = 0 ; i < displayModes.length ; i ++){
	     /*
	      * don't support anything less than 800 x 600 or anything less
	      * than 16 bits
	      */
	     if (displayModes[i].getWidth() < 800 ||
		     displayModes[i].getHeight() < 600 ||
		     displayModes[i].getBitDepth() < 16)
		 continue;
             acceptableModes.add(new MyDisplayMode(displayModes[i]));
         }       
	 modes = (MyDisplayMode []) acceptableModes.toArray(new
		 MyDisplayMode[acceptableModes.size()]);
     }
    
    
    public Object getSelectedItem() {
        return selection;
    }
    
    public void setSelectedItem(Object anItem) {
        selection = (MyDisplayMode)anItem;
    }
    
    public void addListDataListener(javax.swing.event.ListDataListener l) {
    }
    
    public Object getElementAt(int index) {
        return modes[index];
    }
    
    public int getSize() {
        return modes.length;
    }
    
    public void removeListDataListener(javax.swing.event.ListDataListener l) {
    }
    
}
