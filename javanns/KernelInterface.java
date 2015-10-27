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

import java.io.File;


/**
 * Interface class to SNNS kernel. Defines native methods for
 *  accessing SNNS kernel function interface.
 */
public class KernelInterface {
  public KernelInterface() {
    sseArr = new double[100];
  }
  // Unit functions
  public native int     getNoOfUnits();
  public native int     getNoOfSpecialUnits();
  public native int     getFirstUnit();
  public native int     getNextUnit();
  public native void    setCurrentUnit(int unitNo);
  public native int     getCurrentUnit();
  public native String  getUnitName(int unitNo);
  public native void    setUnitName(int unitNo, String unitName);
  public native int     searchUnitName(String unitName);
  public native int     searchNextUnitName();
  public native String  getUnitOutFuncName(int unitNo);
  public native String  getUnitActFuncName(int unitNo);
  public native void    setUnitOutFunc(int unitNo, String unitFnName);
  public native void    setUnitActFunc(int unitNo, String unitFnName);
  public native String  getUnitFTypeName(int unitNo);
  public native double  getUnitActivation(int unitNo);
  public native void    setUnitActivation(int unitNo, double unitAct);
  public native double  getUnitInitialActivation(int unitNo);
  public native void    setUnitInitialActivation(int unitNo, double unitIAct);
  public native double  getUnitOutput(int unitNo);
  public native void    setUnitOutput(int unitNo, double unitOut);
  public native double  getUnitBias(int unitNo);
  public native void    setUnitBias(int unitNo, double unitBias);
  public native int     getUnitSubnetNo(int unitNo);
  public native void    setUnitSubnetNo(int unitNo, int subnetNo);
  public native int     getUnitLayerNo(int unitNo);
  public native void    setUnitLayerNo(int unitNo, int layerNo);
  public native void    getUnitPosition(int unitNo);
  public native void    setUnitPosition(int unitNo);
  public native int     getUnitNoAtPosition(int subnetNo);
  public native int     getUnitNoNearPosition(int subnetNo, int range, int gridWidth);
  //  public native void getUnitCenters(int unitNo, int centerNo, PositionVector unitCenter);
  //  public native void setUnitCenters(int unitNo, int centerNo, PositionVector unitCenter);
  //  public native void getXYTransTable(dummy);
  public native int     getUnitTType(int unitNo);
  public native void    setUnitTType(int unitNo, int unitTType);
  public native void    freezeUnit(int unitNo);
  public native void    unfreezeUnit(int unitNo);
  public native boolean isUnitFrozen(int unitNo);
  public native int     getUnitInputType(int unitNo);
  public native double  getUnitValueA(int unitNo);
  public native void    setUnitValueA(int unitNo, double unitValueA);
  public native int     createDefaultUnit();
  public native int     createUnit(String name, String outFn, String actFn, double iAct, double bias);
  public native int     createFTypeUnit(String ftName);
  public native void    setUnitFType(int unitNo, String ftName);
  public native int     copyUnit(int unitNo, int copyMode);
  public native void    deleteUnitList(int unitNos[]);

  // Site functions
  public native void    createSiteTableEntry(String siteName, String siteFn);
  public native void    changeSiteTableEntry(String oldSiteName, String newSiteName, String newSiteFn);
  public native void    deleteSiteTableEntry(String siteName);
  public native boolean getFirstSiteTableEntry();
  public native boolean getNextSiteTableEntry();
  public native String  getSiteTableFuncName(String siteName);
  public native boolean setFirstSite();
  public native boolean setNextSite();
  public native boolean setSite(String siteName);
  public native double  getSiteValue();
  public native String  getSiteFuncName();
  public native String  getSiteName();
  public native void    setSiteName(String siteName);
  public native void    addSite(String siteName);
  public native boolean deleteSite();


  // Link functions
  public native int     getFirstPredUnit();
  //  public native int     getFirstPredUnitAndData();
  public native int     getNextPredUnit();
  //  public native int getNextPredUnitAndData();
  public native int     getCurrentPredUnit();
  public native int     getFirstSuccUnit(int unitNo);
  public native int     getNextSuccUnit();
  public native boolean isConnected(int sourceUnitNo);
  public native boolean areConnected(int sourceUnitNo, int targetUnitNo);
  public native boolean areConnectedWeight(int sourceUnitNo, int targetUnitNo);
  public native double  getLinkWeight();
  public native void    setLinkWeight(double weight);
  public native void    createLink(int sourceUnitNo, double weight);
  //  public native void createLinkWithAdditionalParameters(int sourceUnitNo, double weight, double a, double b, double c);
  public native void    deleteLink();
  public native void    deleteAllInputLinks();
  public native void    deleteAllOutputLinks();
  public native void    jogWeights(double minus, double plus);
  public native void    jogCorrWeights(double minus, double plus, double mincorr);


