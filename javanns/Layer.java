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


/*----------------------------- imports --------------------------------------*/
import java.util.Vector ;
/*------------------------ class declaration ---------------------------------*/

/**
 * this class represents one layer of the network in the kernel
 *
 * it gets infos about net architecture changes by the layers object
 * and tells this object if it is a hidden layer or when all its units have
 * been deleted
 */
public class Layer{
  Layers layers;
  int number;
  private Vector units = new Vector();
  /**
   * constructor with the Layers class and the layer number as parameter
   * This layer sends the information that it is empty back to the Layers class
   */
  public Layer( Layers layers, int number ) {
    this.layers = layers;
    //network = layers.network;

    this.number = number;
    //lookoutForUnits();
    //            ACHTUNG :
    // Layer kriegt nur noch die nach seiner Konstruktion erstellten Units mit !
    // Es gibt sonst Duplikate in einer Layer, da bei einem
    // NetworkEvent.UNITS_CREATED nicht überprüft wird, ob die Unit schon
    // registriert war!
  }
/*----------------------- public methods -------------------------------------*/
  /**
   * method returns the units belonging to this layer
   *
   * @return array of units
   */
  public Unit[] getUnits(){
    Unit[] us = new Unit[ units.size() ];
    for( int i=0; i<us.length; i++ ) us[i] = (Unit)units.elementAt(i);
    return us;
  }

  public boolean contains( Unit u ){
    //System.out.println("Layer.contains(Unit)");
    return ( indexOf( u ) != -1 );
  }
  /**
   * this method removes the given unit from the representation of the net in
   * this layer object not from the net in the kernel!!
   *
   * @param u the unit to remove from the layer object
   * @return if this was possible
   */
  public boolean removeUnit( Unit u ){
    int no = indexOf( u );
    if( no == -1 ) return false;
    units.removeElementAt( no );
    if( units.size() == 0 ) layers.emptyLayer( this );
    return true;
  }

  /**
   * this method adds a unit to the layer object not to the net in the kernel
   *
   * @param u the unit to add
   */
  public void addUnit( Unit u ){
    if( u.getLayer() == number && !contains( u ) )
      units.addElement( u );
  }

  /**
   * return whether the layer is hidden, that means all units contained are
   * hidden ( this method is used by kohonen networks to get to know how many
   * hidden layers exist )
   *
   * @return whether the layer is hidden or not
   */
  public boolean isHiddenLayer(){
    for( int i=0; i<units.size(); i++ ){
      Unit u = (Unit)units.get(i);
      if( u.getType() != UnitTTypes.HIDDEN && u.getType() != UnitTTypes.SPECIAL_H )
        return false;
    }
    return true;
  }
/*----------------------- private methods ------------------------------------*/
  /*private void lookoutForUnits(){
    units.removeAllElements();
    for( Unit unit = network.getFirstUnit();
         unit != null;
         unit = network.getNextUnit() ){
      if( unit.getLayer() == number ) units.addElement( unit );
    }
  }*/

  private int indexOf( Unit u ){
    return indexOf( u.number );
  }

  private int indexOf( int unit_number ){
    for( int i=0; i<units.size(); i++ )
      if( unit_number == ((Unit)units.elementAt(i)).number ) return i;
    return -1;
  }
  /**
   * not really the implementation of the NetworkListener interface
   * the Layers object gets the information from the net and decides whether
   * the layer should be informed
   *
   * @param evt the NetworkEvent
   */
  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.UNITS_CREATED ){
      Unit[] us = (Unit[])evt.arg;
      for( int i=0; i<us.length; i++ )
        if( us[i].getLayer() == number )
          units.addElement( us[i] );
    }

    else if( evt.id == NetworkEvent.UNITS_DELETED ){
      UnitDeleteArgument uda = (UnitDeleteArgument)evt.arg;
      UnitData[] data = uda.uData;
      Unit unit;
      int no, size = units.size();

      for( int i=0; i<data.length; i++ ){
        no = data[i].number;
        for( int j=0; j<size; j++ ){
          unit = (Unit)units.elementAt( j );
          if( no == unit.number ){
            units.removeElementAt( j );
            j--;
            size--;
          }
          else if( no < unit.number ) {
            unit.number--;
          }
        }
      }
      if( units.size() == 0 ) layers.emptyLayer( this );
    }
  }
  /*
  public void showState(){
    System.out.print("Layer["+number+"]:");
    if( units.size() == 0 ){
      System.out.println(" ist leer ");
      return;
    }
    for( int i=0; i<units.size()-1; i++ )
      System.out.print( ((Unit)units.elementAt(i)).getNumber()+", ");
    System.out.println( ((Unit)units.elementAt(units.size()-1)).getNumber() );
  }
  */
}



