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


/**
 *  Filename: $RCSfile: TextPrinter.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:53:10 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.print;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.print.Printable ;
import java.awt.print.PageFormat ;
import java.awt.Graphics ;
import java.awt.FontMetrics ;
import java.util.Vector;
import java.io.LineNumberReader;
import java.io.StringReader ;
import java.io.IOException ;
import java.util.Date;
import java.text.DateFormat;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * class TextPrinter prints Strings by implementing Printable interface
 * it prints the lines and separates the pages
 */
public class TextPrinter implements Printable
{
  private String text;
  private String[][] pages;
  private boolean finished = true, wrap = false;
  public static final String PAGE_NO = "{page_number}",
                             DATE = "{date}",
                             TIME = "{time}";
  private String first_title, title, footline, date, time;


  /**
   * method enables TextPrinter to force line wrapping
   *
   * @param b <code>true</code> if line wrapping should be forced,
   *          <code>false</code> if not
   */
  public void setLineWrap( boolean b ){
    wrap = b;
  }

  /**
   * method returns <code>true</code> if the TextPrinter is enabled to force
   * line wrapping
   *
   * @return <code>true</code> if line wrapping is forced
   */
  public boolean getLineWrap(){ return wrap; }

  /**
   * method sets the text which should be printed
   *
   * @param text the text to print
   */
  public void setText( String text ){
    this.text = text;
    pages = null;
    finished = false;
  }

  public void setFirstTitle( String first ){ first_title = first; }

  public void setTitle( String title ){ this.title = title; }

  public void setFootLine( String footline ){ this.footline = footline; }

  /**
   * implementation of <code>Printable</code> interface
   * method prints a specified page
   * when there was a new text set since the last page has been printed
   * the pages will be new created
   *
   * @param g the <code>Graphics</code> context
   * @param pf the <code>PageFormat</code> of the printer
   * @param pi the page index
   * @return PAGE_EXISTS or NO_SUCH_PAGE whether the page exists or not
   */
  public int print( Graphics g, PageFormat pf, int pi ){
    System.out.println("print");
    if( date == null ) setDateTime();
    if( pages == null ) setPages( g, pf );
    if( pi >= pages.length ) return NO_SUCH_PAGE;
    System.out.println("drucke Seite "+(pi+1)+" von "+pages.length);
    FontMetrics fm = g.getFontMetrics();
    int lh = fm.getHeight(),
        x = (int)pf.getImageableX(),
        y = (int)pf.getImageableY() + lh;
    for( int l=0; l<pages[pi].length && pages[pi][l]!=null; l++ ){
      String line = pages[pi][l];
      if( line != null ) {
        System.out.println(line);
        g.drawString( line, x, y );
      }
      y += lh;
    }
    finished = pi == pages.length - 1;
    return PAGE_EXISTS;
  }

  /**
   * ifFinished returns <code>true</code> when the pages have been completely
   * printed
   * this method can be used by the calling object to check if a new text should
   * be set to the TextPrinter
   *
   * @return <code>true</code> if the last page has been printed
   *         or <code>false</code> if not
   */
  public boolean isFinished(){ return finished; }

  /**
   * method calculates the pages, that means it tries to layout the text
   * on pages of the specified <code>PageFormat</code> in the specified
   * <code>Graphics</code> context
   */
  private void setPages( Graphics g, PageFormat pf ){
    System.out.println("setPages");
    FontMetrics fm = g.getFontMetrics();
    String[] lines = setLines( g, pf );
    int lpp = (int)pf.getImageableHeight() / fm.getHeight(), p ;

    if( title != null ) lpp--;
    else if( first_title != null ){
      String[] buf = new String[ lines.length +1 ];
      System.arraycopy( lines, 0, buf, 1, lines.length );
      buf[0] = getFirstTitle();
      lines = buf;
    }
    if( footline != null ) lpp--;

    p = lines.length / lpp;
    if( lines.length%lpp != 0 ) p++;
    pages = new String[p][lpp];
    for( int i=0; i<p; i++ ){
      int j = 0;
      if( title != null ){
        if( i == 0 && first_title != null ) pages[0][0] = getFirstTitle();
        else pages[i][0] = getTitle( i+1 );
        j = 1;
      }
      while( j<lpp && i*lpp+j<lines.length  ){
        pages[i][j] = lines[i*lpp+j];
        j++;
      }
      if( footline != null ) pages[i][lpp-1] = getFootline( i+1 );
    }
  }

