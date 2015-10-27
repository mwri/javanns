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
import java.io.File ;
import java.io.IOException ;
import java.io.FileReader ;
import javax.swing.JFileChooser ;
import javax.swing.JFrame ;
import javax.swing.JComponent ;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter ;

/**
 * this class manages the file opening and saving for the snns
 * it shows the file opening and saving dialogs and - depending on the
 * files to save - an extra detail dialog
 * from certain keywords, the FileManager is able to find the right
 * LoaderAndSaver to open the file with
 */
public class FileManager{
  Snns snns;
  private JFileChooser fileChooser ;

  private Vector filters  = new Vector();

  private LoaderAndSaver[] loadersAndSavers;
  private LoaderAndSaver default_las = null;

  public FileManager( Snns snns ){
    this( snns, System.getProperty("user.dir") );
  }

  public FileManager( Snns snns, String path ){
    this.snns = snns;
    fileChooser = new JFileChooser( path );
  }

  /**
   * show the open dialog for the given type of files
   * the LoaderAndSaver implemented a method which returns the file
   * extension to build the file filter for this type of file
   *
   * @param las the LoaderAndSaver which might be loaded
   */
  public void showOpenDialog( LoaderAndSaver las ) throws Exception{
    showOpenDialog( new LoaderAndSaver[]{ las } );
  }

 /**
   * show the open dialog for the given types of files
   * the LoaderAndSaver implemented a method which returns the file
   * extensions to build the file filters for these types of files
   *
   * @param las the array of LoaderAndSavers which might be loaded
   */
  public void showOpenDialog( LoaderAndSaver[] las ) throws Exception{
    Exception excp = null;
    fileChooser.setMultiSelectionEnabled( true );
    loadersAndSavers = las;
    addAllFilters();
    fileChooser.setAcceptAllFileFilterUsed( true );
    if( fileChooser.showOpenDialog( snns ) == JFileChooser.APPROVE_OPTION ){
      File[] files = fileChooser.getSelectedFiles();
      File file = fileChooser.getSelectedFile();
      if( (files == null || files.length == 0) && file != null )
        files = new File[] { file };
      for( int i=0; i<files.length; i++ )
        try{ load( files[i] ); }
        catch( Exception e ){ excp = e; }
    }
    removeAllFilters();
    if( excp != null ) throw excp;
  }

  /**
   * this method shows the saving dialog for a certain LoaderAndSaver
   * that means that it sets the file filter
   *
   * @param las the LoaderAndSaver which is to save
   * @return whether it really has been saved by the method or not
   */
  public boolean showSaveDialog( LoaderAndSaver las ) throws Exception{
    return showSaveDialog( new LoaderAndSaver[]{ las } );
  }


  /**
   * this method shows the saving dialog for certain LoaderAndSavers
   * that means that it sets the file filters
   *
   * @param las the array of LoaderAndSavers which might be saved
   * @return whether it really has been saved by the method or not
   */
  public boolean showSaveDialog( LoaderAndSaver[] las ) throws Exception{
    Exception excp = null;
    fileChooser.setMultiSelectionEnabled( false );
    fileChooser.setAcceptAllFileFilterUsed( false );
    loadersAndSavers = las;

    for( int i=0; i<las.length; i++ ){
      addFilterOf( las[ i ] );
    }
    int ret_val = fileChooser.showSaveDialog( snns );
    boolean saved = false;
    if( ret_val == JFileChooser.APPROVE_OPTION ) {
      try{ saved = save(); }
      catch( Exception e ){ excp = e; }
    }
    removeAllFilters();
    if( excp != null ) throw excp;
    return saved;
  }

  public void setDefaultLAS( LoaderAndSaver las ){
    default_las = las;
  }

   /**
   * Returns only file name, without path or extension:
   *
   * @param fn the complete file name
   */
   public static String getNameOnly(String fn) {
     int i = fn.lastIndexOf(File.separator);
     int j = fn.lastIndexOf(".");
     if(j>i) return fn.substring(i+1, j);
     return fn.substring(i+1);
   }

  /**
   * Returns only file path, without file name:
   *
   * @param fn the complete file name
   */
   public static String getPathOnly(String fn) {
     int i = fn.lastIndexOf(File.separator);
     if(i < 0) return "";
     else return fn.substring(0, i);
   }

  /**
   * this method tries to load the given file by certain LoaderAndSavers
   * if none of them would be able to load the file, it tries to open it with
   * the default LoaderAndSaver
   *
   * @param file the file to load
   * @param las the LoaderAndSaver array which might be able to open the file
   */
  public void load( File file, LoaderAndSaver[] las ) throws Exception{
    loadersAndSavers = las;
    load( file );
  }


  /**
   * this method tries to load the given file
   * if none of the LoaderAndSavers is able to load the file, it tries to open
   * it with the default LoaderAndSaver
   *
   * @param file the file to load
   */
  private void load( File file ) throws Exception{
    FileReader reader = new FileReader( file );
    char[] buffer = new char[ 80 ];
    reader.read( buffer );
    String text = new String( buffer );
    int i = -1, index = -1;
    while( i<loadersAndSavers.length-1 && index == -1 ){
      i++;
      index = text.indexOf( loadersAndSavers[ i ].getKeyword() );
    }
    if( i< loadersAndSavers.length ) loadersAndSavers[ i ].load( file );
    else if( default_las != null ) default_las.load( file );
  }


  /**
   * this method decides by the selected index of the file filters, which file
   * is to save and saves it to the selected file of the file chooser dialog
   *
   * @return whether the file has been saved or not
   */
  private boolean save() throws Exception{
    File file = fileChooser.getSelectedFile();
    FileFilter filter = fileChooser.getFileFilter();
    int index = filters.indexOf( filter );
    LoaderAndSaver las = loadersAndSavers[ index ];
    JPanel acc = las.getAccessory();
    if( acc != null ){
      int choice = JOptionPane.showOptionDialog( snns, acc, "Saving details",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null );
      if( choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION )
        return false;
    }
    if( getFileExtension( file ).equals("") )
      file = new File( file.getCanonicalPath() + "." + las.getFileExtension() );
    las.save( file );
    return true;
  }

  private void addAllFilters(){
    for(int i=0; i<loadersAndSavers.length; i++ )
      addFilterOf( loadersAndSavers[ i ] );
  }

  private void addFilterOf( LoaderAndSaver las ){
    FileFilter filter = getFilterOf( las );
    fileChooser.addChoosableFileFilter( filter );
    filters.addElement( filter );
  }

  private void removeAllFilters(){
    FileFilter filter;
    for(int i=0; i<filters.size(); i++){
      filter = (FileFilter)filters.elementAt( i );
      fileChooser.removeChoosableFileFilter( filter );
    }
    filters.removeAllElements();
  }

  /**
   * this method returns the file extension of the given file
   * for example from a file named "test.doc" it returns "doc"
   *
   * @param file the file the extension should be returned from
   */
  public static String getFileExtension(File file){
    String name = file.getName();
    int index = name.indexOf( "." );
    if( index == -1 ) return "";
    return name.substring( index + 1 );
  }

  private FileFilter getFilterOf( LoaderAndSaver las ){
    final String ending      = las.getFileExtension(),
                 description = las.getDescription();

    return new FileFilter(){
      public boolean accept( File file ){
        if( getFileExtension( file ).equals( ending ) || file.isDirectory() ) return true;
        return false;
      }
      public String getDescription(){ return description ; }
    };
  }
}