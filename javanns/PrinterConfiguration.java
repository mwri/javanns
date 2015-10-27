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
 * Parses the printer_Configuration of a configuration file.
 *
 */
class PrinterConfiguration {

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/

  public int dest;
  public int format;
  public int paper;
  public int autoScale;
  public int clip;
  public int color;
  public int orient;
  public int autoUni;
  public int size;
  public int shape;
  public int printText;
  public int border;
  public int resolution;
  public int displayToPrint;
  public double borderHoriz;
  public double borderVert;
  public double scaleValX;
  public double scaleValY;
  public double unitGray;
  public String fileNameStr;
  public String cmdLineStr;

/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Parses the lines in the configuration files that make up the
   * printer configuration.
   *
   * @param configBuffer configuration file contents
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer) throws IOException {
    dest = configBuffer.readInteger();
    format = configBuffer.readInteger();
    paper = configBuffer.readInteger();
    autoScale = configBuffer.readInteger();
    clip = configBuffer.readInteger();
    color  = configBuffer.readInteger();
    orient = configBuffer.readInteger();
    autoUni = configBuffer.readInteger();
    size = configBuffer.readInteger();
    shape = configBuffer.readInteger();
    printText = configBuffer.readInteger();
    border = configBuffer.readInteger();
    resolution = configBuffer.readInteger();
    displayToPrint = configBuffer.readInteger();
    borderHoriz = configBuffer.readDouble();
    borderVert = configBuffer.readDouble();
    scaleValX = configBuffer.readDouble();
    scaleValY = configBuffer.readDouble();
    unitGray = configBuffer.readDouble();
    fileNameStr = configBuffer.readTextParameter();
    cmdLineStr = configBuffer.readTextParameter();
  }
}