  // Prototype functions
  public native boolean setFirstFTypeEntry();
  public native boolean setNextFTypeEntry();
  public native boolean setFTypeEntry(String name);
  public native String  getFTypeName();
  public native void    setFTypeName(String name);
  public native String  getFTypeActFuncName();
  public native void    setFTypeActFunc(String name);
  public native String  getFTypeOutFuncName();
  public native void    setFTypeOutFunc(String name);
  public native boolean setFirstFTypeSite();
  public native boolean setNextFTypeSite();
  public native String  getFTypeSiteName();
  public native void    setFTypeSiteName(String name);
  public native void    createFTypeEntry(String fType, String actFn, String outFn, String siteNames[]);
  public native void    deleteFTypeEntry(String fType);


  // Function-table functions
  public native int     getNoOfFunctions();
  public native void    getFuncInfo(int funcNo);
  public native boolean isFunction(String fnName, int fnType);
  public native boolean getFuncParamInfo(String fnName, int fnType);


  // Initialization functions:
  public native void    setInitFunc(String function_name, double[] params);  // instead of setInitializationFunc
  public native String  getInitFunc();              // instead of getInitializationFunc
  public native void    initNet() throws KernelException; // instead of initializeNet


  // Activation propagation functions
  public native void    updateSingleUnit(int unitNo);
  public native String  getUpdateFunc();
  public native void    setUpdateFunc(String function_name, double[] params);
  public native void    updateNet() throws KernelException;


  // Learning and pruning functions
  public native void    setLearnFunc(String function_name, double[] params);
  public native void    setFFLearnFunc(String function_name, double[] params);
  public native void    setPruningFunc(String prune_func, String learn_func,
                          double pmax_error_incr, double paccepted_error,
                          boolean precreatef, int pfirst_train_cyc,
                          int pretrain_cyc, double pmin_error_to_stop,
                          double pinit_matrix_value, boolean pinput_pruningf,
                          boolean phidden_pruningf);
  public native String  getLearnFunc();
  public native String  getFFLearnFunc();
  public native String  getPrunFunc();
  public native void    trainNet(int steps) throws KernelException;   // instead of learnAllPatterns
  public native void    trainFFNet(int steps);                        // instead of learnAllPatternsFF
  public native void    trainNet(int pat, int steps) throws KernelException;// instead of learnSinglePattern
  public native void    trainFFNet(int pat, int steps);               // instead of learnSinglePatternFF

  public native void    trainNetFixedTime(int steps, int time) throws KernelException;
  public native void    trainNetFixedTime(int pat, int steps, int time) throws KernelException;

  // Pattern manipulation functions:
  public native void    setPatternNo(int patNo) throws KernelException;
  public native int     getPatternNo();
  public native void    deletePattern();
  public native void    modifyPattern();
  public native void    setRemapFunc(String name, double params[]) throws KernelException;
  public native void    showPattern(int mode) throws KernelException;
  public native void    newPattern();
  public native void    deleteAllPatterns();
  public native void    setShuffle(boolean mode);          // instead of shufflePatterns
  public native void    setSubShuffle(boolean mode);       // instead of shuffleSubPatterns
  public native int     getNoOfPatterns();
  public native int     getTotalNoOfSubPatterns();
  public native int     allocNewPatternSet(String patternFilename);
  public native void    setPattern(String patternFilename);  // instead of setCurrPatSet
  public native void    delPattern(String patternFilename);  // instead of deletePatSet
  public native KernelPatternInfo getPatInfo() throws KernelException;
  public native void    defShowSubPat(int[] insize, int[] outsize, int[] inpos, int[] outpos);
  public native int     defTrainSubPat(int[] insize, int[] outsize, int[] instep, int[] outstep);
  public native int     alignSubPat(int[] inpos, int[] outpos);
  public native KernelSubPatShape getShapeOfSubPattern(int n_pos);
  public native void    setClassDistribution(int[] classDist);
  public native void    setClassInfo(String info);
  public native void    useClassDistribution(boolean flag);


