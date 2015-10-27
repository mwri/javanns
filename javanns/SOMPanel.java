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

import java.awt.* ;
//import java.awt.event.*;
import java.util.Vector ;

public class SOMPanel extends NetworkView/* implements ActionListener*/{
  SOMDistance SOMD;

  public SOMPanel( Snns snns, UIDisplayType dt ){
    super( snns, dt );
    SOMD = new SOMDistance( snns );
    settings.show_links = false;
    settings.top_label_type = NetworkViewSettings.NOTHING;
    settings.base_label_type = NetworkViewSettings.NOTHING;
    settings.grid_size = 30;
    //snns.master.lp.cFunction.addActionListener(this);
    setNetWidth();
  }

  public void paint( Graphics g ){
    super.paint( g );
    Color old_color = g.getColor();
    int unit_no = network.getNumberOfUnits();
    for( int i=0; i<unit_no; i++ ) paint( i+1, g );
    g.setColor( old_color );
  }

  public void networkChanged(NetworkEvent evt){
    super.networkChanged(evt);
    if( evt.id == NetworkEvent.UNITS_CREATED ||
        evt.id == NetworkEvent.NEW_NETWORK_LOADED ||
        evt.id == NetworkEvent.UNITS_MOVED ) setNetWidth();
  }

 /* public void actionPerformed(ActionEvent evt){
    setNetWidth();
  }

  protected void close(){
    snns.master.lp.cFunction.removeActionListener(this);
    super.close();
  }*/

  private void paint( int no, Graphics g ){
    Unit u = network.getUnitNumber( no );
    int[] pos = u.getPosition();
    double[] d = SOMD.getDist( no );
    double v = 0; int ct = 0;
    for( int i=0; i<4; i++ ) if( d[i] > -1 ) { v += d[i] / settings.dist_max; ct++; }
    if( ct > 0 )
      drawNeuron( g, network.getUnitNumber( no ), null, settings.getColor( v / ct ) );
    int x = X(pos[0]), y = Y(pos[1]);
    if( d[0] > -1 ){
      g.setColor( settings.getColor( d[0] / settings.dist_max ) );
      g.fillRect( x + (settings.width>>1), y - (settings.height>>1),
                settings.grid_size - settings.width, settings.height );
    }
    if( d[1] > -1 ){
      g.setColor( settings.getColor( d[1] / settings.dist_max ) );
      g.fillRect( x - (settings.width>>1), y + (settings.height>>1),
                settings.width, settings.grid_size - settings.height );
    }
    if( d[0] > -1 && d[1] > -1 ){
      g.setColor( settings.getColor( .5*(d[0] + d[1]) / settings.dist_max ) );
      g.fillRect( x + (settings.width>>1), y + (settings.height>>1),
                  (settings.grid_size - settings.width)>>1,
                  (settings.grid_size - settings.height)>>1);
    }
    if( d[1] > -1 && d[2] > -1 ){
      g.setColor( settings.getColor( .5*(d[1] + d[2]) / settings.dist_max ) );
      g.fillRect( x - (settings.grid_size>>1), y + (settings.height>>1),
                  (settings.grid_size - settings.width)>>1,
                  (settings.grid_size - settings.height)>>1);
    }
    if( d[2] > -1 && d[3] > -1 ){
      g.setColor( settings.getColor( .5*(d[2] + d[3]) / settings.dist_max ) );
      g.fillRect( x - (settings.grid_size>>1), y - (settings.grid_size>>1),
                  (settings.grid_size - settings.width)>>1,
                  (settings.grid_size - settings.height)>>1);
    }
    if( d[3] > -1 && d[0] > -1 ){
      g.setColor( settings.getColor( .5*(d[3] + d[0]) / settings.dist_max ) );
      g.fillRect( x + (settings.width>>1), y - (settings.grid_size>>1),
                  (settings.grid_size - settings.width)>>1,
                  (settings.grid_size - settings.height)>>1);
    }
  }

  private void setNetWidth(){
    if( snns.master.lp.getFunction().kernel_name.equals("Kohonen") ){
      boolean first = true;
      int min = 0, max = 0;
      Unit[] us = network.getAllUnits();
      for( int i=0; i<us.length; i++ ){
        int type = us[i].getType();
        if( type == UnitTTypes.HIDDEN || type == UnitTTypes.SPECIAL_H ){
          int[] pos = us[i].getPosition();
          if( first ){ min = max = pos[0]; first = false; }
          else{
            if( pos[0] < min ) min = pos[0];
            else if( pos[0] > max ) max = pos[0];
          }
        }
      }
      snns.master.lp.tParam[4].setText(String.valueOf(max-min+1));
    }
  }
}