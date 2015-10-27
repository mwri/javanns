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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import java.text.NumberFormat;


public class LinkEdit extends JPanel implements NetworkListener, ActionListener{
  Snns snns;
  Network network;
  NetworkViewSettings settings;
  JInternalFrame frame;
  JLabel lSourceNo, lTargetNo;
  FlatButton bSourceDown, bSourceUp, bTargetDown, bTargetUp, bDelete;
  JTextField tfWeight;
  Link link;
  Insets insets;
  Rectangle rSource, rTarget;

  public LinkEdit(Snns snns) throws Exception{
    this.snns = snns;
    settings = snns.getCurrentSettings();
    network = snns.network;
    checkUnits();
    network.addListener( this );

    insets = snns.panel_insets;

    addMouseListener(
      new MouseAdapter(){
        public void mouseClicked( MouseEvent evt ){
          if( ( evt.getModifiers() &  InputEvent.BUTTON1_MASK ) == 0 )
            mouseOn( evt.getPoint() );
        }
      }
    );
    setLayout( null );

    JLabel lSource = new JLabel("Source"), lTarget = new JLabel("Target");
    addComp( lSource );
    addComp( lTarget );

    int max_unit = network.getNumberOfUnits(), h, w;

    lSourceNo = new JLabel( String.valueOf( max_unit ), SwingConstants.CENTER );
    lSourceNo.setToolTipText("Number of the source unit");
    addComp( lSourceNo );

    lTargetNo = new JLabel( String.valueOf( max_unit ), SwingConstants.CENTER );
    lTargetNo.setToolTipText("Number of the target unit");
    addComp( lTargetNo );

    tfWeight = new JTextField( 12 );
    tfWeight.setToolTipText("Weight of the link");
    tfWeight.addKeyListener(
        new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == e.VK_ENTER) toNet();
          }
        }
      );
    addComp( tfWeight );

    setButtons();
    Point p = new Point( insets.left, insets.top + lSource.getHeight() + settings.height), q;
    q = moveComp( bSourceDown, p );
    q = moveComp( lSourceNo, q.x, p.y );
    q = moveComp( bSourceUp, q.x, p.y );
    q.x += tfWeight.getWidth() / 4;
    q.y = p.y;
//    q.y = p.y - settings.height;
    q = moveComp( tfWeight, q );
    h = q.y + insets.bottom;
