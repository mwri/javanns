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



/*---------------------------- imports ----------------------------------*/

import java.net.* ;
import java.io.* ;

/*--------------------- class declaration ------------------------------------*/
/**
 * UnitTTypes contains the kernel constants of topological unit types
 */
class UnitTTypes{
  public static final int UNKNOWN     = 0,
                          INPUT       = 1,
                          OUTPUT      = 2,
                          HIDDEN      = 3,
                          DUAL        = 4,
                          SPECIAL     = 5,
                          SPECIAL_I   = 6,
                          SPECIAL_O   = 7,
                          SPECIAL_H   = 8,
                          SPECIAL_D   = 9,
                          SPECIAL_X   = 10,
                          N_SPECIAL_X = 11;

  public static NamedObject[] getTypes() {
    NamedObject[] value = new NamedObject[12];
    value[ 0] = new UnitTType("Unknown", 0);
    value[ 1] = new UnitTType("Input", 1);
    value[ 2] = new UnitTType("Output", 2);
    value[ 3] = new UnitTType("Hidden", 3);
    value[ 4] = new UnitTType("Dual", 4);
    value[ 5] = new UnitTType("Special", 5);
    value[ 6] = new UnitTType("Special input", 6);
    value[ 7] = new UnitTType("Special output", 7);
    value[ 8] = new UnitTType("Special hidden", 8);
    value[ 9] = new UnitTType("Special dual", 9);
    value[10] = new UnitTType("Special X", 10);
    value[11] = new UnitTType("N Special X", 11);
    return value;
  }
}
