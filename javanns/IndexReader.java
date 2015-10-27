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

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.util.Vector;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * This class reads the separate index and help substrings of an index file.
 *
 */
class IndexReader extends LineReader {
  String line;

/*-------------------------------------------------------------------------*
 * constructor
 *-------------------------------------------------------------------------*/
  /**
   *  reads the separate index and help substrings of an index file.
   *
   * @param buffer index
   */
  public IndexReader(byte[] buffer) {
    super(buffer);
    mark(0);
  }


/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Reads index substrings in the index file
   *
   * @throws IOException if can't read the file
   * @return index string content
   */
  public String ReadIndex() throws IOException {
    line = readNextLine();
    int begin = line.lastIndexOf ('"');
    int end   = line.lastIndexOf ('<');
    if(begin >= 0 && end >= 0) return line.substring ( begin + 2, end );
    throw(new IOException("IndexReader.ReadIndex(): Error in help index file."));
  }


  /**
   * Reads help substrings in the index file
   *
   * @param line line content
   * @return help string content
   */
  public String ReadHelp() throws IOException {
    int begin = line.lastIndexOf ('=');
    int end   = line.lastIndexOf ('"');
    if(begin >= 0 && end >= 0) return line.substring(begin + 2, end);
    throw(new IOException("IndexReader.ReadHelp(): Error in help index file."));
  }
}