  /**
   * method separates the text into distinct lines
   *
   * @param g the <code>Graphics</code> context of the printer
   * @param pf the <cod>PageFormat</code> of the paper
   */
  private String[] setLines( Graphics g, PageFormat pf ){
    System.out.println("setLines");
    Vector lines = new Vector();
    int lw = (int)pf.getImageableWidth();
    FontMetrics fm = g.getFontMetrics();
    LineNumberReader lnr = new LineNumberReader( new StringReader( text ) );
    String line;
    try{
      for( line = lnr.readLine(); line != null; line = lnr.readLine() ){
        if( wrap && tooLong( line, fm, lw ) ){
          String[] wl = wrapInLines( line, fm, lw );
          for( int i=0; i<wl.length; i++ ) lines.add( wl[i] );
        }
        else lines.add( line );
      }
    }
    catch( IOException ex ){}
    String[] l = new String[ lines.size() ];
    lines.toArray( l );
    return l;
  }

  /**
   * method checks if the specified line is too long for the paper
   *
   * @param line the text line
   * @param fm the <code>FontMetrics</code> of the printer graphics
   * @param line_width the imageable width of the paper
   * @return <code>true</code> if the line is too long, <code>false</code> if not
   */
  private boolean tooLong( String line, FontMetrics fm, int line_width ){
    return fm.stringWidth( line ) > line_width;
  }

  /**
   * method wraps the given String into several lines which are not longer
   * than <code>line_width</code>
   *
   * @param line the text line
   * @param fm the <code>FontMetrics</code> of the printer graphics
   * @param line_width the imageable width of the paper
   * @return the wrapped lines as an array of Strings
   */
  private String[] wrapInLines( String line, FontMetrics fm, int line_width ){
    Vector lines = new Vector();
    String[] wl = wrapLine( line, fm, line_width );
    lines.add( wl[0] );
    while( tooLong( wl[1], fm, line_width ) ){
      wl = wrapLine( wl[1], fm, line_width );
      lines.add( wl[0] );
    }
    lines.add( wl[1] );
    String[] l = new String[ lines.size() ];
    lines.toArray(l);
    return l;
  }

  private String[] wrapLine( String line, FontMetrics fm, int line_width ){
    int index = line.indexOf(" ");
    if( index == -1 || fm.stringWidth( line.substring(0, index) ) > line_width )
      return forceWrap( line, fm, line_width );
    int last_index = index;
    index = line.indexOf(" ",index+1);
    while( index != -1 && fm.stringWidth(line.substring(0, index))<line_width){
      last_index = index;
      index = line.indexOf(" ",index+1);
    }
    return new String[]{ line.substring( 0, last_index ), line.substring( last_index + 2 ) };
  }

  private String[] forceWrap( String line, FontMetrics fm, int line_width ){
    String[] wl = new String[2];
    int delta = 32,
        length = delta,
        max = line.length();
    if( length > max ) length = max;
    wl[0] = line.substring(0, length);
    while( fm.stringWidth( wl[0] ) < line_width && length != max ){
      length += delta;
      if( length > max ) length = max;
      wl[0] = line.substring(0, length);
    }
    int sign = -1;
    do{
      delta /= 2;
      length += sign * delta;
      wl[0] = line.substring(0, length);
      sign = ( fm.stringWidth( wl[0] ) < line_width )? 1 : -1;
    }while( delta > 0 );
    if( sign == -1 ) length--;
    wl[1] = line.substring(length);
    return wl;
  }

  private String getFirstTitle(){ return first_title; }
  private String getTitle( int no ){ return checkPageNo(title, no); }
  private String getFootline( int no ){ return checkPageNo(footline, no); }
  private void setDateTime(){
    System.out.println("setDateTime");
    Date date_o = new Date();
    date = DateFormat.getDateInstance().format(date_o);
    time = DateFormat.getTimeInstance().format(date_o);
    System.out.println(date+" und "+time);
    first_title = checkDateTime( first_title );
    title = checkDateTime( title );
    footline = checkDateTime( footline );
  }
  private String checkDateTime(String text){
    System.out.println("checkDateTime");
    int i = text.indexOf(DATE);
    while( i > -1 ){
      String nt = text.substring(0, i);
      nt += date;
      nt += text.substring(i+DATE.length());
      text = nt;
      i = text.indexOf(DATE);
    }
    i = text.indexOf(TIME);
    while( i > -1 ){
      String nt = text.substring(0, i);
      nt += time;
      nt += text.substring(i+TIME.length());
      text = nt;
      i = text.indexOf(TIME);
    }
    System.out.println( text );
    return text;
  }
  private String checkPageNo( String text, int no ){
    int i = text.indexOf(PAGE_NO);
    while( i > -1 ){
      String nt = text.substring(0, i);
      nt += no;
      nt += text.substring(i+PAGE_NO.length());
      text = nt;
      i = text.indexOf(PAGE_NO);
    }
    return text;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
