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
/**
 * class PalettePanel is used by NetworkView to present the chroma code of values
 */
public class PalettePanel extends JPanel implements NetworkViewListener{
  NetworkView view;
  NumberFormat format = NumberFormat.getInstance();
  String leftLabel = "units", rightLabel = "links";
  double leftMin = -1, leftMax = 1, rightMin = -1, rightMax = 1;
  Palette palette;
  private boolean first_paint = true;

  public PalettePanel( NetworkView view ){
    format.setMaximumFractionDigits(2);
    this.view = view;
    view.addListener( this );
    setLayout( null );
    palette = new Palette( view );
    getValues();
    palette.setSize( palette.getPreferredSize() );
    add( palette );
  }

  public void paint( Graphics g ){
    super.paint( g );
    FontMetrics fm = g.getFontMetrics();
    Insets insets = view.snns.panel_insets;
    int x, y, maxx = 0, maxy;
    String s = format.format( leftMax );
    y = insets.top + fm.getAscent();
    g.drawString( s, insets.left, y );

    x = Math.max( fm.stringWidth( s ), fm.stringWidth( format.format( leftMin ) ) );
    x = Math.max( fm.getHeight(), x );
    x += insets.left;

    palette.setLocation( x, insets.top + fm.getAscent() / 2 );
    Rectangle r = palette.getBounds();
    x += r.width;

    if( rightLabel != null ){
      s = format.format( rightMax );
      g.drawString( s, x, y );
      maxx = Math.max( fm.getHeight(), fm.stringWidth( s ) );
    }

    y = r.y + r.height / 2;

    AffineTransform T = new AffineTransform(0, -1, 1, 0, 0, 0);
    Font old = g.getFont(), f = old.deriveFont( T );
    g.setFont( f );
    int dx = fm.getAscent() + fm.getLeading(); // Fabian meint, ab JDK 1.4 soll es 0 sein.
    g.drawString(leftLabel, insets.left+dx, y+fm.stringWidth(leftLabel)/2);
    if( rightLabel != null )
      g.drawString(rightLabel, x+dx, y+fm.stringWidth(leftLabel)/2);
    g.setFont(old);

    y = r.y + r.height + fm.getAscent() / 2;
    s = format.format( leftMin );
    g.drawString( s, insets.left, y );

    if( rightLabel != null ){
      s = format.format( rightMin );
      g.drawString( s, x, y );
      maxx = Math.max( maxx, fm.stringWidth( s ) );
    }
    maxx += x + fm.getDescent() + insets.right;

    maxy = y + fm.getAscent() / 2 + fm.getDescent();

    Dimension dmin = new Dimension( maxx, maxy );
    setMinimumSize( dmin );
    setPreferredSize( dmin );
    if( first_paint ) {
      first_paint = false;
      setSize( dmin );
      getParent().validate();
    }
  }

  public void networkViewChanged( NetworkViewEvent evt ){
    if( evt.id == NetworkViewEvent.SETTINGS_CHANGED ) {
      palette.settings = view.settings;
      getValues();
      repaint();
    }
  }

  private void getValues(){
    if( view instanceof SOMPanel ){
      leftLabel = "distance";
      leftMin = 0;
      leftMax = view.settings.dist_max;
      rightLabel = null;
      palette.min = 0;
      palette.max = 1;
    }
    else {
      NetworkViewSettings s = view.settings;
      leftMin =  - s.unit_max;
      leftMax = s.unit_max;
      rightMin = - s.link_max;
      rightMax = s.link_max;
    }
  }
}

