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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File ;  // for separator in Patterns

class CenterLayout implements LayoutManager{
    String comp_name;
    Component comp;
    private boolean filled = false;
    /**
     * Adds the specified component with the specified name to
     * the layout.
     * @param name the component name
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp){
      //System.out.println("CenterLayout.addLayoutComponent");
      comp_name = name;
      this.comp = comp;
      filled = true;
    }

    /**
     * Removes the specified component from the layout.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp){
      //System.out.println("CenterLayout.removeLayoutComponent");
      if( !comp.equals( this.comp ) ) return;
      comp_name = null;
      this.comp = null;
      filled = false;
    }

    /**
     * Calculates the preferred size dimensions for the specified
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     *
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent){
      //System.out.println("CenterLayout.preferredLayoutSize");
      if( !filled ) {
        Component[] c = parent.getComponents();
        if( c.length == 0 ) return new Dimension(0, 0);
        addLayoutComponent( "Heinz", c[0] );
      }
      return comp.getPreferredSize();
    }

    /**
     * Calculates the minimum size dimensions for the specified
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent){
      //System.out.println("CenterLayout.minimumLayoutSize");
      if( !filled ) {
        Component[] c = parent.getComponents();
        if( c.length == 0 ) return new Dimension( 0, 0 );
        addLayoutComponent( "Heinz", c[0] );
      }
      return preferredLayoutSize( parent );
    }

    /**
     * Lays out the container in the specified panel.
     * @param parent the component which needs to be laid out
     */
    public void layoutContainer(Container parent){
      //System.out.println("CenterLayout.layoutContainer");
      if( !filled ) {
        Component[] c = parent.getComponents();
        if( c.length == 0 ) return;
        addLayoutComponent( "Heinz", c[0] );
      }
      Dimension d = parent.getSize(), d1 = comp.getPreferredSize();
      d.width -= d1.width;
      d.height -= d1.height;
      comp.setLocation( d.width / 2, d.height / 2 );
      comp.setSize( d1 );
      comp.setVisible( true );
    }

}
