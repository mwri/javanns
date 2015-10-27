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



import java.util.Vector;
import javax.swing.JLabel ;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream ;
import java.io.FileNotFoundException ;

import java.io.File;

/**
 * this class is able to read out the function definition file
 * it returns the found functions one by one to the functions object when it
 * is initiatlizing
 * it also looks if there is additional information about the function in the
 * help index file
 */
class FunctionReader{
  private InputStream reader;
  private HelpReader helpReader;
  static String separator = "|",
                function = "Function",
                comment = "Comment";
  private boolean eof = false;
  private String string = "";
  private int buffer_size = 512;
  Snns snns;

  /**
   * initializes the function reader and looks for the help index file
   */
  public FunctionReader( Snns snns ) throws Exception{
    this.snns = snns;
    ClassLoader cl = getClass().getClassLoader();
    reader = cl.getResourceAsStream(Snns.FN_DECL_FILE_NAME);
    if( reader == null ) throw
      new Exception("Couldn`t find function declaration file. Wrong release");
    try{ lookForHelp(); }
    catch( Exception ex ){}
  }

  /**
   * method returns the next function which could be read out of the
   * function definition file
   * it reads the file out until the next function tag is read
   * it sets the marker behind the end of the next function-begin-tag
   *
   * @return the next function
   */
  public Function getNextFunction() throws Exception{
    String fBegin = "<"+function+">",
           fEnd   = "<\\"+function+">",
           cBegin = "<"+comment+">";
    byte[] buffer = new byte[ buffer_size ];
    int index = string.indexOf( fBegin ), commentI = string.indexOf( cBegin );

    while( commentI != -1 && ( commentI < index || index == -1 ) ){
      comment2trash();
      index = string.indexOf( fBegin );
      commentI = string.indexOf( cBegin );
    }

    while( index == -1 && !eof ){
      int read = reader.read( buffer );
      eof = ( read == -1 );
      if( !eof ) {
        string += new String( buffer, 0, read );
        index = string.indexOf( fBegin );
        commentI = string.indexOf( cBegin );
        while( commentI != -1 && ( commentI < index || index == -1 ) ) {
          comment2trash();
          index = string.indexOf( fBegin );
          commentI = string.indexOf( cBegin );
        }
      }
    }
    if( index == -1 ) return null;
    string = string.substring( index + function.length() + 2);

    index = string.indexOf( fEnd );
    while( index == -1 && !eof ){
      int read = reader.read( buffer );
      eof = ( read == -1 );
      if( !eof ) {
        string += new String( buffer, 0, read );
        index = string.indexOf( fEnd );
      }
    }
    if( index == -1 ){
      index = Math.min( string.length(), 160 );
      String text = string.substring( 0, index );
      throw new IOException(
        "<\\"+function+"> missing in function declaration file.\nException ocurred while reading:\n"
        + text + "\n..."
      );
    }

    return readFunction();
  }

  /**
   * this method tries to start the help index reader
   */
  private void lookForHelp() throws Exception{
    InputStream indexStream;
    String helpIndexFile = snns.properties.getProperty(JavaNNSProperties.SNN_BOOK_URL_KEY);
    if( helpIndexFile == null ) return;
    helpIndexFile += Snns.HELP_INDEX_FILE_NAME;

    if( snns.applet != null ){
      URL serverURL = snns.applet.getDocumentBase();
      String helpIndex = serverURL.toString();
      String fileName = helpIndexFile;
      int index = fileName.lastIndexOf("/");
      if( index == -1 ) index = fileName.lastIndexOf("\\");
      if( index != -1 ) fileName = fileName.substring( index );
      helpIndex = helpIndex.substring( 0, helpIndex.lastIndexOf("/") ) + "/" + fileName;
      URL helpURL = new URL( helpIndex );
      indexStream = helpURL.openStream();
    }
    else {
      //ClassLoader cl = getClass().getClassLoader();
      URL url = new URL(helpIndexFile);//cl.getResource( helpIndexFile );
      indexStream = url.openStream();
    }

    int available = indexStream.available();
    byte[] buffer = new byte[ available ];
    indexStream.read( buffer );
    IndexReader indexReader = new IndexReader( buffer );
    helpReader = new HelpReader( indexReader );
  }

