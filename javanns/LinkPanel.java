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

public class LinkPanel extends JPanel implements ActionListener, NetworkListener{
  JInternalFrame frame;
  Snns snns;
  Network network;
  JRadioButton rbAutoAss, rbInter, rbNextLayer;
  JCheckBox cbShortcut, cbItself;
  JButton bOk, bCancel;
  InternalFrameListener frameListener
    = new InternalFrameAdapter(){
      public void internalFrameClosed( InternalFrameEvent evt ){
        removeFromLists();
      }
    };

  public LinkPanel( Snns snns ) {
    this.snns = snns;
    network = snns.network;
    network.addListener( this );
    setLayout( null );

    ButtonGroup bgMode = new ButtonGroup();

    rbInter = new JRadioButton("Connect selected units", true );
    rbInter.addActionListener( this );
    addComp( rbInter );
    bgMode.add( rbInter );

    rbNextLayer = new JRadioButton("Connect feed-forward", false );
    rbNextLayer.addActionListener( this );
    addComp( rbNextLayer );
    bgMode.add( rbNextLayer );

    cbShortcut = new JCheckBox("With shortcut connections", false );
    cbShortcut.setEnabled( false );
    addComp( cbShortcut );

    rbAutoAss = new JRadioButton("Auto-associative", false );
    rbAutoAss.addActionListener( this );
    addComp( rbAutoAss );
    bgMode.add( rbAutoAss );

    cbItself = new JCheckBox( "Allow self-connections", false );
    cbItself.setEnabled( false );
    addComp( cbItself );

    bOk = new JButton("Connect source with selected units");
    addComp( bOk );
    bOk.setText("Mark selected units as source");
    bOk.addActionListener( this );

    bCancel = new JButton("Close");
    addComp( bCancel );
    bCancel.addActionListener( this );

    int x1 = 20, dy = 5, width, height;
    Rectangle r = moveTo( rbInter, x1, 10 );
    dy += r.height;
    r = moveTo( rbNextLayer, x1, r.y + dy );
    r = moveTo( cbShortcut, 2 * x1, r.y + dy );
    r = moveTo( rbAutoAss, x1, r.y + dy );
    r = moveTo( cbItself, 2 * x1, r.y + dy);
    r = moveTo( bOk, x1, r.y + 2 * dy );
    width = r.x + r.width + x1;

    bCancel.getBounds( r );
    Rectangle r2 = bOk.getBounds();
    bCancel.setLocation( width - ( r.width + x1 ), r2.y + 2 * dy );
    bCancel.getBounds( r );
    height = r.y + r.height + 2 * dy;

    frame = new JInternalFrame("Create links", false, true, false, true );
    frame.setContentPane( this );
    frame.setSize( width, height );
  }

/*------------------------- private methods ----------------------------------*/
  private void removeFromLists(){
    network.removeListener( this );
  }

  private void reset(){
    rbInter.setEnabled( true );
    rbNextLayer.setEnabled( true );
    rbAutoAss.setEnabled( true );
    cbItself.setEnabled( true );
    rbInter.setSelected( true );
    bOk.setText("Mark selected units as source");
  }
/*----------------------------- interfaces -----------------------------------*/
// implementing ActionListener :
  public void actionPerformed( ActionEvent evt ){
    Object src = evt.getSource();

    if( src == rbInter )
      if( rbInter.isSelected() ){
        bOk.setText("Mark selected units as source");
        cbShortcut.setEnabled( false );
        cbItself.setEnabled( false );
      }

    if( src == rbAutoAss )
      if( rbAutoAss.isSelected() ) {
        bOk.setText("Connect selected units");
        cbShortcut.setEnabled( false );
        cbItself.setEnabled( true );
      }
    if( src == rbNextLayer )
      if( rbNextLayer.isSelected() ){
        bOk.setText("Connect");
        cbShortcut.setEnabled( true );
        cbItself.setEnabled( false );
      }
    if( src == bOk ){
      if( rbInter.isSelected() ){
        if( !network.sourceUnitsSelected() ){
          if( network.unitsSelected() ) {
            rbAutoAss.setEnabled( false );
            rbNextLayer.setEnabled( false );
            network.setSourceUnits();
            bOk.setText("Connect source with selected units");
          }
          else snns.showException( new Exception("No source units selected"), this );
        }
        else{
          if( network.unitsSelected() ) {
            network.createLinks();
            reset();
          }
          else snns.showException( new Exception("No target units selected"), this );
        }
      }

      else if( rbNextLayer.isSelected() )
        network.createLinks( cbShortcut.isSelected() );

      else if( rbAutoAss.isSelected() ){
        if( network.unitsSelected() )
          network.createLinks( network.getSelectedUnits(), cbItself.isSelected() );
        else snns.showException( new Exception("No units selected"), this );
      }
    }

    if( src == bCancel ){
      network.deselectUnits();
      network.deselectSourceUnits();
      removeFromLists();
      frame.dispose();
    }
  }

  // implemeting NetworkListener :
  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.NEW_NETWORK_LOADED ||
      evt.id == NetworkEvent.UNITS_DELETED ){
      removeFromLists();
      frame.dispose();
    }
  }

/*---------------------- private methods -------------------------------------*/
  private void addComp( JComponent c ){
    c.setSize( c.getPreferredSize() );
    c.setVisible( true );
    add( c );
  }
  private Rectangle moveTo( JComponent c, int x, int y ){
    c.setLocation( x, y );
    Rectangle r = c.getBounds( null );
    return r;
  }
}