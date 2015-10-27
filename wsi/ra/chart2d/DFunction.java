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
 *  Filename: $RCSfile: DFunction.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:45 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Point ;
import java.awt.Color ;
import java.awt.Graphics ;

/*==========================================================================*
 * ABSTRACT CLASS DECLARATION
 *==========================================================================*/

public abstract class DFunction extends DComponent
{
  public DFunction(){
    rectangle = DRectangle.getAll();
  }

  public abstract boolean isDefinedAt( double source );
  public abstract boolean isInvertibleAt( double image );
  public abstract double getSourceOf( double image ) throws IllegalArgumentException;
  public abstract double getImageOf( double source ) throws IllegalArgumentException;

  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );

    DRectangle rect = m.getDRectangle(),
               src_rect = m.getSourceOf( rect );
    Point sw = m.getPoint( rect.x, rect.y ),
          ne = m.getPoint( rect.x + rect.width, rect.y + rect.height );
    int int_w = ne.x - sw.x;
    Point last = null, next;
    for( int i = 0; i < int_w; i++ ){
      double x_val = src_rect.x + i / (double)int_w * src_rect.width ;
      if( m.x_scale != null ) x_val = m.x_scale.getImageOf( x_val );
      if( isDefinedAt( x_val ) ){
        next = m.getPoint( x_val, getImageOf( x_val ) );
        if( last != null ) g.drawLine( last.x, last.y, next.x, next.y );
        last = next;
      }
      else last = null;
    }
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
