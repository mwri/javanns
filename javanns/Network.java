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



/*---------------------------- imports ---------------------------------------*/
import java.io.* ;
import java.util.* ;

import java.util.jar.*;
import javax.swing.* ;
import java.awt.* ;

// imports for NetTrainer:
import java.awt.event.*;


/*----------------------- class declaration ----------------------------------*/
public class Network extends LASAdapter {
  public static final boolean under_construction = false;
  Snns snns;
  KernelInterface ki;
  NetworkAccessory accessory;
  Vector listeners = new Vector();
  Vector training_listeners = new Vector();
  Function[] functions = new Function[Function.differentTypes];
  double[][] parameters = new double[Function.differentTypes][5];
  private String net_name = "default";
  static String libName = System.mapLibraryName(Snns.LIBRARY_NAME);

  private boolean is_ready = false;

  // for creating new links :
  private Unit[] source_units;

  // for unit selection :
  private boolean[] selection_flags;
  private int selectedUnitsCount = 0;

  // layers :
  Layers layers;

  // maximal x coordinate of an unit:
  private int[] max_coord = new int[]{ -1, -1 };

/*-------------------------- constructor -------------------------------------*/
  public Network( Snns snns ) throws Exception{
    this.snns = snns;
    if( !loadKernel()) throw
      new Exception("The system couldn´t find the library SNNS_jkr\nJavaNNS couldn´t start.");
    layers = new Layers( this );
    new NetChangeListener();
    /*
    String name;
    int i, fnNo = ki.getNoOfFunctions();
    ki.functionName = "";
    System.out.println("Number of functions: "+fnNo);
    for( i=0; i<fnNo; i++ ){
      ki.getFuncInfo( i );
      name = ki.functionName;
      ki.getFuncParamInfo( name, i );
      System.out.println( "Name: "+name );
      System.out.println( "Type: "+ki.functionType );
      System.out.println( "Parameter: in["+ki.function_inputs+"], out["+ki.function_outputs+"]");
    }
    */
  }

  /**
   * method tries to load the library
   * it first tries to find it at the place specified by the properties entry
   * if this wasn´t possible, an dialog is shown to ask the user, whether he
   * knows the place of the library or where it is to put. In the last case
   * the method extracts the library to the specified path from the jar-file, if
   * this could be found
   */
  boolean loadKernel(){
    ki = new KernelInterface();

    String dll_path = snns.properties.getProperty(JavaNNSProperties.LIBRARY_PATH_KEY);
    try { ki.loadLibrary(dll_path); return true; }
    catch(UnsatisfiedLinkError e) {
      DLLPathDialog dpd = new DLLPathDialog(snns, dll_path, libName);
      dll_path = dpd.getPath();
      if(dll_path == null) return false;
      try {
        ki.loadLibrary(dll_path);
        setDLLPathProperty(dll_path);
        return true;
      }
      catch(UnsatisfiedLinkError e2) {
        if(! extractLibraryFromJar(dll_path)) return false;
        try{
          ki.loadLibrary(dll_path);
          setDLLPathProperty(dll_path);
        }
        catch(UnsatisfiedLinkError ex) { return false; }
        return true;
      }
    }
  }