  // File I/O functions
  public native String  loadNet(String filename) throws KernelException;
  public native void    saveNet(String filename, String netname);
  public native void    loadPattern(String filename) throws KernelException;     // instead of loadNewPatterns
  public native void    savePattern(String filename, String patternname);  // instead of saveNewPatterns
  public native void    saveResult(String filename,
                          int startpat, int endpat,
                          boolean inclin, boolean inclout,
                          String file_mode);                               // instead of saveResultParam


  // Symbol table functions
  // boolean getFirstSymbolTableEntry(char **name, int *type);
  // boolean getNextSymbolTableEntry(char **name, int *type);
  public native boolean symbolSearch(String symbol, int type);


  // Miscelaneous other interface functions
  public native String  getVersion();
  public native void    getUnitDefaults();
  public native void    setUnitDefaults(double act, double bias,
                          int io, int subnet, int layer,
                          String actFn, String outFn);
  public native void    setSeed(int seed);
  public native int     getNoOfInputUnits();
  public native int     getNoOfOutputUnits();
  public native int     getNoOfSpecialInputUnits();
  public native int     getNoOfSpecialOutputUnits();
  public native void    resetNet();


  // Memory management functions
  public native void    allocateUnits(int n);
  //public native void    getMemoryManagerInfo(int *unit_bytes, int *site_bytes,
  //                        int link_bytes, int NTable_bytes,
  //                        int STable_bytes, int FTable_bytes);
  public native void    deleteNet();


  // ART interface functions
  //public native void getClassifiedStatus(art_cl_status *status);
  public native int     getClassNo();
  public native int     getN();
  public native int     getM();
  public native int     getNa();
  public native int     getMa();
  public native int     getNb();
  public native int     getMb();


  // Undocumented but useful functions

  /**
   * Returns training error of the network / unit. Used by analyzer.
   *
   * @param currPatt    current pattern
   * @param unitNo      unit (neuron) number
   * @param errorType   type of error to compute: network absolute (1), network squared (2) or unit absolute (3)
   * @param average     <code>true</code> if error should be divided by the number of output units
   * @return network or unit training error
   */
  public native double  analyzer_error(int currPatt, int unitNo, int errorType, boolean average);


  // variables:
  public String siteName, siteFunction; // set by getFirstSiteTableEntry and getNextSiteTableEntry
  public String functionName = "";      // set by getFuncInfo
  public int functionType;              // set by getFuncInfo
  public int function_inputs;           // set by getFuncParamInfo
  public int function_outputs;          // set by getFuncParamInfo
  public int sites;                     // set by getNetInfo
  public int links;                     // set by getNetInfo
  public int symbols;                   // set by getNetInfo
  public int functions;                 // set by getNetInfo
  public double defaultActivation;      // set by getUnitDefaults
  public double defaultBias;            // set by getUnitDefaults
  public int defaultIOType;             // set by getUnitDefaults
  public int defaultSubnet;             // set by getUnitDefaults
  public int defaultLayer;              // set by getUnitDefaults
  public String defaultActFunction;     // set by getUnitDefaults
  public String defaultOutFunction;     // set by getUnitDefaults


  // from Batchman, without implementation in kernel interface:
  public native void setSubPattern(String list_of_params);
  public native void setParallelMode(boolean mode);
  public native void setCascadeParams(double max_outp_uni_error,
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
                                      double[] modParams,
                                      boolean cacheUnitAct);
  public native void testNet();         // instead of testAllPatterns

  /**
   * Prunes the network.
   */
  public native void pruneNet();

  /**
   * Performs the initial step for network pruning.
   * Used instead of <code>pruneNet</code> to allow for display update
   * during pruning.
   *
   * @return maximum accepted error
   */
  public native double pruneNet_FirstStep();

  /**
   * Performs a single step for network pruning.
   * Used instead of <code>pruneNet</code> to allow for display update
   * during pruning.
   *
   * @return current network error
   */
  public native double pruneNet_Step();

  /**
   * Performs the last step for network pruning.
   * Used instead of <code>pruneNet</code> to allow for display update
   * during pruning.
   */
  public native void   pruneNet_LastStep();

  public native void pruneTrainNet();
  public native void pruneNetNow();
  public native void delCandUnits();
  //public native void setActFunc(arglist_type *arglist);


  // extra methods for JavaGUI
  public native int  getCurrPatternSetNo();
  public native int  getNoOfPatternSets();
  public native String getPatternSet(int setNo);
  public native String getCurrPatternSet();
  public native void renamePatternSet(int setNo, String newName);
  public native void renamePatternSet(String name, String newName);
  public native void renameCurrPatternSet(String newName);
  public native void resetSNNS();

