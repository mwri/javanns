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



import java.io.File ;
import java.io.IOException ;
import javax.swing.JPanel ;

/**
 * simple class which has an default implementation of the method
 * of LoaderAndSaver and contains some useful variables
 */
public class LASAdapter implements LoaderAndSaver{
  protected File homeFile = null;
  protected String LASname;
  protected boolean content_changed = false;

  public String getLASName(){ return LASname; }

  public String getKeyword(){ return "" ; }

  public String getFileExtension(){ return ""; }
  public String getDescription(){ return ""; }

  public JPanel getAccessory(){ return null; }

  public void save( File file ) throws Exception{ }

  public void load( File file ) throws Exception{ }

  public boolean hasHomeFile(){
    return ( homeFile != null );
  }

  public boolean contentChanged(){ return content_changed; }

  public void save() throws IOException{
    if( hasHomeFile() )
      try{ save( homeFile ); }
      catch( Exception e ){
        if( e instanceof IOException ) throw (IOException)e;
      }
  }

  public File getFile() { return homeFile; }

}