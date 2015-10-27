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
import java.awt.event.* ;
import java.awt.* ;

/*----------------------- class declaration ----------------------------------*/

/**
 * LayerPanel is the panel to construct a new layer of the network
 */
class LayerPanel extends JPanel implements ActionListener, NetworkListener{
  Snns snns;
  Network network;
  JButton bCreate, bClose;
  UnitDetailPanel details;
  LayerBoundsPanel bounds;
  JInternalFrame frame;

  public LayerPanel( Snns snns ){
    network = snns.network;
    network.addListener( this );

    bounds = new LayerBoundsPanel( snns.panel_insets );
    bounds.setXPos( network.getMaxXCoordinate() + 2 );

    details = new UnitDetailPanel( snns, false );
    details.setBorder( BorderFactory.createTitledBorder("Unit detail") );

    JPanel pOptions = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    pOptions.setLayout( gbl );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;
    gbc.weightx = 0.5;

    gbc.anchor = gbc.CENTER;
    bCreate = new JButton("Create");
    bCreate.addActionListener( this );
    gbl.setConstraints( bCreate, gbc );
    pOptions.add( bCreate );

    gbc.gridx = 1;
    bClose = new JButton("Close");
    bClose.addActionListener( this );
    gbl.setConstraints( bClose, gbc );
    pOptions.add( bClose );

    gbl = new GridBagLayout();
    setLayout( gbl );
    gbc.gridx = gbc.gridy = 0;

    gbc.fill = gbc.HORIZONTAL;
    gbl.setConstraints( bounds, gbc );
    add( bounds );

    gbc.gridy = 1;
    gbc.fill = gbc.NONE;
    gbl.setConstraints( details, gbc );
    add( details );

    gbc.gridy = 2;
    gbc.fill = gbc.HORIZONTAL;
    gbl.setConstraints( pOptions, gbc );
    add( pOptions );

    frame = new JInternalFrame("Create layers", false, true, false, true);
    frame.addInternalFrameListener(
      new InternalFrameAdapter(){
        public void internalFrameClosed(){
          removeFromLists();
        }
      }
    );
    frame.setContentPane( this );
    frame.pack();
  }
// implementing ActionListener :
  public void actionPerformed(ActionEvent evt){
    Object src = evt.getSource();
    if( src == bCreate ){
      int w, h, x, y, z, l, s;
      double bias, init_act;
      String name;
      try{ w = bounds.getWidth2(); }
      catch(Exception e){ showException( e ); return; }
      try{ h = bounds.getHeight2(); }
      catch(Exception e){ showException( e ); return; }
      try{ x = bounds.getXPos(); }
      catch(Exception e){ showException( e ); return; }
      try{ y = bounds.getYPos(); }
      catch(Exception e){ showException( e ); return; }
      try{ z = bounds.getZPos(); }
      catch(Exception e){ showException( e ); return; }
      try{ bias = details.getBias(); }
      catch(Exception e){ showException( e ); return; }
      try{ init_act = details.getInitAct(); }
      catch(Exception e){ showException( e ); return; }
      try{ l = details.getLayer(); }
      catch(Exception e){ showException( e ); return; }
      try{ s = details.getSubnet(); }
      catch(Exception e){ showException( e ); return; }

      if( w < 1 ) {
        showException( new Exception("Width has to be bigger than 1") );
        return;
      }
      if( h < 1 ) {
        showException( new Exception("Height has to be bigger than 1") );
        return ;
      }

      UnitData[] data = new UnitData[ w * h ];
      int[] pos;
      String act_name = details.getActFnName(),
             out_name = details.getOutFnName();
      int type = details.getType();
      for( int i=0; i<w; i++ )
        for( int j=0; j<h; j++ ){
          pos = new int[]{ x + i, y + j, z };
          data[ i + j * w ] = new UnitData( "noName", type, act_name, out_name,
                                            bias, init_act, pos, l, s );
        }
      network.createUnits( data );
    }
    else if( src == bClose ) {
      removeFromLists();
      frame.dispose();
    }
  }
// implementing NetworkListener:
  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.UNITS_CREATED ||
      evt.id == NetworkEvent.UNITS_DELETED ||
      evt.id == NetworkEvent.UNITS_MOVED){
      Network net = evt.getNetwork();
      if(net.under_construction) System.out.println( "LayerPanel.networkChanged" );
      bounds.setXPos( net.getMaxXCoordinate() + 1 );
      details.setLayer( net.getMaxLayerNo() + 1 );
    }
  }
/*---------------------------- private methods -------------------------------*/

  private void removeFromLists(){
    network.removeListener( this );
  }
  private void showException( Exception e ){
    snns.showException( e, this );
  }
}

