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


/*----------------------- class declaration ----------------------------------*/
/**
 * the calss Functions collects all functions used by the snns
 * it separates the different types of functions in different arrays
 */
class Functions {
  Snns snns;
  Vector[] functions = new Vector[ Function.differentTypes ];
  int maximumLabelWidth = -1;

  /**
   * initializes the functions collector, that means
   * the functions are read out of a certain function definition file
   * which contains names of parameters and default values
   *
   * @param the snns main program
   */
  public Functions( Snns snns ) throws Exception{
    this.snns = snns;
    for( int i=0; i<Function.differentTypes; i++ )
      functions[ i ] = new Vector();
    readFunctions();
  }


/*---------------------- public methods --------------------------------------*/

  /**
   * this method returns an array of functions of a certain given type
   *
   * @param type the java function type id
   * @return the array of functions
   */
  public Function[] getFunctionsOfType( int type ){
    Function[] fns = new Function[ functions[type].size() ];
    for( int i=0; i<fns.length; i++ )
      fns[i] = (Function)functions[type].elementAt(i);
    return fns;
  }

  /**
   * this method returns the default function of the given type
   * this simply means that the first function which was read
   * out of the function definition file is returned
   *
   * @param type the java function type
   * @return the default function of the given type
   */
  public Function getDefaultFunctionOfType( int  type ){
    return (Function)functions[ type ].elementAt( 0 );
  }

  /**
   * this method looks for an certain function by the given name and the
   * given java function type id
   *
   * @param kernel_name the kernel name of the function
   * @param fn_type the java function type id of the function
   * @return the function when it could be found
   */
  public Function getFunction( String kernel_name, int fn_type ){
    int i=0;
    Function f = null, g;
    while( f == null && i<functions[fn_type].size() ){
      g = (Function)functions[fn_type].elementAt(i);
      if( g.kernel_name.equals( kernel_name ) ) f = g;
      i++;
    }
    return f;
  }

  /**
   * this method adds the given function to it function type array
   *
   * @param f the function to add
   */
  private void addFunction(Function f){
    functions[ f.type ].addElement( f );
    int width = f.getMaxLabelWidth();
    if( width > maximumLabelWidth ) maximumLabelWidth = width;
  }

  /**
   * this method starts the FunctionReader annd adds the found functions the
   * the vectors
   */
  private void readFunctions() throws Exception{
    FunctionReader reader;

    /*FileWriter fnDataWriter = new FileWriter("NewFnDecl.txt"),
               helpWriter = new FileWriter("NewHelpIndex.html");
    char lf = (char)10;*/

    Function f;
    reader = new FunctionReader( snns );
    f = reader.getNextFunction();
    while( f != null ){
      addFunction( f );

      /*String line = "<Function>"+f.kernel_name+"|"+f.show_name+"|"+f.getKernelType()+"|"+f.nP;
      Parameter p;
      for( int i=0; i<f.nP; i++ ){
        p = f.parameter[i];
        line += "|"+p.parStr+"|"+p.toolTip+"|"+p.getDefaultValue();
      }
      line += "<\\Function>"+lf;
      fnDataWriter.write( line );

      if( f.helpExists() ){
        line = "<A HREF="+f.help+">"+f.kernel_name+"</A>"+lf;
        helpWriter.write( line );
      }*/
      f = reader.getNextFunction();
    }

    //fnDataWriter.flush();
    //helpWriter.flush();
  }

}


