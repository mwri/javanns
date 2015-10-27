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
 * This class searches the correspondent help page for the requested index.
 *
 */
class HelpReader {
  IndexReader ir;
  String[] indizes, pages;

  public HelpReader( IndexReader ir ){
    this.ir = ir;
    boolean ok = true;
    Vector n = new Vector(), p = new Vector();
    while( ok ){
      try{
        String[] ih = getNextInfo();
        n.addElement( ih[0] );
        p.addElement( ih[1] );
      }
      catch( IOException e ){ ok = false; }
    }
    indizes = new String[ n.size() ];
    pages = new String[ p.size() ];
    for( int i=0; i<n.size(); i++ ){
      indizes[i] = (String)n.elementAt(i);
      pages[i] = (String)p.elementAt(i);
    }
  }
/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/


  /**
   * looks for information to the given key
   *
   * @param  key key to search for
   * @return the information or <code>null<\code> if nothing could be found
   */
  public String getInfoTo(String key) {
    String help = null;
    boolean found = false;
    int i=0;
    while( i<indizes.length && !found ) {
      String hh = indizes[i];
      if( hh.equals( key ) ){
        found = true;
        help = pages[i];
      }
      i++;
    }
    //if( found ) System.out.println("HelpReader.getInfoTo("+keyWord+"): "+help);
    //else System.out.println("HelpReader.getInfoTo("+keyWord+"): nichts gefunden");
    return help;
  }

  /**
   * looks for information to the given key
   *
   * @param  key  key to search for
   * @param  dflt default value if the key cannot be found
   * @return the information for the key or default value
   */
  public String getInfoTo(String key, String dflt) {
    String help = getInfoTo(key);
    if(help == null) help = dflt;
    return help;
  }


  /*--------------------------------------------------------------------------
   * private methods
   *------------------------------------------------------------------------*/

  /**
   * Looks for the next pair of key word and information string
   *
   * @return an array of Strings:
   *   the first is the key word,
   *   the second the information string
   */
  private String[] getNextInfo() throws IOException {
    String[] ih = new String[2];
    ih[0] = ir.ReadIndex();
    ih[1] = ir.ReadHelp();
    return ih;
  }
}
