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

import javax.swing.* ;
import javax.swing.event.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.MemoryImageSource ;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;

/*---------------------- class declaration -----------------------------------*/

class ProjectionPanel extends JPanel implements ActionListener,
                                                NetworkListener,
                                                Printable{
  Snns snns;
  FlatButton bLeft, bUp, bRight, bDown, bZoomIn, bZoomOut, bLowerRes, bHigherRes;
  JInternalFrame frame;
  Network network;
  NetworkViewSettings settings;
  int input1, input2, target;
  private static final int MAXRES = 256;
  ProjectionGraph graph;

  public ProjectionPanel( Snns snns ){
    this.snns = snns;
    settings = snns.getCurrentSettings();
    network = snns.network;
    network.addListener( this );

    // Start with left panel
    ImageIcon icon = snns.icons.getIcon("ZoomIn16.gif", "Zoom in");
    bZoomIn = new FlatButton( icon );
    bZoomIn.setToolTipText("Zoom in");
    bZoomIn.addActionListener( this );

    icon = snns.icons.getIcon("ZoomOut16.gif", "Zoom out");
    bZoomOut = new FlatButton( icon );
    bZoomOut.setToolTipText("Zoom out");
    bZoomOut.addActionListener( this );

    icon = snns.icons.getIcon("leftArrow-12.gif", "Move left");
    bLeft = new FlatButton( icon );
    bLeft.setToolTipText("Move left");
    bLeft.addActionListener( this );

    icon = snns.icons.getIcon("highResolution.gif", "Higher resolution");
    bHigherRes = new FlatButton( icon );
    bHigherRes.setToolTipText("Increase resolution");
    bHigherRes.addActionListener( this );

    icon = snns.icons.getIcon("lowResolution.gif", "Lower resolution");
    bLowerRes = new FlatButton( icon );
    bLowerRes.setToolTipText("Decrease resolution");
    bLowerRes.addActionListener( this );

    JPanel pLeft = new JPanel();
    pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS) );
    pLeft.add(bZoomIn);
    pLeft.add(bZoomOut);
    pLeft.add( Box.createVerticalGlue() );
    pLeft.add(bLeft);
    pLeft.add( Box.createVerticalGlue() );
    pLeft.add(bHigherRes);
    pLeft.add(bLowerRes);

    // Top panel contains only arrow
    icon = snns.icons.getIcon("upArrow-12.gif", "Move up");
    bUp = new FlatButton( icon );
    bUp.setToolTipText("Move up");
    bUp.addActionListener( this );

    JPanel pTop = new JPanel();
    pTop.setLayout(new BoxLayout(pTop, BoxLayout.X_AXIS) );
    pTop.add( Box.createHorizontalGlue() );
    pTop.add(bUp);
    pTop.add( Box.createHorizontalGlue() );

    // Main panel is the projection graph
    graph = new ProjectionGraph( this );
    input1 = graph.input[ 0 ].getNumber();
    input2 = graph.input[ 1 ].getNumber();
    target = graph.target.getNumber();

    // Bottom panel contains only arrow
    icon = snns.icons.getIcon("downArrow-12.gif", "Move down");
    bDown = new FlatButton( icon );
    bDown.setToolTipText("Move down");
    bDown.addActionListener( this );

    JPanel pBottom = new JPanel();
    pBottom.setLayout(new BoxLayout(pBottom, BoxLayout.X_AXIS) );
    pBottom.add( Box.createHorizontalGlue() );
    pBottom.add(bDown);
    pBottom.add( Box.createHorizontalGlue() );

    // Right panel contains only arrow
    icon = snns.icons.getIcon("rightArrow-12.gif", "Move right");
    bRight = new FlatButton(icon);
    bRight.setToolTipText("Move right");
    bRight.addActionListener(this);

    JPanel pRight = new JPanel();
    pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS) );
    pRight.add( Box.createVerticalGlue() );
    pRight.add(bRight);
    pRight.add( Box.createVerticalGlue() );

    // Organize all panels in GridBagLayout
//    setBorder(BorderFactory.createEmptyBorder(2,0,0,2));
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);

    gbc.fill = gbc.BOTH;
    gbc.gridheight = 3;
    gbl.setConstraints(pLeft, gbc);
    add(pLeft);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbl.setConstraints(pTop, gbc);
    add(pTop);

    gbc.gridy = gbc.RELATIVE;
    gbc.weightx = gbc.weighty = 1;
    gbl.setConstraints(graph, gbc);
    add(graph);

    gbc.weightx = gbc.weighty = 0;
    gbl.setConstraints(pBottom, gbc);
    add(pBottom);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridheight = 3;
    gbc.weightx = gbc.weighty = 0;
    gbl.setConstraints(pRight, gbc);
    add(pRight);

    frame = new JInternalFrame("Projection to "
                               + graph.target.getName() + "["
                               + target + "]",
                               true, true, true, true);
    frame.addInternalFrameListener(
      new InternalFrameAdapter(){
        public void internalFrameClosed( InternalFrameEvent evt ){
          close();
        }
      }
    );
    frame.setContentPane( this );
    frame.setSize( 400, 300 );
    enableEvents( AWTEvent.KEY_EVENT_MASK );
  }

  /**
   * shows the exception, removes the panel from listener lists and closes the frame
   *
   * @param the exception
   * @param the source object
   */

  public void close( Exception e, Object src ){
    snns.showException( e, src );
    close();
  }

  /**
   * removes the panel from the listener lists and closes the frame
   */
  private void close(){
    snns.network.removeListener( this );
    if( frame.isVisible() ) frame.dispose();
  }

