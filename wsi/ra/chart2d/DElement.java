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
 *  Filename: $RCSfile: DElement.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:37 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Color ;

/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/

/**
 * some useful methods for objects which should be paintable in a scaled area
 */
public interface DElement
{
  Color DEFAULT_COLOR = Color.black;
  DRectangle getRectangle();

  void setDParent( DParent parent );
  DParent getDParent();

  void paint( DMeasures m );
  void repaint();

  void setVisible( boolean aFlag );
  boolean isVisible();

  void setColor( Color color );
  Color getColor();

  void setDBorder( DBorder b );
  DBorder getDBorder();
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
