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



/*-------------------------- imports -----------------------------------------*/
import java.io.File ;
import java.io.IOException ;
import java.util.Vector;


/*------------------------------- class declaration --------------------------*/
/**
 * Pattern objects are used to store patterns of the network if they were
 * deleted. So, by the undo-redo function of the network they can be restored.
 */
class Pattern{
  double[] input, output;

  /**
   * creates a copy of the current activation values of the network
   *
   * @param network the net to get the values from
   */
  public Pattern( Network network ){
    int i;
    Unit[] units = network.getInputUnits();
    input = new double[ units.length ];
    for( i=0; i<units.length; i++ )
      input[ i ] = units[ i ].getActivation();
    units = network.getOutputUnits();
    output = new double[ units.length ];
    for( i=0; i<units.length; i++ )
      output[ i ] = units[ i ].getActivation();
  }
}
