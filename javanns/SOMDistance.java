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

import java.util.Vector ;

public class SOMDistance implements NetworkListener{
  Snns snns;
  Network network;
  KernelInterface ki;
  private double[][] dist;
  private int[] left_neig, upper_neig;
  private boolean[] done;

  public SOMDistance( Snns snns ) {
    this.snns = snns;
    network = snns.network;
    ki = network.ki;
    network.addListener( this );
    calc();
  }

  public double[] getDist( int no ){
    double[] d = new double[ 4 ];
    int ln = left_neig[ no - 1 ], un = upper_neig[ no - 1 ];
    d[0] = dist[ no - 1 ][0]; // right
    d[1] = dist[ no - 1 ][1]; // bottom
    if( ln > 0 ) d[2] = dist[ln-1][0]; // left
    else d[2] = -1;
    if( un > 0 ) d[3] = dist[un-1][1]; // top
    else d[3] = -1;
    return d;
  }

  public void networkChanged(NetworkEvent evt){
    if(evt.id == NetworkEvent.NETWORK_DELETED ||
       evt.id == NetworkEvent.NETWORK_INITIALIZED ||
       evt.id == NetworkEvent.LINKS_CREATED ||
       evt.id == NetworkEvent.LINKS_DELETED ||
       evt.id == NetworkEvent.NETWORK_PRUNED ||
       evt.id == NetworkEvent.NETWORK_TRAINED ||
       evt.id == NetworkEvent.NEW_NETWORK_LOADED ||
       evt.id == NetworkEvent.UNITS_CREATED ||
       evt.id == NetworkEvent.UNITS_DELETED ||
       evt.id == NetworkEvent.UNITS_MOVED  )
      calc();
  }

  private void calc(){
    int unit_no = ki.getNoOfUnits();
    dist = new double[ unit_no ][ 2 ];
    left_neig = new int[ unit_no ];
    upper_neig = new int[ unit_no ];
    done = new boolean[ unit_no ];
    for( int i=0; i<unit_no; i++ ){
      if( !done[i] ){
        double[] w = weightsTo( i + 1 );
        calcAround( i + 1, w );
      }
    }
    /*
    for( int i=0; i<unit_no; i++ )
      for( int j=0; j<2; j++)
        System.out.println("w["+(i+1)+"]["+j+"]="+dist[i][j]);
    */
  }
  private void calcAround( int no, double[] w1 ){
    if( done[ no - 1 ] ) return;
    done[ no - 1 ] = true;
    ki.getUnitPosition( no );
    ki.posX++;
    int sn = ki.getUnitSubnetNo( no ),
        no2 = ki.getUnitNoAtPosition( sn );
    if( no2 > 0 ){
      double[] w2 = weightsTo( no2 );
      dist[ no - 1 ][0] = getDist( w1, w2 );
      left_neig[ no2 - 1 ] = no;
      calcAround( no2, w2 );
      ki.getUnitPosition( no );
    }
    else {
      ki.posX--;
      dist[ no - 1 ][ 0 ] = -1;
    }

    ki.posY++;
    no2 = ki.getUnitNoAtPosition( sn );
    if( no2 > 0 ){
      double[] w2 = weightsTo( no2 );
      dist[ no - 1 ][1] = getDist( w1, w2 );
      upper_neig[ no2 - 1 ] = no;
      calcAround( no2, w2 );
    }
    else dist[ no - 1 ][ 1 ] = -1;
  }

  private double getDist( double[] w1, double[] w2 ){
    double d = 0;
    for( int i=0; i<w1.length; i++ ) d += (w1[i] - w2[i])*(w1[i] - w2[i]);
    return Math.sqrt( d );
  }

  private double[] weightsTo( int no ){
    int unit_no = ki.getNoOfUnits();
    double[] w = new double[ unit_no ];
    ki.setCurrentUnit( no );
    for( int n2 = ki.getFirstPredUnit(); n2 != 0; n2 = ki.getNextPredUnit() )
      w[ n2 - 1 ] = ki.getLinkWeight();
    return w;
  }
}