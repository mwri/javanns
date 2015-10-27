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


/**
 * this class represents a link between two units
 * it just contains the kernel numbers of the units it is between, so if
 * some units are deleted while a link object is held, this object doesn´t
 * represent the original link anymore!
 */
public class Link {
  private KernelInterface ki;
  private int source, target;

  /**
   * this constructor can be called to create a new link between the given units
   *
   * @param ki the kernel interface
   * @param sourceNo the number of the source unit
   * @param targetNo the number of the target unit
   * @param create a boolean whether the link should be created or not
   */
  public Link( KernelInterface ki, int sourceNo, int targetNo, boolean create ) {
    this.ki = ki;
    source = sourceNo;
    target = targetNo;
    if( create ){
      ki.setCurrentUnit( target );
      if( !ki.isConnected( source ) )
        ki.createLink( source, 0.0 );
    }
  }

  /**
   * this constructor tries to create a link
   * the LinkData object can be obtained by deleting certain links ( @see #delete() )
   * so this constructor is used, when deleted link should be recreated
   *
   * @param ki the kernel interface
   * @param data the LinkData object
   */
  public Link( KernelInterface ki, LinkData data ){
    this.ki = ki;
    source = data.source;
    target = data.target;
    ki.setCurrentUnit( target );
    if( !ki.isConnected( source ) )
      ki.createLink( source, data.weight );
  }


  public boolean equals(Object o){
    if( !( o instanceof Link ) ) return false;
    Link l = (Link)o;
    if( l.source == source && l.target == target ) return true;
    return false;
  }

  public LinkData delete(){
    LinkData data = new LinkData( this );
    ki.setCurrentUnit( target );
    if( ki.isConnected( source ) ) ki.deleteLink();
    return data;
  }

  public Unit getSourceUnit(){ return new Unit( ki, source ); }
  public Unit getTargetUnit(){ return new Unit( ki, target ); }
  public double getWeight(){
    ki.setCurrentUnit( target );
    if( !ki.isConnected( source ) ) return 0.0;
    return ki.getLinkWeight();
  }
  public void setWeight( double weight ){
    ki.setCurrentUnit( target );
    if( !ki.isConnected( source ) ) return;
    ki.setLinkWeight( weight );
  }
  public boolean isSelfConnection(){ return (source == target); }

  public Object clone(){
    return new Link( ki, source, target, false );
  }
}


