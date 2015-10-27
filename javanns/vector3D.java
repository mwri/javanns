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


/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.math.*;
import java.awt.*;
import java.io.*;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * Parses 3D vectors from the configuration file.
 *
 */
class vector3D {

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/
  public double value[];


/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/
  /**
   * calculation of rad values
   *
   * @param  phi input double value
   * @return rad value
   */
  public double rad(double phi) { return (phi / 180.0) * Math.PI; }

  /**
   * calculates the rad value from a grad value
   *
   */
  public void rad() {
    value[0] = rad( value [0] );
    value[1] = rad( value [1] );
    value[2] = rad( value [2] );
  }

  /**
   * calculation of deg values
   *
   * @param  phi input double value
   * @return deg value
   */
  public double deg(double phi) { return (180.0 * phi) / Math.PI; }

  /**
   * calculates the deg value from a grad value
   *
   */
  public void deg() {
    value[0] = deg( value[0] );
    value[1] = deg( value[1] );
    value[2] = deg( value[2] );
  }

  /**
   * Parses a 3D vector from the configuration file.
   *
   * @param configBuffer configuration file contents
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer) throws IOException {
    value = new double[4];
    configBuffer.readVector3D(value);
  }
}
