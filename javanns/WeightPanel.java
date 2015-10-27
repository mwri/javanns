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

import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.MemoryImageSource ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.util.Vector;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;

public class WeightPanel extends JPanel implements ActionListener,
                                                   Printable{
  Snns snns;
  JInternalFrame frame;
  WeightGraph graph;
  // components for tool panel:
  Icon iGridOn, iGridOff;
  FlatButton bGrid, bAutoFocus;

  public WeightPanel( Snns snns ){
    this.snns = snns;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

    graph = new WeightGraph( snns, this );
    add( graph );
    add( getTools() );

    frame = new JInternalFrame("Linkweights",true, true, true, true);
    frame.addInternalFrameListener(
      new InternalFrameAdapter(){
        public void internalFrameClosed( InternalFrameEvent evt ){
          graph.removeFromLists();
        }
      }
    );
    frame.setContentPane( this );
    frame.setSize( 400, 300 );
  }

  public void actionPerformed( ActionEvent evt ){
    Object src = evt.getSource();

    if( src == bGrid ){
      if( graph.isGridVisible() ){
        graph.setGridVisible( false );
        bGrid.setIcon( iGridOn );
        bGrid.setToolTipText("Turn grid on");
      }
      else{
        graph.setGridVisible( true );
        bGrid.setIcon( iGridOff );
        bGrid.setToolTipText("Turn grid off");
      }
    }
    else if( src == bAutoFocus ) graph.setAutoFocus( true );
  }

  public int print( Graphics g, PageFormat pf, int pi ){
    //System.out.println("WeightPanel.print(...)");
    return graph.print( g, pf, pi );
  }

  private JPanel getTools(){
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
    iGridOn = snns.icons.getIcon("graphGrid.gif", "Grid on" );
    iGridOff = snns.icons.getIcon("graphNoGrid.gif", "Grid off" );
    bGrid = new FlatButton( iGridOn );
    bGrid.setToolTipText("Turn grid on");
    bGrid.addActionListener( this );
    panel.add( bGrid );

    bAutoFocus = new FlatButton( snns.icons.getIcon("Zoom16.gif", "Autofocus") );
    bAutoFocus.setToolTipText("Autofocus");
    bAutoFocus.addActionListener( this );
    panel.add( bAutoFocus );

    return panel;
  }
}

