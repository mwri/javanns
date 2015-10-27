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


/**
 * Class PatternSet collects a few patterns and remembers if the patterns have
 * been modified, new patterns have been added or if some were deleted
 * Because it is used by ControlPanel to be inserted in NamedComboBoxes it
 * implements NamedObject.
 * The inherited String LASname represents either the canonical path of the
 * file of this pattern set or if it was created in this session and not yet
 * saved the short name
 */
class PatternSet extends LASAdapter implements NetworkListener, NamedObject{
  Snns snns;
  PatternSets master;
  Network net;
  KernelInterface ki;

  private boolean completely_constructed = false;

  /**
   * Creates an PatternSet object which is not completely useable and not
   * added to the PatternSets of JavaNNS until the FileManager asks it to load
   * the patterns from a pattern definition file
   * This constructor is used to implement the LoaderAndSaver ability
   *
   * @see PatternSets.getCover();
   *
   * @param sets the pattern sets object
   */
  PatternSet( PatternSets sets ){
    snns = sets.snns;
    master = sets;
    net = master.network;
    ki = net.ki;
  }

  /**
   * Allocates a new PatternSet with the specified name
   * This constructor really creates a new pattern set in the kernel with the
   * given name and sets this pattern set as current and validation set
   *
   * @param sets the pattern sets object
   * @param name the name of the pattern set
   */
  PatternSet(PatternSets sets, String name){
    this(sets);
    LASname = name;
    ki.allocNewPatternSet(name); //reicht nicht aus um das Pattern set wirklich als current zu setzen!!!
    master.sets.add( this );
    master.checkCurrent();
    //master.setCurrent( this );
    master.setValidationSet( this );
    init();
    completely_constructed = true;
    net.fireEvent( NetworkEvent.PATTERN_SET_CREATED );
  }

  /**
   * just to save space
   */
  private void init(){
    //System.out.println("PatternSet.init()");
    net.addListener( this );
  }

  /**
   * method returns the keyword which identifies pattern definition files
   *
   * @return the key
   */
  public String getKeyword(){ return "SNNS pattern definition file" ; }

  /**
   * method returns the file extension of pattern files
   * used for the file filter in the FileManager
   *
   * @return the file extension
   */
  public String getFileExtension(){ return "pat"; }

  /**
   * method returns a short description of the files
   * this texts is shown in the FileManager in the FileFilter ComboBox
   *
   * @return the description
   */
  public String getDescription(){ return "Pattern files *.pat"; }

  /**
   * method saves the pattern set in a certain file and resets
   * the content_changed flag
   *
   * @param the File
   */
  public void save( File file ) throws Exception{
    String path = file.getCanonicalPath();
    if( !FileManager.getFileExtension( file ).equals("pat") ) path += ".pat";
    ki.savePattern( path, LASname );
    homeFile = file;
    content_changed = false;
  }

  /**
   * method loads new patterns from the specified file
   */
  public void load( File file ) throws Exception{
    if( completely_constructed ){
      PatternSet set = master.getCover();
      set.load( file );
      return;
    }
    String path = file.getCanonicalPath();
    ki.loadPattern( path );
    LASname = path;
    homeFile = file;
    master.sets.add( this );
    master.current = this;
    master.validation = this;
    init();
    completely_constructed = true;
    net.fireEvent( NetworkEvent.PATTERN_SET_LOADED, LASname );
    try {
/*        System.out.println("1");
      KernelPatternInfo kpi = ki.getPatInfo();
        System.out.println("2");
      ki.defShowSubPat(new int[]{7}, new int[]{}, new int[]{0}, new int[]{});
        System.out.println("3");
      ki.showPattern( 2 );
        System.out.println("4");
*/
      net.fireEvent( NetworkEvent.PATTERN_CHANGED );
    }
    catch(Exception e) {
      System.out.println(e);
    }

  }


  /**
   * method looks if the pattern set has been modified
   */
  public void networkChanged( NetworkEvent evt ) {
    if( !master.getCurrent().equals( this ) ) return;
    if(evt.id == NetworkEvent.PATTERN_CREATED ||
       evt.id == NetworkEvent.PATTERN_DELETED ||
       evt.id == NetworkEvent.PATTERN_MODIFIED ) content_changed = true;
  }

  /**
   * returns the name of the object which is shown when the
   * askForSaving dialog of this LoaderAndSaver object is shown
   *
   * @return the name to show in the askForSaving dialog
   */
  public String getLASName(){ return getName() + " pattern set"; }

  /**
   * Deletes the specified pattern set from memory.
   *
   * @param name pattern set name
   */
  public boolean delete( PatternSets sets ){
    boolean really = true;
    if( content_changed ) really = snns.askForSaving( this );
    if( !really ) return false;
    ki.delPattern( LASname );
    net.removeListener( this );
    return true;
  }

  /**
   * method checks if tho given NamedObject is equal to this set
   * that means, it is also an instance of PatternSet and has the same LASname
   *
   * @return o the NamedObject the pattern set is to compare with
   * @return <code>true</code> if their names were equal
   */
  public boolean equals(NamedObject o) {
    return equals( (Object)o );
  }

  /**
   * method checks if the given Object is equal to this set
   * that means, it is also an instance of PatternSet and has the same LASname
   *
   * @return o the Object the pattern set is to compare with
   * @return <code>true</code> if their names were equal
   */
  public boolean equals( Object o ){
    if( !( o instanceof PatternSet ) ) return false;
    PatternSet set = (PatternSet)o;
    if( LASname.equals( set.LASname ) ) return true;
    return false;
  }

  /**
   * method returns the short name of the set
   * (i.e. the file name )
   *
   * @return the short name
   */
  public String getName() { return FileManager.getNameOnly( LASname ); }

  /**
   * nethod returns the full name of the set
   * (i.e. the canonical path of the file)
   *
   * @return the full name
   */
  public String getFullName() { return LASname; }
}
