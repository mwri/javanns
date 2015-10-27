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

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;


/**
 * DivisorCurve is nearly an instance of DAequiDistPoints. But here it is
 * possible to set a certain divisor to the y-values of the points
 */
class DivisorCurve extends DAequiDistPoints{
  double divisor = 1;

  public DivisorCurve( double minX, double distance ){
    super( minX, distance, 10 );
  }

  public void setYDivisor( double d ){
    if( d == divisor || d == 0 ) return;
    divisor = d;
    repaint();
  }

  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    Color old_color = g.getColor();
    if( color != null ) g.setColor( color );
    else g.setColor( DEFAULT_COLOR );
    if( connected && getSize() > 1 ){
      Point p1, p2;
      p1 = m.getPoint( x.getImage(0), y.getImage(0) / divisor );
      for( int i=1; i<getSize(); i++ ){
        p2 = m.getPoint( x.getImage(i), y.getImage(i) / divisor );
        g.drawLine( p1.x, p1.y, p2.x, p2.y );
        p1 = p2;
        if( icon != null ){
          g.translate(p1.x, p1.y );
          icon.paint( g );
          g.translate( -p1.x, -p1.y );
        }
      }
    }
    else{
      Point p;
      for( int i=0; i<getSize(); i++ ){
        p = m.getPoint( x.getImage(i), y.getImage(i) / divisor );
        if( icon == null ) g.drawLine(p.x, p.y, p.x, p.y );
        else{
          g.translate(p.x, p.y );
          icon.paint( g );
          g.translate( -p.x, -p.y );
        }
      }
    }
    g.setColor( old_color );
  }

  public DRectangle getRectangle(){
    DRectangle rect = super.getRectangle();
    if( rect.isEmpty() || rect.isAll() ) return rect;
    rect.y /= divisor;
    rect.height /= divisor;
    return rect;
  }
}
