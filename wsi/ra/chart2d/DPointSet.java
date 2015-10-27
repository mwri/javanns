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
 *  Filename: $RCSfile: DPointSet.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:52:25 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.* ;
import wsi.ra.tool.IntegerArrayList;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class DPointSet extends DComponent
{
  protected DPointIcon icon = null;

  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/

  protected DIntDoubleMap x, y;
  protected boolean connected;
  protected Stroke stroke = new BasicStroke();
  protected JumpManager jumper = new JumpManager();

  /*-------------------------------------------------------------------------*
   * constructor
   *-------------------------------------------------------------------------*/
  public DPointSet(){
    this(10, 2);
  }


  public DPointSet( int initial_capacity ){
    this(initial_capacity, 2);
  }

  public DPointSet( int initial_capacity, int length_multiplier ){
    this( new DArray(initial_capacity, length_multiplier),
          new DArray(initial_capacity, length_multiplier) );
  }

  public DPointSet(DIntDoubleMap x_values, DIntDoubleMap y_values){
    if( x_values.getSize() != y_values.getSize() ) throw
      new IllegalArgumentException(
        "The number of x-values has to be the same than the number of y-values"
      );
    x = x_values;
    y = y_values;
    restore();
    setDBorder(new DBorder(1,1,1,1));
  }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  public void paint( DMeasures m ){
    Graphics2D g = (Graphics2D)m.getGraphics();
    g.setStroke(stroke);
    if( color != null ) g.setColor( color );
    int size = getSize();
    if( connected && size > 1 ){
      jumper.restore();
      while( jumper.hasMoreIntervals() ){
        int[] interval = jumper.nextInterval();
        Point p1 = null, p2;
        for( int i=interval[0]; i<interval[1]; i++ ){
          p2 = m.getPoint( x.getImage(i), y.getImage(i) );
          if( p1 != null)
            g.drawLine( p1.x, p1.y, p2.x, p2.y );
          if( icon != null ){
            g.setStroke( new BasicStroke() );
            g.translate(p2.x, p2.y);
            icon.paint(g);
            g.translate(-p2.x, -p2.y);
            g.setStroke( stroke );
          }
          p1 = p2;
        }
      }
    }
    else{
      Point p;
      for( int i=0; i<size; i++ ){
        p = m.getPoint( x.getImage(i), y.getImage(i) );
        if( icon == null ){
          g.drawLine(p.x - 4, p.y - 4, p.x + 4, p.y + 4);
          g.drawLine(p.x + 4, p.y - 4, p.x - 4, p.y + 4);
        }
        else{
          g.setStroke( new BasicStroke() );
          g.translate(p.x, p.y);
          icon.paint(g);
          g.translate(-p.x, -p.y);
        }
      }
    }
    g.setStroke( new BasicStroke() );
  }

  public void addDPoint( DPoint p ){
    x.addImage(p.x);
    y.addImage(p.y);
    rectangle.insert(p);
    repaint();
  }

  public void addDPoint( double x, double y ){
    addDPoint(new DPoint(x, y));
  }

  /**
   * method causes the DPointSet to interupt the connected painting at the
   * current position
   */
  public void jump(){
    jumper.addJump();
  }

  /**
   * method removes all jump positions
   * if the DPointSet is connected, all points will be painted connected to
   * their following point
   */
  public void removeJumps(){
    jumper.reset();
  }

  /**
   * method returns the DPoint at the given index
   *
   * @param index the index of the DPoint
   * @return the DPoint at the given index
   */
  public DPoint getDPoint( int index ){
    if( index >= x.getSize() ) throw new ArrayIndexOutOfBoundsException(index);
    DPoint p = new DPoint( x.getImage( index ), y.getImage( index ) );
    p.setIcon( icon );
    p.setColor( color );
    return p;
  }

  /**
   * method puts the given DPoint at the given position in the set
   *
   * @param index the index of the point
   * @param p     the point to insert
   */
  public void setDPoint( int index, DPoint p ){
    if( index >= x.getSize() ) throw new ArrayIndexOutOfBoundsException(index);
    rectangle.insert(p);
    x.setImage(index,p.x);
    y.setImage(index,p.y);
    restore();
    repaint();
  }

  /**
   * method sets an icon for a better displaying of the point set
   *
   * @param icon the DPointIcon
   */
  public void setIcon( DPointIcon icon ){
    this.icon = icon;
    if( icon == null ) setDBorder(new DBorder(1,1,1,1));
    else setDBorder( icon.getDBorder() );
  }

  /**
   * method returns the current icon of the point set
   *
   * @return the DPointIcon
   */
  public DPointIcon getIcon(){
    return icon;
  }

  /**
   *  method sets the stroke of the line
   *  if the points were not connected, they now will be connected
   *
   * @param s the new stroke
   */
  public void setStroke( Stroke s ){
    if( s == null ) s = new BasicStroke();
    stroke = s;
    repaint();
  }

  /**
   * method returns the current stroke of the line
   *
   * @return the stroke
   */
  public Stroke getStroke(){
    return stroke;
  }

  public void setConnected( boolean aFlag ){
    boolean changed = !( aFlag == connected );
    connected = aFlag;
    if( changed ) repaint();
  }

  public void removeAllPoints(){
    if( x.getSize() == 0 ) return;
    x.reset();
    y.reset();
    jumper.reset();
    repaint();
    rectangle = DRectangle.getEmpty();
  }

  public String toString(){
    String text = "wsi.ra.chart2d.DPointSet[size:"+getSize();
    for( int i=0; i<x.getSize(); i++ )
      text += ",("+x.getImage(i)+","+y.getImage(i)+")";
    text += "]";
    return text;
  }

  /**
   * method returns the index to the nearest <code>DPoint</code> in this <code>DPointSet</code>.
   *
   * @return the index to the nearest <code>DPoint</code>. -1 if no nearest <code>DPoint</code> was found.
   */
  public int getNearestDPointIndex(DPoint point){
    double minValue = Double.MAX_VALUE;
    int    minIndex = -1;
    for( int i=0; i<x.getSize(); i++ ){
      double dx = point.x - x.getImage(i);
      double dy = point.y - y.getImage(i);
      double dummy = dx*dx + dy*dy;
      if (dummy < minValue){
        minValue = dummy;
        minIndex = i;
      }
    }
    return minIndex;
  }

  /**
   * method returns the nearest <code>DPoint</code> in this <code>DPointSet</code>.
   *
   * @return the nearest <code>DPoint</code>
   */
  public DPoint getNearestDPoint(DPoint point){
    int    minIndex = getNearestDPointIndex(point);

    if(minIndex == -1) return null;
    else return new DPoint(x.getImage(minIndex), y.getImage(minIndex));
  }

  public int getSize(){
    int size = x.getSize();
    if( size != y.getSize() ) throw
      new ArrayStoreException(
        "The number of x-values is not equal to the number of y-values.\n"
        +"The size of the DPointSet isn´t clear."
      );
    return size;
  }

  protected void restore(){
    if( getSize() == 0){
      rectangle = DRectangle.getEmpty();
      return;
    }
    double min_x = x.getMinImageValue(),
           max_x = x.getMaxImageValue(),
           min_y = y.getMinImageValue(),
           max_y = y.getMaxImageValue();
    rectangle = new DRectangle(min_x, min_y, max_x - min_x, max_y - min_y );
  }

  /**
   * this class stores the jump positions (see this.jump)
   */
  class JumpManager{
    protected IntegerArrayList jumps = new IntegerArrayList();
    protected int index = -1;

    public void addJump(){
      jumps.add(getSize());
    }

    public int[] nextInterval(){
      int no_jumps = jumps.size();
      if( index >= no_jumps ) throw
        new ArrayIndexOutOfBoundsException("No more intervals in JumpManager");

      int[] inter = new int[2];

      if( index == -1 ) inter[0] = 0;
      else inter[0] = jumps.get(index);

      index++;

      if( index < no_jumps ) inter[1] = jumps.get(index);
      else inter[1] = getSize();

      return inter;
    }

    public boolean hasMoreIntervals(){
      return index < jumps.size();
    }

    public void restore(){
      index = -1;
    }

    public void reset(){
      index = -1;
      jumps.clear();
    }
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
