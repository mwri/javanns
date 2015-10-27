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
import java.awt.event.* ;
import java.awt.* ;
import javax.swing.border.* ;


/**
 * class CCPanel is the defining class for the layout of the cascade correlation
 * panel
 */
class CCPanel extends JTabbedPane implements ActionListener{
  Snns snns;
  CCFunctions functions;
  JPanel pGeneral, pMod, pCand, pOut, pInit, pLearn;
  JInternalFrame frame;

  // wird zur Erkennung in Snns.actionPerformed(...) benutzt:
  public static final String TITLE = "Cascade Correlation & TACOMA";

  double maxOutErr, minCovCh, errCh;
  double minInit, maxInit;

  int candPat, maxCovUp, maxCandNo, outPat, maxEpochs, cascades;

  double[] param = new double[5],
           learnParam = new double[5];

  ThreadChief tc;
  volatile boolean is_learning = false;


  Function initFn, updateFn, learnFnCascade, learnFnTacoma;

  // components for "General" panel:
  JTextField tMaxOutErr;
  JCheckBox cbCovNErr, cbCache, cbPrune;
  JLabel lMini;
  NamedComboBox ncbMini;

  // components for "Modification" panel:
  NamedComboBox ncbMod;
  JLabel[] lParam = new JLabel[5];
  JTextField[] tParam = new JTextField[5];

  // components for "Candidates" panel:
  JTextField tMinCovCh, tCandPat, tMaxCovUp, tMaxCandNo;
  NamedComboBox ncbActFn;

  // components for "Output" panel:
  JTextField tErrCh, tOutPat, tMaxEpochs;

  // components for "Init" panel:
  JTextField tMinInit, tMaxInit;

  // components for "Learn" panel:
  NamedComboBox ncbLearnFn;
  JLabel[] lLearnParam;
  JTextField[] tLearnParam;
  JTextField tCascades;

  Insets insets;
  JButton bInit, bLearn;

  public CCPanel( Snns s ) {
    snns = s;
    functions = new CCFunctions( snns );
    insets = new Insets(3, 3, 2, 2); // snns.panel_insets;

    initFn = new Function(snns, Function.INIT, "CC_Weights");
    learnFnCascade = new Function(snns, Function.LEARN, "CC", "snn-77-1.html");  /* !!!!!!!!!!!!!!!!!!!!!! */
    learnFnTacoma  = new Function(snns, Function.LEARN, "TACOMA");
    updateFn = new Function(snns, Function.UPDATE, "CC_Order");

    JPanel pMain = new JPanel();
    buildGeneral();
    buildMod();
    buildCand();
    buildOut();
    buildInit();
    buildLearn();

    setDefaultValues();

    addTab("General", pGeneral);
    addTab("Modification", pMod);
    addTab("Candidates", pCand);
    addTab("Output", pOut);
    addTab("Init", pInit);
    addTab("Learn", pLearn);

    setBorder(new EmptyBorder(insets));
    pMain.setLayout(new CenterLayout());
    pMain.add( this );

    actionPerformed( new ActionEvent( ncbMod, 0, "" ) );
    actionPerformed( new ActionEvent( ncbLearnFn, 0, "" ) );

    frame = new JInternalFrame( TITLE, false, true, false, true );
    frame.setContentPane( pMain );
    frame.pack();

    tc = new ThreadChief(){
      public void stopped(Object o){
        if( o instanceof Exception )
          snns.showException((Exception)o, this);
        is_learning = false;
        bLearn.setText("Learn");
      }
    };
    tc.sleep = 100;
    tc.awake = 1;
  }
/*------------------------ interfaces ----------------------------------------*/