  /////////////////////////////////////////////////////////////////////////////////////////////
  // GUI related variables:
  public int posX, posY, posZ;          // used and set by getUnitPosition and getUnitNoAtPosition
  public int cycles;
  public int subpatterns;
  public int output_units;
  public double link_weight;            // set by getFirstPredUnit and getNextPredUnit
  public double activation, init_activation, output, bias, sse, mse, ssepu;
  public double[] sseArr;
  public int steps_done;

  /////////////////////////////////////////////////////////////////////////////////////////////
  // method for loading of native library

  public void loadLibrary(String path) throws UnsatisfiedLinkError {
    if(Snns.applet != null) path = System.getProperty("user.dir", "");
    if( path == null ) System.loadLibrary(Snns.LIBRARY_NAME);
    else System.load(
      path + File.separatorChar + System.mapLibraryName(Snns.LIBRARY_NAME)
    );
  }




  /**
   * Exception thrown by kernel native methods.
   */
  static public class KernelException extends Exception {
    public KernelException(String msg) {
      super(msg);
    }
  }



  /**
   * Java counterpart for pattern_set_info and pattern_descriptor
   *   of the kernel.
   */
  static public class KernelPatternInfo {
    // Take care that the following constants have the same value as in the
    // native part (defined in glob_typ.h):
    public static final int MAX_NO_OF_VAR_I_DIM = 2;
    public static final int MAX_NO_OF_VAR_O_DIM = 2;
    public static final int NO_OF_REMAP_PARAMS  = 5;

  ///////////////////////////////////////////////////////////////////////////
  // pattern_set_info:
    int number_of_pattern;     // the number of patterns (pairs) in this set
    int virtual_no_of_pattern; // the number of patterns (pairs) if class_distrib_active == TRUE
    boolean output_present;    // TRUE if output pattern present
    boolean fixed_fixsizes;    // TRUE if the fixsizes of all pattern are equal
    int in_fixsize;            // if fixed_fixsizes TRUE, fixsize of the input pattern, else -1
    int out_fixsize;           // if fixed_fixsizes TRUE, fixsize of the output pattern, else -1
    int in_number_of_dims;     // number of variable input dimensions
    int out_number_of_dims;    // number of variable output dimensions
    int[] in_max_dim_sizes     // maximum size for each variable input dimension
      = new int[MAX_NO_OF_VAR_I_DIM];
    int[] out_max_dim_sizes    // maximum size for each variable output dimension
      = new int[MAX_NO_OF_VAR_O_DIM];
    int[] in_min_dim_sizes     // minimum size for each variable input dimensions
      = new int[MAX_NO_OF_VAR_I_DIM];
    int[] out_min_dim_sizes    // minimum size for each variable output dimensions
      = new int[MAX_NO_OF_VAR_O_DIM];

    int classes;                  // number of pattern classes if > 0
    String[] class_names          // array of <classes> class names, ordered
      = new String[2];            //   (kernel expects the array to exist, so create one)
    boolean class_distrib_active; // class amount redistribution is active
    int[] class_redistribution    // amounts for redistrib. <classes> entries
      = new int[2];               //   (kernel expects the array to exist, so create one)

    String remap_function;        // name of remap function or NULL
    double[] remap_params         // remap function parameters
      = new double [NO_OF_REMAP_PARAMS];
    int no_of_remap_params;       // number of remap function parameters


    ///////////////////////////////////////////////////////////////////////////
    // pattern_descriptor:
    int input_dim;             // number of variable input dimensions
    int[] input_dim_sizes
      = new int[MAX_NO_OF_VAR_I_DIM];  // actual size of each variable input dimensions
    int input_fixsize;         // size of the fixed part of the in pattern (0 if no input pattern present)

    int output_dim;            // number of variable output dimensions
    int[] output_dim_sizes     // actual size of each variable output dimensions
      = new int[MAX_NO_OF_VAR_O_DIM];
    int output_fixsize;        // size of the fixed part of the out pattern (0 if no output pattern present)
    int my_class;              // class index of this pattern if classes available, -1 otherwise
  }



  /**
   * Object returned from getShapeOfSubPattern.
   */
  static public class KernelSubPatShape {
    int[] insize  = new int[KernelPatternInfo.MAX_NO_OF_VAR_I_DIM];
    int[] outsize = new int[KernelPatternInfo.MAX_NO_OF_VAR_O_DIM];
    int[] inpos   = new int[KernelPatternInfo.MAX_NO_OF_VAR_I_DIM];
    int[] outpos  = new int[KernelPatternInfo.MAX_NO_OF_VAR_O_DIM];
  }

}