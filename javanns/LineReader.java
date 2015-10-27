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
 * Reads the SNNS configuration file, line for line.
 *
 */
class LineReader extends ByteArrayInputStream {

  /*-------------------------------------------------------------------------*
   * constructor
   *-------------------------------------------------------------------------*/

  /**
   * Class constructor.
   *
   * @param buffer line content
   */
  public LineReader(byte[] buffer) {
    super(buffer);
    mark(0);
  }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/
  /**
   * Reads an empty line in the configuration file.
   *
   * @return the line read
   * @throws IOException if can't read the file
   */
  public String readNextLine() throws IOException {
    int length         = 0;
    int start_position = super.pos;
    boolean lf         = false;
    int position;

    for(position = start_position; position < super.count && !lf; position++) {
      int character = read();
      lf = (character == 10);
    }
    if(!lf) throw(new IOException("Error in configuration file."));
    mark(position + 1);
    length = position - start_position - 1;
    return new String(buf, start_position, length);
  }


  /**
   * Reads a 3D vector from the configuration file.
   *
   * @param real place to store the read vector
   * @throws IOException if can't read the file
   */
  public void readVector3D(double real[]) throws IOException {
    String line = readNextLine();
    int position = line.lastIndexOf (':');
    if(position == - 1) throw(new IOException("Error in configuration file."));
    ++position;
    for(int index = 0; index < 3; index++ ) {
      String number = line.substring (position, position + 10);
      position += 10;
      try { real[index] = Double.valueOf(number.trim()).doubleValue(); }
      catch(NumberFormatException exception) {
        throw(new IOException("Error in configuration file."));
      }
    }
  }

  /**
   * Reads a double from the configuration file.
   *
   * @return the read number
   * @throws IOException if can't read the file
   */
  public double readDouble() throws IOException {
    double x;
    String line = readNextLine();
    int position = line.lastIndexOf(':');
    if(position == - 1) throw(new IOException("Error in configuration file."));
    String number = line.substring ( position + 1 );
    try { x = Double.valueOf(number.trim()).doubleValue(); }
    catch( NumberFormatException exception ) {
      throw(new IOException("Error in configuration file."));
    }
    return x;
  }

  /**
   * Reads an integer from the configuration file.
   *
   * @return the read number
   * @throws IOException if can't read the file
   */
  public int readInteger() throws IOException {
    int i;
    String line = readNextLine();
    int position = line.lastIndexOf (':');
    if(position == - 1) throw(new IOException("Error in configuration file."));
    String number = line.substring ( position + 1 );
    try { i = Integer.valueOf(number.trim ()).intValue(); }
    catch( NumberFormatException exception ) {
      throw(new IOException("Error in configuration file."));
    }
    return (i);
  }

  /**
   * Reads a boolean from the configuration file.
   *
   * @return the read value
   * @throws IOException if can't read the file
   */
  public boolean readBoolean() throws IOException {
    String line = readNextLine();
    int position = line.lastIndexOf (':');
    if( position == - 1 ) throw(new IOException("Error in configuration file."));
    String number = line.substring ( position + 1 );
    try {
      if(Integer.valueOf(number.trim()).intValue() == 0) {
        return false;
      }
      else return true;
    }
    catch(NumberFormatException exception) {
      throw(new IOException("Error in configuration file."));
    }
  }

  /**
   * Reads a text parameter from the configuration file.
   *
   * @return the read text
   * @throws IOException if can't read the file
   */
  public String readTextParameter() throws IOException {
    String line = readNextLine();
    int position = line.lastIndexOf (':');
    if( position == - 1 ) throw(new IOException("Error in configuration file."));
    String buffer = line.substring ( position + 1 );
    return buffer.trim();
  }

  /**
   * Reads a text line from the configuration file
   *
   * @return the line read
   * @throws IOException if can't read the file
   */
  public String readText()  throws IOException {
    String line = readNextLine();
    int position = line.lastIndexOf ('#');
    if( position == - 1 ) throw(new IOException("Error in configuration file."));
    return line.substring ( 0, position );
  }
}
