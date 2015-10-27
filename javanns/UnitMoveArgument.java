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

/*------------------------- class declaration --------------------------------*/
/**
 * class UnitMoveArgument collects all information belonging to the move of
 * some units : the units, the move vector of these units
 *              and the move vector of all units, if some of them would get
 *              negative coordinates
 */
class UnitMoveArgument{
  Unit[] units;
  int[] delta, delta_all;

  public UnitMoveArgument( Unit[] units, int[] delta, int[] delta_all ){
    this.units = units;
    this.delta = delta;
    this.delta_all = delta_all;
  }

  /**
   * this method returns the inverse argument
   * it is used by NetworkUndoRed
   *
   * @return the inverse UnitMoveArgument
   */
  public UnitMoveArgument inverse(){
    int[] deltaInv = new int[ delta.length ],
          delta_allInv = new int[ 3 ];
    System.arraycopy( delta, 0, deltaInv, 0, delta.length );
    System.arraycopy( delta_all, 0, delta_allInv, 0, 3);
    for( int i=0; i<delta.length; i++ ) deltaInv[ i ] *= -1;
    for( int i=0; i<delta_all.length; i++ ) delta_allInv[ i ] *= -1;
    return new UnitMoveArgument( units, deltaInv, delta_allInv );
  }
}
