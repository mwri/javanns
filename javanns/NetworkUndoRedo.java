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



import javax.swing.JMenuItem ;
import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;

/*--------------------- class declaration ------------------------------------*/
public class NetworkUndoRedo implements NetworkListener, ActionListener{
  Snns snns;
  private int steps, index = 0, firstIndex = 0, lastIndex = 0;
  private boolean listen = true;
  private NetworkEvent[] stack;
  private JMenuItem mUndo, mRedo;

/*---------------------- constructor -----------------------------------------*/
  public NetworkUndoRedo( Snns snns, int steps ) {
    this.snns = snns;
    snns.network.addListener( this );

    this.steps = steps;
    stack = new NetworkEvent[ steps ];
  }

/*---------------------- public methods --------------------------------------*/
  public void addMenus(JMenuItem mUndo, JMenuItem mRedo) {
    mUndo.addActionListener( this );
    mRedo.addActionListener( this );
    this.mUndo = mUndo;
    this.mRedo = mRedo;
  }

// implementing ActionListener :
  public void actionPerformed( ActionEvent evt ){
    NetworkEvent ne;
    if( evt.getSource() == mUndo ){
      index--;
      if( index == -1 ) index += steps;
      listen = false;
      ne = stack[ index ];
      try{ undo( ne ); }
      catch( Exception e ) {
        index++;
        index = index%steps;
        snns.showException(
          new Exception("Undo error: "+e.getMessage()+"\nwhile undoing:"+ne.getMessage() ),
          this
        );
      }
      listen = true;
      enableMenus( ( index != firstIndex ), true );
    }
    else{
      listen = false;
      ne = stack[ index ];
      try{
        redo( ne );
        index++;
        index = index%steps;
      }
      catch( Exception e ){
        snns.showException(
          new Exception("Redo error: "+e.getMessage()+"\nwhile redoing:"+ne.getMessage() ),
          this
        );
      }
      listen = true;
      enableMenus( true, ( index != lastIndex%steps ) );
    }
    setToolTipText();
  }

// implementing NetworkListener :
  public void networkChanged( NetworkEvent evt ){
    if( !listen ) return;
    if( evt.id == NetworkEvent.NEW_NETWORK_LOADED ){
      firstIndex = lastIndex = index = 0;
      enableMenus( false, false );
    }
    else if(evt.id == NetworkEvent.LINKS_CREATED ||
      evt.id == NetworkEvent.LINKS_DELETED ||
      evt.id == NetworkEvent.UNITS_CREATED ||
      evt.id == NetworkEvent.UNITS_DELETED ||
      evt.id == NetworkEvent.UNITS_MOVED ||
      evt.id == NetworkEvent.PATTERN_DELETED ||
      evt.id == NetworkEvent.PATTERN_MODIFIED ||
      evt.id == NetworkEvent.NETWORK_NAME_CHANGED ||
      evt.id == NetworkEvent.NETWORK_DELETED ){
      stack[ index ] = evt;
      index++;
      if( index == steps ) index = 0;
      if( index == firstIndex ) firstIndex++;
      lastIndex = index;
      enableMenus( true, false );
    }
    setToolTipText();
  }

/*-------------------- private methods ---------------------------------------*/

  private void undo( NetworkEvent evt ) throws Exception{
    Network network = evt.getNetwork();
    int type = evt.id;
    Object arg = evt.arg;

    switch( type ){
      case NetworkEvent.PATTERN_DELETED :
        network.createPattern( (Pattern)arg );
        break;
      case NetworkEvent.PATTERN_MODIFIED:
        evt.arg = network.replacePattern( (Pattern)arg );
        break;
      case NetworkEvent.LINKS_CREATED   :
        evt.arg = network.deleteLinks( (Link[])arg );
        break;
      case NetworkEvent.LINKS_DELETED   :
        evt.arg = network.createLinks( (LinkData[])arg );
        break;
      case NetworkEvent.UNITS_CREATED   :
        evt.arg = network.deleteUnits( (Unit[])arg );
        break;
      case NetworkEvent.UNITS_DELETED   :
        evt.arg = network.recreateUnits( (UnitDeleteArgument)arg );
        break;
      case NetworkEvent.UNITS_MOVED     :
        network.moveUnits( ( (UnitMoveArgument)arg ).inverse() );
        break;
      case NetworkEvent.NETWORK_NAME_CHANGED :
        evt.arg = network.setName( (String)arg );
        break;
      case NetworkEvent.NETWORK_DELETED :
        NetworkDeleteArgument nda = (NetworkDeleteArgument)arg;
        network.recreateUnits( nda.uda );
        network.setName( nda.old_name );
    }
  }

  private void redo( NetworkEvent evt ) throws Exception{
    Network network = evt.getNetwork();
    int type = evt.id;
    Object arg = evt.arg;

    switch( type ){
      case NetworkEvent.PATTERN_DELETED:
        network.deletePattern();
        break;
      case NetworkEvent.PATTERN_MODIFIED:
        evt.arg = network.replacePattern( (Pattern)arg );
        break;
      case NetworkEvent.LINKS_CREATED  :
        evt.arg = network.createLinks( (LinkData[])arg );
        break;
      case NetworkEvent.LINKS_DELETED  :
        evt.arg = network.deleteLinks( (Link[])arg );
        break;
      case NetworkEvent.UNITS_CREATED  :
        evt.arg = network.recreateUnits( (UnitDeleteArgument)arg );
        break;
      case NetworkEvent.UNITS_DELETED  :
        evt.arg = network.deleteUnits( (Unit[])arg );
        break;
      case NetworkEvent.UNITS_MOVED    :
        network.moveUnits( (UnitMoveArgument)arg );
        break;
      case NetworkEvent.NETWORK_NAME_CHANGED :
        evt.arg = network.setName( (String)arg );
        break;
      case NetworkEvent.NETWORK_DELETED :
        evt.arg = network.deleteNetwork();
    }
  }

  private void enableMenus( boolean undo, boolean redo ){
    mUndo.setEnabled( undo );
    mRedo.setEnabled( redo );
  }

  private void setToolTipText(){
    NetworkEvent evt = stack[ ( index + steps - 1 )%steps ];
    if( evt != null ) mUndo.setToolTipText( evt.getMessage() );
    evt = stack[ index ];
    if( evt != null ) mRedo.setToolTipText( evt.getMessage() );
  }
}