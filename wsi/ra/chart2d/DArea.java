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
 *  Filename: $RCSfile: DArea.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:49:56 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.Vector;

// zum Drucken:
import java.awt.print.*;
import wsi.ra.print.PagePrinter;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * DArea is the crossing of the <code>JComponent</code>s and the
 * <code>DComponent</code>s. It's the <code>DParent</code> which can be added to
 * <code>JComponent</code>s
 */
public class DArea extends JComponent implements DParent, Printable
{
  private static final boolean under_construction = false;

  /**
   * the default minimal rectangle which is shown
   */
  public static final DRectangle DEFAULT_MIN_RECT = new DRectangle(-1, -1, 2, 2);


  /**
   * the container in which all DElements of the area are contained except
   * the grid
   */
  private DContainer container;

  /**
   * min_rectangle is set, when all elements are removed
   * the intersection of visible_rect and max_rectangle is the currently visible
   * rectangle
   */
  protected DRectangle min_rect     = DEFAULT_MIN_RECT,
                       visible_rect = DEFAULT_MIN_RECT;

  protected Double min_x, min_y, max_x, max_y;

  /**
   * the grid of the area
   */
  private DGrid grid;

  private boolean auto_focus = false,
                  auto_grid = false,
                  grid_to_front = false;

  /**
   * maximal number of grid lines
   */
  private int max_grid = 10;

  /**
   * the measures of the area
   * it calculates the coordinates
   */
  private DMeasures measures;


  private DBorder dborder = new DBorder();

  /**
   * initializes the DArea with the initial capacity of 10 components
   */
  public DArea(){
    this( 10 );
  }

  /**
   * initializes the DArea with the specialized initial capacity of components
   * (@see java.util.Vector)
   *
   * @param the initial capacity
   */
  public DArea( int initial_capacity ){
    container = new DContainer();
    container.setDParent( this );
    grid = new DGrid( visible_rect, 1, 1 );
    grid.setVisible( false );
    grid.setDParent( this );
    measures = new DMeasures( this );
  }

  /**
   * returns the currently visible rectangle in DArea coordinates
   *
   * @return DRectangle the size and position of the visible area
   */
  public DRectangle getDRectangle(){
    DRectangle rect = (DRectangle)visible_rect.clone();
    if( min_x != null ) rect.x      = Math.max(rect.x,      getMinX()            );
    if( min_y != null ) rect.y      = Math.max(rect.y,      getMinY()            );
    if( max_x != null ) rect.width  = Math.min(rect.width,  getMaxX() - getMinX());
    if( max_y != null ) rect.height = Math.min(rect.height, getMaxY() - getMinY());
    return rect;
  }

  /**
   * switches the auto focus of this DArea on or off
   *
   * @param b on or off
   */
  public void setAutoFocus( boolean b ){
    boolean old = auto_focus;
    auto_focus = b;
    if( old != b ) repaint();
  }

  /**
   * returns whether the DArea's auto focus is on or not
   *
   * @return <code>true</code> or <code>false</code>
   */
  public boolean isOnAutoFocus(){ return auto_focus; }

  /**
   * sets the visible rectangle to this size
   *
   * @param x the x coordinate of the left border
   * @param y the y coordinate of the bottom border
   * @param width the width of the area
   * @param height the height of the area
   */
  public void setVisibleRectangle( double x, double y, double width, double height ){
    //System.out.println("DArea.setVisibleRectangle(...)");
    setVisibleRectangle( new DRectangle( x, y, width, height ) );
  }

  /**
   * sets the visible rectangle
   *
   * @param rect the visible <code>DRectangle</code> in DArea coordinates
   */
  public void setVisibleRectangle( DRectangle rect ){
    if( under_construction )System.out.println("DArea.setVisibleRectangle(DRectangle)");
    if( rect.isEmpty() ) throw
      new IllegalArgumentException(
        "You shopuld never try to set an empty rectangle\n"
        + "as the visible rectangle of an DArea" );

    if( !rect.equals( visible_rect ) && rect.width > 0 && rect.height > 0 ){
      auto_focus = false;
      visible_rect = (DRectangle)rect.clone();
      repaint();
    }
  }

