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


/**
 *  Filename: $RCSfile: DRectangle.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:52:33 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.* ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class DRectangle extends DComponent
{
  public double x, y, width, height;
  public static final int PART = 0, ALL = 1, EMPTY = 2;
  protected int status;
  protected Color fillColor;

  private DRectangle( int status ){
    super(true);
    this.status = status;
  }

  public DRectangle( double x, double y, double width, double height ){
    super(true);
    this.x = x;
    this.y = y;
    if( width < 0 ) throw
      new IllegalArgumentException("Width of a DRectangle has to be >= 0");
    this.width = width;
    if( height < 0 ) throw
      new IllegalArgumentException("Height of a DRectangle has to be >= 0");
    this.height = height;
    status = PART;
  }

  public DRectangle getRectangle(){ return this; }

  public void paint( DMeasures m ){
    if( isEmpty() ) return;
    Graphics g = m.getGraphics();
    Color old_color = g.getColor();
    DRectangle rect = m.getDRectangle();
    rect = rect.getIntersection( this );
    Point p1 = m.getPoint( rect.x, rect.y ),
          p2 = m.getPoint( rect.x + rect.width, rect.y + rect.height );
    if( fillColor != null ){
      g.setColor( fillColor );
      g.fillRect( p1.x, p2.y, p2.x - p1.x, p1.y - p2.y );
    }
    if( !isAll() ){
      if( color != null ) g.setColor( color );
      else g.setColor( DEFAULT_COLOR );
      g.drawRect( p1.x, p2.y, p2.x - p1.x, p1.y - p2.y );
    }
    g.setColor( old_color );
  }

  public boolean contains( DPoint p ){
    if( status == ALL ) return true;
    if( status == EMPTY ) return false;
    if( p.x < x ) return false;
    if( p.y < y ) return false;
    if( p.x > x + width ) return false;
    if( p.y > y + height ) return false;
    return true;
  }

  public boolean contains( DRectangle rect ){
    if( status == ALL || rect.isEmpty() ) return true;
    if( status == EMPTY || rect.isAll() ) return false;
    if( !contains( new DPoint( rect.x, rect.y ) ) ) return false;
    if( !contains( new DPoint( rect.x + rect.width, rect.y + rect.height ) ) ) return false;
    return true;
  }

  public DRectangle getIntersection( DRectangle r ){
    if( status == ALL ) return (DRectangle)r.clone();
    if( status == EMPTY ) return DRectangle.getEmpty();
    if( r.status == ALL ) return (DRectangle)clone();
    if( r.status == EMPTY ) return DRectangle.getEmpty();
    DRectangle s = (DRectangle)this.clone();
    if( s.x < r.x ){
      s.x = r.x;
      s.width -= r.x - s.x;
    }
    if( s.y < r.y ){
      s.y = r.y;
      s.height -= r.y - s.y;
    }
    if( s.x + s.width > r.x + r.width )
      s.width = r.x + r.width - s.x;
    if( s.y + s.height > r.y + r.height )
      s.height = r.y + r.height - s.y;
    if( s.width < 0 || s.height < 0 ) return DRectangle.getEmpty();
    else return s;
  }

  /**
   * method resizes the rectangle to insert p
   *
   * @param the dPoint p to insert
   * @return true when the size of the rectangle changed
   */
  public boolean insert( DPoint p ){
    if( p.x == Double.NaN || p.y == Double.NaN ) return false;
    if( isAll() ) return false;
    if( contains( p ) ) return false;
    if( isEmpty() ){
      x = p.x; y = p.y; width = height = 0;
      status = PART;
      return true;
    }
    if( p.x < x ) {
      width += x - p.x;
      x = p.x;
    }
    else if( p.x > x + width ) width = p.x - x;
    if( p.y < y ) {
      height += y - p.y;
      y = p.y;
    }
    else if( p.y > y + height ) height = p.y - y;
    return true;
  }

  /**
   * method inserts the given rectangle to this instance of it
   * and returns true when the size changed
   *
   * @param rect the rectangle to inserts
   * @return true if the size changed
   */
  public boolean insert( DRectangle rect ){
    if( isAll() || rect.isEmpty() ) return false;
    if( rect.isAll() ){ status = ALL; return true; }
    if( isEmpty() ){
      x = rect.x; y = rect.y; width = rect.width; height = rect.height;
      status = PART;
      return true;
    }
    boolean changed = false;
    changed = insert( new DPoint( rect.x, rect.y ) );
    changed = insert( new DPoint( rect.x + rect.width, rect.y + rect.height ) )? true : changed;
    return changed;
  }

  public Object clone(){
    DRectangle copy = new DRectangle( x, y, width, height );
    copy.status = status;
    if( color != null ) copy.color = new Color( color.getRGB() );
    return copy;
  }

  public String toString(){
    String text = "DRectangle[ ";
    switch( status ){
      case ALL   : text += "all"; break;
      case EMPTY : text += "empty"; break;
      case PART  : text += x+", "+y+", "+width+", "+height;
    }
    text += " ]";
    return text;
  }

  public boolean equals( DRectangle r ){
    if( r.status != status ) return false;
    if( r.x != x ) return false;
    if( r.y != y ) return false;
    if( r.width != width ) return false;
    if( r.height != height ) return false;
    return true;
  }

  public void setFillColor( Color fill_color ){
    if( fillColor == null || !fillColor.equals( fill_color ) ){
      fillColor = fill_color;
      repaint();
    }
  }

  public Color getFillColor(){
    return fillColor;
  }

  public static DRectangle getAll(){ return new DRectangle( ALL ); }
  public boolean isAll(){ return status == ALL; }
  public static DRectangle getEmpty(){ return new DRectangle( EMPTY ); }
  public boolean isEmpty(){ return status == EMPTY; }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
