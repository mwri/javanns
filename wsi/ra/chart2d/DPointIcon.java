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


package wsi.ra.chart2d;

/**
 *  Filename: $RCSfile: DPointIcon.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

import java.awt.Graphics;
/**
 * A simple interface which can be used to paint certain icons at DPoints
 * ( @see chart2d.DPoint.setIcon or chart2d.DPointSet.setIcon ).
 * Different points may be easier recognized in a complex graph.
 * The container does not guarantee that the whole icon is visible in the graph
 * because the icon does not concern the DRectangle of the DElement.
 */

public interface DPointIcon {

  /**
   * this method has to be overridden to paint the icon. The point itself lies
   * at coordinates (0, 0)
   */
  void paint( Graphics g );

  /**
   * the border which is necessary to be paint around the DPoint that the whole
   * icon is visible
   *
   * @return the border
   */
  DBorder getDBorder();
}

