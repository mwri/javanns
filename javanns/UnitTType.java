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


/*------------------ class declaration ---------------------------------------*/
class UnitTType implements NamedObject{
  private int number;
  private String name;

  public UnitTType(String name, int number){
    this.name = name;
    this.number = number;
  }

  public int getNumber(){ return number; }

  public String getName(){ return name; }

  /**
   * Checks if this object equals another one.
   *
   * @return <code>true</code> if objects are equal;
   */
  public boolean equals(NamedObject o) {
    return super.equals(o);
  }
}
