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


/*---------------------- class declaration -----------------------------------*/
/**
 * class UnitData contains all information about an unit
 * it is used for ( re- ) creating units
 */
class UnitData{
  int layer, subnet, type, number = 0;
  int[] pos;
  String name, act_fn_name, out_fn_name;
  double act, init_act, bias;

  public UnitData( Unit unit ){
    number = unit.getNumber();
    type = unit.getType();
    layer = unit.getLayer();
    subnet = unit.getSubnetNo();
    name = unit.getName();
    act_fn_name = unit.getActFnName();
    out_fn_name = unit.getOutFnName();
    act = unit.getActivation();
    init_act = unit.getInitAct();
    bias = unit.getBias();
    pos = unit.getPosition();
  }

  public UnitData( String name,
                   int type,
                   String act_fn_name,
                   String out_fn_name,
                   double bias,
                   double init_act,
                   int[] pos,
                   int layer,
                   int subnet ){
    this.name = name;
    this.type = type;
    this.act_fn_name = act_fn_name;
    this.out_fn_name = out_fn_name;
    this.bias = bias;
    this.init_act = init_act;
    this.pos = pos;
    this.layer = layer;
    this.subnet = subnet;
  }

  public Object clone(){
    return new UnitData( name, type, act_fn_name, out_fn_name, bias, init_act, pos, layer, subnet );
  }


}