  /**
   * sets the minimal rectangle
   *
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public void setMinRectangle( double x, double y, double width, double height ){
    setMinRectangle( new DRectangle( x, y, width, height ) );
  }

  /**
   * sets the minimal rectangle
   *
   * @param rect the visible <code>DRectangle</code> in DArea coordinates
   */
  public void setMinRectangle( DRectangle rect ){
    if( rect.isEmpty() ) min_rect = DEFAULT_MIN_RECT;
    else min_rect = (DRectangle)rect.clone();
  }

  /**
   * method returns the minimal rectangle which is set as the visible when all
   * elements are removed and the area is on auto focus
   *
   * @return the minmal rectangle
   */
  public DRectangle getMinRectangle(){
    return (DRectangle)min_rect.clone();
  }

  /**
   * method sets the maximal rectangle whioch can be viewed with the
   * DArea. This method can be used if the area is used with scale functions
   * which are not invertible on all reals
   *
   * @param x the minmal x value
   * @param y the minmal y value
   * @param width of the maximal rectangle
   * @param height of the maximal rectangle
   */
  public void setMaxRectangle( double x, double y, double width, double height ){
    setMaxRectangle( new DRectangle( x, y, width, height ) );
  }


  /**
   * method sets the maximal rectangle whioch can be viewed with the
   * DArea. This method can be used if the area is used with scale functions
   * which are not invertible on all reals
   *
   * @param the rect maximal rectangle of the DArea
   * @deprecated see setMinX, setMinY, setMaxX, setMaxY
   */
  public void setMaxRectangle( DRectangle rect ){
    if( !rect.contains( min_rect ) ) throw
      new IllegalArgumentException("Maximal rectangle does not contain minmal rectangle");

    setMinX( rect.x );
    setMinY( rect.y );
    setMaxX( rect.x + rect.width );
    setMaxY( rect.y + rect.height );
  }

  /**
   * method returns the maximal rectangle of the area
   *
   * @return the maximal rectangle
   * @deprecated see getMaxX, getMaxY, getMinX, getMinY
   */
  public DRectangle getMaxRectangle(){
    return new DRectangle(min_x.doubleValue(),
                          min_y.doubleValue(),
                          max_x.doubleValue() - min_x.doubleValue(),
                          max_y.doubleValue() - min_y.doubleValue() );
  }

  /**
   * method sets the minimal x-value which can be displayed by the DArea
   * might be helpful, if scale functions are used which are not defined overall
   *
   * @param mix the minimal x-value
   */
  public void setMinX( double mix ){
    if( mix > min_rect.x ) throw
      new IllegalArgumentException(
        "Mimimal y-value axes intersects minmal rectangle.");
    min_x = new Double( mix );
  }

  /**
   * method resets the minimal x-value
   */
  public void releaseMinX(){
    min_x = null;
  }

  /**
   * method returns the minmal x-value which can be displayed in the DArea
   *
   * @return the minmal x-value
   */
  public double getMinX(){
    if( min_x != null ) return min_x.doubleValue();
    return 0;
  }


  /**
   * method sets the minimal y-value which can be displayed by the DArea
   * might be helpful, if scale functions are used which are not defined overall
   *
   * @param miy the minimal y-value
   */
  public void setMinY( double miy ){
    if( miy > min_rect.y ) throw
      new IllegalArgumentException(
        "Mimimal y-value axes intersects minmal rectangle.");
    min_y = new Double( miy );
  }

  /**
   * method resets the minimal y-value
   */
  public void releaseMinY(){
    min_y = null;
  }