  /**
   * method reads out the function definition file at the current marker
   * position to get the kernel name, the more convenient displayed name and
   * parameter labels and default values
   *
   * @return the function object build from the read information
   */
  private Function readFunction() throws Exception{
    String kernelName, showName, help = null, pName, pTT;
    int type, pN, i, index;
    Parameter[] p;
    double defV;

    index = string.indexOf( separator );
    kernelName = string.substring( 0, index );
    string = string.substring( index + 1 );

    index = string.indexOf( separator );
    showName = string.substring( 0, index );
    string = string.substring( index + 1 );

    try{
      index = string.indexOf( separator );
      type = Integer.parseInt( string.substring( 0, index ) );
      string = string.substring( index + 1 );
    }
    catch( Exception e ){
      throw new Exception("Couldn't read the type of function "+showName+"("+kernelName+")");
    }

    /*index = string.indexOf( separator );
    help = string.substring( 0, index );
    string = string.substring( index + 1 );
    if( help.equals("") ) help = null;*/
    try{
      index = string.indexOf( separator );
      i = string.indexOf("<\\"+function+">");
      if( index == -1 || i < index ) index = i;
      pN = Integer.parseInt( string.substring( 0, index ) );
      string = string.substring( index + 1 );
    }
    catch( Exception e ){
      throw new Exception("Couldn't read the number of parameters of function "+showName+"("+kernelName+")");
    }

    p = new Parameter[ pN ];
    if( pN > 0 ){
      try{
        for( i=0; i<pN-1; i++ ){
          index = string.indexOf( separator );
          pName = value2greek( string.substring( 0, index ) );
          string = string.substring( index + 1 );

          index = string.indexOf( separator );
          pTT = value2greek( string.substring( 0, index ) );
          string = string.substring( index + 1 );

          index = string.indexOf( separator );
          defV = Double.valueOf( string.substring( 0, index ) ).doubleValue();
          string = string.substring( index + 1 );

          p[i] = new Parameter( pName, pTT, defV );
        }

        i = pN - 1;
        index = string.indexOf( separator );
        pName = value2greek( string.substring( 0, index ) );
        string = string.substring( index + 1 );

        index = string.indexOf( separator );
        pTT = value2greek( string.substring( 0, index ) );
        string = string.substring( index + 1 );

        index = string.indexOf("<");
        defV = Double.valueOf( string.substring( 0, index ) ).doubleValue();
        string = string.substring( index + 1 );

        p[i] = new Parameter( pName, pTT, defV );
      }
      catch( Exception e ){
        throw new Exception("Couldn't read the parameters of function "+showName+"("+kernelName+")");
      }
    }

    if( helpReader != null ) help = helpReader.getInfoTo( kernelName );
    return new Function( snns, Function.getJavaFnType( type ), kernelName, showName, p, help );
  }

  /**
   * this method checks if there is a greek letter given in the defining
   * sequenze of the function and if it finds one it tries to replace the
   * unicode hex or decimal value by the character
   *
   * @param s the string to examine
   * @return the string after the unicode values have been replaced
   */
  private String value2greek( String s ) throws Exception{
    if( s.equals("") ) return null;
    String t = "";
    int index = s.indexOf("{");
    while( index != -1 ){
      t += s.substring( 0, index );
      s = s.substring( index + 1 );
      int value, end = s.indexOf("}");
      if( end == -1 ) throw new Exception("\"}\" missing in the function declaration file");
      value = string2int( s.substring( 0, end ) );
      t += new String( new char[]{ (char)value } );
      s = s.substring( end + 1 );
      index = s.indexOf("{");
    }
    t += s;
    return t;
  }

  /**
   * method converts an string with including hex or decimal value to an integer
   *
   * @param s the string to convert
   * @return the integer value
   */
  private int string2int( String s ){
    s = s.toLowerCase();
    int index = s.indexOf("x");
    if( index == -1 ) return Integer.parseInt(s);
    else {
      s = s.substring( index + 1 );
      return strhex2int(s);
    }
  }

  /**
   * method converts an string containing a hex number to an integer number
   *
   * @param s the string to convert
   * @return the integer value
   */
  private int strhex2int( String s ){
    int pot = 1, value = 0;
    for( int i = s.length() - 1; i > -1; i-- ){
      int single = charhex2int( s.charAt( i ) );
      value += single * pot;
      pot *= 16;
    }
    return value;
  }

  /**
   * method converts a given character which represents an hex number to an
   * integer
   *
   * @param ch the char containing the value
   * @return the integer
   */
  private int charhex2int( char ch ){
    int chval = (int)ch, value;
    if( chval < 3 * 16 + 10 )
      value = chval - 3 * 16;
    else value = chval - 6 * 16 - 1 + 10;
    return value;
  }

  /**
   * method cuts the comments out
   */
  private void comment2trash() throws Exception{
    String begin = "<"+comment+">", end = "<\\"+comment+">";
    byte[] buffer = new byte[ buffer_size ];
    int index = string.indexOf( begin );
    if( index == -1 ) return;
    string = string.substring( index );
    index = string.indexOf( end );
    while( index == -1 && !eof ){
      int read = reader.read( buffer );
      eof = ( read == -1 );
      string = string.substring( string.length() - end.length() );
      if(!eof ){
        string += new String( buffer, 0, read );
        index = string.indexOf( end );
      }
    }
    if( index == -1 ){
      index = Math.min( string.length(), 160 );
      String text = string.substring( 0, index );
      throw new Exception(
        "<\\"+comment+"> missing in function declaration file.\nException occurred while reading:\n"
        + text + "\n..."
      );
    }
    string = string.substring( index + end.length() );
  }
}
