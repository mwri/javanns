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


/*---------------------- class declaration -----------------------------------*/

/**
 * class SimpleFunction represents functions without additional information
 * that means only the kernel name and the displayed name are stored
 */
class SimpleFunction implements NamedObject{
  String kernel_name, show_name;
  Parameter[] parameter;


  public SimpleFunction( String kernel_name, String show_name){
    this( kernel_name, show_name, 0 );
  }

  public SimpleFunction( String kernel_name, String show_name, int param_no ){
    this.kernel_name = kernel_name;
    this.show_name = show_name;
    parameter = new Parameter[ param_no ];
  }

  public String getName(){ return show_name; }

  public String getKernelName() { return kernel_name; }

  public boolean equals( NamedObject o ){
    if( !( o instanceof SimpleFunction ) ) return false;
    SimpleFunction f = (SimpleFunction)o;
    if( !f.getName().equals( show_name ) ) return false;
    if( !f.kernel_name.equals( kernel_name ) ) return false;
    return true;
  }

  public int getMaxLabelWidth(){
    JLabel l = new JLabel();
    int width, max = 0;
    for( int i=0; i<parameter.length; i++ ){
      l.setText( parameter[i].parStr );
      width = l.getPreferredSize().width;
      if( width > max ) max = width;
    }
    return max;
  }

  public String toString(){
    return "SimpleFunction["+kernel_name+", "+show_name+", "+parameter.length+" param]";
  }
}