  /**
   * Extracts the native kernel library from the
   * JAR containing the JavaNNS distribution and
   * stores it in the specified path.
   *
   * @param   path  folder where to store the library
   * @return  true if the library was successfuly extracted
   */
  boolean extractLibraryFromJar(String dll_path) {
    File jar = new File(Snns.JAR_FILE_NAME);
    if( !jar.exists() ) throw new
      UnsatisfiedLinkError("Couldn´t find the JavaNNS.jar file");

    try {
      JarFile jf = new JarFile(jar);
      Enumeration entries = jf.entries();
      JarEntry je = null;
      while(entries.hasMoreElements()) {
        je = (JarEntry)entries.nextElement();
        if(je.getName().equals(libName)) break;
      }
      if(je == null) {
        System.out.println(
          "Could not find the kernel library " + libName +
          "in the jar file.\n" +
          "Probaly a wrong release."
        );
        return false;
      }
      InputStream is = jf.getInputStream(je);
      FileOutputStream fos =
        new FileOutputStream(dll_path + File.separatorChar + libName);
      byte[] buffer = new byte[16384];
      for(int read = is.read(buffer); read > -1; read = is.read(buffer))
        fos.write(buffer, 0, read);
      is.close();
      fos.flush();
      fos.close();
      return true;
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }


  void setDLLPathProperty(String dll_path){
    if( snns.properties.editable() ){
      snns.properties.setProperty(JavaNNSProperties.LIBRARY_PATH_KEY,dll_path);
      try{ snns.properties.save(); }
      catch(IOException ex){}
    }
  }



/*---------------------- listeners & events ----------------------------------*/
  /**
   * adds a new network listener
   * this listener gets infos about all network events
   *
   * @param listener
   */
  public void addListener( NetworkListener l ){
    if( !listeners.contains(l) )  listeners.add(l);
  }

  public void addTrainingListener(NetworkListener l){
    if( !training_listeners.contains(l) ) training_listeners.add(l);
  }

  /**
   * method removes a certain listener from the listener list
   */
  public void removeListener( NetworkListener l ){
    listeners.remove(l);
    training_listeners.remove(l);
  }

  /**
   * sends infos to all listeners
   *
   * @param type the id of the event
   */
  public void fireEvent( int type ){
    fireEvent( type, null );
  }

  /**
   * sends infos to all listeners
   *
   * @param type the id of the event
   * @param the argument
   */
  public void fireEvent( int type, Object arg ){
    fireEvent( new NetworkEvent( this, type, arg ) );
  }


  /**
   * sends infos to all listeners
   *
   * @param the event
   */
  public void fireEvent( NetworkEvent evt ){
    if( under_construction ) System.out.println("Network.fireEvent(): "+evt.getMessage() );
    /*
    for( int i=listeners[ evt.id ].size()-1; i>-1; i-- )
      ( (NetworkListener)listeners[ evt.id ].get( i ) ).networkChanged( evt );
    */
    for( int i=0; i < listeners.size(); i++ ){
      NetworkListener nl = (NetworkListener)listeners.get(i);
      if( under_construction ) System.out.println("next recipient: " + nl.getClass() );
      nl.networkChanged( evt );
    }
    if( under_construction ) System.out.println("Network.fireEvent Ende");
  }


/*---------------- methods concerning the whole network ----------------------*/
  /**
   * method returns the name of the network
   */
  public String getName(){ return net_name; }

  /**
   * method sets a new name to the network
   */
  public String setName( String name ){
    return setName( name, true );
  }

  /**
   * method sets a new name to the network
   */
  private String setName( String name, boolean fire_event ){
    String old_name = net_name;
    net_name = name;
    if( fire_event && !net_name.equals( old_name ) )
      fireEvent( NetworkEvent.NETWORK_NAME_CHANGED, old_name );
    return old_name;
  }

  /**
   * method returns the number of units
   */
  public int getNumberOfUnits(){
    if( under_construction ) System.out.println("Network.getNumberOfUnits()");
    return ki.getNoOfUnits();
  }
  /**
   * method returns the number of input units
   */
  public int getNumberOfInputUnits(){
    return ki.getNoOfInputUnits();
  }
  /**
   * method returns an array of input units
   */
  public Unit[] getInputUnits(){
    Unit[] units = new Unit[ ki.getNoOfInputUnits() ];
    int index = 0;
    for( int no = 1; no < ki.getNoOfUnits() + 1; no++ )
      if( ki.getUnitTType( no ) == UnitTTypes.INPUT )
        units[ index++ ] = new Unit( ki, no );
    return units;
  }
  /**
   * method returns number of output units
   */
  public int getNumberOfOutputUnits(){
    return ki.getNoOfOutputUnits();
  }
  /**
   * method returns an array of output units
   */
  public Unit[] getOutputUnits(){
    Unit[] units = new Unit[ ki.getNoOfOutputUnits() ];
    int index = 0;
    for( int no = 1; no < ki.getNoOfUnits() + 1; no++ )
      if( ki.getUnitTType( no ) == UnitTTypes.OUTPUT )
        units[ index++ ] = new Unit( ki, no );
    return units;
  }
  /**
   * method updates the network with the current update function
   */
  public void updateNet() throws Exception{
    ki.updateNet();
    fireEvent( NetworkEvent.NETWORK_UPDATED );
  }
/*-------------------------- functions & parameters --------------------------*/
  /**
   * method sets a function
   *
   * @param function
   * @param parameters
   */
  public void setFunction( Function f, double[] p) throws Exception{
    if( under_construction ) System.out.println("Network.setFunction: "+f);
    switch( f.type ){
      case Function.INIT      : ki.setInitFunc(f.kernel_name, p); break;
      case Function.LEARN     : ki.setLearnFunc(f.kernel_name, p); break;
      case Function.REMAPPING : ki.setRemapFunc(f.kernel_name, p); break;
      case Function.UPDATE    : ki.setUpdateFunc(f.kernel_name, p); break;
      default                 : return;
    }
    functions[f.type] = f;
    parameters[f.type] = p;
  }
  /**
   * method returns the currently chosen function of the specialized type
   *
   * @param the function type
   * @return the function
   */
  public Function getFunction( int type ){
    if( type < 0 || type > functions.length ) return null;
    return functions[ type ];
  }

  /**
   * method returns the parameter values of the specialized function type
   *
   * @param the funciton type
   * @return the parameter values
   */
  public double[] getParameters( int function_type ){
    if( function_type < 0 || function_type > parameters.length ) return null;
    return parameters[ function_type ];
  }

  public void showFnList(){
    if( under_construction ) System.out.println("Network.showFnList()");
    String text = "";
    for( int i=0; i< functions.length; i++ )
      if( functions[i] != null )
        text += "\n"+functions[i].show_name;
    System.out.println( text );
  }

  public void showState(){
    System.out.println("Network name: "+getName());
    System.out.println("Training pattern set: "+snns.patternSets.current.getName());
    System.out.println("Validation pattern set: "+snns.patternSets.validation.getName());
    showFnList();
  }

  private void updateFnList( int type ){
    String kn = null;
    switch ( type ) {
      case Function.LEARN   : kn = ki.getLearnFunc();
                              break;
      case Function.INIT    : kn = ki.getInitFunc();
                              break;
      case Function.PRUNING : kn = ki.getPrunFunc();
                              break;
      case Function.UPDATE  : kn = ki.getUpdateFunc();
                              break;
    }
    if( kn != null ) functions[ type ] = snns.functions.getFunction( kn, type );
    if( under_construction ) {
      System.out.println("Network.updateFnList( int )");
      showFnList();
    }
  }

/*-----------------------------             ----------------------------------*/
  /**
   * method sets the parameters for Cascade Correlation
   * with several parameters ( more details in kernel )
   */
  public void setCascadeParams(double max_outp_uni_error,
                                      String learn_func,
                                      boolean print_covar,
                                      boolean prune_new_hidden,
                                      String mini_func,
                                      double min_covar_change,
                                      int cand_patience,
                                      int max_no_covar,
                                      int max_no_cand_units,
                                      String actfunc,
                                      double error_change,
                                      int output_patience,
                                      int max_no_epochs,
                                      String modification,
                                      double[] modP,
                                      boolean cacheUnitAct){

    ki.setCascadeParams( max_outp_uni_error,
                         learn_func,
                         print_covar,
                         prune_new_hidden,
                         mini_func,
                         min_covar_change,
                         cand_patience,
                         max_no_covar,
                         max_no_cand_units,
                         actfunc,
                         error_change,
                         output_patience,
                         max_no_epochs,
                         modification,
                         modP,
                         cacheUnitAct);
  }
  /**
   * method sets the pruning function
   * with several parameters ( more details in kernel )
   */
  public void setPruningFunc(String prune_func, String learn_func,
                          double pmax_error_incr, double paccepted_error,
                          boolean precreatef, int pfirst_train_cyc,
                          int pretrain_cyc, double pmin_error_to_stop,
                          double pinit_matrix_value, boolean pinput_pruningf,
                          boolean phidden_pruningf){

    ki.setPruningFunc(prune_func, learn_func,
                          pmax_error_incr, paccepted_error,
                          precreatef, pfirst_train_cyc,
                          pretrain_cyc, pmin_error_to_stop,
                          pinit_matrix_value, pinput_pruningf,
                          phidden_pruningf);
  }

  /**
   * method tells the kernel to start pruning
   */
  public void pruneNet(boolean refreshDisplay) {
    if(!refreshDisplay) ki.pruneNet();
    else {
      double max_err = ki.pruneNet_FirstStep();
      do {
        fireEvent(NetworkEvent.NETWORK_PRUNED);
        try { Thread.sleep(100); }
        catch( Exception e ) { }
      }
      while(ki.pruneNet_Step() <= max_err);
      ki.pruneNet_LastStep();
    }
    fireEvent(NetworkEvent.NETWORK_PRUNED);
  }

  /**
   * Initializes the network using the current init function
   */
  public void initNet() throws Exception {
    ki.initNet();
    fireEvent( NetworkEvent.NETWORK_INITIALIZED );
  }


  /**
   * Trains the network with all patterns from the current pattern set.
   *
   * @param steps the number of training steps
   * @param shuffle whether the patterns are trained next by next or not
   * @param subShuffle if the subpatterns are shuffled
   */
  public void trainNet(ThreadChief tc, int steps, boolean shuffle, boolean subShuffle){
    if( under_construction ) System.out.println("Network.trainNet( int )");
    ki.setShuffle(shuffle);
    ki.setSubShuffle(subShuffle);
    setSubPatternScheme();
    NetTrainer trainer = new NetTrainer(this, tc, steps, shuffle, subShuffle);
  }

  /**
   * method validates the training state of the networks by the specified
   * validation PatternSet
   * it returns the medium squared sum of the errors
   * medium means, the sum is divided by the number of patterns
   *
   * @param val_set the validation pattern set
   * @return the medium squared sum error
   */
  public double validate(PatternSet val_set, boolean shuffle, boolean subShuffle){
    if( under_construction ) System.out.println("Network.validate(PatternSet)");
    PatternSets sets = snns.patternSets;
    PatternSet training_set = sets.getCurrent();
    sets.setCurrent(val_set);
    ki.setShuffle(shuffle);
    ki.setSubShuffle(subShuffle);
    setSubPatternScheme();
    ki.testNet();
    sets.setCurrent(training_set);
    setSubPatternScheme();
    return ki.sse;
  }


  /**
   * Trains the network the current pattern.
   *
   * @param number of training steps
   */
  public void trainNet_CurrentPattern( ThreadChief tc,
        int steps,boolean shuffle, boolean subShuffle){
    if( under_construction ) System.out.println("Network.trainNet_CurrentPattern( int )");
    ki.setShuffle(shuffle);
    ki.setSubShuffle(subShuffle);
    setSubPatternScheme();
    NetTrainer trainer = new NetTrainer(this, tc, steps, shuffle, subShuffle, getCurrentPatternNo());
  }


  /**
   * Returns training error of the network
   *
   * @param errorType   <code>true</code> if error should be squared, else the absolute error is returned
   * @param average     <code>true</code> if error should be divided by the number of output units
   * @return network training error ( or -1 if something went wrong )
  public native double  analyzer_error(int currPatt, int unitNo, int errorType, boolean average);
   */
  /*public double getError( boolean squared, boolean average ){
    System.out.println("Network.getError(boolean, boolean)");
    try{
      System.out.println( ki.getPatternNo()+", 1," + ( squared? 2 : 1 ) + "," + average );
      return ki.analyzer_error( ki.getPatternNo(), 1, ( squared? 2 : 1 ), average );
    }
    catch( Exception e ){
      snns.showException( e, this );
      return -1;
    }
  } */

  /**
   * Returns absolute training error of a unit
   *
   * @param unitNo unit number
   * @return absolute unit training error ( or -1 if something went wrong )
   */
  /*public double getError( int unitNo ){
    if( under_construction ) System.out.println("Network.getError(int)");
    try{
      System.out.println( ki.getPatternNo()+", "+ unitNo+", 3, false ");
      return ki.analyzer_error( ki.getPatternNo(), unitNo, 3, false );
    }
    catch( Exception e ){
      snns.showException( e, this );
      return -1;
    }
  } */
  /**
   * Returns training error of the network of the current pattern
   *
   * @param errorType   <code>true</code> if error should be squared, else the absolute error is returned
   * @param average     <code>true</code> if error should be divided by the number of output units
   * @return network training error ( or -1 if something went wrong )
  public native double  analyzer_error(int currPatt, int unitNo, int errorType, boolean average);
   */
  public double getError( boolean squared, boolean average ) throws Exception{
    ki.showPattern(1);
    ki.updateNet();
    Pattern p1 = new Pattern( this ), p2;
    ki.showPattern(2);
    p2 = new Pattern( this );
    double v = 0;
    for( int i=0; i<p1.output.length; i++ ){
      if( squared ) v += ( p1.output[i] - p2.output[i] ) * ( p1.output[i] - p2.output[i] );
      else v += Math.abs( p1.output[i] - p2.output[i] );
    }
    if( average ) v /= (double)p1.output.length;
    return v;
  }

  /**
   * Returns absolute training error of a unit of the current pattern
   *
   * @param unitNo unit number
   * @return absolute unit training error ( or -1 if something went wrong )
   */
  public double getError( int unitNo ) throws Exception{
    ki.showPattern(1);
    ki.updateNet();
    double v = ki.getUnitActivation( unitNo );
    ki.showPattern(2);
    v -= ki.getUnitActivation( unitNo );
    return Math.abs(v);
  }

  /**
   * deletes the current network and all patterns
   */
  public NetworkDeleteArgument deleteNetwork(){
    boolean really = true;
    if( content_changed ) really = snns.askForSaving( this );
    if( !really ) return null;
    homeFile = null;
    if( ki.getNoOfUnits() == 0 ){
      setName( "default" );
      return null;
    }
    setName( "default", false );
    selection_flags = null;
    max_coord[0] = max_coord[1] = -1;
    NetworkDeleteArgument nda = new NetworkDeleteArgument( net_name, deleteAllUnits() );
    fireEvent( NetworkEvent.NETWORK_DELETED, nda );
    content_changed = false;
    return nda;
  }

  /**
   * returns the maximal layer number
   */
  public int getMaxLayerNo(){
    if( under_construction )
      System.out.println( "Network.getMaxLayerNo: " + layers.maxLayerNo );
    return layers.maxLayerNo;
  }

  /**
   * method returns the maximal x coordinate of an unit
   */
  public int getMaxXCoordinate(){ return max_coord[0]; }

  /**
   * method returns the maximal y coordinate of an unit
   */
  public int getMaxYCoordinate(){ return max_coord[1]; }


// Methode, die aus Traditionsgründen auf's KernelInterface draufgepackt wurde,
// deren Notwendigkeit noch geklärt werden muß:
  public int getNoOfSubpats(){ return ki.subpatterns; }

/*----------------------- selected units -------------------------------------*/
  /**
   * Gets the kernel numbers of all selected units.
   *
   * @return array of selected units
   */
  public Unit[] getSelectedUnits(){
    Unit[] units = new Unit[ selectedUnitsCount ];
    int i=0, pos = 0;
    while( i<ki.getNoOfUnits() && pos < selectedUnitsCount ){
      if( selection_flags[i] ) units[ pos++ ] = new Unit( ki, i+1 );
      i++;
    }
    return units;
  }

  /**
   * Checks if there are selected units in the network.
   *
   * @return <code>true</code>, if there are selected units
   */
  public boolean unitsSelected(){
    return ( selectedUnitsCount != 0 );
  }

  public void deselectUnit(Unit u){
    if( deselectUnit(u.getNumber()) )
      fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * Deselects all units in the network.
   */
  public void deselectUnits(){ deselectUnits( true ); }

  private void deselectUnits( boolean fire_event ){
    if( selectedUnitsCount == 0 ) return;
    selectedUnitsCount = 0;
    selection_flags = new boolean[ selection_flags.length ];
    if( fire_event ) fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * Checks if given unit is selected.
   *
   * @return <code>true</code> if unit is selected, <code>false</code> if not.
   */
  public boolean isUnitSelected( Unit unit ){
    return isUnitSelected( unit.getNumber() );
  }

  /**
   * Changes the selection state of a unit.
   *
   * @param unit the unit whose state is to be changed
   */
  public void changeUnitSelection( Unit unit ){
    if( isUnitSelected( unit.getNumber() ) ) deselectUnit( unit.getNumber() );
    else selectUnit( unit.getNumber() );
    fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * method sets a certain unit selected
   *
   * @param unit to select
   */
  public void selectUnit( Unit unit ){
    boolean changed = selectUnit( unit.getNumber() );
    if( changed ) fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * method sets an vector of units selected
   *
   * @param units to select
   */
  public void selectUnits( Vector units ){
    selectUnits( units, false );
  }

  /**
   * method sets an vector of units selected
   *
   * @param units to select
   * @param if only these units should be selected
   */
  public void selectUnits( Vector units, boolean exclusive ){
    if( exclusive ) deselectUnits( false );
    Unit unit;
    boolean changed = false;
    for( int i=0; i<units.size(); i++ ){
      unit = (Unit)units.elementAt( i );
      changed = ( selectUnit( unit.getNumber() ) )? true : changed;
    }
    if( changed ) fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * method returns the number of selected units
   *
   * @return number of selected units
   */
  public int getSelectedUnitsCount(){ return selectedUnitsCount; }


  /**
   * Selects all units.
   */
  public void selectAll(){
    int noAll = ki.getNoOfUnits();
    boolean changed = false;
    if( selectedUnitsCount != noAll ) changed = true;
    selectedUnitsCount = noAll;
    selection_flags = new boolean[noAll];
    for( int i=0; i<noAll; i++ ) selection_flags[ i ] = true;
    if( changed ) fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * method returns the unit with the highest activation value,
   * called the winner unit
   *
   * @return the winner unit
   */
  public Unit getWinnerUnit(){
    if( under_construction ) System.out.println("Network.getWinnerUnit()");
    int no = 1, unit_ct = ki.getNoOfUnits();
    if( unit_ct == 0 ) return null;
    double max = ki.getUnitActivation(1);
    for( int i=2; i<=unit_ct; i++ ){
      double v = ki.getUnitActivation( i );
      if( v > max ) {
        max = v;
        no = i;
      }
    }
    Unit winner = new Unit( ki, no );
    if( under_construction ) System.out.println("Network.getWinnerUnit()-->"+winner);
    return winner;
  }

/*--------------- private unit selection methods -----------------------------*/
  private boolean deselectUnit( int number ){
    if( selection_flags[number-1] ) {
      selectedUnitsCount--;
      selection_flags[number-1] = false;
      return true;
    }
    return false;
  }
  private boolean isUnitSelected( int number ){
    if( selection_flags == null ) return false;
    if( selection_flags.length < number ) return false;
    return selection_flags[number-1];
  }

  private boolean selectUnit( int newNumber ){
    int i=0;
    if( isUnitSelected( newNumber ) ) return false;
    if( selection_flags == null ) selection_flags = new boolean[newNumber];
    if( newNumber > selection_flags.length ){
      boolean[] oldFlags = selection_flags;
      int oldSize = selection_flags.length;
      selection_flags = new boolean[newNumber];
      System.arraycopy( oldFlags, 0, selection_flags, 0, oldSize );
    }
    selection_flags[newNumber-1] = true;
    selectedUnitsCount++;
    return true;
  }

/*----------------------- unit functions -------------------------------------*/
  /**
   * method looks for a unit at a certain position
   *
   * @param units position
   * @return position
   */
  public Unit getUnitAtXY( int[] pos ){
    int noAll = ki.getNoOfUnits();
    for( int i=0; i<noAll; i++ ){
      ki.getUnitPosition( i + 1 );
      if( ki.posX == pos[ 0 ] && ki.posY == pos[ 1 ] ) return new Unit( ki, i + 1 );
    }
    return null;
  }
  /**
   * method returns the unit with the given number
   *
   * @param kernel number of thew unit
   * @return unit with this number
   */
  public Unit getUnitNumber( int unit_number ){
    if( unit_number < 1 || unit_number > ki.getNoOfUnits() ) return null;
    return new Unit(ki, unit_number);
  }

  /**
   * method returns an array of all units
   *
   * @return array of units
   */
  public Unit[] getAllUnits(){
    Unit[] us = new Unit[ ki.getNoOfUnits() ];
    for( int i=0; i<us.length; i++ ) us[i] = new Unit( ki, i+1 );
    return us;
  }
  /**
   * method returns the unit with kernel number 1
   */
  public Unit getFirstUnit(){
    int number =  ki.getFirstUnit();
    return ( number < 1 )? null : new Unit( ki, number);
  }
  /**
   * method returns the unit with the next higher
   * kernel number than the current unit
   */
  public Unit getNextUnit(){
    int number = ki.getNextUnit();
    return ( number < 1 )? null : new Unit( ki, number );
  }
  public Unit getCurrentUnit(){
    int no = ki.getCurrentUnit();
    if( no == 0 ) return null;
    return new Unit(ki, no);
  }

  public void setCurrentUnit( Unit u ){
    ki.setCurrentUnit( u.getNumber() );
  }

  /**
   * method moves an array of units
   *
   * @param units
   * @param vector
   */
  public void moveUnits( Unit[] units, int[] delta ){
    if( delta.length > 3 ) return;
    int[] pos, delta_all = new int[]{ 0, 0, 0 };
    int i, j;
    boolean moveAll = false;

    for( j=0; j<units.length; j++){
      pos = units[ j ].getPosition();
      for( i=0; i<delta.length; i++ ){
        pos[ i ] += delta[ i ];
        if( - pos[ i ] > delta_all[ i ] ) {
          delta_all[ i ] = - pos[ i ];
          moveAll = true;
        }
      }
      units[ j ].setPosition( pos );
    }
    Unit unit;
    if( moveAll ){
      for( unit = getFirstUnit(); unit != null; unit = getNextUnit() ){
        pos = unit.getPosition();
        for( i=0; i<delta.length; i++ )
          pos[ i ] += delta_all[ i ];
        unit.setPosition( pos );
      }
    }
    checkMaxCoord();
    UnitMoveArgument arg = new UnitMoveArgument(units, delta, delta_all );
    fireEvent( NetworkEvent.UNITS_MOVED, arg ) ;
  }

  /**
   * method moves some units by an UnitMoveArgument used by NetworkUndoRedo
   *
   * @param UnitMoveArgument containing units and vectors
   */
  public void moveUnits( UnitMoveArgument arg ){
    int[] pos;
    int i, j;
    Unit unit;

    // some units :
    for( j=0; j<arg.units.length; j++){
      pos = arg.units[ j ].getPosition();
      for( i=0; i<arg.delta.length; i++ ) pos[ i ] += arg.delta[ i ];
      arg.units[ j ].setPosition( pos );
    }

    // all together now :
    for( unit = getFirstUnit(); unit != null; unit = getNextUnit() ){
      pos = unit.getPosition();
      for( i=0; i<arg.delta.length; i++ ) pos[ i ] += arg.delta_all[ i ];
      unit.setPosition( pos );
    }
    checkMaxCoord();
    fireEvent( NetworkEvent.UNITS_MOVED, arg );
  }

  /**
   * method creates a list of units by their data
   *
   * @return array of units
   */
  public Unit[] createUnits( UnitData[] data ){
    Unit[] units = new Unit[ data.length ];
    for( int i=0; i<data.length; i++ ){
      units[ i ] = new Unit( ki, data[ i ] );
      if( data[ i ].pos[0] > max_coord[0] ) max_coord[0] = data[i].pos[0];
      if( data[ i ].pos[1] > max_coord[1] ) max_coord[1] = data[i].pos[1];
    }
    fireEvent( NetworkEvent.UNITS_CREATED, units );
    return units;
  }
  /**
   * recreates the deleted units with their original numbers
   *
   * @return the recreated units
   */
  public Unit[] recreateUnits( UnitDeleteArgument uda ){
    if( under_construction ) System.out.println("Network.recreateUnits(UnitDeleteArgument):");
    Unit unit;
    int i, j, noAll, source;
    UnitData[] uData = uda.uData;
    LinkData[] lData = uda.lData;
    noAll = ki.getNoOfUnits();

    // default units an die bestehenden Units anfügen:
    for( i=0; i<uData.length; i++ ) ki.createDefaultUnit();

    if( noAll > 0 ){
      // Verschiebungstabelle erstellen:
      int[] newNos = new int[ noAll ];
      for( i=0; i<noAll; i++ ) newNos[i] = i+1;
      for( i=uData.length-1; i!=-1; i-- )
        for( j=0; j<noAll; j++ )
          if( newNos[j] >= uData[i].number ) newNos[j]++;
      //System.out.println("Verschiebungstabelle erstellt");

      // bestehende links und units verschieben:
      Vector data = new Vector(), oldSources = new Vector();
      LinkData[] dataA;
      double weight;
      for( i=noAll; i!=0; i-- ){
        ki.setCurrentUnit(i);

        // zuerst die unit:
        unit = new Unit(ki, i);
        if( newNos[i-1] != i ) {
          unit.moveTo( newNos[i-1] );
          //System.out.println("Unit "+i+" um "+(newNos[i-1]-i)+" verschoben");
        }

        // dann die links:
        // relevante links sammeln:
        for( j=ki.getFirstPredUnit(); j!=0; j=ki.getNextPredUnit() ){
          if( newNos[i-1]!=i || newNos[j-1]!=j ){
            oldSources.addElement( new Integer( j ) );
            weight = ki.getLinkWeight();
            data.addElement( new LinkData( newNos[j-1], newNos[i-1], weight ) );
            //System.out.println("("+j+", "+i+") --> ("+source+", "+target+")");
          }
        }

        // an der alten Position löschen:
        for( j=0; j<oldSources.size(); j++ ){
          source = ( (Integer)oldSources.elementAt(j) ).intValue();
          ki.isConnected( source );
          ki.deleteLink();
        }
        oldSources.removeAllElements();
        dataA = new LinkData[ data.size() ];
        for( j=0; j<data.size(); j++ ) dataA[j] = (LinkData)data.elementAt(j);

        // an der neuen Position wieder herstellen:
        createLinks( dataA, false );
        data.removeAllElements();
        //if( dataA.length != 0 )
          //System.out.println(dataA.length+" dazugehoerige links verschoben");
      }
    }

    // gelöschte units einbauen:
    Unit[] units = new Unit[ uData.length ];
    for( i=0; i<uData.length; i++ ){
      unit = new Unit(ki, uData[i].number);
      unit.takeValuesFrom( uData[i] );
      if( uData[i].pos[0] > max_coord[0] ) max_coord[0] = uData[i].pos[0];
      if( uData[i].pos[0] > max_coord[1] ) max_coord[1] = uData[i].pos[0];
      units[i] = unit;
    }

    // gelöschte links einbauen:
    createLinks( lData, false );
    fireEvent( NetworkEvent.UNITS_CREATED, units );
    return units;
  }
  /**
   * method deletes first the links betwenn the selected units
   * and then the units themselves
   */
  public UnitDeleteArgument deleteUnits(){
    return deleteUnits( getSelectedUnits() );
  }

  /**
   * method deletes certain units after deleting their links
   *
   * @return an UnitDeleteArgument used by NetworkUndoRedo
   */
  public UnitDeleteArgument deleteUnits( Unit[] units ){
    LinkData[] lData = deleteLinks( units, false );
    UnitData[] uData = deleteOnlyUnits( units );
    deselectUnits();
    UnitDeleteArgument uda = new UnitDeleteArgument( lData, uData );
    fireEvent( NetworkEvent.UNITS_DELETED, uda );
    return uda;
  }

  private UnitDeleteArgument deleteAllUnits(){
    selectAll();
    return deleteUnits();
  }

  /**
   * method copies an existing unit in a certain mode :
   * with all input and output connections,
   * just input connections,
   * only output connections,
   * only the unit
   *
   * @param number of the original unit
   * @param copy mode
   * @param position
   * @return number of the new unit ( negative when something was wrong )
   */
  public int copyUnit( int orig_unit_number, int mode, int[] pos ){
    int unit_number = ki.copyUnit( orig_unit_number, mode );
    Unit unit = new Unit( ki, unit_number );
    unit.setPosition( pos );
    fireEvent( NetworkEvent.UNITS_CREATED, new Unit[]{ unit } );
    return unit_number;
  }

/*------------------- private unit functions ---------------------------------*/

  /**
   * method deletes certain units
   *
   * @param units to delete
   * @return unit data ( to recreate if necessary )
   */
  private UnitData[] deleteOnlyUnits( Unit[] units ){
    UnitData[] data = new UnitData[ units.length ];
    int[] list = new int[ units.length ];
    sort( units ); // <---- WICHTIG FÜR LAYERS !!!
    for( int i=0; i<data.length; i++ ) {
      list[i] = units[i].getNumber();
      data[i] = new UnitData( units[i] );
    }
    ki.deleteUnitList( list );
    checkMaxCoord();
    return data;
  }

/*------------------------ link functions ------------------------------------*/
  /**
   * method returns the link between two units
   *
   * @return the <code>Link<\code>
   */
  public Link getLink(Unit source, Unit target){
    Link l = null;
    int old = ki.getCurrentUnit();
    ki.setCurrentUnit( target.getNumber() );
    if( ki.isConnected( source.getNumber() ) )
      l = new Link( ki, source.getNumber(), target.getNumber(), false );
    ki.setCurrentUnit( old );
    return l;
  }
  /**
   * method sets the selected source units for the new links
   * and repaints the views
   */
  public void setSourceUnits(Unit[] unit){
    source_units = unit;
    fireEvent( NetworkEvent.SOURCE_UNITS_CHANGED );
  }

  /**
   * method sets the currently selected units as source units for new links
   */
  public void setSourceUnits(){
    source_units = getSelectedUnits();
    deselectUnits();
    fireEvent( NetworkEvent.SELECTED_UNITS_CHANGED );
  }

  /**
   * method sets the selected surce units deselected
   */
  public void deselectSourceUnits(){
    source_units = null;
    fireEvent( NetworkEvent.SOURCE_UNITS_CHANGED );
  }

  /**
   * method returns if a certain unit is selected as source unit
   *
   * @param unit
   * @return unit is selected as source unit
   */
  public boolean isSelectedSourceUnit( Unit unit ){
    if( source_units == null ) return false;
    for( int i=0; i<source_units.length; i++ )
      if( source_units[ i ].getNumber() == unit.getNumber() ) return true;
    return false;
  }

  /**
   * method returns if source units are selected
   */
  public boolean sourceUnitsSelected(){
    return ( source_units != null );
  }

  /**
   * method returns true if units are connected
   */
  public boolean areConnected( int source_unit, int target_unit ){
    return ki.areConnected( source_unit, target_unit );
  }

  public boolean areConnected( Unit source, Unit target ){
    return areConnected( source.getNumber(), target.getNumber() );
  }

  public boolean isConnected( Unit source ){
    return ki.isConnected( source.getNumber() );
  }
  /**
   * method creates new links by given link data
   *
   * @param array of link data objects
   * @return array of new links
   */
  public Link[] createLinks( LinkData[] data ){
    return createLinks( data, true );
  }

  /**
   * method creates new auto-assoziative links between some units
   *
   * @param array of source units
   * @param a flag if the units should be connected with themselves
   * @return array of new links
   */
  public Link[] createLinks( Unit[] units, boolean with_themselves ){
    Link[] links = createLinks( units, units, with_themselves, true );
    deselectUnits();
    return links;
  }

  /**
   * method creates links between the layers
   * without shortcuts means that the layers are connected with the following
   * with shortcuts means lower layers are connected with all higher layers
   *
   * @param including shortcuts <code>true</code> for shortcuts.
   * @return the array of new links
   */
  public Link[] createLinks( boolean shortcuts ){
    Vector links = new Vector();
    Link[] l;
    int i, j, k;
    Layer source, target;
    if( !shortcuts ){
      source = layers.getLayerNo( 1 );
      for( i=0; i<layers.maxLayerNo - 1; i++ ){
        target = layers.getLayerNo( i + 2 );
        if( source != null && target != null ){
          l = createLinks( source.getUnits(), target.getUnits(), true, false );
          for( k=0; k<l.length; k++ ) links.addElement( l[k] );
        }
        source = target;
      }
    }
    else{
      for( i=0; i<layers.maxLayerNo - 1; i++ ){
        source = layers.getLayerNo( i + 1 );
        if( source != null ){
          for( j=i+1; j<layers.maxLayerNo; j++ ){
            target = layers.getLayerNo( j + 1 );
            if( target != null ){
              l = createLinks( source.getUnits(), target.getUnits(), true, false );
              for( k=0; k<l.length; k++ ) links.addElement( l[k] );
            }
          }
        }
      }
    }
    l = new Link[ links.size() ];
    for( i=0; i<l.length; i++ ) l[i] = (Link)links.elementAt(i);
    fireEvent( NetworkEvent.LINKS_CREATED, l );
    return l;
  }

  /**
   * method creates links between selected source units and the currently
   * selected units as target units and deselects the selcted source units afterward
   */
  public Link[] createLinks(){
    if( source_units != null && unitsSelected() ) {
      Unit[] target = getSelectedUnits();
      Link[] links = createLinks( source_units, getSelectedUnits(), true, false );
      deselectSourceUnits();
      deselectUnits();
      fireEvent( NetworkEvent.LINKS_CREATED, links );
      return links;
    }
    else return null;
  }

  /**
   * method deletes an array of links
   *
   * @param array of links to delete
   * @return array of link data objects to recreate the links
   */
  public LinkData[] deleteLinks( Link[] links ){
    LinkData[] data = new LinkData[ links.length ];
    for( int i=0; i<data.length; i++ ){
      data[ i ] = new LinkData( links[ i ] );
      ki.setCurrentUnit( links[i].getTargetUnit().getNumber() );
      if ( ki.isConnected( links[i].getSourceUnit().getNumber() ) ) ki.deleteLink();
    }
    fireEvent( NetworkEvent.LINKS_DELETED, data );
    return data;
  }

  /**
   * method deletes all links connecting the parameter units with the network
   *
   * @return an array of link data to recreate the links
   */
  public LinkData[] deleteLinks( Unit[] units, boolean fire_event ){
    Vector data = new Vector();
    int i, j, source, target;
    Vector links = new Vector();

    // deletes all incoming links to the units
    for( i=0; i<units.length; i++ ){
      target = units[i].getNumber();
      ki.setCurrentUnit( target );
      for( source=ki.getFirstPredUnit(); source!=0; source=ki.getNextPredUnit() )
        links.addElement( new Link(ki, source, target, false) );
      for( j=0; j<links.size(); j++ )
        data.addElement( ( (Link)links.elementAt(j)  ).delete() );
      links.removeAllElements();
    }
    // deletes all outgoing links from the units
    for( i=0; i<units.length; i++ ){
      source = units[i].getNumber();
      ki.setCurrentUnit( source );
      for( target=ki.getFirstSuccUnit( source ); target!=0; target=ki.getNextSuccUnit() )
        links.addElement( new Link(ki, source, target, false) );
      for( j=0; j<links.size(); j++ )
        data.addElement( ( (Link)links.elementAt(j)  ).delete() );
      links.removeAllElements();
    }

    // converts the LinkData Vector into an array
    LinkData[] dataA = new LinkData[ data.size() ];
    for( i=0; i<data.size(); i++ )
      dataA[i] = (LinkData)data.elementAt(i);
    if( fire_event && dataA.length > 0 ) fireEvent( NetworkEvent.LINKS_DELETED, dataA );
    return dataA;
  }

  /**
   * method deletes all links between the selected units
   *
   * @return array of link data objects to recreate the links
   */
  public LinkData[] deleteLinks(){
    Unit[] us = getSelectedUnits();
    Vector links = new Vector();
    for( int i=0; i<us.length; i++ )
      for( int j=0; j<us.length; j++ )
        if( ki.areConnected(us[i].number, us[j].number) )
          links.add( new Link( ki, us[i].number, us[j].number, false ) );
    Link[] l = new Link[links.size()];
    links.toArray(l);
    return deleteLinks( l );
  }




/*----------------------- private link methods -------------------------------*/

  private Link[] createLinks( LinkData[] data, boolean fire_event ){
    Link[] links = new Link[ data.length ];
    for( int i=0; i<data.length; i++ ){
      links[ i ] = new Link( ki, data[ i ] );
    }
    if( fire_event ) fireEvent( NetworkEvent.LINKS_CREATED, links );
    return links;
  }

  private Link[] createLinks( Unit[] sourceUs,
                              Unit[] targetUs,
                              boolean with_themselves,
                              boolean fire_event ){
    Vector data = new Vector();
    Unit source, target;
    boolean connect = true, created = false;
    for( int i=0; i<sourceUs.length; i++ ){
      source = sourceUs[ i ];
      for( int j=0; j<targetUs.length; j++ ){
        target = targetUs[ j ];
        if( !with_themselves ) connect = !( source.getNumber() == target.getNumber() );
        if( connect )
          data.addElement(
            new LinkData( source.getNumber(), target.getNumber(), 0.0 )
          );
        connect = true;
      }
    }
    LinkData[] lData = new LinkData[ data.size() ];
    for( int i=0; i<data.size(); i++ )
      lData[ i ] = (LinkData)data.elementAt( i );
    Link[] links = null;
    links = createLinks( lData, fire_event );
    return links;
  }





/*---------------------- pattern methods -------------------------------------*/

  /**
   * method returns <code>true</code> if the network has patterns
   *
   * @return <code>true</code> if the network has patterns
   */
  public boolean hasPatterns(){ return ( ki.getNoOfPatterns() != 0 ); }
  /**
   * method returns the number of patterns
   *
   * @return the number of patterns
   */
  public int getNumberOfPatterns(){ return ki.getNoOfPatterns(); }

  /**
   * method deletes the current pattern
   */
  public void deletePattern() throws Exception{
    if( ki.getNoOfPatterns() == 0 ) return;
    int no = ki.getPatternNo();
    Pattern pat = new Pattern( this );
    ki.deletePattern();
    if( no > ki.getNoOfPatterns() ) no--;
    if( no > 0 ){
      ki.setPatternNo( no );
      ki.showPattern( 2 );
      fireEvent( NetworkEvent.PATTERN_CHANGED );
    }
    fireEvent( NetworkEvent.PATTERN_DELETED, pat );
  }

  /**
   * method replaces the current pattern of the current set
   * by current activation values
   */
  public void modifyPattern(){
    Pattern pat = new Pattern( this );
    ki.modifyPattern();
    fireEvent( NetworkEvent.PATTERN_MODIFIED, pat );
  }

  /**
   * method creates a new pattern by current activation values
   */
  public void createPattern() throws Exception{
    if( under_construction ) System.out.println("Network.createPattern()");
    if( ki.getCurrPatternSet() == null )
      throw new Exception("No pattern set defined");
    ki.newPattern();
    fireEvent( NetworkEvent.PATTERN_CREATED );
  }

  /**
   * method (re-)creates a pattern set by values
   *
   * @param a Pattern object
   */
  public void createPattern( Pattern pat ){
    if( pat.input.length != ki.getNoOfInputUnits() ) return;
    if( pat.output.length != ki.getNoOfOutputUnits() ) return;
    Unit[] units = getInputUnits();
    int i;
    for( i = 0; i< units.length; i++ )
      ki.setUnitActivation( units[ i ].getNumber(), pat.input[ i ] );
    units = getOutputUnits();
    for( i = 0; i< units.length; i++ )
      ki.setUnitActivation( units[ i ].getNumber(), pat.output[ i ] );
    ki.newPattern();
    fireEvent( NetworkEvent.PATTERN_CREATED );
  }

  /**
   * method replaces an existing pattern by the values of
   * a <code>Pattern</code> object
   *
   * @param the Pattern object
   */
  public Pattern replacePattern( Pattern pat ){
    if( pat.input.length != ki.getNoOfInputUnits() ) return null;
    if( pat.output.length != ki.getNoOfOutputUnits() ) return null;
    Pattern old_pat = new Pattern( this );
    Unit[] units = getInputUnits();
    int i;
    for( i = 0; i< units.length; i++ )
      ki.setUnitActivation( units[ i ].getNumber(), pat.input[ i ] );
    units = getOutputUnits();
    for( i = 0; i< units.length; i++ )
      ki.setUnitActivation( units[ i ].getNumber(), pat.output[ i ] );
    ki.modifyPattern();
    fireEvent( NetworkEvent.PATTERN_MODIFIED, pat );
    return old_pat;
  }

  /**
   * Makes pattern with the given number current.
   *
   * @param the pattern number
   * @param only_input <code>true</code> if only the input pattern should be set
   *
   * @return <code>true</code> if this was possible
   */
  public boolean setPattern(int patNr, boolean only_input) throws Exception{
    if( under_construction ) System.out.println("Network.setPattern("+patNr+", "+only_input+")");
    if( ki.getNoOfUnits() == 0 ) throw new Exception("No network defined");
    if( ki.getNoOfPatternSets() == 0 ) return false;
    int tot_pat = ki.getNoOfPatterns();
    if( patNr < 1 || patNr > tot_pat ) return false;
    try{
      int old = getCurrentPatternNo();
      ki.setPatternNo(patNr);
      if(only_input) ki.showPattern(1);  // OUTPUT_NOTHING
      else           ki.showPattern(2);  // OUTPUT_ACT
      if(old != patNr) fireEvent( NetworkEvent.PATTERN_CHANGED, new Integer( old ) );
      return true;
    }
    catch( Exception e ){ return false; }
  }

  /**
   * Sets the previous pattern in the set.
   *
   * @return <code>true</code> if this was possible
   */
  public boolean setPreviousPattern(boolean only_input) throws Exception{
    return setPattern( getCurrentPatternNo() - 1, only_input );
  }


  /**
   * Sets the next pattern in the set.
   *
   * @return <code>true</code> if this was possible
   */
  public boolean setNextPattern(boolean only_input) throws Exception{
    return setPattern( getCurrentPatternNo() + 1, only_input );
  }

  public boolean setFirstPattern(boolean only_input) throws Exception{
    return setPattern(1, only_input);
  }
  public boolean setLastPattern(boolean only_input) throws Exception{
    return setPattern(ki.getNoOfPatterns(), only_input);
  }

  public int getCurrentPatternNo(){
    if( under_construction ) System.out.println("Network.getCurrentPatternNo()");
    if( ki.getNoOfPatterns() == 0 ) return 0;
    return ki.getPatternNo();
  }

  public KernelInterface.KernelPatternInfo getPatInfo(){
    if( under_construction ) System.out.println("Network.getPatInfo()");
    if( ki.getNoOfPatternSets() == 0 /*|| ki.getNoOfPatterns() == 0*/ )
      return null;
    KernelInterface.KernelPatternInfo kpi = null;
    try{ kpi = ki.getPatInfo(); }
    catch( KernelInterface.KernelException ex ){ return null; }
    return kpi;
  }

  public int defTrainSubPat(int[] insize, int[] outsize, int[] instep, int[] outstep) {
    if( under_construction ) System.out.println("Network.defTrainSubPat()");
    return ki.defTrainSubPat(insize, outsize, instep, outstep);
  }

  public void defShowSubPat(int[] insize, int[] outsize, int[] inpos, int[] outpos) throws Exception {
    if( under_construction ) System.out.println("Network.defShowSubPat()");
    if( ki.getNoOfUnits() == 0 ) throw new Exception("No network defined");
    if( ki.getNoOfPatternSets() == 0 ) return;
    ki.defShowSubPat(insize, outsize, inpos, outpos);
    ki.showPattern(2);
    fireEvent(NetworkEvent.SUBPATTERN_CHANGED);
  }

  /**
   * Sets the subpattern learning and displaying scheme.
   * Not a very elegant way, actually a callback method
   * for training and analyzer.
   */
  public void setSubPatternScheme() {
    if( under_construction ) System.out.println("setSubpatternSheme()");
    snns.master.sp.setSubPatternScheme();
  }



/*------------------------ interfaces ----------------------------------------*/
// implementing LASAdapter :

  public String getLASName(){ return net_name + " network"; }

  public String getKeyword(){
    return "SNNS network definition file";
  }
  public String getFileExtension(){
    return "net";
  }
  public String getDescription(){
    return "Network files *.net";
  }
  public JPanel getAccessory(){
    return accessory = new NetworkAccessory( this );
  }
  public void save() throws IOException{
    if( homeFile == null ) throw new IOException( "network has no home file" );
    save( homeFile );
  }
  public void save( File file ) throws IOException{
    String path = file.getCanonicalPath();
    if( accessory != null ) {
      setName( accessory.getName(), true );
      accessory = null;
    }
    ki.saveNet( path, net_name );
    content_changed = false;
  }
  public void load( File file ) throws Exception{
    if( under_construction ) System.out.println("Network.load(File)");
    boolean really = true;
    if( content_changed ) really = snns.askForSaving( this );
    if( really ){
      String path = file.getCanonicalPath();
      setName( ki.loadNet( path ), false );
      content_changed = false;
      homeFile = file;
      selection_flags = new boolean[ ki.getNoOfUnits() ];
      selectedUnitsCount = 0;
      checkMaxCoord();
      updateFnList( Function.LEARN );
      updateFnList( Function.UPDATE );
      fireEvent( NetworkEvent.NEW_NETWORK_LOADED, getName() );
    }
  }

/*----------------------------------------------------------------------------*/

  private void checkMaxCoord(){
    max_coord[0] = max_coord[1] = -1;
    for( int i=1; i<ki.getNoOfUnits() + 1; i++ ){
      ki.getUnitPosition( i );
      if( ki.posX > max_coord[0] ) max_coord[0] = ki.posX;
      if( ki.posY > max_coord[1] ) max_coord[1] = ki.posY;
    }
  }

  /**
   * method sorts the units from higher to lower number
   */
  private void sort( Unit[] units ){
    int i, j;
    Unit unit;
    for( i=0; i<units.length - 1; i++ )
      for( j=i+1; j<units.length; j++ ){
        if( units[ i ].getNumber() < units[ j ].getNumber() ){
          unit = units[ i ];
          units[ i ] = units[ j ];
          units[ j ] = unit;
        }
      }
  }


  class NetChangeListener implements NetworkListener {
    public NetChangeListener() {
      addListener(this );
    }
    public void networkChanged(NetworkEvent evt) {
      switch(evt.id){
        case NetworkEvent.LINKS_CREATED   :
        case NetworkEvent.LINKS_DELETED   :
        case NetworkEvent.NETWORK_PRUNED  :
        case NetworkEvent.NETWORK_TRAINED :
        case NetworkEvent.UNITS_CREATED   :
        case NetworkEvent.UNITS_DELETED   :
        case NetworkEvent.UNITS_MOVED     :
          content_changed = true;
      }
    }
  }
}

