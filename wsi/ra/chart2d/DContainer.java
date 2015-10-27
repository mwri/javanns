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
 *  Filename: $RCSfile: DContainer.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:50:29 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.util.Vector ;
import java.awt.Color ;
import java.awt.Graphics;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class DContainer extends DComponent implements DParent{
  /**
   * the elements of the container and their keys ( Strings )
   */
  protected Vector elements, keys;

  public DContainer(){ this(10); }

  public DContainer(int initial_capacity){
    super();
    elements = new Vector(initial_capacity);
    keys = new Vector(initial_capacity);
  }

  public void repaint( DRectangle r ){
    DParent parent = getDParent();
    if( parent != null ) parent.repaint( r );
  }

  /**
   * method adds a new DElement to the container
   * if the container already contains this element, the method simply returns
   * the method checks whether the DElement is an ancestor of the container
   * itself. In that case it throws an IllegalArgumentException.
   *
   * @param e the new DElement to add
   */
  public void addDElement( DElement e ){
    addDElement( null, e );
  }


  /**
   * method adds a new DElement to the container by a certain key name
   * if the container already contains this element, the method simply returns
   * the method checks whether the DElement is an ancestor of the container
   * itself. In that case it throws an IllegalArgumentException.
   *
   * @param key the name of the DElement in the container
   * @param e   the new DElement to add
   */
  public void addDElement( String key, DElement e ){
    if( elements.contains( e ) ) return;
    if( e instanceof DParent ){
      DParent he = (DParent)e, me = (DParent)this;
      if( he == me ) throw new
        IllegalArgumentException("Adding DParent to itself");
      me = getDParent();
      while( me != null ){
        if( he == me )throw new
          IllegalArgumentException("Adding DContainer's parent to itself");
        if( me instanceof DElement )
          me = ((DElement)me).getDParent();
        else me = null;
      }
    }
    elements.add( e );
    addDBorder( e.getDBorder() );
    keys.add( key );
    e.setDParent( this );
    DRectangle r = e.getRectangle();
    rectangle.insert( r );
    if( e.isVisible() ) repaint( r );
  }

  /**
   * method removes a certain DElement from the container an returns whether it
   * contained it before
   *
   * @param e the DElement to remove
   * @return if this was possible
   */
  public boolean removeDElement( DElement e ){
    int index = elements.indexOf( e );
    if( index > -1 ){
      elements.remove( index );
      keys.remove( index );
      repaint( e.getRectangle() );
      restore();
      return true;
    }
    return false;
  }

  /**
   * method removes all DElements from the container
   */
  public void removeAllDElements()
  {
    elements.removeAllElements();
    keys.removeAllElements();
    rectangle = DRectangle.getEmpty();
    repaint();
  }

  /**
   * method returns all DElements of the container
   *
   * @return the elements of the container
   */
  public DElement[] getDElements(){
    DElement[] es = new DElement[ elements.size() ];
    elements.toArray(es);
    return es;
  }

  /**
   * method returns the first DElement of the contaioner belonging to
   * the given key
   *
   * @param key the name of the DElement in the container
   * @return the element when it could be found or null else
   */
  public DElement getDElement( String key ){
    int index = -1;
    for( int i=0; index == -1 && i < keys.size(); i++ )
      if( ((String)keys.get(i)).equals( key ) ) index = i;
    return (index<keys.size())? (DElement)elements.get(index):(DElement)null;
  }

// implementing DComponent:
  /**
   * method calls all currently visible DElements of the container to paint
   * themselves by the given DMeasures object
   *
   * @param m the DMeasures object
   */
  public void paint( DMeasures m ){
    DElement e;
    for( int i=0; i<elements.size(); i++ ){
      e = (DElement)elements.elementAt(i);
      if( e.isVisible() && !m.getDRectangle().getIntersection( e.getRectangle() ).isEmpty()){
        m.g.setColor( DEFAULT_COLOR );
        e.paint( m );
      }
    }
  }
  /**
   * method returns true, if the given element is contained in the container
   *
   * @param e the element
   * @return if it is contained
   */
  public boolean contains( DElement e ){
    return elements.contains( e );
  }

  /**
   * method sets the given color to all contained DElements of the container
   *
   * @param c the new Color of the elements
   */
  public void setColor(Color c){
    for( int i=0; i<elements.size(); i++ )
      ((DElement)elements.get(i)).setColor(c);
    super.setColor(c);
  }

  /**
   * method adds the given border to the current border of the container
   * that means, that if necessary it will enlarge it and tells it to its
   * parent
   *
   * @param b the border to add
   */
  public void addDBorder(DBorder b){
    if( getDBorder().insert(b) && parent != null ) parent.addDBorder(b);
  }

  public void restoreBorder(){
    DBorder b = new DBorder();
    for( int i=0; i<elements.size(); i++ )
      b.insert( ((DElement)elements.get(i)).getDBorder() );
    setDBorder( b );
  }


  /**
   * restores the container, that means that the rectangle is completely new calculated
   * this method is used after removing one of the elements
   */
  boolean restore(){
    DRectangle old = (DRectangle)rectangle.clone();
    rectangle = DRectangle.getEmpty();
    setDBorder( new DBorder() );
    for( int i=0; i<elements.size(); i++ ){
      DElement elt = (DElement)elements.elementAt(i);
      rectangle.insert(elt.getRectangle());
      addDBorder( elt.getDBorder() );
    }
    return !old.equals( rectangle );
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
