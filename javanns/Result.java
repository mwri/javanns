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



import javax.swing.* ;
import java.awt.Dimension ;
import java.awt.Point ;
import java.io.File ;
import java.io.IOException ;

public class Result extends LASAdapter{
  Network network;
  ResultAccessory accessory;

  public Result( Network network ) {
    LASname = "Result";
    this.network = network;
    accessory = new ResultAccessory();
  }

  public String getKeyword(){ return "braucht´s nich" ; }

  public String getFileEnding(){ return "res"; }

  public String getDescription(){ return "Result files *.res"; }

  public JPanel getAccessory(){
    accessory.setLastPatternCount( network.getNumberOfPatterns() );
    return accessory;
  }

  public void save( File file ) throws IOException{
    //System.out.println("Result.save: "+file);
    network.ki.saveResult(file.getCanonicalPath(),
                   accessory.startPattern(),
                   accessory.endPattern(),
                   accessory.inclInputPatterns(),
                   accessory.inclOutputPatterns(),
                   accessory.getFileMode()
    );
  }

  public void load( File file ) throws IOException{
    throw new IOException("Cannot read result files");
  }

}
