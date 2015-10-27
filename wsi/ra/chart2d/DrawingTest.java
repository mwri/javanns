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


package wsi.ra.chart2d;

import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.MemoryImageSource ;

/**
 * this class is a demonstration class for the DDrawing package
 * Four graphs are shown:
 * the first graph shows where the mouse clicked,
 * the second graph shows the number of mouse clicks on certain x-positions,
 * the third graph shows the number of mouse clicks on certain y-positions and
 * the last graph shows how images can be handeled by the package
 */
public class DrawingTest extends JFrame{
  /**
   * the array of graphs
   */
  DArea area[];

  /**
   * the point set for the first graph
   */
  DPointSet pointSet;

  /**
   * the peak set for the second graph
   */
  DPeakSet peakSet;

  /**
   * constructs the described graphs in a JFrame
   */
  public DrawingTest() {
    super("DrawingTest");
    // sets the layout so that all graphs are shown in one row
    getContentPane().setLayout( new GridLayout(1, 4) );

    area = new DArea[3];
    for( int i=0; i<area.length; i++ ){
      area[i] = new DArea();
      area[i].setBackground( Color.white );
      area[i].setPreferredSize( new Dimension( 200, 150 ) );

      /*
      sets the initializing rectangle and the rectangle which is shown
      when auto focus is selected and all elements of the DArea are removed
      */
      area[i].setMinRectangle( 1, 1, 1, 1 );
      getContentPane().add( area[i] );
    }
    pointSet = new DPointSet( 10 );
    pointSet.setConnected( true );
    pointSet.setIcon(
      new DPointIcon(){
        public void paint( Graphics g ){
          g.drawOval(-4, -4, 8, 8);
        }

        public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
      }
    );
    pointSet.setStroke( new BasicStroke( (float)2.0 ) );

    // adds an DElement to the DArea
    area[0].addDElement( pointSet );

    DLabel lab = new DLabel("LABEL",
                            pointSet,
                            DLabel.LEFT,
                            DLabel.TOP,
                            DLabel.LEFT,
                            DLabel.BOTTOM );
    area[0].addDElement( lab );

    // legt das dargestellte Rechteck fest
    area[0].setVisibleRectangle( 0, 0, 10, 10 );

    // zeichnet, angepasst an den Rahmen, ein Gitternetz in den Graphen
    area[0].setAutoGrid(true);

    // DArea extends JComponent. So all listeners to JComponents can be added.
    area[0].addMouseListener(
      new MouseAdapter(){
        public void mouseClicked( MouseEvent evt ){
          addValue( evt.getPoint() );
        }
      }
    );

    // sets the border around the first graph
    // see ScaledBorder in this package
    area[0].setBorder( new ScaledBorder() );

    DFunction exp = new Exp();
    exp.setColor( Color.red );
    area[0].addDElement( exp );

    peakSet = new DPeakSet(1);
    // the inner color of the peaks is set by setFillColor, the color of the
    // border is set by setColor
    peakSet.setFillColor( Color.green.darker().darker() );
    peakSet.setColor( Color.magenta );
    area[1].addDElement(peakSet);

    // auto focus sets the size of the area exactly to the minimal size, so
    // that all added elements are visible
    area[1].setAutoFocus( true );

    // to get a logarithmic scale you have to choose the exponetial function
    // as scale function
    area[1].setYScale( exp );

    // because the scale function Exp is used
    // it is necessary to tell the DArea not to try to paint the lower line
    // of the peaks so let the minimum be 0.0001 for the y-values
    area[1].setMinY(.0001);

    // auto grid does not look very pretty on areas with non identity scales
    // this should be improved
    area[1].setAutoGrid( true );

    // die Angabe des Rechtecks ist notwendig, da ansonsten immer das maximale
    // Rechteck welches fuer area[1] vorgesehen war angenommen wuerde
    exp = new Exp();
    exp.rectangle = new DRectangle(0, 1, 5, 5);
    area[1].addDElement( exp );

    ScaledBorder sb = new ScaledBorder();
    sb.x_label = "x - Werte";
    sb.y_label = "Anzahl";
    // you can tell the scaled border not to select a certain minimal distance
    // between displayed labels, this concerns the ( linear ) source values
    // of the scale. In this case you might take the log value of 10 so we can
    // see the y-values 1, 10, 100 ...
    sb.setSrcdY( Math.log( 10 ) );
    // the scaled border formats the numbers on the axis by an object, which
    // implements the java.text.NumberFormat interface.
    // Default value for these formatters (one for the x- and one for the y-axis)
    // is the default DecimalFormat for the current locale
    ((java.text.DecimalFormat)sb.format_y).applyPattern("0.###E0");
    area[1].setBorder( sb );

    area[2].addDElement( new TestImage( area[2] ) );
    // DMouseZoom allows the user, to scroll and zoom the area
    new DMouseZoom( area[2] );
    area[2].setBorder( new ScaledBorder() );
    // ToolTipText zeigt die aktuelle Position des Mauszeigers in
    // Graphenkoordinaten an
    area[2].addMouseMotionListener(
      new MouseMotionAdapter(){
        public void mouseMoved(MouseEvent evt){
          DPoint dp = area[2].getDMeasures().getDPoint(evt.getPoint());
          area[2].setToolTipText(dp.toString());
        }
      }
    );

    pack();
    setVisible( true );
    addWindowListener(
      new WindowAdapter(){
        public void windowClosing( WindowEvent evt ){
          System.exit(0);
        }
      }
    );
  }

