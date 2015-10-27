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
import javax.swing.* ;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;


class Palette extends JComponent{
  NetworkViewSettings settings;
  double min = -1, max = 1;

  public Palette( NetworkView view ){
    settings = view.settings;
    setBorder( BorderFactory.createLoweredBevelBorder() );
    setPreferredSize( new Dimension(20, 200) );
  }

  public void paint( Graphics g ){
    //System.out.println("Palette.paint()");
    super.paint( g );
    Color old_color = g.getColor();
    double range = max - min;
    Dimension d = getSize();
    Insets insets = getInsets();
    int realH = d.height - insets.top - insets.bottom;
    for( int y = 0; y < realH; y++ ){
      double v = max - ( y * range ) / (double)realH;
      g.setColor( settings.getColor( v ) );
      g.drawLine( insets.left, y + insets.top, d.width - insets.right, y + insets.top);
    }
    g.setColor( old_color );
  }
}
