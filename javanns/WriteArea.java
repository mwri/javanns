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

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.io.* ;
import java.util.Date ;

// drucken:
import wsi.ra.print.TextPrinter ;
import java.awt.print.* ;
import java.util.Vector ;


/**
 * class WriteArea implements OutputStream in a JTextArea
 * so it can be used for example as System.out or System.err
 */
class WriteArea extends OutputStream{
  JTextArea tArea;
  boolean content_changed = false;

  public WriteArea( int rows, int columns, boolean line_wrap ){
    tArea = new JTextArea( rows, columns );
    tArea.setLineWrap( line_wrap );
  }

  public void write(int b) throws IOException{
    char ch = (char)b;
    tArea.append( String.valueOf(ch) );
    content_changed = true;
  }
}
