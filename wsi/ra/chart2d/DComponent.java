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
 *  Filename: $RCSfile: DComponent.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:21 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Color ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * <code>DComponent</code> is the mother of all objects which can be displayed
 * by a <code>DArea</code> object, even when it would be also enough to
 * implement the <code>DElement</code> interface to an class
 *
 * DComponent is abstract because the paint method has to be overridden
 */
public abstract class DComponent implements DElement
{
  /**
   * the color of the component
   */
  protected Color color;

  /**
   * the rectangle in which the component lies
   */
  protected DRectangle rectangle;

  /**
   * the parent of the component which is responsible for repainting
   */
  protected DParent parent;


  private boolean visible = true;



  /**
   * this border respresents the additional space around the clip of the
   * graphics context, which is calculated by the union of all DRectangles of
   * the components. For example it is used by DPointIcons or DLabels.
   */
  private DBorder border = new DBorder();


  /**
   * this constructor is necessary to avoid infinite loops in constructing
   * DRectangles
   */
  DComponent(boolean is_rect){}

  public DComponent(){ rectangle = DRectangle.getEmpty(); }

  /**
   * returns the rectangle in which the object lies
   */
  public DRectangle getRectangle(){
    return (DRectangle)rectangle.clone();
  }


  /**
   * method sets a certain border around the contained rectangle
   *
   * @param b the new DBorder
   */
  public void setDBorder( DBorder b ){
    if( parent != null ) {
      if( border.insert(b) ) { parent.addDBorder( b ); repaint(); }
      else { border = b; parent.restoreBorder(); }
    }
    else border = b;
  }

  /**
   * method returns the current border around the rectangle
   *
   * @return the DBorder of the DComponent
   */
  public DBorder getDBorder(){
    return border;
  }

  /**
   * sets the parent of the component, which should take care of painting the
   * component to the right time
   */
  public void setDParent( DParent parent ){
    if( this.parent != null && this.parent != parent ){
      this.parent.removeDElement( this );
      this.parent.repaint( getRectangle() );
    }
    this.parent = parent;
  }

  /**
   * returns the parent of the component
   */
  public DParent getDParent(){ return parent; }

  /**
   * invoces the parent to repaint the rectangle in which the component lies
   */
  public void repaint(){
    //System.out.println("DComponent.repaint()");
    if( parent != null ) parent.repaint( getRectangle() );
  }

  /**
   * sets the color of the component
   */
  public void setColor( Color color ){
    if( this.color == null || !this.color.equals( color ) ) {
      this.color = color;
      repaint();
    }
  }

  /**
   * returns the color of the component
   */
  public Color getColor(){ return color; }

  /**
   * sets the component visible or not
   */
  public void setVisible( boolean aFlag ){
    boolean changed = ( aFlag != visible );
    visible = aFlag;
    if( changed ) repaint();
  }

  /**
   * returns if the component should be visible when the parent shows the right
   * area
   */
  public boolean isVisible(){ return visible; }

}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
