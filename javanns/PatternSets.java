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



/*-------------------------- imports -----------------------------------------*/
import java.io.File ;
import java.io.IOException ;
import java.util.Vector;

/*---------------------- class declaration -----------------------------------*/
/**
 * class PatternSets organizes the different patter sets of the kernel
 * that means it collects them and remembers which of them is
 * currently selected
 */
class PatternSets{
  Snns snns;
  Network network;
  KernelInterface ki;
  Vector sets;
  PatternSet current, validation;

/*--------------------- constructor ------------------------------------------*/
  /**
   * creates a new PatternSets object
   *
   * @param snns the JavaSNNS centre
   * @param initial_capacity like initial_capacity in java.util.Vector
   */
  public PatternSets( Snns snns, int initial_capacity ){
    this.snns = snns;
    network = snns.network;
    ki = network.ki;
    sets = new Vector( initial_capacity );
  }
/*------------------- public methods -----------------------------------------*/
  public boolean deleteSet( String name ){
    if( name == null ) return false;
    int i=0;
    boolean found = false, done = false;
    while( !found && i<sets.size() ){
      PatternSet set =(PatternSet)sets.get(i);
      if( name.equals( set.LASname ) ){
        found = true;
        done = deleteSet( set );
      }
    }
    return done;
  }

  public boolean deleteSet( PatternSet set ){
    if( !set.delete( this ) ) return false;
    sets.remove( set );
    if( current == set || validation == set ) init();
    network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
    return true;
  }

  /**
   * method deletes all pattern sets
   */
  public void deleteAllPatternSets(){
    for( int i=0; i<sets.size(); i++ ){
      PatternSet set = (PatternSet)sets.get(i);
      deleteSet(set);
    }
    network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
  }



  /**
   * method returns the number of the pattern sets
   *
   * @return the number of the sets
   */
  public int size(){
    return sets.size();
  }

  /**
   * method returns the currently selected pattern set
   *
   * @return the currently selected pattern set
   */
  public PatternSet getCurrent(){
    //System.out.println("PatternSets.getCurrent");
    return current;
  }

  /**
   * method sets the specialized pattern set as currently selected
   * now the patterns of this set can be updated or trained
   *
   * @param set the pattern set which should be selected
   */
  public void setCurrent( PatternSet set ){
    //System.out.println("PatternSets.setCurrent: "+set.getFullName() );
    ki.setPattern( set.getFullName() );
    current = set;
    network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
  }

  /**
   * method returns the pattern set of the specialized index
   * NOTE : this index needs not to remain if other sets are deleted
   *
   * @param no the index of the set
   * @return the set at the given index
   */
  public PatternSet getSet( int no ){
    //System.out.println("PatternSets.checkCurrent: " + no);
    return (PatternSet)sets.get(no);
  }
  /**
   * method return the pattern set with the given name
   * the name can either be the short name of the pattern set
   * (i.e. the name of the file if it was already saved) or the canonical path
   * (@see java.io.File)
   *
   * @param name the name of the set
   * @return the pattern set with this name or null
   */
  public PatternSet getSet( String name ){
    //System.out.println("PatternSets.getSet: " + name);
    for( int i=0; i<sets.size(); i++ ){
      PatternSet ps = (PatternSet)sets.get(i);
      if( ps.getName().equals(name) ||
          ps.getFullName().equals(name) ) return ps;
    }
    return null;
  }

  /**
   * method advises the PatternSets to check if the pattern set which is
   * memoried as currently selected is really selected in the kernel
   */
  public void checkCurrent(){
    //System.out.println("PatternSets.checkCurrent");
    PatternSet old = current;
    if( sets.size() == 0 ) return;
    String name = ki.getCurrPatternSet();
    current = getSet( name );
    if( old == null || !old.equals(current) )
      network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
  }
  /**
   * method sets the validation pattern set which will be used as test set when
   * the net is trained
   *
   * @param set the new validation set
   */
  public void setValidationSet( PatternSet set ){
    //System.out.println("PatternSets.setValidationSet");
    validation = set;
    network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
  }

  /**
   * method returns the current validation set
   *
   * @return the validation set
   */
  public PatternSet getValidationSet(){
    //System.out.println("PatternSets.getValidationSet");
    return validation;
  }

  /**
   * method initializes the currently chosen PatternSets
   * it takes the first loaded PatternSets and sets this as current
   * trainig and as current validation set
   */
  private void init(){
    //System.out.println("PatternSets.init");
    if( sets.size() == 0 ) {
      current = null;
      validation = null;
    }
    else{
      PatternSet set = (PatternSet)sets.get(0);
      ki.setPattern( set.getFullName() );
      current = set;
      validation = set;
    }
    network.fireEvent( NetworkEvent.PATTERN_SET_CHANGED );
  }

  public PatternSet getCover(){
    return new PatternSet(this);
  }

  public PatternSet createSet(String name){
    return new PatternSet(this, name);
  }
}
