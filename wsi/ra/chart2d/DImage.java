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
 *  Filename: $RCSfile: DImage.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:51:02 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Image ;
import java.awt.Point ;
import java.awt.Graphics ;
import java.awt.image.ImageObserver ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class DImage extends DRectangle
{
  Image image;
  ImageObserver observer;

  public DImage( double x, double y, double width, double height, ImageObserver observer ){
    super( x, y, width, height );
    this.observer = observer;
  }

  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    DParent parent = getDParent();
    Point p1 = m.getPoint( x, y ),
          p2 = m.getPoint( x + width, y + height );
    if( image == null ) g.drawRect( p1.x, p2.y, p2.x - p1.x, p1.y - p2.y );
    else g.drawImage( image, p1.x, p2.y, p2.x - p1.x, p1.y - p2.y, observer );
  }

  public void setImage( Image img ){
    if( img.equals( image ) ) return;
    image = img;
    repaint();
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
