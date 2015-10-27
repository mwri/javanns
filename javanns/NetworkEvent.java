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



/*------------------------- imports ------------------------------------------*/
import java.awt.Event ;

/*----------------------- class declaration ----------------------------------*/
public class NetworkEvent extends Event{

  /**
   * ids of the events
   */
  public static final short NEW_NETWORK_LOADED = 0,
                          NETWORK_INITIALIZED = 1,
                          NETWORK_UPDATED = 2,
                          NETWORK_TRAINED = 3,
                          UNITS_MOVED = 4,
                          SELECTED_UNITS_CHANGED = 5,
                          UNITS_CREATED = 6,
                          UNITS_DELETED = 7,
                          LINKS_CREATED = 8,
                          LINKS_DELETED = 9,
                          SOURCE_UNITS_CHANGED = 10,
                          PATTERN_DELETED = 11,
                          NETWORK_DELETED = 12,
                          PATTERN_SET_CHANGED = 13,
                          UNIT_VALUES_EDITED = 14,
                          NETWORK_NAME_CHANGED = 15,
                          PATTERN_MODIFIED = 16,
                          PATTERN_CHANGED = 17,
                          PATTERN_SET_LOADED = 18,
                          PATTERN_CREATED = 19,
                          PATTERN_SET_CREATED = 20,
                          NETWORK_PRUNED = 21,
                          LONG_TASK_STARTED = 22,
                          LONG_TASK_OVER = 23,
                          SUBPATTERN_CHANGED = 24,
                          TRAINING_DETAILS = 25;

/*----------------------- constructors ---------------------------------------*/
  public NetworkEvent( Network network, int type ) {
    this( network, type, null );
  }

  public NetworkEvent( Network network, int type, Object arg ){
    super( network, type, arg );
  }

/*----------------------- public methods -------------------------------------*/

  public Network getNetwork(){ return (Network)target; }

  public String toString(){ return getMessage(); }

  public String getMessage(){
    String text = getMessage( id );
    switch( id ){
      case NEW_NETWORK_LOADED :
      case PATTERN_SET_LOADED : text += ": "+arg;
    }
    return text;
  }

  public static String getMessage( int type ){
    switch( type ){
      case NEW_NETWORK_LOADED     : return "New network loaded";
      case NETWORK_INITIALIZED    : return "Network initialized";
      case NETWORK_UPDATED        : return "Network updated";
      case NETWORK_TRAINED        : return "Network trained";
      case UNITS_MOVED            : return "Units moved" ;
      case SELECTED_UNITS_CHANGED : return "Selected units changed" ;
      case UNITS_CREATED          : return "List of units created" ;
      case UNITS_DELETED          : return "List of units deleted" ;
      case LINKS_CREATED          : return "List of links created" ;
      case LINKS_DELETED          : return "List of links deleted" ;
      case SOURCE_UNITS_CHANGED   : return "Selected source units changed" ;
      case PATTERN_DELETED        : return "The current pattern has been deleted";
      case NETWORK_DELETED        : return "Network was deleted";
      case PATTERN_SET_CHANGED    : return "Pattern set has changed";
      case UNIT_VALUES_EDITED     : return "Some values of units have been changed";
      case NETWORK_NAME_CHANGED   : return "Name of the network changed";
      case PATTERN_MODIFIED       : return "The current pattern has been modified";
      case PATTERN_CHANGED        : return "The current pattern has changed";
      case PATTERN_SET_LOADED     : return "A new pattern set has been loaded";
      //case PATTERN_SET_DELETED    : return "The current pattern set has been deleted";
      case PATTERN_CREATED        : return "A new pattern has been created";
      case PATTERN_SET_CREATED    : return "A new pattern set has been created";
      case SUBPATTERN_CHANGED     : return "The current subpattern has changed";
      //case LOG_DELETED            : return "The log panel has be wished out";
      default                     : return "";
    }
  }
}