  /**
   * method returns the minmal y-value which can be displayed in the DArea
   *
   * @return the minmal y-value
   */
  public double getMinY(){
    if( min_y != null ) return min_y.doubleValue();
    return 0;
  }



  /**
   * method sets the maximal x-value which can be displayed by the DArea
   * might be helpful, if scale functions are used which are not defined overall
   *
   * @param max the maximal x-value
   */
  public void setMaxX( double max ){
    if( max < min_rect.x + min_rect.width ) throw
      new IllegalArgumentException(
        "Maximal x-value axes intersects minmal rectangle.");
    max_x = new Double( max );
  }

  /**
   * method resets the maximal x-value
   */
  public void releaseMaxX(){
    max_x = null;
  }

  /**
   * method returns the maxmal x-value which can be displayed in the DArea
   *
   * @return the maxmal x-value
   */
  public double getMaxX(){
    if( max_x != null ) return max_x.doubleValue();
    return 0;
  }



  /**
   * method sets the maximal y-values which can be displayed by the DArea
   * might be helpful, if scale functions are used which are not defined overall
   *
   * @param may the maximal y-value
   */
  public void setMaxY( double may ){
    if( may < min_rect.y + min_rect.height ) throw
      new IllegalArgumentException(
        "Maximal y-value axes intersects minmal rectangle.");
    max_y = new Double( may );
  }

  /**
   * method resets the maximal y-value
   */
  public void releaseMaxY(){
    max_y = null;
  }

  /**
   * method returns the maximal y-value which can be displayed in the DArea
   *
   * @return the maximal y-value
   */
  public double getMaxY(){
    if( max_y != null ) return max_y.doubleValue();
    return 0;
  }


  /**
   * paints the DArea by a Graphics object
   *
   * @param g the java.awt.Graphics object
   */
  public void paint( Graphics g ){
    if( under_construction ) System.out.println("DArea.paint(Graphics)");
    if( auto_focus )  {
      container.restore();
      visible_rect = (DRectangle)container.getRectangle().clone();
    }
    if( visible_rect.isEmpty() )  visible_rect = (DRectangle)min_rect.clone();
    super.paint( g );

    measures.setGraphics( g );
    if( grid.isVisible() && !grid_to_front ) paintGrid( measures );
    container.paint( measures );
    if( grid.isVisible() && grid_to_front ) paintGrid( measures );
  }

  /**
   * repaints a part of the visible area
   *
   * @param r the rectangle to repaint
   */
  public void repaint( DRectangle r ){
    if( under_construction ) System.out.println("DArea.repaint(DRectangle)"+r);
    if( r == null ) throw
      new IllegalArgumentException("Cannot repaint a null DRectangle");
    if( r.isAll() || auto_focus ) repaint();
    else{
      Point p1 = measures.getPoint( r.x, r.y ),
            p2 = measures.getPoint( r.x + r.width, r.y + r.height);
      if( p1 == null || p2 == null ) repaint();
      else {
        DBorder b = getDBorder();
        repaint( p1.x - b.left,
                    p2.y - b.top,
                    p2.x - p1.x + 1 + b.left + b.right,
                    p1.y - p2.y + 1 + b.top + b.bottom );
      }
    }
  }

  /**
   * adds a new component to the area
   *
   * @param e the new DElement
   */
  public void addDElement( DElement e ){
    container.addDElement( e );
  }

  /**
   * removes a certain element from the area
   *
   * @param e the element to remove
   */
  public boolean removeDElement( DElement e ){
    return container.removeDElement( e );
  }

  /**
   * removes all elements from the area
   */
  public void removeAllDElements()
  {
    visible_rect = (DRectangle)min_rect.clone();
    container.removeAllDElements();
  }

  /**
   * returnes all DElements in the container
   *
   * @return the elements of the container
   */
  public DElement[] getDElements(){
    return container.getDElements();
  }

