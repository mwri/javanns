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
 * Parses the part of the configuration file that is defined as
 * d3_light_type.
 *
 */
class D3LightType {

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/

  public int shade_mode;
  public vector3D position;
  public double Ia;
  public double Ka;
  public double Ip;
  public double Kd;


/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Parses the lines in the configuration file that content the fields
   * of the d3_light_type.
   *
   * @param configBuffer configuration file contents
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer) throws IOException {
    position = new vector3D();
    position.readConfiguration(configBuffer);

    Ia = configBuffer.readDouble();
    Ka = configBuffer.readDouble();
    Ip = configBuffer.readDouble();
    Kd = configBuffer.readDouble();
  }
}
