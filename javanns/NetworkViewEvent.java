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



import javax.swing.JInternalFrame;

public class NetworkViewEvent extends java.awt.Event {

  public static final int VIEW_CLOSED = 0,
                          VIEW_ACTIVATED = 1,
                          VIEW_DEACTIVATED = 2,
                          SETTINGS_CHANGED = 3;

  public NetworkViewEvent( NetworkView view, int type ) {
    super( view, type, null );
  }
  public NetworkView getView(){ return (NetworkView)target; }
  public String getMessage(){
    int no = getView().net_view_no;
    String text = "Undefined NetworkViewEvent";
    switch( id ){
      case VIEW_CLOSED      : text = "NetworkView["+no+"] has been closed"; break;
      case VIEW_ACTIVATED   : text = "NetworkView["+no+"] has been activated"; break;
      case VIEW_DEACTIVATED : text = "NetworkView["+no+"] has been deactivated"; break;
      case SETTINGS_CHANGED : text = "The settings of NetworkView["+no+"] have been changed";
    }
    return text;
  }
}