/*------------------------- interfaces ---------------------------------------*/
  // implementing ActionListener :
  public void actionPerformed(ActionEvent evt){
    Object src = evt.getSource();
    DRectangle rect = graph.getDRectangle();

    if( src == bLeft ){
      double d = rect.width * .5;
      rect.x -= d;
    }
    else if( src == bUp ){
      double d = rect.height * .5;
      rect.y += d;
    }
    else if( src == bRight ){
      double d = rect.width * .5;
      rect.x += d;
    }
    else if( src == bDown ){
      double d = rect.height * .5;
      rect.y -= d;
    }
    else if( src == bHigherRes ) {
      if(graph.res < MAXRES) graph.res *= 2;
      if(graph.res >= MAXRES) bHigherRes.setEnabled(false);
      bLowerRes.setEnabled(true);
      try{ graph.update(); }
      catch( Exception e ){ close(); }
    }
    else if( src == bLowerRes ){
      if(graph.res > 1) graph.res /= 2;
      if(graph.res <= 1) bLowerRes.setEnabled(false);
      bHigherRes.setEnabled(true);
      try{ graph.update(); }
      catch( Exception e ){ close(); }
    }
    else if( src == bZoomIn ){
      double d = rect.height * .25;
      rect.y += d;
      rect.height -= 2 * d;

      d = rect.width * .25;
      rect.x += d;
      rect.width -= 2 * d;
    }
    else if( src == bZoomOut ){
      double d = rect.height * .25;
      rect.y -= d;
      rect.height += 2 * d;

      d = rect.width * .25;
      rect.x -= d;
      rect.width += 2 * d;
    }
    graph.setVisibleRectangle( rect );
  }
  // implementing NetworkListener :
  public void networkChanged(NetworkEvent evt){
    int id = evt.id;
    if( id == NetworkEvent.NETWORK_DELETED ||
        id == NetworkEvent.NEW_NETWORK_LOADED ||
        id == NetworkEvent.NETWORK_PRUNED ) close();

    else if( id == NetworkEvent.UNITS_DELETED ){
      boolean changed = false;
      UnitDeleteArgument uda = (UnitDeleteArgument)evt.arg;
      UnitData[] data = uda.uData;
      int no;
      for( int i=0; i<data.length; i++ ){
        no = data[ i ].number;
        if( no == input1 || no == input2 || no == target ) { close(); return; }
        if( no < input1 ) { input1--; changed = true ; }
        if( no < input2 ) { input2--; changed = true ; }
        if( no < target ) { target--; changed = true ; }
      }
      if( changed ){
        Network network = evt.getNetwork();
        Unit[] input = new Unit[]{
          network.getUnitNumber( input1 ),
          network.getUnitNumber( input2 ) };
        Unit targetU = network.getUnitNumber( target );
        try{ graph.setUnits( input, targetU ); }
        catch( Exception e ){ close( e, this ); }
        frame.setTitle("Projection to " + targetU.getName() + "[" + target + "]");
        frame.repaint();
      }
    }

    else if( id == NetworkEvent.UNITS_CREATED ){
      boolean changed = false;
      Unit[] units = (Unit[])evt.arg;
      int no;
      for( int i=units.length-1; i!=-1; i-- ){
        no = units[ i ].getNumber();
        if( no <= input1 ) { input1++; changed = true ; }
        if( no <= input2 ) { input2++; changed = true ; }
        if( no <= target ) { target++; changed = true ; }
      }
      if( changed ){
        Network network = evt.getNetwork();
        Unit[] input = new Unit[]{
          network.getUnitNumber( input1 ),
          network.getUnitNumber( input2 )
        };
        Unit targetU = network.getUnitNumber( target );
        try{ graph.setUnits( input, targetU ); }
        catch( Exception e ){ close( e, this ); }
        frame.setTitle("Projection to " + targetU.getName() + "[" + target + "]");
        frame.repaint();
      }
    }
    else if( id == NetworkEvent.NETWORK_INITIALIZED ||
      evt.id == NetworkEvent.NETWORK_TRAINED ||
      evt.id == NetworkEvent.LINKS_DELETED ||
      evt.id == NetworkEvent.LINKS_CREATED )
      graph.update();
  }

  // implementing Printable:
  public int print( Graphics g, PageFormat pf, int pi ){
    return graph.print( g, pf, pi );
  }

  public static boolean projectionIsPossible(Network net){
    boolean isPossible = false;
    if( net.getSelectedUnitsCount() == 3 ) {
      int input = 0, hiddenOrOutput = 0;
      Unit[] units = net.getSelectedUnits();
      for( int i=0; i<3; i++ )
        switch( units[ i ].getType() ){
          case UnitTTypes.INPUT  : input++; break;
          case UnitTTypes.HIDDEN :
          case UnitTTypes.OUTPUT : hiddenOrOutput++;
        }
      if( input == 2 && hiddenOrOutput == 1 ) isPossible = true;
    }
    return isPossible;
  }
}
