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
import javax.swing.* ;
import javax.swing.event.* ;
import java.awt.* ;
import java.awt.event.* ;

/*--------------------- class declaration ------------------------------------*/
class Unit {
  public static final double DEFAULT_BIAS     = 0.0,
                             DEFAULT_INIT_ACT = 0.0,
                             DEFAULT_ACT      = 0.0;

  private KernelInterface ki;
  int number;

  /**
   * constructor for reference to the unit
   * does not create a new unit
   */
  public Unit( KernelInterface ki, int number ){
    this.ki = ki;
    this.number = number;
  }

  /**
   * constructor creates a new unit by unit data
   */
  public Unit( KernelInterface ki, UnitData data ){
    this.ki = ki;
    number = ki.createUnit( data.name,
                            data.out_fn_name,
                            data.act_fn_name,
                            data.init_act,
                            data.bias );
    ki.setUnitTType( number, data.type );
    ki.setUnitActivation( number, data.act );
    ki.setUnitLayerNo( number, data.layer );
    ki.setUnitSubnetNo( number, data.subnet );
    setPosition( data.pos );
  }
/*------------------------------- public methods -----------------------------*/
  public boolean equals( Object o ){
    if( !( o instanceof Unit ) ) return false;
    Unit u = (Unit)o;
    if( u.number == number ) return true;
    return false;
  }
  /**
   * method deletes the unit and returns its data for later renaissance
   *
   * @return UnitData
   */
  public UnitData delete(){
    UnitData data = new UnitData( this );
    ki.deleteUnitList( new int[] { number } );
    return data;
  }

  public void moveTo( int newNo ){
    if( newNo > ki.getNoOfUnits() ) return;
    Unit u = new Unit( ki, newNo );
    u.takeValuesFrom( new UnitData( this ) );
  }

  public void takeValuesFrom( UnitData data ){
    setActFnName( data.act_fn_name );
    setActivation( data.act );
    setBias( data.bias );
    setInitAct( data.init_act );
    setLayer( data.layer );
    setName( data.name );
    setOutFnName( data.out_fn_name );
    setPosition( data.pos );
    setSubnetNo( data.subnet );
    setType( data.type );
  }

  public int getNumber(){ return number; }

  public String getName(){ return ki.getUnitName( number ); }
  public void setName( String name ){ ki.setUnitName( number, name ); }

  public String getActFnName(){ return ki.getUnitActFuncName( number ); }
  public void setActFnName( String name ){ ki.setUnitActFunc( number, name ); }

  public String getOutFnName(){return ki.getUnitOutFuncName( number ); }
  public void setOutFnName( String name ){ ki.setUnitOutFunc( number, name ); }

  public double getActivation(){ return ki.getUnitActivation( number ); }
  public void setActivation( double act ){ ki.setUnitActivation( number, act ); }

  public double getInitAct(){ return ki.getUnitInitialActivation( number ); }
  public void setInitAct( double act ){ ki.setUnitInitialActivation( number, act ); }

  public double getBias(){ return ki.getUnitBias( number ); }
  public void setBias( double bias ){ ki.setUnitBias( number, bias ); }

  public int[] getPosition(){
    ki.getUnitPosition( number );
    return new int[]{ ki.posX, ki.posY, ki.posZ };
  }
  public void setPosition( int[] pos ){
    ki.posX = pos[ 0 ]; ki.posY = pos[ 1 ]; ki.posZ  = pos[ 2 ];
    ki.setUnitPosition( number );
  }

  public int getLayer(){ return ki.getUnitLayerNo( number ); }
  public void setLayer( int layer ){ ki.setUnitLayerNo( number, layer ); }

  public int getSubnetNo(){ return ki.getUnitSubnetNo( number ); }
  public void setSubnetNo( int subnet ){ ki.setUnitSubnetNo( number, subnet ); }

  public double getOutput(){ return ki.getUnitOutput( number ); }
  public void setOutput( double out ){ ki.setUnitOutput( number, out ); }

  public int getType(){ return ki.getUnitTType( number ); }
  public void setType( int type ){ ki.setUnitTType( number, type ); }

  public Vector getAllIncomingLinks(){
    Vector links = new Vector();
    ki.setCurrentUnit( number );
    for( int i=ki.getFirstPredUnit(); i!=0; i=ki.getNextPredUnit() )
      links.addElement( new Link( ki, i, number, false ) );
    return links;
  }
  public Vector getAllOutgoingLinks(){
    Vector links = new Vector();
    for( int i=ki.getFirstSuccUnit( number ); i!=0; i=ki.getNextSuccUnit() )
      links.addElement( new Link( ki, number, i, false ) );
    return links;
  }

  /**
   * method returns the neigbourhood of the given unit
   * means the max. 26 units next to he given one
   *
   * @param the center unit
   * @return <code>java.util.Vector<\code> of neighbour units
   */
  public synchronized Vector getNeighbourhood(){
    Vector nu = new Vector();
    ki.getUnitPosition( number );
    int[] cen = new int[]{ ki.posX, ki.posY, ki.posZ };
    for( int no=ki.getFirstUnit(); no != 0; no = ki.getNextUnit() ){
      ki.getUnitPosition(no);
      if( no != number ){
        int d = cen[0]-ki.posX; d *= d;
        if( d < 2 ){
          d += (cen[1]-ki.posY)*(cen[1]-ki.posY);
          if( d < 3 ){
            d += (cen[2]-ki.posZ)*(cen[2]-ki.posZ);
            if( d < 4 ) nu.add( new Unit(ki, no) );
          }
        }
      }
    }
    return nu;
  }
  public String toString(){
    return getName()+"["+number+"]";
  }

}





