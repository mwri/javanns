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

import javax.swing.* ;
import javax.swing.event.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.MemoryImageSource ;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;


/*------------------- class declaration --------------------------------------*/
class ProjectionGraph extends DArea{
  ProjectionPanel panel;
  Network network;
  NetworkViewSettings settings;
  Unit[] input = new Unit[2];
  Unit target;
  int res = 8;
  DImage image;
  ScaledBorder border;

  public ProjectionGraph( ProjectionPanel panel ){
    super( 1 );
    //System.out.println("ProjectionGraph(PP)");

    this.panel = panel;
    settings = panel.settings;
    network = panel.network;
    Unit[] units = network.getSelectedUnits();

    setBackground( settings.background_color );
    setForeground( settings.text_color );

    int i, pos = 0, type;
    for( i=0; i<3; i++ ){
      type = units[i].getType();
      if( type == UnitTTypes.INPUT ) input[pos++] = units[i];
      else if( type == UnitTTypes.HIDDEN ||
               type == UnitTTypes.OUTPUT ) target = units[i];
    }

    image = new DImage( -1, -1, 2, 2, this ){
      public void paint( DMeasures m ){
        if( !equals( m.getDRectangle() ) ) update();
        super.paint( m );
      }
    };
    addDElement( image );
    setVisibleRectangle( image );

    border = new ScaledBorder();
    border.x_label = input[0].getName() + " [" + input[0].getNumber() + "]" ;
    border.y_label = input[1].getName() + " [" + input[1].getNumber() + "]" ;
    border.show_arrows = false;
    setBorder( border );

    new DMouseZoom( this );

    update();
  }

  /**
   * updates the graph when settings changed
   */
  public void update(){
    //System.out.println("ProjectionGraph.update()");
    if( visible_rect == null ) return;
    int index = 0;
    int[] pix = new int[ res * res ];
    for( int v=0; v<res; v++ )  {
      for( int h=0;  h<res; h++ )  {
        input[0].setActivation( visible_rect.x + visible_rect.width * ( h + 0.5 ) / res );
        input[1].setActivation( visible_rect.y + visible_rect.height * ( 1 - ( v + 0.5 ) / res ) );
        try{ network.updateNet(); }
        catch( Exception e ){ panel.close( e, this ); }
        pix[index++] = (255<<24) | settings.getColor( target.getActivation() ).getRGB();
      }
    }
    image.setImage( createImage( new MemoryImageSource( res, res, pix, 0, res ) ) );

    image.x = visible_rect.x;
    image.y = visible_rect.y;
    image.width = visible_rect.width;
    image.height = visible_rect.height;
  }

  public void setUnits( Unit[] input, Unit target ) throws Exception{
    if( input.length != 2 ||
        input[0].getType() != UnitTTypes.INPUT ||
        input[1].getType() != UnitTTypes.INPUT ||
      ( target.getType() != UnitTTypes.HIDDEN && target.getType() != UnitTTypes.OUTPUT )
    )
      throw new Exception("Projection Error : Wrong unit selection");

    this.input = input;
    this.target = target;
    border.x_label = input[0].getName() + " [" + input[0].getNumber() + "]" ;
    border.y_label = input[1].getName() + " [" + input[1].getNumber() + "]" ;
    update();
  }
}
