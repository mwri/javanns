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

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * class Function represents functions with their parameters, default values
 * and information about getting more infos to it
 *
 */
class Function extends SimpleFunction{
  /*-------------------------------------------------------------------------*
   * public member variables
   *-------------------------------------------------------------------------*/

  /*----------- function types ----------------------------------------------*/

  /**
   * ID for init fns
   */
  public static final int INIT = 0;

  /**
   * ID for update fns
   */
  public static final int UPDATE = 1;

  /**
   * ID for learn fns
   */
  public static final int LEARN = 2;

  /**
   * ID for pruning fns
   */
  public static final int PRUNING = 3;

  /**
   * ID for remapping fns
   */
  public static final int REMAPPING = 4;

  /**
   * ID for unit activation fns
   */
  public static final int ACTIVATION_FN = 5;

  /**
   * ID for unit output fns
   */
  public static final int OUTPUT_FN = 6;

  /**
   * ID for pruning learn functions
   */
  public static final int PR_LEARN = 7;

  /**
   * ID for cascade correlation learn functions
   */
  public static final int CC_LEARN = 8;

  /**
   * ID for cascade correlation minimalizing functions
   */
  public static final int CC_MINI = 9;

  /**
   * ID for cascade correlation modifikation functions
   */
  public static final int CC_MODI = 10;

  /**
   * ID for cascade activation functions
   */
  public static final int CC_ACT = 11;

  /**
   * the number of existing different function types
   */
  public static final int differentTypes = 12;


  /*--------------- other variables -----------------------------------------*/

  /**
   * function type ID
   */
  public int type;

  /**
   * numer of parameters the function uses
   */
  public int nP;

  public String help = "";
  /*--------------- private variables ---------------------------------------*/

  private Snns snns;
  private boolean helpExists;

  /*--------------------------------------------------------------------------*
   * constructors
   *-------------------------------------------------------------------------*/

  /**
   * class constructor without presentation name, parameters and help page
   *
   * @param s snns class
   * @param id function ID number
   * @param kn name of the initializing function, as used by SNNS kernel
   */
  public Function(Snns s, int t, String kn){
    this(s, t, kn, null);
  }

  /**
   * class constructor without presentation name and parameters
   *
   * @param s snns class
   * @param id function ID number
   * @param kn name of the initializing function, as used by SNNS kernel
   * @param help the help page in the SNNSbook
   */
  public Function(Snns s, int t, String kn, String help){
    this(s, t, kn, kn, new Parameter[0], help);
  }

  /**
   * class constructor with number of function parameters
   *
   * @param s snns class
   * @param id function ID number
   * @param kn name of the initializing function, as used by SNNS kernel
   * @param sn user-friendly name of the initializing function
   * @param p array of parameters
   * @param help the help page in the SNNSbook
   */
  public Function(Snns s, int t, String kn, String sn, Parameter[] p, String help){
    super( kn, sn, p.length );
    parameter = p;
    snns = s;
    type = t;
    nP = p.length;
    this.help = help;
    helpExists = ( help != null );
  }

  /*----------------------------------------------------------------------*
   * public methods
   *----------------------------------------------------------------------*/

  /**
   * shows help to the function document
   */
  public void showHelp() throws Exception{
    if( helpExists() ){
      if( snns.applet != null){
        URL helpURL = new URL( snns.properties.getProperty(snns.properties.SNN_BOOK_URL_KEY) + help );
        snns.applet.getAppletContext( ).showDocument(helpURL, "Help");
      }
      else {
        String[] command =  new String[]{
          snns.properties.getProperty(snns.properties.BROWSER_NAME_KEY),
          snns.properties.getProperty(snns.properties.SNN_BOOK_URL_KEY) + help};
        Runtime.getRuntime().exec( command );
      }
    }
  }

  /**
   * returns true when help page is available
   *
   * @return boolean help page exists
   */
  public boolean helpExists(){
    return helpExists;
  }

  // NamedObject interface:

  /**
   * method returns the kernel id for that type of function
   *
   * @return the kernel id of the function type
   */
  public int getKernelType(){ return getKernelType( type ); }

  /**
   * method returns the corresponding kernel id to the given
   * java function type id
   *
   * @param java_fn_type the java function type id
   * @return corresponding kernel function type id
   */
  public static int getKernelType( int java_fn_type ){
    int kt;
    switch( java_fn_type ){
      case ACTIVATION_FN : kt = 2; break;
      case INIT          : kt = 6; break;
      case LEARN         : kt = 4; break;
      case OUTPUT_FN     : kt = 1; break;
      case PRUNING       : kt = 10; break;
      case REMAPPING     : kt = 12; break;
      case UPDATE        : kt = 5; break;
      case PR_LEARN      : kt = 13; break;
      case CC_LEARN      : kt = 14; break;
      case CC_MINI       : kt = 15; break;
      case CC_MODI       : kt = 16; break;
      case CC_ACT        : kt = 17; break;
      default            : kt = -1;
    }
    return kt;
  }

  /**
   * method returns the java function type id corresponding to the given
   * kernel function type id
   *
   * @param kernel_fn_type the kernel function type id
   * @param corresponding java function type id
   */
  public static int getJavaFnType( int kernel_fn_type ){
    int jt;
    switch( kernel_fn_type ){
      case 1  : jt = OUTPUT_FN ; break;
      case 2  : jt = ACTIVATION_FN ; break;
      case 4  : jt = LEARN ; break;
      case 5  : jt = UPDATE ; break;
      case 6  : jt = INIT ; break;
      case 10 : jt = PRUNING ; break;
      case 12 : jt = REMAPPING ; break;
      case 13 : jt = PR_LEARN ; break;
      case 14 : jt = CC_LEARN ; break;
      case 15 : jt = CC_MINI ; break;
      case 16 : jt = CC_MODI ; break;
      case 17 : jt = CC_ACT ; break;
      default : jt = -1;
    }
    return jt;
  }
/* aus glob_typ.h:
#define  OUT_FUNC        1
#define  ACT_FUNC        2
#define  SITE_FUNC       3
#define  LEARN_FUNC      4
#define  UPDATE_FUNC     5
#define  INIT_FUNC       6
#define  ACT_DERIV_FUNC  7
#define  JOG_WEIGHT_FUNC 8
#define  ACT_2_DERIV_FUNC 9
#define  PRUNING_FUNC    10
#define  TEST_FUNC       11
#define  REMAP_FUNC       12
*/
}
