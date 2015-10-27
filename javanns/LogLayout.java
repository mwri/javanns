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

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.io.* ;
import java.util.Date ;

// drucken:
import wsi.ra.print.TextPrinter ;
import java.awt.print.* ;
import java.util.Vector ;

/**
 * LogLayout manages the layout for the LogPanel
 */
class LogLayout implements LayoutManager{
  LogPanel log;
  Insets insets;
  int dya2b, dxb;

  public LogLayout( LogPanel log, Insets insets, int dy_area2buttons, int dx_buttons){
    this.log = log;
    this.insets = insets;
    dya2b = dy_area2buttons;
    dxb = dx_buttons;
  }

  /**
   * Adds the specified component with the specified name to
   * the layout.
   * @param name the component name
   * @param comp the component to be added
   */
  public void addLayoutComponent(String name, Component comp){
    //System.out.println("LogLayout.addLayoutComponent");
  }

  /**
   * Removes the specified component from the layout.
   * @param comp the component to be removed
   */
  public void removeLayoutComponent(Component comp){
    //System.out.println("LogLayout.removeLayoutComponent");
  }

  /**
   * Calculates the preferred size dimensions for the specified
   * panel given the components in the specified parent container.
   * @param parent the component to be laid out
   *
   * @see #minimumLayoutSize
  */
  public Dimension preferredLayoutSize(Container parent){
    //System.out.println("LogLayout.preferredLayoutSize");
    Dimension d = log.writer.tArea.getPreferredSize();
    d.height += log.bClear.getPreferredSize().height;
    return d;
  }

  /**
   * Calculates the minimum size dimensions for the specified
   * panel given the components in the specified parent container.
   * @param parent the component to be laid out
   * @see #preferredLayoutSize
  */
  public Dimension minimumLayoutSize(Container parent){
    //System.out.println("LogLayout.minimumLayoutSize");
    return preferredLayoutSize(parent);
  }

  /**
   * Lays out the container in the specified panel.
   * In this case: it sets the text area in the top center of the container
   * and puts the buttons in the lower corners
   *
   * @param parent the component which needs to be laid out
   */
  public void layoutContainer(Container parent){
    //System.out.println("LogLayout.llayoutContainer");
    Dimension d = parent.getSize(),
              dClear = log.bClear.getPreferredSize(),
              dState = log.bState.getPreferredSize(),
              dClose = log.bClose.getPreferredSize();
    int y = d.height - dClear.height - insets.bottom;
    log.spArea.setLocation(insets.left, insets.top);
    log.spArea.setSize( d.width - insets.left - insets.right, y - dya2b );
    log.bClear.setSize( dClear );
    log.bState.setSize( dState );
    log.bClose.setSize( dClose );

    int x = d.width - insets.right - dClose.width;
    log.bClose.setLocation( x, y);
    x -= dxb + dState.width;
    log.bState.setLocation( x, y );
    x -= dxb + dClear.width;
    log.bClear.setLocation( x, y );
  }
}
