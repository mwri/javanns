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
import java.awt.event.* ;
import java.awt.image.MemoryImageSource ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.util.Vector;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;


class WeightGraph extends DArea implements NetworkListener{
  Snns snns;
  WeightPanel panel;
  DImage image;
  NetworkViewSettings settings;
  Network network;


  public WeightGraph(Snns snns, WeightPanel panel ) {
    this.snns = snns;
    this.panel = panel;
    settings = snns.getCurrentSettings();
    network = snns.network;
    network.addListener( this );
    setAutoGrid( true );
    setGridToFront( true );
    setBackground( settings.background_color );
    setForeground( settings.text_color );

    int no_units = network.getNumberOfUnits();
    image = new DImage( .5, .5, no_units, no_units, this );
    addDElement( image );
    setVisibleRectangle( image );

    ScaledBorder sb = new ScaledBorder();
    sb.x_label = "source";
    sb.y_label = "target";
    sb.minimal_increment = 1;
    setBorder( sb );

    new DMouseZoom( this );
    update();
  }

  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.NETWORK_DELETED ) panel.frame.dispose();
    else if( evt.id == NetworkEvent.LINKS_CREATED ||
      evt.id == NetworkEvent.LINKS_DELETED ||
      evt.id == NetworkEvent.NETWORK_INITIALIZED ||
      evt.id == NetworkEvent.NETWORK_PRUNED ||
      evt.id == NetworkEvent.NETWORK_TRAINED ||
      evt.id == NetworkEvent.NEW_NETWORK_LOADED) update();
  }

  private synchronized void update(){
    if( visible_rect == null ) return;
    int no_units = network.getNumberOfUnits();
    int[] pix = new int[ no_units * no_units ];
    for( Unit t = network.getFirstUnit(); t != null; t = network.getNextUnit() ){
      Vector links = t.getAllIncomingLinks();
      for( int i=0; i<links.size(); i++ ){
        Link l = (Link)links.elementAt(i);
        Unit s = l.getSourceUnit();
        pix[ s.getNumber() + ( no_units - t.getNumber() ) * no_units - 1] =
          (255<<24) | settings.getColor( l.getWeight() ).getRGB();
      }
    }

    image.setImage( createImage( new MemoryImageSource( no_units, no_units, pix, 0, no_units ) ) );

    image.x = image.y = .5;
    image.width = image.height = no_units;
  }

  public void removeFromLists(){
    network.removeListener( this );
  }
}
