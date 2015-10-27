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
 *  Filename: $RCSfile: IntegerArrayList.java,v $
 *  Purpose:  This class is similar to 'java.util.ArrayList', except that it can
 *            only hold and manage integer values.
 *  Language: Java
 *  Compiler: JDK 1.2
 *  Authors:  Fred Rapp
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:53:21 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.tool;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * This class is similar to 'java.util.ArrayList', except that it can
 * only hold and manage integer values.
 */
public class IntegerArrayList
{
  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/

  private int[] integerArray = null;
  private int   integerCount = 0;

  private int initialSize;
  private int incrementFactor;

  /*------------------------------------------------------------------------*
   * constructor
   *------------------------------------------------------------------------*/

  public IntegerArrayList()
  {
    // call other constructor with default values
    this(100, 3);
  }

  public IntegerArrayList(int initialSize, int incrementFactor)
  {
    this.initialSize     = initialSize;
    this.incrementFactor = incrementFactor;

    // create new integer array of initial size
    integerArray = new int[initialSize];
  }

  public void destroy() { integerArray = null; integerCount = 0; }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  /**
   * Clears the contents of this integer list and sets it back to it's initial state.
   */
  public void clear() { integerArray = new int[initialSize]; integerCount = 0; }

  /**
   * Returns the number of components in this list.
   */
  public int size() { return integerCount; }

  /**
   * Parses given string to integer and adds it to the integer list.
   * @return true if parsing was successful, false if an error occured.
   */
  public boolean add(String stringValue)
  {
    boolean success = false;
    try {
      int value = Integer.parseInt(stringValue);
      add(value);
      success = true;
    }
    catch (Exception e) {}
    return success;
  }

  /**
   * Adds a new integer value to the integer list.
   */
  public void add(int value)
  {
    // check integer array size
    if (integerCount == integerArray.length)
    {
      // increase size of int array
      int old_length = integerArray.length;
      int new_length = incrementFactor * old_length;
      int[] new_array = new int[new_length];
      for (int i=0; i<old_length; i++) {
        new_array[i] = integerArray[i];
      }
      integerArray = new_array;
    }
    // add given value to array
    integerArray[integerCount++] = value;
  }

  /**
   * Returns the integer value at the given position or 'min-value' if out of array bounds.
   */
  public int get(int index)
  {
    if ((index < 0) || (index >= integerCount)) { return Integer.MIN_VALUE; }

    return integerArray[index];
  }

  /**
   * Returns the contents of this list as an array of integer values.
   * @param exactLength  determines if the returned array should have the exactly right length, or if longer arrays are allowed
   */
  public int[] getIntegerArray(boolean exactLength)
  {
    // check if we can simply return our internal integer array
    if (!exactLength || (integerArray.length == integerCount)) { return integerArray; }

    // make a copy of the array with the exactly right length
    int size = integerCount;
    int[] new_array = new int[ size];
    for (int i=0; i< size; i++) { new_array[i] = integerArray[i]; }
    return new_array;
  }

  /**
   * Returns the contents of this list as an array of double values.
   */
  public double[] getDoubleArray()
  {
    // make a copy of the array
    int size = integerCount;
    double[] new_array = new double[ size];
    for (int i=0; i< size; i++) { new_array[i] = integerArray[i]; }
    return new_array;
  }

  /**
   * Returns the contents of this list as an array of strings.
   */
  public String[] getStringArray()
  {
    // make a copy of the array
    int size = integerCount;
    String[] new_array = new String[ size];
    for (int i=0; i<size; i++) { new_array[i] = "" + integerArray[i]; }
    return new_array;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
