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
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File ;  // for separator in Patterns

/*----------------- class declaration ----------------------------------------*/
class UpdatePanel extends ControlPanel implements NetworkListener {
  JLabel lChanger;
  FlatButton bFirst, bPrev, bNext, bLast;
  //JButton bCurrent;
  JTextField tfCurrent;

  public UpdatePanel( MasterControl master ){
    super(master, "Updating function: ", Function.UPDATE);
    master.up = this;

    Network network = master.network;
    network.addListener( this );

//    lChanger = new JLabel("Select Pattern:");
//    add( lChanger );
//    p = movePref( lChanger, x_coord[0], y );

    y += 4;

    bLast = new FlatButton(snns.icons.getIcon("rightEndArrow.gif", "Last"));
    add(bLast);
    bLast.addActionListener(this);
    bLast.setToolTipText("Update network with the last pattern");
    p = movePrefRevX(bLast, 1, y);

    bNext = new FlatButton(snns.icons.getIcon("rightArrow.gif", "Next"));
    add(bNext);
    bNext.addActionListener(this);
    bNext.setToolTipText("Update network with the next pattern");
    p = movePrefRevX(bNext, p.x, y);

    tfCurrent = new JTextField( 4 );
    tfCurrent.setHorizontalAlignment(JTextField.CENTER);
    tfCurrent.addMouseListener(
      new MouseAdapter(){
        public void mouseClicked(MouseEvent e){
          actionPerformed( new ActionEvent( tfCurrent, 0, "" ) );
        }
      }
    );
    if(network != null) tfCurrent.setText( String.valueOf(network.getCurrentPatternNo())) ;
    else tfCurrent.setText("0");
    Dimension d1 = tfCurrent.getPreferredSize();
    Dimension d2 = bLast.getPreferredSize();
    tfCurrent.setSize(new Dimension(d1.width, d2.height));
    tfCurrent.addActionListener(this);
    tfCurrent.setToolTipText("Update network with the current pattern");
    add(tfCurrent);
    p = moveRevX(tfCurrent, p.x, y);

    bPrev = new FlatButton(snns.icons.getIcon("leftArrow.gif", "Previous"));
    add(bPrev);
    bPrev.addActionListener(this);
    bPrev.setToolTipText("Update network with the previous pattern");
    p = movePrefRevX(bPrev, p.x, y);

    bFirst = new FlatButton(snns.icons.getIcon("leftEndArrow.gif", "First"));
    add(bFirst);
    bFirst.addActionListener(this);
    bFirst.setToolTipText("Update network with the first pattern");
    p = movePrefRevX(bFirst, p.x, y);

    newNet();
  }

  /**
   * Event handler for UpdatePanel.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    Network network = master.network;
    Object src = e.getSource();
    if(src == bFirst) {
      try{
        network.setFirstPattern(true);
        network.setFunction(getFunction(), getParameters() );
        network.updateNet();
      }
      catch( Exception ex ){ showException( ex ); }
    }
    else if(src == bPrev) {
      try{
        network.setPreviousPattern(true);
        network.setFunction(getFunction(), getParameters() );
        network.updateNet();
      }
      catch( Exception ex ){ showException( ex ); }
    }
    else if(src == tfCurrent) {
      int old = network.getCurrentPatternNo();
      boolean did_it = false;
      try{
        did_it = network.setPattern(Integer.parseInt(tfCurrent.getText()), true);
        if( did_it ){
          network.setFunction(getFunction(), getParameters() );
          network.updateNet();
        }
      }
      catch( Exception ex ){
        if( !( ex instanceof NumberFormatException ) )
          showException( ex );
      }
      if( !did_it ) tfCurrent.setText(String.valueOf(old));
    }
    else if(src == bNext) {
      try{
        network.setNextPattern(true);
        network.setFunction(getFunction(), getParameters() );
        network.updateNet();
      }
      catch( Exception ex ){ showException( ex ); }
    }
    else if(src == bLast) {
      try{
        network.setLastPattern(true);
        network.setFunction(getFunction(), getParameters() );
        network.updateNet();
      }
      catch( Exception ex ){ showException( ex ); }
    }
  }

  // implementing NetworkListener:
  /**
   * method changes the current number of the pattern when another pattern
   * was selected
   */
  public void networkChanged(NetworkEvent evt) {
    switch(evt.id){
      case NetworkEvent.NEW_NETWORK_LOADED  : newNet();
      case NetworkEvent.PATTERN_SET_CHANGED :
      case NetworkEvent.PATTERN_CHANGED     :
      case NetworkEvent.PATTERN_DELETED     :
      case NetworkEvent.NETWORK_DELETED     :
        tfCurrent.setText( String.valueOf(evt.getNetwork().getCurrentPatternNo())) ;
    }
    /*Network network = master.network;
    if(network != null) tfCurrent.setText( String.valueOf(network.getCurrentPatternNo())) ;
    else tfCurrent.setText("0");
    if( evt.id == NetworkEvent.NEW_NETWORK_LOADED ) newNet();*/
  }

  private void newNet(){
    if( master.network == null ) return;
    Function fn = master.network.getFunction( Function.UPDATE );
    cFunction.setSelectedItem( fn );
  }
}