  public void actionPerformed( ActionEvent evt ){
    //System.out.println("CascadeCorr.actionPerf()");
    Object src = evt.getSource();

    if(src == cbPrune) {
      ncbMini.setEnabled(cbPrune.isSelected());
      lMini.setEnabled(cbPrune.isSelected());
    }

    if( src == ncbMod ){
      SimpleFunction f = (SimpleFunction)ncbMod.getSelectedObject();
      if( f == null ) return;
      int i;
      Parameter parameter;
      for( i=0; i<f.parameter.length; i++ ){
        parameter = f.parameter[i];
        lParam[i].setVisible( true );
        lParam[i].setText( parameter.parStr );
        lParam[i].setToolTipText( parameter.toolTip );
        tParam[i].setVisible( true );
        tParam[i].setText( String.valueOf( parameter.getDefaultValue() ) );
        tParam[i].setToolTipText( parameter.toolTip );
      }
      for( i=f.parameter.length; i<5; i++ ){
        lParam[i].setVisible( false );
        tParam[i].setVisible( false );
      }
      if(f.getKernelName().equals("TACOMA")) {
        cbPrune.setEnabled(false);
        ncbMini.setEnabled(false);
        lMini.setEnabled(false);
      }
      else {
        cbPrune.setEnabled(true);
        ncbMini.setEnabled(cbPrune.isSelected());
        lMini.setEnabled(cbPrune.isSelected());
      }
    }

    else if( src == ncbLearnFn ){
      SimpleFunction f = (SimpleFunction)ncbLearnFn.getSelectedObject();
      if( f == null ) return;
      int i;
      Parameter parameter;
      for( i=0; i<f.parameter.length; i++ ){
        parameter = f.parameter[i];
        lLearnParam[i].setVisible( true );
        lLearnParam[i].setText( parameter.parStr );
        lLearnParam[i].setToolTipText( parameter.toolTip );
        tLearnParam[i].setVisible( true );
        tLearnParam[i].setText( String.valueOf( parameter.getDefaultValue() ) );
        tLearnParam[i].setToolTipText( parameter.toolTip );
      }
      for( i=f.parameter.length; i<5; i++ ){
        lLearnParam[i].setVisible( false );
        tLearnParam[i].setVisible( false );
      }
    }

    else if( src == bInit ){
      Network network = snns.network;

      try{ minInit = Double.valueOf( tMinInit.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Minimum initializing activation value has to be number");
        return;
      }
      try{ maxInit = Double.valueOf( tMaxInit.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Maximum initializing activation value has to be a number");
        return;
      }

      // sollte eigentlich der kernel übernehmen:
      network.deselectUnits();
      int type;
      for( Unit u=network.getFirstUnit(); u!=null; u=network.getNextUnit() ){
        type = u.getType();
        if( !( type == UnitTTypes.INPUT || type == UnitTTypes.OUTPUT ) )
          network.selectUnit( u );
      }
      network.deleteUnits();

      try{
        network.setFunction( initFn, new double[]{ minInit, maxInit, 0, 0, 0 } );
        network.initNet();
      }
      catch( Exception e ){
        showException( "Couldn't initialize the network\n"+e.getMessage() );
      }
    }

    else if( src == bLearn ){
      if( is_learning ) tc.stop = true;
      else learn();
    }
  }

/*---------------------- private methods -------------------------------------*/

  /**
   * this method checks the value entries of the panels and puts them to the
   * kernel to train the net
   */
  private void learn(){
    Network network = snns.network;
    Function learnFn;

    try{ maxOutErr = Double.valueOf( tMaxOutErr.getText() ).doubleValue(); }
    catch( Exception e ){
      showException("Maximum output unit error has to be a double value");
      return;
    }
    try{ minCovCh = Double.valueOf( tMinCovCh.getText() ).doubleValue(); }
    catch( Exception e ){
      showException("Minimum covariance change has to be a double value");
      return;
    }
    try{ errCh = Double.valueOf( tErrCh.getText() ).doubleValue(); }
    catch( Exception e ){
      showException("Error change has to be a double value");
      return;
    }
    try{ candPat = Integer.parseInt( tCandPat.getText() ); }
    catch( Exception e ){
      showException("Candidate patience has to be an integer value");
      return;
    }
    try{ maxCovUp = Integer.parseInt( tMaxCovUp.getText() ); }
    catch( Exception e ){
      showException("Maximum number of covariance updates has to be an integer value");
      return;
    }
    try{ maxCandNo = Integer.parseInt( tMaxCandNo.getText() ); }
    catch( Exception e ){
      showException("Maximum number of candidate units has to be an integer value");
      return;
    }
    try{ outPat = Integer.parseInt( tOutPat.getText() ); }
    catch( Exception e ){
      showException("Output patience has to be an integer value");
      return;
    }
    try{ maxEpochs = Integer.parseInt( tMaxEpochs.getText() ); }
    catch( Exception e ){
      showException("Maximum number of epochs has to be an integer value");
      return;
    }

    String learn_func, actfunc, mini_func, modification;
    SimpleFunction f = (SimpleFunction)ncbLearnFn.getSelectedObject();
    if( f == null ) {
      showException("No learning function selected");
      return;
    }
    learn_func = f.kernel_name;

    f = (SimpleFunction)ncbActFn.getSelectedObject();
    if( f == null ) {
      showException("No activation function selected");
      return;
    }
    actfunc = f.kernel_name;

    f = (SimpleFunction)ncbMini.getSelectedObject();
    if( f == null ) {
      showException("No minimalizing function selected");
      return;
    }
    mini_func = f.kernel_name;

    f = (SimpleFunction)ncbMod.getSelectedObject();
    if(f == null) {
      showException("No modification selected");
      return;
    }
    modification = f.kernel_name;
    if(modification.equals("TACOMA")) learnFn = learnFnTacoma;
    else learnFn = learnFnCascade;

    boolean print_covar = cbCovNErr.isSelected(),
            cacheUnitAct = cbCache.isSelected(),
            prune_new_hidden = cbPrune.isSelected();

    double[] p = new double[5];
    try{
      for( int i=0; i<5; i++ )
      p[i] = Double.valueOf( tLearnParam[i].getText() ).doubleValue();
    }
    catch( Exception e ){
      showException("Learn parameters have to be numbers");
      return;
    }

    try{
      cascades = Integer.parseInt( String.valueOf( tCascades.getText() ) );
    }
    catch( Exception e ){
      showException("Number of cascades has to be an integer");
      return;
    }

    double[] modParam = new double[5];
    try{
      for( int i=0; i<5; i++ )
      modParam[i] = Double.valueOf( tLearnParam[i].getText() ).doubleValue();
    }
    catch( Exception e ){
      showException("Modification parameters have to be numbers");
      return;
    }

    network.setCascadeParams(maxOutErr,
                        learn_func,
                        print_covar,
                        prune_new_hidden,
                        mini_func,
                        minCovCh,
                        candPat,
                        maxCovUp,
                        maxCandNo,
                        actfunc,
                        errCh,
                        outPat,
                        maxEpochs,
                        modification,
                        modParam,
                        cacheUnitAct);

    bLearn.setText("Stop");
    tc.stop = false;
    is_learning = true;

    try{
      network.setFunction( updateFn, new double[] { 0, 0, 0, 0, 0 } );
      network.setFunction( learnFn, p );
      network.trainNet(tc, cascades, false, false);  // change booleans!!!!!!!!!!!!!!!!!!
    }
    catch( Exception e ){
      tc.stopped( e );
    }
  }


  /*
  the following method initialize the panels of the main tabbed pane
  */

  private void buildGeneral(){
    pGeneral = new JPanel();
    JLabel lMaxOutErr = new JLabel("Maximum output unit error: ");

    tMaxOutErr = new JTextField(10);
    cbCovNErr  = new JCheckBox("Print covariance and error", false );
    cbCache    = new JCheckBox("Cache the unit activation", false );
    cbPrune    = new JCheckBox("Prune new hidden unit", false );
    lMini      = new JLabel("Minimize: ");
    ncbMini    = new NamedComboBox();
    ncbMini.addItems(functions.mini);

    lMaxOutErr.setToolTipText("Abort learning if the error is below");
    tMaxOutErr.setToolTipText("Abort learning if the error is below");
    cbCovNErr.setToolTipText("Trace error development");
    cbCache.setToolTipText("Speeds up computing at cost of memory");
    cbPrune.setToolTipText("Enable Pruning Cascade Correlation");
    lMini.setToolTipText("Criterion for pruning");
    ncbMini.setToolTipText("Criterion for pruning");

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = insets;
    gbc.weightx = gbc.weighty = 0.5;
    pGeneral.setLayout( gbl );
    gbc.gridx = gbc.gridy = 0;

    gbl.setConstraints( lMaxOutErr, gbc );
    pGeneral.add( lMaxOutErr );

    gbc.gridx = 1;
    gbl.setConstraints( tMaxOutErr, gbc );
    pGeneral.add( tMaxOutErr );
    gbc.gridx = 0;
    gbc.gridy++;

    gbc.gridwidth = 2;
    gbl.setConstraints( cbCovNErr, gbc );
    pGeneral.add( cbCovNErr );

    gbc.gridy++;
    gbl.setConstraints( cbCache, gbc );
    pGeneral.add( cbCache );

    gbc.gridy++;
    gbl.setConstraints(cbPrune, gbc);
    pGeneral.add(cbPrune);
    cbPrune.addActionListener(this);

    gbc.gridwidth = 1;
    gbc.gridy++;
    gbl.setConstraints( lMini, gbc );
    pGeneral.add( lMini );
    gbc.gridx = 1;
    gbl.setConstraints( ncbMini, gbc );
    pGeneral.add( ncbMini );
  }

  private void buildMod(){
    pMod = new JPanel();

    JLabel lMod = new JLabel("Modification: ");
    ncbMod = new NamedComboBox();
    ncbMod.addItems( functions.modi );
    ncbMod.addActionListener( this );

    lMod.setToolTipText("Optional algorithm modification");
    ncbMod.setToolTipText("Optional algorithm modification");

    JPanel pParam = new JPanel();
    pParam.setLayout( null );
    pParam.setBorder( BorderFactory.createTitledBorder("Parameters") );
    Insets in = pParam.getInsets();

  // parameter panel:{
    Dimension
      dLabel = new Dimension( functions.getMaxLabelWidth(),
                              ( new JLabel("N") ).getPreferredSize().height ),
      dTextF = ( new JTextField( 8 ) ).getPreferredSize(),
      pref   = new Dimension();

    int i, h, v, dx = 2 * ( insets.left + insets.right) + dLabel.width + dTextF.width;

    pref.height = ( param.length + 1 )/2 *
      ( insets.top + Math.max( dTextF.height, dLabel.height ) + insets.bottom )
      + in.top + in.bottom;
    pref.width = 2 * dx + in.left + in.right ;
    pParam.setPreferredSize( pref );

    for( i=0; i<param.length; i++ ){
      h = i%2; v = i/2;
      lParam[i] = new JLabel("noName");
      lParam[i].setSize( lParam[i].getPreferredSize() );
      lParam[i].setLocation(
        in.left + insets.left + h * dx,
        in.top + insets.top + v * ( dTextF.height + insets.top + insets.bottom )
      );
      pParam.add( lParam[i] );

      tParam[i] = new JTextField( 8 );
      tParam[i].setSize( tParam[i].getPreferredSize() );
      tParam[i].setLocation(
        in.left + 2 * insets.left + dLabel.width + insets.right + h * dx,
        in.top + insets.top + v * ( dTextF.height + insets.top + insets.bottom )
      );
      pParam.add( tParam[i] );
    }
  // }

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = insets;
    gbc.weightx = gbc.weighty = 0.5;
    gbc.fill = gbc.HORIZONTAL;
    pMod.setLayout( gbl );

    gbc.gridx = gbc.gridy = 0;
    gbl.setConstraints( lMod, gbc );
    pMod.add( lMod );

    gbc.gridx = 1;
    gbl.setConstraints( ncbMod, gbc );
    pMod.add( ncbMod );

    gbc.gridwidth = 2;
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbl.setConstraints( pParam, gbc );
    pMod.add( pParam );
  }

  private void buildCand(){
    pCand = new JPanel();
    JLabel lMinCovCh = new JLabel("Minimum covariance change: "),
           lCandPat = new JLabel("Candidate patience: "),
           lMaxCovUp = new JLabel("Max. no. of covariance updates: "),
           lMaxCandNo = new JLabel("Max. no. of candidate units: "),
           lActFn = new JLabel("Activation function: ");

    tMinCovCh = new JTextField( 6 );
    tCandPat = new JTextField();
    tMaxCovUp = new JTextField();
    tMaxCandNo = new JTextField();
    ncbActFn = new NamedComboBox();
    ncbActFn.addItems( functions.actFn );

    lMinCovCh.setToolTipText("Fraction of old covariance");
    tMinCovCh.setToolTipText("Fraction of old covariance");
    lCandPat.setToolTipText("Steps to wait with evaluating covariance change");
    tCandPat.setToolTipText("Steps to wait with evaluating covariance change");
    lMaxCovUp.setToolTipText("Max. updates before accepting a new cascade");
    tMaxCovUp.setToolTipText("Max. updates before accepting a new cascade");
    lMaxCandNo.setToolTipText("Canidate pool size");
    tMaxCandNo.setToolTipText("Canidate pool size");
    lActFn.setToolTipText("Activation function for the candidates");
    ncbActFn.setToolTipText("Activation function for the candidates");

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    pCand.setLayout( gbl );
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = insets;
    // gbc.weightx = gbc.weighty = 0.5;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbl.setConstraints( lMinCovCh , gbc );
    pCand.add( lMinCovCh );

    gbc.gridx = 1;
    gbl.setConstraints( tMinCovCh , gbc );
    pCand.add( tMinCovCh );

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbl.setConstraints( lCandPat , gbc );
    pCand.add( lCandPat );

    gbc.gridx = 1;
    gbl.setConstraints( tCandPat , gbc );
    pCand.add( tCandPat );

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbl.setConstraints( lMaxCovUp , gbc );
    pCand.add( lMaxCovUp );

    gbc.gridx = 1;
    gbl.setConstraints( tMaxCovUp , gbc );
    pCand.add( tMaxCovUp );

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbl.setConstraints( lMaxCandNo , gbc );
    pCand.add( lMaxCandNo );

    gbc.gridx = 1;
    gbl.setConstraints( tMaxCandNo , gbc );
    pCand.add( tMaxCandNo );

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbl.setConstraints( lActFn , gbc );
    pCand.add( lActFn );

    gbc.gridx = 1;
    gbl.setConstraints( ncbActFn , gbc );
    pCand.add( ncbActFn );
  }

  private void buildOut(){
    pOut = new JPanel();
    JLabel lErrCh = new JLabel("Error change: "),
           lOutPat = new JLabel("Output patience: "),
           lMaxEpochs = new JLabel("Max. no. of epochs: ");

    tErrCh = new JTextField( 8 );
    tOutPat = new JTextField( 8 );
    tMaxEpochs = new JTextField( 8 );

    lErrCh.setToolTipText("Fraction of old error");
    tErrCh.setToolTipText("Fraction of old error");
    lOutPat.setToolTipText("Steps to wait with evaluating error change");
    tOutPat.setToolTipText("Steps to wait with evaluating error change");
    lMaxEpochs.setToolTipText("Max. epochs before accepting a new cascade");
    tMaxEpochs.setToolTipText("Max. epochs before accepting a new cascade");

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    pOut.setLayout( gbl );
    gbc.insets = insets;
    // gbc.weightx = gbc.weighty = 0.5;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = gbc.EAST;
    gbl.setConstraints( lErrCh, gbc );
    pOut.add( lErrCh );

    gbc.gridx = 1;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tErrCh, gbc );
    pOut.add( tErrCh );

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = gbc.EAST;
    gbl.setConstraints( lOutPat, gbc );
    pOut.add( lOutPat );

    gbc.gridx = 1;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tOutPat, gbc );
    pOut.add( tOutPat );

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = gbc.EAST;
    gbl.setConstraints( lMaxEpochs, gbc );
    pOut.add( lMaxEpochs );

    gbc.gridx = 1;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tMaxEpochs, gbc );
    pOut.add( tMaxEpochs );
  }

  private void buildInit(){
    pInit = new JPanel();

    JLabel lMinInit = new JLabel("min "),
           lMaxInit = new JLabel("max ");

    tMinInit = new JTextField(6);
    tMinInit.setToolTipText("Minimum activation value");
    lMinInit.setToolTipText("Minimum activation value");
    tMaxInit = new JTextField(6);
    tMaxInit.setToolTipText("Maximum activation value");
    lMaxInit.setToolTipText("Maximum activation value");

    bInit = new JButton("Init");
    bInit.addActionListener( this );
    bInit.setToolTipText("Delete hidden layers and initialize network");

    GridBagLayout gbl = new GridBagLayout();
    pInit.setLayout( gbl );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = insets;
    gbc.weightx = gbc.weighty = 0.5;

    gbc.gridx = gbc.gridy = 0;
    gbc.anchor = gbc.EAST;
    gbl.setConstraints(lMinInit, gbc );
    pInit.add(lMinInit);
    gbc.gridx = 1;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tMinInit, gbc );
    pInit.add( tMinInit );
    gbc.gridx = 2;

    gbc.anchor = gbc.EAST;
    gbl.setConstraints(lMaxInit, gbc);
    pInit.add(lMaxInit, gbc);
    gbc.gridx = 3;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tMaxInit, gbc );
    pInit.add( tMaxInit );
    gbc.gridy++;

    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( bInit, gbc );
    pInit.add( bInit );
  }

  private void buildLearn(){
    pLearn = new JPanel();

    lLearnParam = new JLabel[5];
    tCascades = new JTextField( 8 );
    ncbLearnFn = new NamedComboBox();
    ncbLearnFn.addItems( functions.learn );
    ncbLearnFn.addActionListener( this );
    bLearn = new JButton("Learn");
    bLearn.addActionListener( this );

    JLabel lCascades = new JLabel("Cascades: "),
           lLearnFn = new JLabel("Learning function: ");

    lCascades.setToolTipText("Maximum number of cascades (hidden layers)");
    tCascades.setToolTipText("Maximum number of cascades (hidden layers)");
    lLearnFn.setToolTipText("Subordinate function for training the candidates");
    ncbLearnFn.setToolTipText("Subordinate function for training the candidates");

    JPanel pParam = new JPanel();
    pParam.setLayout( null );
    pParam.setBorder( BorderFactory.createTitledBorder("Parameters") );
    Insets in = pParam.getInsets();

    // parameter panel:{
    Dimension
      dLabel = new Dimension( functions.getMaxLabelWidth(),
                              ( new JLabel("N") ).getPreferredSize().height ),
      dTextF = ( new JTextField( 8 ) ).getPreferredSize(),
      pref   = new Dimension();

    int i, h, v, dx = 2 * ( insets.left + insets.right) + dLabel.width + dTextF.width,
        maxParamNo = learnParam.length;

    lLearnParam = new JLabel[ maxParamNo ];
    tLearnParam = new JTextField[ maxParamNo ];

    pref.height = ( maxParamNo + 1 )/2 *
      ( insets.top + Math.max( dTextF.height, dLabel.height ) + insets.bottom )
      + in.top + in.bottom;
    pref.width = 2 * dx + in.left + in.right ;
    pParam.setPreferredSize( pref );

    for( i=0; i<maxParamNo; i++ ){
      h = i%2; v = i/2;
      lLearnParam[i] = new JLabel("noName");
      lLearnParam[i].setSize( lLearnParam[i].getPreferredSize() );
      lLearnParam[i].setLocation(
        in.left + insets.left + h * dx,
        in.top + insets.top + v * ( dTextF.height + insets.top + insets.bottom )
      );
      pParam.add( lLearnParam[i] );

      tLearnParam[i] = new JTextField( 8 );
      tLearnParam[i].setSize( tLearnParam[i].getPreferredSize() );
      tLearnParam[i].setLocation(
        in.left + 2 * insets.left + dLabel.width + insets.right + h * dx,
        in.top + insets.top + v * ( dTextF.height + insets.top + insets.bottom )
      );
      pParam.add( tLearnParam[i] );
    }
  // }


    GridBagLayout gbl = new GridBagLayout();
    pLearn.setLayout( gbl );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = insets;
    gbc.weightx = gbc.weighty = 0.5;

    gbc.gridx = gbc.gridy = 0;
    gbl.setConstraints( lLearnFn, gbc );
    pLearn.add( lLearnFn );

    gbc.gridx = 1;
    gbl.setConstraints( ncbLearnFn, gbc );
    pLearn.add( ncbLearnFn );

    gbc.gridy = 1;
    gbc.gridx = 0;
    gbc.gridwidth = 3;
    gbc.fill = gbc.HORIZONTAL;
    gbl.setConstraints( pParam, gbc );
    pLearn.add( pParam );

    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.anchor = gbc.EAST;
    gbc.fill = gbc.NONE;
    gbl.setConstraints( lCascades, gbc );
    pLearn.add( lCascades );

    gbc.gridx = 1;
    gbc.anchor = gbc.WEST;
    gbl.setConstraints( tCascades, gbc );
    pLearn.add( tCascades );

    gbc.gridx = 2;
    gbc.anchor = gbc.EAST;
    gbl.setConstraints( bLearn, gbc );
    pLearn.add( bLearn );
  }

  /**
   * this method contains the default values and sets them to the text fields
   */
  private void setDefaultValues(){
    maxOutErr = 0.2;
    tMaxOutErr.setText( String.valueOf( maxOutErr ) );
    minCovCh = 0.04;
    tMinCovCh.setText( String.valueOf( minCovCh ) );
    candPat = 25;
    tCandPat.setText( String.valueOf( candPat ) );
    maxCovUp = 200;
    tMaxCovUp.setText( String.valueOf( maxCovUp ) );
    maxCandNo = 8;
    tMaxCandNo.setText( String.valueOf( maxCandNo ) );
    errCh = 0.01;
    tErrCh.setText( String.valueOf( errCh ) );
    outPat = 50;
    tOutPat.setText( String.valueOf( outPat ) );
    maxEpochs = 200;
    tMaxEpochs.setText( String.valueOf( maxEpochs ) );
    minInit = -1;
    tMinInit.setText( String.valueOf( minInit ) );
    maxInit = 1;
    tMaxInit.setText( String.valueOf( maxInit ) );
    learnParam = new double[]{0.2, 0, 0, 0, 0};
    for( int i=0; i<5; i++ )
      tLearnParam[i].setText( String.valueOf( learnParam[i] ) );
    cascades = 10;
    tCascades.setText( String.valueOf( cascades ) );
  }

  private void showException( String text ){
    snns.showException( new Exception( text ), this );
  }
}
