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
 *  Filename: $RCSfile: DAequiDistPoints.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:49:47 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.* ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * DAequiDistPoints represents a special kind of <code>DPointSet</code> with
 * constant x-distance between the points
 */
public class DAequiDistPoints extends DPointSet{

  /**
   * minX is the start-x-value for the set,
   * distance the constant x-distance
   */
  public DAequiDistPoints( double minX, double distance ){
    this( minX, distance, 10 );
  }

  /**
   * minX is the start-x-value for the set,
   * distance the constant x-distance,
   * initial-capacity of the array
   */
  public DAequiDistPoints( double minX, double distance, int initial_capacity ){
    super(new AffineMap(minX,distance), new DArray(initial_capacity));
  }

  /**
   * this method adds some new values to the currently stored values
   * it enlarges the array and repaints the set
   *
   * @param v double array of new values
   */
  public void addValues(double[] v){
    for( int i=0; i<v.length; i++ )
      addDPoint(0, v[i]);
    restore();
    repaint();
  }

  /**
   * @deprecated @see #getSize()
   */
  public int size(){ return getSize(); }

}

class AffineMap implements DIntDoubleMap{
  double c, m;
  int size;

  public AffineMap(double c, double m){
    this.c = c;
    this.m = m;
  }

  public boolean setImage(int index, double v){
    return false;
  }

  public double getImage(int index){
    if( index >= size ) throw
      new ArrayIndexOutOfBoundsException(index);
    return c + index * m;
  }

  public boolean addImage(double v){
    size++;
    return (m != 0);
  }

  public int getSize(){
    return size;
  }

  public double getMaxImageValue(){
    if( size == 0 ) throw
      new IllegalArgumentException("AffineMap is empty. No maximal value exists");
    if( m < 0 ) return c;
    else return c + m * (size-1);
  }

  public double getMinImageValue(){
    if( size == 0 ) throw
      new IllegalArgumentException("AffineMap is empty. No minimal value exists");
    if( m < 0 ) return c + m * (size-1);
    else return c;
  }

  public boolean restore(){
    return false;
  }

  public void reset(){
    size = 0;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
