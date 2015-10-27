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
 *  Filename: $RCSfile: DLine.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:51:27 $
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

public class DLine extends DComponent
{
  DPoint start, end;

  public DLine( double x1, double y1, double x2, double y2 ){
    this( new DPoint( x1, y1 ), new DPoint( x2, y2 ) );
  }

  public DLine( DPoint start, DPoint end ){
    this.start = start;
    this.end = end;
  }
  public DLine( double x1, double y1, double x2, double y2, Color color ){
    this( new DPoint( x1, y1 ), new DPoint( x2, y2 ), color );
  }

  public DLine( DPoint start, DPoint end, Color color ){
    this.start = start;
    this.end = end;
    this.color = color;
  }

  public DRectangle getRectangle(){
    double x = start.x, y = start.y, width = end.x - x, height = end.y - y;
    if( width < 0 ) { x += width; width *= -1; }
    if( height < 0 ) { y += height; height *= -1; }
    return new DRectangle( x, y, width, height );
  }

  public void paint( DMeasures m ){
    //System.out.println("DLine.paint(Measures): "+this);
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );
    Point p1 = m.getPoint( start ),
          p2 = m.getPoint( end ) ;
    g.drawLine( p1.x, p1.y, p2.x, p2.y );
  }

  public String toString(){
    return "DLine[("+start.x+","+start.y+") --> ("+end.x+","+end.y+", color: "+color+"]";
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
