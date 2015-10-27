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
 *  Filename: $RCSfile: DPoint.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:52:08 $
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

public class DPoint extends DComponent
{
  public double x, y;
  public String label;
  protected DPointIcon icon = null;
  public DPoint( ){
  }
  public void initpoint( double x, double y ){
    this.x = x;
    this.y = y;
    rectangle = new DRectangle( x, y, 0, 0 );
  }
  public DPoint( double x, double y ){
    this.x = x;
    this.y = y;
    rectangle = new DRectangle( x, y, 0, 0 );
  }

  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );
    Point dp = m.getPoint( this );
    if( label != null ){
      FontMetrics fm = g.getFontMetrics();
      g.drawString( label,
                    dp.x - fm.stringWidth( label ) / 2,
                    dp.y + fm.getAscent()
      );
    }
    if( icon == null )
      g.drawRect( dp.x, dp.y, 1, 1 );
    else{
      g.translate( dp.x, dp.y );
      icon.paint( g );
      g.translate( -dp.x, -dp.y );
    }
  }

  /**
   * method sets an icon for a better displaying of the point
   *
   * @param icon the DPointIcon
   */
  public void setIcon( DPointIcon icon ){
    this.icon = icon;
    if( icon == null ) setDBorder(new DBorder(1,1,1,1));
    else setDBorder( icon.getDBorder() );
  }

  /**
   * method returns the current icon of the point
   *
   * @return the DPointIcon
   */
  public DPointIcon getIcon(){
    return icon;
  }

  public Object clone(){
    DPoint copy = new DPoint( x, y );
    copy.color = color;
    return copy;
  }

  public String toString(){
    String text = "DPoint[";
    if( label != null ) text += label+", ";
    text += "x: "+x+", y: "+y+", color: "+color+"]";
    return text;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
