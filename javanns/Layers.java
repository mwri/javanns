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
 * this object represents the complete network
 * it seperates the units in different layers
 *
 * it gets the information about changes of the net architecture from the
 * NetworkListener interface
 */
class Layers implements NetworkListener{
  Network network;
  Vector layers = new Vector();
  private boolean[] layerNoExists;
  int maxLayerNo = 0;

  /**
   * constructor with the network as parameter
   * builds up the layers which exist in the network
   */
  public Layers( Network network ){
    this.network = network;
    network.addListener( this );
    lookoutForLayers();
  }

/*----------------------- public methods -------------------------------------*/
  /**
   * Method returns a certain layer
   *
   * @param the number of the layer
   * @return the layer or null if no layer of the given number exists
   */
  public Layer getLayerNo( int no ){
    if( layerNoExists.length < no || !layerNoExists[ no - 1 ] ) return null;
    boolean found = false;
    Layer layer = null;
    int i=0;
    while( !found ){
      layer = (Layer)layers.elementAt( i++ );
      if( layer.number == no ) found = true;
    }
    return layer;
  }

  /**
   * method the Layer class uses to give the information to the Layers class
   * that no units anymore left in the layer
   *
   * @param the empty layer
   */
  public void emptyLayer( Layer layer ){
    layers.removeElement( layer );
    layerNoExists[ layer.number - 1 ] = false;
    while( maxLayerNo > 0 && !layerNoExists[ maxLayerNo - 1 ] ) maxLayerNo--;
  }

  /**
   * method returns the hidden layers
   *
   * @return <code>java.util.Vector<\code> of hidden layers
   */
  public Vector getHiddenLayers(){
    Vector hidden = new Vector();
    for( int i=0; i<layers.size(); i++ ){
      Layer l = (Layer)layers.get(i);
      if( l.isHiddenLayer() ) hidden.add( l );
    }
    return hidden;
  }
/*--------------------- private methods --------------------------------------*/
  /**
   * method takes one unit after the other and puts them into their layers
   */
  private void lookoutForLayers(){
    layers.removeAllElements();
    layerNoExists = new boolean[5];
    int i, no, total = network.getNumberOfUnits();
    for( i=0; i<total; i++ ){
      no = network.getUnitNumber( i+1 ).getLayer();
      Layer layer = getLayerNo( no );
      if( layer == null ){
        if( layerNoExists.length < no ){
          boolean[] target = new boolean[ layerNoExists.length * 2 ];
          System.arraycopy( layerNoExists, 0, target, 0, layerNoExists.length );
          layerNoExists = target;
        }
        layer = new Layer( this, no );
        layers.addElement( layer );
        layerNoExists[ no - 1 ] = true;
        if( no > maxLayerNo ) maxLayerNo = no;
      }

      layer.addUnit(network.getUnitNumber(i+1));
    }
  }
/*----------------------- interfaces -----------------------------------------*/
  /**
   * method checks if the number of layers changed
   *
   * @param the network event, either
   * NetworkEvent.NETWORK_DELETED,
   * NetworkEvent.NEW_NETWORK_LOADED or
   * NetworkEvent.UNITS_CREATED
   */
  public void networkChanged( NetworkEvent evt ){

    if( evt.id == NetworkEvent.NETWORK_DELETED ){
      layers.removeAllElements();
      layerNoExists = new boolean[5];
      maxLayerNo = 0;
    }

    else if( evt.id == NetworkEvent.NEW_NETWORK_LOADED )
      lookoutForLayers();

    else if( evt.id == NetworkEvent.UNITS_CREATED ){
      Unit[] units = (Unit[])evt.arg;
      int i, no;
      for( i=0; i<units.length; i++ ){
        no = units[ i ].getLayer();
        if( layerNoExists.length < no ){
          boolean[] target = new boolean[ layerNoExists.length * 2 ];
          System.arraycopy( layerNoExists, 0, target, 0, layerNoExists.length );
          layerNoExists = target;
        }
        if( !layerNoExists[ no - 1 ] ){
          layers.addElement( new Layer( this, no ) );
          layerNoExists[ no - 1 ] = true;
          if( no > maxLayerNo ) maxLayerNo = no;
        }
      }
      for( i=0; i<layers.size(); i++ )
        ( (Layer)layers.elementAt(i) ).networkChanged( evt );
    }

    else if( evt.id == NetworkEvent.UNITS_DELETED ){
      for( int i=layers.size() - 1; i>-1; i-- )
        ( (Layer)layers.elementAt(i) ).networkChanged( evt );
    }

    else if( evt.id == NetworkEvent.UNIT_VALUES_EDITED ){
      Unit[] us;
      if( evt.arg != null ) us = (Unit[])evt.arg;
      else us = network.getAllUnits();
      int nno;
      Layer l;
      for( int i=0; i<us.length; i++ ){
        nno = us[i].getLayer();
        l = getLayerNo( nno );
        if( l == null || !l.contains( us[i] ) ){ // wurde in der Lage verschoben
          boolean found = false;
          int ono=0;
          while( ono<layers.size() && !found ){
            l = (Layer)layers.elementAt(ono);
            if( l.contains( us[i] ) ) found = true;
            else ono++;
          }
          if( found ) l.removeUnit( us[i] ); // aus der alten rausgeworfen
          l = getLayerNo( nno );
          if( l == null ) {
            l = new Layer( this, nno );
            layers.addElement( l );
            if( layerNoExists.length < nno ){
              boolean[] target = new boolean[ layerNoExists.length * 2 ];
              System.arraycopy( layerNoExists, 0, target, 0, layerNoExists.length );
              layerNoExists = target;
            }
            layerNoExists[nno-1] = true;
            if( nno > maxLayerNo ) maxLayerNo = nno;
          }
          l.addUnit( us[i] ); // in eine neue eingesetzt
        }
      }
    }
    //showState();
  }
/*
  private void showState(){
    System.out.println("max. Layer: " + maxLayerNo );
    Layer layer = null;
    boolean found = false;
    int i, j;
    for( i=0; i<layerNoExists.length; i++ ){
      if( layerNoExists[i] ){
        found = false;
        j=0;
        while ( !found ){
          layer = (Layer)layers.elementAt( j++ );
          if( layer.number == i+1 ) found = true;
        }
        layer.showState();
      }
    }
  }
*/
}
