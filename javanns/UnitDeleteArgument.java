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


/*------------------ class declaration ---------------------------------------*/
/**
 * Class UnitDeleteArgument collects all information about deleting several
 * units and their links to others. It helps to recreate the units after deleting.
 */
class UnitDeleteArgument{
  LinkData[] lData;
  UnitData[] uData;

  /**
   * constructor with the link and unit data needed to reconstruct the part
   * of the net
   */
  public UnitDeleteArgument( LinkData[] lData, UnitData[] uData ){
    this.lData = lData;
    this.uData = uData;
  }
}