  /**
   * method returns true, if the given element is contained in the container
   *
   * @param e the element
   * @return if it is contained
   */
  public boolean contains( DElement e ){
    return container.contains( e );
  }


  /**
   * sets the grid visible or not
   *
   * @param aFlag visible or not
   */
  public void setGridVisible( boolean aFlag ){
    if( under_construction ) System.out.println("DArea.setGridVisisble: "+aFlag);
    grid.rectangle = getDRectangle();
    grid.setVisible( aFlag );
  }

  /**
   * returns if the grid is visible
   * <code>true</code> if the grid is visible or <code>false</code> if not
   *
   * @return true or false
   */
  public boolean isGridVisible(){
    return grid.isVisible();
  }

  /**
   * sets the grid to the front
   * that means that the grid is painted as last element
   * default value is <code>false</code>
   *
   * @param aFlag grid t front or not
   */
  public void setGridToFront( boolean aFlag ){
    boolean old = grid_to_front;
    grid_to_front = aFlag;
    if( old != aFlag && grid.isVisible() ) repaint();
  }

  /**
   * sets the grid's horizontal and vertical distance
   * that means that the grid's lines will have these distances
   * in area coordinates
   *
   * @param hor_dist the horizontal distance
   * @param ver_dist the vertical distance
   */
  public void setGrid( double hor_dist, double ver_dist ){
    grid = new DGrid( visible_rect, hor_dist, ver_dist );
    grid.setDParent( this );
    auto_grid = false;
    repaint();
  }

  /**
   * sets the auto grid on or off
   * if it's on, the grid's distances (@see #setGrid(double, double))
   * are automatically calculated
   *
   * @param b auto grid on or not
   */
  public void setAutoGrid( boolean b ){
    if( b ) {
      grid.rectangle = getDRectangle();
      grid.setVisible( true );
    }
    if( b == auto_grid ) return;
    auto_grid = b;
    repaint();
  }

  /**
   * returns if the auto grid is switched on
   *
   * @return true if the grid is on, else false
   */
  public boolean hasAutoGrid(){ return auto_grid; }

  /**
   * sets the color of the grid
   *
   * @param java.awt.Color
   */
  public void setGridColor( Color color ){
    grid.setColor( color );
  }

  /**
   * sets the maximal number of grid lines
   * default value is 10
   *
   * @param no maximal number of grid lines
   */
  public void setMaxGrid( int no ){
    if( no < 1 ) return;
    int old = max_grid;
    max_grid = no;
    if( old != no ) repaint();
  }

  /**
   * prints the area and it's content
   * @see java.awt.print.Printable and
   * @see java.awt.print.PrintJob
   *
   * @param g  the Graphics object
   * @param pf the @see java.awt.print.PageFormat
   * @param pi the page index
   *
   * @return int @see java.awt.print.Printable
   */
  public int print( Graphics g, PageFormat pf, int pi ){
    if( under_construction ) System.out.println("DArea.print(...)");
    if( pi > 0 ) return Printable.NO_SUCH_PAGE;

    Border sb = getBorder();
    if( !(sb instanceof ScaledBorder) ) sb = null;
    else ( (ScaledBorder)sb ).show_outer_border = false;
    PagePrinter printer = new PagePrinter( this, g, pf );
    int ret = printer.print();
    if( sb != null ) ( (ScaledBorder)sb ).show_outer_border = true;
    return ret;
  }

  /**
   * sets a new scale function to the x-axis
   * That means that not the standard linear scale is shown but the image
   * of the linear scale under the given function
   *
   * @param x_s the scale function for the x-axis
   */
  public void setXScale( DFunction x_s ){
    if( x_s == null && measures.x_scale == null ) return;
    measures.x_scale = x_s;
    repaint();
  }


  /**
   * sets a new scale function to the y-axis
   * That means that not the standard linear scale is shown but the image
   * of the linear scale under the given function
   *
   * @param y_s the scale function for the y-axis
   */
  public void setYScale( DFunction y_s ){
    if( y_s == null && measures.y_scale == null ) return;
    measures.y_scale = y_s;
    repaint();
  }