//    q.y = p.y - settings.height;
    q.y -= tfWeight.getHeight();
    q = moveComp( bDelete, q );
    p.x = q.x + tfWeight.getWidth() / 4;
    q = moveComp( bTargetDown, p );
    q = moveComp( lTargetNo, q.x, p.y );
    q = moveComp( bTargetUp, q.x, p.y );
    w = q.x + insets.right;

    setPreferredSize( new Dimension( w, h ) );
    Rectangle r = lSourceNo.getBounds();
    moveComp( lSource, r.x + ( r.width - lSource.getWidth() ) / 2, insets.top );
    lTargetNo.getBounds( r );
    moveComp( lTarget, r.x + ( r.width - lTarget.getWidth() ) / 2, insets.top );
    newLink();

    frame = new JInternalFrame("LinkEdit",false, true, false, true);
    frame.addInternalFrameListener(
      new InternalFrameAdapter(){
        public void internalFrameClosed( InternalFrameEvent evt ){
          removeFromLists();
        }
      }
    );
    frame.setContentPane( this );
    frame.pack();
  }

  public void paint( Graphics g ){
    super.paint(g);
    Dimension d = getSize();
    Color old_color = g.getColor();
    g.setColor( settings.getColor( link.getSourceUnit().getActivation() ) );
    Rectangle rSourceNo = lSourceNo.getBounds(), rWeight = tfWeight.getBounds();
    int dh = rSourceNo.x + ( rSourceNo.width - settings.width ) / 2 - insets.left;

    rSource = new Rectangle( insets.left + dh, rSourceNo.y - settings.height,
                             settings.width, settings.height );
    rTarget = new Rectangle( d.width - settings.width - dh - insets.right,
                             rSourceNo.y - settings.height,
                             settings.width, settings.height );

    g.fillRect( rSource.x, rSource.y, rSource.width, rSource.height );
    g.setColor( settings.getColor( link.getWeight() ) );
    g.drawLine( rSource.x + rSource.width, rSource.y + rSource.height / 2,
//                rWeight.x, rSource.y + rSource.height / 2 );
//    g.drawLine( rWeight.x + rWeight.width, rSource.y + rSource.height / 2,
                rTarget.x, rSource.y + rSource.height / 2 );
    g.setColor( settings.getColor( link.getTargetUnit().getActivation() ) );
    g.fillRect( rTarget.x, rTarget.y, rTarget.width, rTarget.height );
    g.setColor( old_color );
  }

  public static boolean linkSelected( Network net ){
    boolean selected = false;
    Unit[] us = net.getSelectedUnits();
    if( us == null ) return false;
    if( us.length != 2 ) return false;
    if( net.areConnected( us[0].getNumber(), us[1].getNumber() ) ) return true;
    else if( net.areConnected( us[1].getNumber(), us[0].getNumber() ) ) return true;
    return false;
  }

  public void actionPerformed(ActionEvent evt ){
    Object src = evt.getSource();
    if( src == bSourceDown ){
      Unit t = link.getTargetUnit();
      int s_no = link.getSourceUnit().getNumber();
      network.setCurrentUnit( link.getTargetUnit() );
      boolean found = false;
      while( s_no > 1 && !found ){
        s_no--;
        Unit s = network.getUnitNumber( s_no );
        if( network.isConnected( s ) ){
          found = true;
          Vector v = new Vector(2);
          v.addElement(s);
          v.addElement(t);
          network.selectUnits( v, true );
        }
      }
    }
    else if( src == bSourceUp ){
      Unit t = link.getTargetUnit();
      int s_no = link.getSourceUnit().getNumber(), max = network.getNumberOfUnits();
      network.setCurrentUnit( link.getTargetUnit() );
      boolean found = false;
      while( s_no < max && !found ){
        s_no++;
        Unit s = network.getUnitNumber( s_no );
        if( network.isConnected( s ) ){
          found = true;
          Vector v = new Vector(2);
          v.addElement(s);
          v.addElement(t);
          network.selectUnits( v, true );
        }
      }
    }
    else if( src == bTargetDown ){
      Unit s = link.getSourceUnit();
      int t_no = link.getTargetUnit().getNumber();
      boolean found = false;
      while( t_no > 1 && !found ){
        t_no--;
        Unit t = network.getUnitNumber( t_no );
        if( network.areConnected( s, t ) ){
          found = true;
          Vector v = new Vector(2);
          v.addElement(s);
          v.addElement(t);
          network.selectUnits( v, true );
        }
      }
    }
    else if( src == bTargetUp ){
      Unit s = link.getSourceUnit();
      int t_no = link.getTargetUnit().getNumber(), max = network.getNumberOfUnits();
      boolean found = false;
      while( t_no < max && !found ){
        t_no++;
        Unit t = network.getUnitNumber( t_no );
        if( network.areConnected( s, t ) ){
          found = true;
          Vector v = new Vector(2);
          v.addElement(s);
          v.addElement(t);
          network.selectUnits( v, true );
        }
      }
    }
    else if( src == bDelete ){
      KernelInterface ki = network.ki;
      int s = link.getSourceUnit().getNumber(),
          t = link.getTargetUnit().getNumber(), no;
      LinkData old = link.delete();
      Vector v = new Vector(2);
      ki.setCurrentUnit( t );
      no = ki.getFirstPredUnit();
      if( no < 1 ){
        no = ki.getFirstSuccUnit( s );
        if( no < 1 ) close();
        else{
          v.add( new Unit(ki, s));
          v.add( new Unit(ki,no));
        }
      }
      else{
        v.add( new Unit(ki,no));
        v.add( new Unit(ki, t));
      }
      if( no > 0 ){
        link = network.getLink( (Unit)v.get(0), (Unit)v.get(1) );
        network.selectUnits( v, true );
        network.fireEvent( NetworkEvent.LINKS_DELETED, new LinkData[]{ old } );
      }
    }
  }

  public void networkChanged(NetworkEvent evt){
    if(evt.id == NetworkEvent.LINKS_DELETED ||
       evt.id == NetworkEvent.NETWORK_DELETED ||
       evt.id == NetworkEvent.NETWORK_INITIALIZED ||
       evt.id == NetworkEvent.NETWORK_PRUNED ||
       evt.id == NetworkEvent.NETWORK_TRAINED ||
       evt.id == NetworkEvent.NETWORK_UPDATED ||
       evt.id == NetworkEvent.NEW_NETWORK_LOADED ||
       evt.id == NetworkEvent.UNIT_VALUES_EDITED ||
       evt.id == NetworkEvent.UNITS_DELETED ||
       evt.id == NetworkEvent.SELECTED_UNITS_CHANGED){
      if( checkUnits() ) newLink();
      else close();
    }
  }

  private void setButtons(){
    Icon icon = snns.icons.getIcon("leftArrow-12.gif");
    bSourceDown = new FlatButton( icon );
    bSourceDown.setToolTipText("Next lower source unit");
    bSourceDown.addActionListener( this );
    addComp( bSourceDown );

    bTargetDown = new FlatButton( icon );
    bTargetDown.setToolTipText("Next lower target unit");
    bTargetDown.addActionListener( this );
    addComp( bTargetDown );

    icon = snns.icons.getIcon("rightArrow-12.gif");
    bSourceUp = new FlatButton( icon );
    bSourceUp.setToolTipText("Next higher source unit");
    bSourceUp.addActionListener( this );
    addComp( bSourceUp );

    bTargetUp = new FlatButton( icon );
    bTargetUp.setToolTipText("Next higher target unit");
    bTargetUp.addActionListener( this );
    addComp( bTargetUp );

    icon = snns.icons.getIcon("delete.gif");
    bDelete = new FlatButton( icon );
    bDelete.setToolTipText("Delete the currently selected link");
    bDelete.addActionListener( this );
    addComp( bDelete );
  }

  private void addComp( JComponent c ){
    c.setSize( c.getPreferredSize() );
    add( c );
  }

  private Point moveComp( JComponent c, int x, int y ){
    return moveComp( c, new Point( x, y ) );
  }

  private Point moveComp( JComponent c, Point p ){
    c.setLocation( p );
    Point p2 = (Point)p.clone();
    p2.x += c.getWidth();
    p2.y += c.getHeight();
    return p2;
  }

  private void removeFromLists(){
    network.removeListener( this );
  }

  private boolean checkUnits(){
    Unit[] us = network.getSelectedUnits();
    if( !linkSelected( network ) ) return false;
    if( network.areConnected( us[0].getNumber(), us[1].getNumber() ) )
      link = network.getLink( us[0], us[1] );
    else link = network.getLink( us[1], us[0] );
    return true;
  }

  private void newLink(){
    lSourceNo.setText( String.valueOf( link.getSourceUnit().getNumber() ) );

// etwas aufwaendig zwar
// Locale.ENGLISH ist noetig, da er sonst ein Komma anstelle eines Punkts
// macht und er dann mault, wenn man Eintraege modifiziert
    NumberFormat format = NumberFormat.getNumberInstance( java.util.Locale.ENGLISH );
// standardmaessig sind 3 eingestellt, von daher:
    format.setMaximumFractionDigits(6);
    tfWeight.setText( format.format( link.getWeight() ) );

    lTargetNo.setText( String.valueOf( link.getTargetUnit().getNumber() ) );
    repaint();
  }

  private void toNet(){
    double w = 0;
    try{ w = Double.valueOf( tfWeight.getText() ).doubleValue(); }
    catch( Exception e ){
      snns.showException( new Exception("Weight has to be a double value"), this );
      return;
    }
    link.setWeight( w );
    network.fireEvent( NetworkEvent.UNIT_VALUES_EDITED );
  }

  private void mouseOn( Point p ){
    Unit[] u = new Unit[1];
    if( rSource.contains( p ) ) u[0] = link.getSourceUnit();
    else if( rTarget.contains( p ) ) u[0] = link.getTargetUnit();
    if( u[0] != null ) snns.unitDetail.showDetails( u );
  }

  private void close(){
    removeFromLists();
    frame.dispose();
  }
}