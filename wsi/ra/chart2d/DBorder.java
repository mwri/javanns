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
 *  Filename: $RCSfile: DBorder.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:13 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */
package wsi.ra.chart2d;

import java.awt.Insets;

public class DBorder extends Insets{

  public DBorder(){
    this( 0, 0, 0, 0 );
  }

  public DBorder(int top, int left, int bottom, int right ){
    super( top, left, bottom, right );
  }

  public boolean insert( DBorder b ){
    boolean changed = false;
    if( b.top    > top    ){ top    = b.top;    changed = true; }
    if( b.bottom > bottom ){ bottom = b.bottom; changed = true; }
    if( b.left   > left   ){ left   = b.left;   changed = true; }
    if( b.right  > right  ){ right  = b.right;  changed = true; }
    return changed;
  }

  public boolean equals( Object o ){
    if( o instanceof DBorder )
      return super.equals( o );
    return false;
  }

  public String toString(){
    return "wsi.ra.chart2d.DBorder[top="+top+",left="+left
           +",bottom="+bottom+",right="+right+"]";
  }
}