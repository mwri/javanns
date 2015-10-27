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
 * d3_state_type.
 *
 */
class D3StateType {

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/

  public vector3D trans_vec, rot_vec, scale_vec;
  public vector3D trans_step, rot_step, scale_step;
  public vector3D viewpoint;
  public double unit_aspect;
  public double link_scale;
  public double pos_link_trigger;
  public double neg_link_trigger;
  public int font;
  public int projection_mode;
  public int model_mode;
  public int color_mode;
  public int link_mode;
  public D3UnitModeType unit_mode;
  public D3LightType light;

/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Parses the lines in the configuration files that content the fields
   * of the d3_state_type.
   *
   * @param configBuffer configuration file contents
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer) throws IOException {
    rot_vec = new vector3D();
    rot_vec.readConfiguration(configBuffer);
    rot_vec.rad();

    trans_vec = new vector3D();
    trans_vec.readConfiguration(configBuffer);
    scale_vec = new vector3D();
    scale_vec.readConfiguration(configBuffer);

    rot_step = new vector3D();
    rot_step.readConfiguration(configBuffer);
    rot_step.rad();

    trans_step = new vector3D();
    trans_step.readConfiguration(configBuffer);
    scale_step = new vector3D();
    scale_step.readConfiguration(configBuffer);

    projection_mode = configBuffer.readInteger();

    viewpoint = new vector3D();
    viewpoint.readConfiguration(configBuffer);

    font = configBuffer.readInteger();

    model_mode = configBuffer.readInteger();
    link_mode = configBuffer.readInteger();
    link_scale = configBuffer.readDouble();
    unit_aspect = configBuffer.readDouble();

    unit_mode = new D3UnitModeType();
    unit_mode.readConfiguration(configBuffer);

    light = new D3LightType();
    light.readConfiguration(configBuffer);
  }
}
