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

import javax.swing.* ;
import java.awt.event.* ;
import java.awt.* ;
import javax.swing.border.* ;



/*---------------------- class declaration -----------------------------------*/
/**
 * the class CCFunctions is a container class for all fuinctions which are
 * purposed for the cascade correlation algorithm
 */
class CCFunctions{
  Function[] learn, modi, mini, actFn;

  int maxLabelWidth = -1 ;

  /**
   * the constructor takes the functions from the complete function set of the
   * snns functions
   */
  public CCFunctions( Snns snns ){
    Functions fns = snns.functions;
    learn = fns.getFunctionsOfType( Function.CC_LEARN );
    modi = fns.getFunctionsOfType( Function.CC_MODI );
    mini = fns.getFunctionsOfType( Function.CC_MINI );
    actFn = fns.getFunctionsOfType( Function.CC_ACT );
  }
  /**
   * method returns the maximal width of the strings of the labels
   * it is used to lay out the panels
   *
   * @return the maximal pixel width of the labels
   */
  public int getMaxLabelWidth(){
    if( maxLabelWidth > 0 ) return maxLabelWidth;
    int i, j, width;
    SimpleFunction[][] f = new SimpleFunction[][]{ actFn, learn, mini, modi };
    for( i=0; i<f.length; i++ ){
      for( j=0; j<f[i].length; j++ ){
        width = f[i][j].getMaxLabelWidth();
        if( width > maxLabelWidth ) maxLabelWidth = width;
      }
    }
    return maxLabelWidth;
  }
}
