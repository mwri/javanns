/*************************************************************************

This program is copyrighted. Please refer to COPYRIGHT.txt for the
copyright notice.

This file is part of JavaNNS.

JavaNNS is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

JavaNNS is distributed in the hope that it will be useful,
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JavaNNS; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*************************************************************************/


package javanns;

import javax.swing.* ;
import javax.swing.event.*;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.*;
import java.io.IOException ;
import java.util.Vector ;
import java.util.Properties;
import java.io.*;
import java.awt.print.* ;
import java.net.URL;


/**
 * this class manages the frame menu items in the windows menu
 * it simply has to be constructed, it adds it itself to the menu and
 * removes autonomously
 */
class WindowMenuItem extends JMenuItem implements PropertyChangeListener,
                                                            ActionListener{
  private JInternalFrame frame;
  private JMenu menu;
  private boolean is_added;

  public WindowMenuItem(JInternalFrame frame, JMenu menu){
    super(frame.getTitle());
    this.frame = frame;
    this.menu = menu;
    if( frame.isVisible() ) menu.add(this);
    addActionListener(this);
    frame.addInternalFrameListener(
      new InternalFrameAdapter(){
        public void internalFrameClosed(InternalFrameEvent e) {
          removeWMI();
        }
      }
    );
    frame.addComponentListener(
      new ComponentAdapter(){
        public void componentShown(ComponentEvent e) {addWMI();}
        public void componentHidden(ComponentEvent e) {removeWMI();}
      }
    );
    frame.addPropertyChangeListener(frame.TITLE_PROPERTY, this);
  }

  public void actionPerformed(ActionEvent evt){
    frame.toFront();
    try{
      frame.setSelected(true);
      frame.setIcon(false);
    }
    catch(java.beans.PropertyVetoException ex){
      System.out.println("Couldn´t select or deiconify the frame");
    }
  }

  public void propertyChange(PropertyChangeEvent e){
    setText(e.getNewValue().toString());
  }

  private void addWMI(){
    if(!is_added){
      menu.add(this);
      is_added = true;
    }
  }

  private void removeWMI(){
    menu.remove(this);
    is_added = false;
  }
}
