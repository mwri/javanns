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
 *  Filename: $RCSfile: DGrid.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:54 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Color ;
import java.awt.Graphics ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * this class paints a grid with certain line distances on a DParent
 */
public class DGrid extends DComponent
{
  /**
   * the distances between the lines
   */
  double hor_dist, ver_dist;

  private Color DEFAULT_COLOR = Color.lightGray;

  /**
   * constructor with the size and position of the grid and the line distances
   *
   * @param rectangle the rectangle around the grid
   * @param hor_dist the horizontal distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param ver_dist vertical distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   */
  public DGrid( DRectangle rectangle, double hor_dist, double ver_dist ){
    this.rectangle = rectangle;
    this.hor_dist = hor_dist;
    this.ver_dist = ver_dist;
    color = DEFAULT_COLOR;
  }

  /**
   * constructor with the size and position of the grid and the line distances
   *
   * @param rectangle the rectangle around the grid
   * @param hor_dist the horizontal distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param ver_dist the vertical distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param color the color of the grid
   *        ( can also be set by setColor( java.awt.Color ) )
   */
  public DGrid( DRectangle rectangle, double hor_dist, double ver_dist, Color color ){
    this.rectangle = rectangle;
    this.hor_dist = hor_dist;
    this.ver_dist = ver_dist;
    this.color = color;
  }

  /**
   * paints the grid...
   *
   * @param m the <code>DMeasures</code> object to paint the grid
   */
  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );
    double minX, minY, pos;
    DPoint p1, p2;
    DLine l;

    minX = (int)( rectangle.x / hor_dist );
    if( minX * hor_dist <= rectangle.x ) minX++;
    minX *= hor_dist;
    minY = (int)( rectangle.y / ver_dist );
    if( minY * ver_dist <= rectangle.y ) minY++;
    minY *= ver_dist;

    p1 = new DPoint( 0, rectangle.y );
    p2 = new DPoint( 0, rectangle.y + rectangle.height );
    for( pos = minX; pos<=rectangle.x + rectangle.width; pos += hor_dist ){
      p1.x = p2.x = pos;
      l = new DLine( p1, p2, color );
      l.paint( m );
    }

    p1.x = rectangle.x;
    p2.x = p1.x + rectangle.width;
    for( pos = minY; pos<=rectangle.y + rectangle.height; pos += ver_dist ){
      p1.y = p2.y = pos;
      l = new DLine( p1, p2, color );
      l.paint( m );
    }
  }

  public String toString(){
    return "chart2d.DGrid[ hor: "+hor_dist+", ver: "+ver_dist+" ]";
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