  public static void main( String[] args ){
    new DrawingTest();
  }

  private void addValue( Point p ){
    DMeasures m = area[0].getDMeasures();
    DPoint q = m.getDPoint( p );
    if( pointSet.getSize() > 10 ) {
      peakSet.removeAllDElements();
      pointSet.removeAllPoints();
    }
    peakSet.addValue( q.x );
    pointSet.addDPoint( q.x, q.y );
  }
}
/**
 * the TestImage shows, how the images can react, when the size of their
 * DParents changed
 */
class TestImage extends DImage{
  int res = 50;
  DArea area;

  /**
   * creates an image of the size 10 x 10 at the position -5 x -5
   *
   * NOTE: the coordinates -5 x -5 belong to the minimal displayed
   * x- and y-values, that means they lie in the left lower corner
   */
  public TestImage( DArea area ){
    super( -5, -5, 10, 10, null );
    this.area = area;
  }

  /**
   * this method overrides the method in DImage
   * so it is possible for the image to react if the size of the parent
   * has changed
   */
  public void paint( DMeasures m ){
    if( !equals( m.getDRectangle() ) ) update( m );
    super.paint( m );
  }

  /**
   * this method calculates the new image if the size has changed
   */
  private void update( DMeasures m ){
    DRectangle rect = area.getDRectangle();
    int index = 0, c;
    double minx = rect.x,
           miny = rect.y,
           h_step = rect.width / (double)res,
           v_step = rect.height / (double)res;
    int[] pix = new int[ res * res ];
    for( int v=0; v<res; v++ )  {
      for( int h=0;  h<res; h++ )  {
        c = (int)( Math.sin( minx + h * h_step ) * 255 );
        c = c<<8;
        c += (int)( Math.cos( miny + rect.height - v * v_step ) * 255 );
        c = c<<8;
        c = (255<<24)|c;
        pix[ index++ ] = c;
      }
    }
    setImage( area.createImage( new MemoryImageSource( res, res, pix, 0, res ) ) );
    x = rect.x;
    y = rect.y;
    width = rect.width;
    height = rect.height;
  }
}

/**
 * an example for a (scale) function
 */
class Exp extends DFunction{
  public boolean isDefinedAt( double source ){ return true; }
  public boolean isInvertibleAt( double image ){ return image > 0; }
  public double getImageOf( double source ){ return Math.exp( source ); }
  public double getSourceOf( double target ){
    if( target <= 0 ) throw
      new IllegalArgumentException(
        "Can not calculate log on values smaller than or equal 0"
      );
    return Math.log( target );
  }
}