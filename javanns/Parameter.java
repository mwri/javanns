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



/*---------------------------- imports ----------------------------------*/

import java.net.* ;
import java.io.* ;


/*------------------------ class declaration ----------------------------*/

/**
 * Class Parameter, helping class for Function
 * To overwrite in case of network or pattern depending default value.
 */

public class Parameter{
  /**
   * default value of the parameter, if exists
   */
  private double value;

  /**
   * name of the parameter
   */
  String parStr ;

  /**
   * further description of the parameter
   */
  String toolTip ;

  /**
   * is true (default), when a default Value exists
   */
  boolean defaultValueExists = true;

  /**
   * class SNNS, only for network or pattern depending default value
   */
  Snns snns = null;

/*----------------------- constructors ---------------------------------------*/

  /**
   * class constructor for parameter with its name (tool tip gets the same value) and
   * the SNNS class to calculate the default value
   *
   * @param n parameter name
   * @param s snns class
   */
  public Parameter(String n, Snns s) {
    this(n, s, null);
  }

  /**
   * class constructor for parameter with name, the SNNS kernel interface
   * to calculate the default value and tool tip text
   *
   * @param n parameter name
   * @param s Snns class
   * @param t toolTip text
   */
  public Parameter(String n, Snns s, String t) {
    parStr = n;
    snns = s;
    toolTip = t;
    defaultValueExists = false;
  }

  /**
   * class constructor for parameter with name and default value
   *
   * @param n parameter name
   * @param v default value
   */
  public Parameter(String n, double v){
    this(n, v, null);
  }

  /**
   * class constructor for parameter with name, default value and tool tip text
   *
   * @param n parameter name
   * @param v default value
   * @param t toolTip text
   */
  public Parameter(String n, double v, String t){
    parStr = n;
    value = v;
    toolTip = t;
  }

  /**
   * class constructor for parameter with name, default value and tool tip text
   *
   * @param n parameter name
   * @param t toolTip text
   * @param v default value
   */
  public Parameter(String n, String t, double v){
    parStr = n;
    value = v;
    toolTip = t;
  }

/*------------------------ methods -------------------------------------------*/

  /**
   * getDefaultParameter returns a default value.
   *
   * @return the default value
   */
  public double getDefaultValue(){
    if(defaultValueExists) return value;
    else return 0.0;
  }
}