  /**
   *  returns the measures of the DArea
   *  The DMeasures object calculates the different coodinates and contains a
   *  Graphics object to paint the DElements of the area
   *
   * @return the measures of the DArea
   */
  public DMeasures getDMeasures(){
    return measures;
  }

  /**
   * method 'adds' a certain border around the contained rectangle
   * that means that it takes the old border
   * and takes the maxima of the old and the new values
   *
   * @param clip the java.awt.Insets object of the new clip
   */
  public void addDBorder( DBorder b ){
    dborder.insert(b);
  }

  /**
   * method returns the current border around the rectangle
   *
   * @return the border
   */
  public DBorder getDBorder(){
    return dborder;
  }

  public void restoreBorder(){
    dborder = container.getDBorder();
    if( under_construction ) System.out.println("DArea.restoreBorder -> "+dborder);
  }

  /**
   * method paints the grid
   * how the method paints the grid depends on whether the area is wrapped in a
   * <code>ScaledBorder</code> or not and on the auto_grid option
   */
  private void paintGrid( DMeasures m ){
    if( under_construction ) System.out.println("DArea.paintGrid(DMeasures)");
    grid.rectangle = getDRectangle();
    if( auto_grid ){
      Border b = getBorder();
      if( b instanceof ScaledBorder ){
        ScaledBorder sb = (ScaledBorder)b;
        paintGrid( sb, m );
        return;
      }
      else{
        grid.hor_dist = ScaledBorder.aBitBigger( grid.rectangle.width / max_grid );
        grid.ver_dist = ScaledBorder.aBitBigger( grid.rectangle.height / max_grid );
      }
    }
    grid.paint( m );
  }


  /**
   * paints the grid when auto_grid is selected and the area is surrounded
   * by an instance of ScaledBorder
   *
   * @param sb the ScaledBorder around the area
   * @param m the measures of the area
   */
  private void paintGrid(ScaledBorder sb, DMeasures m){
    if( under_construction ) System.out.println("DArea.paintGrid(ScaledBorder, DMeasures)");
    Dimension d = getSize();
    FontMetrics fm = m.getGraphics().getFontMetrics();
    grid.hor_dist = sb.getSrcdX(fm, d);
    grid.ver_dist = sb.getSrcdY(fm, d);

    if( m.x_scale == null && m.y_scale == null ) grid.paint( m );

    else{// selber malen
      Graphics g = m.g;
      g.setColor( grid.getColor() );

      DRectangle rect = getDRectangle(),
                 src_rect = m.getSourceOf( rect );

      int x = (int)(src_rect.x / grid.hor_dist),
          y = (int)(src_rect.y / grid.ver_dist);
      if( x * grid.hor_dist < src_rect.x ) x++;
      if( y * grid.ver_dist < src_rect.y ) y++;

      DPoint min = new DPoint( rect.x, rect.y ),
             max = new DPoint( min.x + rect.width, min.y + rect.height );

      double pos;

      for( ; (pos = x * grid.hor_dist) < src_rect.x + src_rect.width; x++ ){
        if( m.x_scale != null ) pos = m.x_scale.getImageOf( pos );
        Point p1 = m.getPoint( pos, min.y ),
              p2 = m.getPoint( pos, max.y );
        g.drawLine( p1.x, p1.y, p2.x, p2.y );
      }

      for( ; (pos = y * grid.ver_dist) < src_rect.y + src_rect.height; y++ ){
        if( m.y_scale != null ) pos = m.y_scale.getImageOf( pos );
        Point p1 = m.getPoint( min.x, pos ),
              p2 = m.getPoint( max.x, pos );
        g.drawLine( p1.x, p1.y, p2.x, p2.y );
      }
    }
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
