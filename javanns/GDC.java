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

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * GDC is the drawing area for learning error
 *
 */
class GDC extends DArea {
  GraphPanel panel;
  private DivisorCurve[] curves;
  private DivisorCurve val_curve;
  private int cIndex;

  public GDC( GraphPanel panel ){
    this.panel = panel;
    setVisibleRectangle( 0, 0, 100, 100 );
    setAutoGrid( true );
    setGridColor( new Color(0xcc, 0xcc, 0xcc) );
    setBackground( Color.white );
    setPreferredSize( new Dimension( 500, 400 ) );
    ScaledBorder border = new ScaledBorder();
    border.x_label = border.y_label = null;
    border.foreground = Color.gray;
    setBorder( border );

    initCurves();
  }

  /**
   * method removes all lines from the graph and resets the curve index to zero
   */
  public void clearGraph(){
    for( int i=0; i<curves.length; i++ )
      curves[i].removeAllPoints();
    cIndex = 0;
    val_curve.removeAllPoints();
  }

  /**
   * method adds the given array of values to the current selected curve
   *
   * @param v the array of values
   */
  public int addValues( double[] v ){
    curves[ cIndex ].addValues( v );
    return curves[ cIndex ].getSize();
  }

  /**
   * method adds validation values to the validation curve
   * from the given start of the validation
   *
   * @param v the array of validation values
   * @param start the step count when the validation started
   */
  public int addValValues( double[] v, int start ){
    int s = 0, size = val_curve.getSize();
    if( size > 0 ) s = (int)val_curve.getDPoint(size-1).x+1;
    if( s != start ){
      Color c = val_curve.getColor();
      double d = val_curve.divisor;
      removeDElement( val_curve );
      val_curve = new DivisorCurve( start, 1 );
      val_curve.divisor = d;
      val_curve.setConnected( true );
      val_curve.setColor( c );
      addDElement( val_curve );
    }
    val_curve.addValues( v );
    return val_curve.getSize();
  }

  /**
   * Switches to the next colored curve in ErrorGraph. If all colors
   * are exhausted, starts again from the first one.
   *
   */
  public void nextCurve() {
    // increase counter only if the current curve has been used
    if(curves[cIndex].getSize() != 0) {
      cIndex++;
      if( cIndex == curves.length ) cIndex = 0;
      curves[cIndex].removeAllPoints();
    }
    val_curve.removeAllPoints();
  }

  /**
   * Sets the constant divisor to specified value.
   *
   */
  public void setYDivisor(double d) {
    //System.out.println("GDC.setYDivisor(double): "+d);
    for( int i=0; i<curves.length; i++ )
      curves[i].setYDivisor( d );
    val_curve.setYDivisor( d );
  }

  /**
   * Increases x-axis range. Normaly as a response to a button click.
   *
   * @return <code>false</code> if the upper limit has been reached,
   *         <code>true</code> if not
   */
  public boolean increaseXRange() {
    final int MAXX = 1000000;
    double o = visible_rect.width, n = increaseRange(o, MAXX);
    if( n != o ){
      visible_rect.width = n;
      repaint();
    }
    return visible_rect.width < MAXX;
  }

  /**
   * Decreases x-axis range. Normaly as a response to a button click.
   *
   * @return <code>false</code> if the lower limit has been reached,
   *        <code>true</code> if not
   */
  public boolean decreaseXRange() {
    final int MINX = 100;
    double o = visible_rect.width, n = decreaseRange(o, MINX);
    if( n != o ){
      visible_rect.width = n;
      repaint();
    }
    return visible_rect.width > MINX;
  }

  /**
   * Returns x-axis range.
   *
   * @return maximum x value visible
   */
  public double getXRange() {
    return visible_rect.width;
  }

  /**
   * Increases y-axis range. Normaly as a response to a button click.
   *
   * @return <code>false</code> if the upper limit has been reached,
   *        <code>true</code> if not
   */
  public boolean increaseYRange() {
    final int MAXY = 1000000;
    double o = visible_rect.height, n = increaseRange(o, MAXY);
    if( n != o ){
      visible_rect.height = n;
      repaint();
    }
    return visible_rect.height < MAXY;
  }

  /**
   * Decreases y-axis range. Normaly as a response to a button click.
   *
   * @return <code>false</code> if the lower limit has been reached,
   *        <code>true</code> if not
   */
  public boolean decreaseYRange() {
    final double MINY = .1;
    double o = visible_rect.height, n = decreaseRange(o, MINY);
    if( n != o ){
      visible_rect.height = n;
      repaint();
    }
    return visible_rect.height > MINY;
  }

  /**
   * Returns y-axis range.
   *
   * @return maximum y value visible
   */
  public double getYRange() {
    return visible_rect.height;
  }


  /**
   * Increases an axis range.
   *
   * @param cr current range
   * @param max maximum allowed range
   * @return 2 or 2.5 times increased range
   */
  private double increaseRange(double cr, double max) {
    int m = normalizeRange(cr);
    if(cr < max) switch(m) {
      case 2:  cr *= 2.5; break;
      default: cr *= 2;
    }
    return cr;
  }

  /**
   * Decreases an axis range.
   *
   * @param cr current range
   * @param min minimum allowed range
   * @return 2 or 2.5 times decreased range
   */
  private double decreaseRange(double cr, double min) {
    int m = normalizeRange(cr);
    if(cr > min) switch(m) {
      case 5:  cr /= 2.5; break;
      default: cr /= 2;
    }
    return cr;
  }

  /**
   * Normalizes a <i>double</i> to range [1, 9]
   *
   * @param m number to be normalized
   * @return integer value of normalized number
   */
  private int normalizeRange(double m) {
    if(m>1) while(m>=10) m /= 10;
    else while(m<1) m *= 10;
    return (int)m;
  }


  /**
   * this method initializes the curves, that means that it sets different
   * colors to them and adds them to the DArea
   */
  private void initCurves(){
    Color cCurve[] = { Color.black, Color.blue, Color.red, Color.green,
    Color.blue.darker(), Color.red.darker(), Color.green.darker(),
    Color.orange, Color.cyan, Color.magenta };

    curves = new DivisorCurve[ cCurve.length ];
    for( int i=0; i<cCurve.length; i++ ){
      curves[i] = new DivisorCurve(1, 1 );
      curves[i].setColor( cCurve[i] );
      curves[i].setConnected( true );
      addDElement( curves[i] );
    }
    cIndex = 0;

    val_curve = new DivisorCurve(1, 1);
    val_curve.setColor( Color.pink );
    val_curve.setConnected( true );
    addDElement( val_curve );
  }


}
