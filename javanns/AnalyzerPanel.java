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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.print.*;
import wsi.ra.chart2d.*;

/**
 * AnalyzerPanel is a multifunctional graph panel, which shows certain
 * properties of the network or the pattern set
 */

class AnalyzerPanel extends JPanel implements ActionListener,
                                              Printable,
                                              ChangeListener{

  /**
   * the central body of the program
   */
  Snns snns;

  /**
   *  analyzer's frame
   */
  JInternalFrame frame;


  /**
   * counting the steps
   */
  int time = 0;

  /**
   * the line index ( several lines in different colours can be shown )
   */
  private int lIndex = 0;

  volatile boolean is_testing = false;
  ThreadChief tc;


  /**
   * components of the panels in the frame
   */
  // Components for main panel:
  DArea dArea;
  DPointSet[] points;
  Icon iGridOn, iGridOff;
  FlatButton  bGrid, bClear, bDecreaseX, bIncreaseX, bDecreaseY, bIncreaseY,
              bLeft, bUp, bRight, bDown, bNewLine, bAutoFocus;
  ScaledBorder sbArea;

  // Components for setup panel:
  JComboBox jcbXValues, jcbYValues, jcbErrors;
  JPanel pXDetails, pYDetails;
  JTextField tfXUnitNo, tfYUnitNo;
  JRadioButton rbXAct, rbYAct;
  JCheckBox cbAVE, cbChangeColors;

  // Components for test panel:
  JTextField tfSteps, tfTrainingSteps;
  JButton bTest;
  JCheckBox cbChangePattern, cbTrainNet;
  JLabel lTrainingSteps;

  //Components for axis panel:
  JTextField tfXScale, tfYScale;
  JCheckBox cbXScaleDefault, cbYScaleDefault;
  JButton bApplyPatterns;

  /**
   * constructor of the analyzer
   * the frame is just created not added to the container
   *
   * @param snns the central part of JavaNNS
   */
  public AnalyzerPanel( Snns s ) {
    snns = s;

    JPanel pMain = getMain();

    JTabbedPane pane = new JTabbedPane();
    pane.addTab( "Setup", getSetup() );
    pane.addTab( "Test", getTest() );
    pane.addTab( "Scales", getScale() );

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;
    gbc.weightx = gbc.weighty = .5;
    gbc.gridx = gbc.gridy = 0;
    gbc.fill = gbc.BOTH;
    setLayout( gbl );

    gbl.setConstraints( pMain, gbc );
    add( pMain );

    gbc.gridy = 1;
    gbc.weighty = 0;
    gbl.setConstraints( pane, gbc );
    add( pane );

    frame = new JInternalFrame("Analyzer", true, true, true, true );
    frame.setContentPane( this );
    frame.pack();

    tc = new ThreadChief(){
      public void stopped(Object o){
        if( o instanceof Exception )
          snns.showException((Exception)o, this);
        is_testing = false;
        bTest.setText("Test");
        releaseLogPanel();
      }
    };
  }

  public void addPoint(double x, double y){
    points[lIndex].addDPoint(x,y);
  }

  public void jump(){
    points[lIndex].jump();
    if( cbChangeColors.isSelected() ){
      lIndex++;
      lIndex %= points.length;
    }
  }

/*------------------------- interfaces ---------------------------------------*/

  public void actionPerformed( ActionEvent evt ){
    //System.out.println("Analyzer.actionPerf");
    Object src = evt.getSource();
    DRectangle rect = dArea.getDRectangle();

    if( src == bGrid ){
      if( dArea.isGridVisible() ){
        dArea.setGridVisible( false );
        bGrid.setIcon( iGridOn );
        bGrid.setToolTipText("Turn grid on");
      }
      else{
        dArea.setGridVisible( true );
        bGrid.setIcon( iGridOff );
        bGrid.setToolTipText("Turn grid off");
      }
    }

    else if( src == bClear ) {
      for( int i = 0; i<points.length; i++ ) points[i].removeAllPoints();
      time = lIndex = 0;
    }

    else if( src == jcbXValues ){
      int sel = jcbXValues.getSelectedIndex();
      switch( sel ){
        case 0 : setXUnitPanel(); break;
        case 1 : setXTimePanel(); break;
        case 2 : setXPatNoPanel(); break;
      }
      pXDetails.validate();
    }

    else if( src == jcbYValues ){
      int sel = jcbYValues.getSelectedIndex();
      if( sel == 0 ) setYUnitPanel();
      else setYErrorPanel();
      pYDetails.validate();
    }

    else if( src == jcbErrors ) setYErrorPanel();

    else if( src == bTest ){
      if( is_testing ) tc.stop = true;
      else{
        lockLogPanel();
        bTest.setText("Stop");
        tc.stop = false;
        is_testing = true;
        new Analyzer(this);
      }
    }

    else if( src == bDecreaseX ){
      rect.x += rect.width / 4;
      rect.width /= 2;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bIncreaseX ){
      rect.width *= 2;
      rect.x -= rect.width / 4;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bDecreaseY ){
      rect.y += rect.height / 4;
      rect.height /= 2;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bIncreaseY ){
      rect.height *= 2;
      rect.y -= rect.height / 4;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bLeft ){
      rect.x -= rect.width / 4 ;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bUp ){
      rect.y += rect.height / 4 ;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bRight ){
      rect.x += rect.width / 4 ;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == bDown ){
      rect.y -= rect.height / 4 ;
      dArea.setVisibleRectangle( rect );
    }

    else if( src == cbTrainNet ){
      boolean selected = cbTrainNet.isSelected();
      tfTrainingSteps.setEnabled( selected );
      lTrainingSteps.setEnabled( selected );
    }

    else if( src == bNewLine ) newLine();

    else if( src == bAutoFocus ) dArea.setAutoFocus( true );

    else if( src == bApplyPatterns ){
      String pat = getXFormPattern();
      java.text.DecimalFormat form = new java.text.DecimalFormat();
      if( pat != null ) form.applyPattern(pat);
      sbArea.format_x = form;

      pat = getYFormPattern();
      form = new java.text.DecimalFormat();
      if( pat != null ) form.applyPattern(pat);
      sbArea.format_y = form;

      dArea.repaint();
    }
  }

  public void stateChanged(ChangeEvent e){
    Object src = e.getSource();
    if( src == cbXScaleDefault )
      tfXScale.setEnabled(!cbXScaleDefault.isSelected());
    else if( src == cbYScaleDefault )
      tfYScale.setEnabled(!cbYScaleDefault.isSelected());
  }

  public void newLine(){
    lIndex++;
    lIndex %= points.length;
    points[ lIndex ].removeAllPoints();
  }

  void lockLogPanel(){
    snns.pLog.enableOnly(this);
  }

  void releaseLogPanel(){
    snns.pLog.release(this);
  }


  public int print( Graphics g, PageFormat pf, int pi ){
    return dArea.print( g, pf, pi );
  }

  /**
   * method returns the currently selected unit for the x-values
   *
   * @return the selected x-unit
   */
  Unit getXUnit() throws Exception{
    int no = -1;
    try{ no = Integer.parseInt( tfXUnitNo.getText() ); }
    catch( Exception e ){
      throw new Exception("The unit number on x-axis has to be an integer value");
    }
    Unit unit = snns.network.getUnitNumber( no );
    if( unit == null )
      throw new Exception("Wrong unit number on x-axis");
    return unit;
  }


  /**
   * method returns the currently selected unit for the y-values
   *
   * @return the selected y-unit
   */
  Unit getYUnit() throws Exception{
    int no = -1;
    try{ no = Integer.parseInt( tfYUnitNo.getText() ); }
    catch( Exception e ){
      throw new Exception("The unit number on y-axis has to be an integer value");
    }
    Unit unit = snns.network.getUnitNumber( no );
    if( unit == null )
      throw new Exception("Wrong unit number on y-axis");
    return unit;
  }

  /**
   * method returns the number of steps choosen by the steps text field
   *
   * @return the number of selected steps
   */
  int getSteps() throws Exception{
    int no = -1;
    try{ no = Integer.parseInt( tfSteps.getText() ); }
    catch( Exception e ){
      throw new Exception("The number of steps has to be an integer value");
    }
    return no;
  }

  /**
   * method returns the number of training steps selected in the training steps
   * text field
   *
   * @return the number of training steps
   */
  int getTrainingSteps() throws Exception{
    int no = -1;
    try{ no = Integer.parseInt( tfTrainingSteps.getText() ); }
    catch( Exception e ){
      throw new Exception("The number of training steps has to be an integer value");
    }
    return no;
  }

  String getXFormPattern(){
    if( cbXScaleDefault.isSelected() ) return null;
    return tfXScale.getText();
  }

  String getYFormPattern(){
    if( cbYScaleDefault.isSelected() ) return null;
    return tfYScale.getText();
  }


  /*
  the following methods initialize the different panels of the ananlyzer
   */

  private JPanel getMain(){
    Color[] cLines = { Color.black, Color.blue, Color.red, Color.green,
                       Color.blue.darker(), Color.red.darker(), Color.green.darker(),
                       Color.orange, Color.cyan, Color.magenta };

    // left panel:
    Icon icon = snns.icons.getIcon("decreaseYAxis.gif", "Zoom in");
    bDecreaseY = new FlatButton( icon );
    bDecreaseY.setToolTipText("Decrease y-axis");
    bDecreaseY.addActionListener( this );
    icon = snns.icons.getIcon("increaseYAxis.gif", "Zoom out");
    bIncreaseY = new FlatButton( icon );
    bIncreaseY.setToolTipText("Increase y-axis");
    bIncreaseY.addActionListener( this );

    icon = snns.icons.getIcon("leftArrow-12.gif", "Move left");
    bLeft = new FlatButton( icon );
    bLeft.setToolTipText("Move left");
    bLeft.addActionListener( this );

    iGridOn = snns.icons.getIcon("graphGrid.gif", "Grid on" );
    iGridOff = snns.icons.getIcon("graphNoGrid.gif", "Grid off" );
    Icon iNewGraph = snns.icons.getIcon("newGraph.gif", "Clear graph" );
    bGrid = new FlatButton( iGridOn );
    bGrid.setToolTipText("Turn grid on");
    bGrid.addActionListener( this );
    bClear = new FlatButton( iNewGraph );
    bClear.setToolTipText( "Clear graph" );
    bClear.addActionListener( this );

    JPanel pLeft = new JPanel();
    pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS) );
    pLeft.add(bIncreaseY);
    pLeft.add(bDecreaseY);
    pLeft.add( Box.createVerticalGlue() );
    pLeft.add(bLeft);
    pLeft.add( Box.createVerticalGlue() );
    pLeft.add(bGrid);
    pLeft.add(bClear);

    // top panel:
    icon = snns.icons.getIcon("upArrow-12.gif", "Move up");
    bUp = new FlatButton( icon );
    bUp.setToolTipText("Move up");
    bUp.addActionListener( this );

    JPanel pTop = new JPanel();
    pTop.setLayout(new BoxLayout(pTop, BoxLayout.X_AXIS) );
    pTop.add( Box.createHorizontalGlue() );
    pTop.add(bUp);
    pTop.add( Box.createHorizontalGlue() );

    // center panel:
    dArea = new DArea();
    dArea.setMinRectangle( new DRectangle( -2, -2, 4, 4 ) );
    dArea.setAutoGrid( true );
    dArea.setGridVisible( false );
    dArea.setGridColor( Color.lightGray );
    dArea.setBackground( Color.white );
    dArea.setPreferredSize( new Dimension( 400, 300 ) );
    sbArea = new ScaledBorder();
    sbArea.foreground = Color.gray;
    dArea.setBorder( sbArea );
    new DMouseZoom( dArea );
    points = new DPointSet[ cLines.length ];
    for( int i=0; i<cLines.length; i++ ){
      points[i] = new DPointSet( 5 );
      points[i].setColor( cLines[i] );
      points[i].setConnected( true );
      dArea.addDElement( points[i] );
    }

    // bottom panel:
    icon = snns.icons.getIcon("decreaseXAxis.gif", "Zoom in");
    bDecreaseX = new FlatButton( icon );
    bDecreaseX.setToolTipText("Decrease x-axis");
    bDecreaseX.addActionListener( this );
    icon = snns.icons.getIcon("increaseXAxis.gif", "Zoom out");
    bIncreaseX = new FlatButton( icon );
    bIncreaseX.setToolTipText("Increase x-axis");
    bIncreaseX.addActionListener( this );

    icon = snns.icons.getIcon("downArrow-12.gif", "Move down");
    bDown = new FlatButton( icon );
    bDown.setToolTipText("Move down");
    bDown.addActionListener( this );

    icon = snns.icons.getIcon("errorGraph.gif", "New line");
    bNewLine = new FlatButton( icon );
    bNewLine.setToolTipText("New line");
    bNewLine.addActionListener( this );

    icon = snns.icons.getIcon("Zoom16.gif", "Autofocus");
    bAutoFocus = new FlatButton( icon );
    bAutoFocus.setToolTipText("Autofocus");
    bAutoFocus.addActionListener( this );

    JPanel pBottom = new JPanel();
    pBottom.setLayout(new BoxLayout(pBottom, BoxLayout.X_AXIS) );
    pBottom.add( bNewLine );
    pBottom.add( bAutoFocus );
    pBottom.add( Box.createHorizontalGlue() );
    pBottom.add(bDown);
    pBottom.add( Box.createHorizontalGlue() );
    pBottom.add( bDecreaseX );
    pBottom.add( bIncreaseX );

    // right panel:
    icon = snns.icons.getIcon("rightArrow-12.gif", "Move right");
    bRight = new FlatButton(icon);
    bRight.setToolTipText("Move right");
    bRight.addActionListener(this);

    JPanel pRight = new JPanel();
    pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS) );
    pRight.add( Box.createVerticalGlue() );
    pRight.add(bRight);
    pRight.add( Box.createVerticalGlue() );

    // put all together:
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    JPanel panel = new JPanel();
    panel.setLayout(gbl);

    gbc.fill = gbc.BOTH;
    gbc.gridheight = 3;
    gbl.setConstraints(pLeft, gbc);
    panel.add(pLeft);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbl.setConstraints(pTop, gbc);
    panel.add(pTop);

    gbc.gridy = gbc.RELATIVE;
    gbc.weightx = gbc.weighty = 1;
    gbl.setConstraints(dArea, gbc);
    panel.add(dArea);

    gbc.weightx = gbc.weighty = 0;
    gbc.gridwidth = 2;
    gbl.setConstraints(pBottom, gbc);
    panel.add(pBottom);
    gbc.gridwidth = 1;

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.weightx = gbc.weighty = 0;
    gbl.setConstraints(pRight, gbc);
    panel.add(pRight);

    return panel;
  }


  private JPanel getSetup(){
    JLabel lXaxis = new JLabel("x-axis: "),
           lYaxis = new JLabel("y-axis: ");

    jcbXValues = new JComboBox();
    jcbXValues.addItem( "Unit" );
    jcbXValues.addItem( "Time");
    jcbXValues.addItem( "Pattern no." );
    jcbXValues.setToolTipText("Values displayed on x-axis");
    jcbXValues.addActionListener( this );

    jcbYValues = new JComboBox();
    jcbYValues.addItem( "Unit" );
    jcbYValues.addItem( "Error" );
    jcbYValues.setPreferredSize( jcbXValues.getPreferredSize() );
    jcbYValues.setToolTipText("Values displayed on y-axis");
    jcbYValues.addActionListener( this );

    tfXUnitNo = new JTextField( 4 );
    tfXUnitNo.setToolTipText("Number of the unit on x-axis");
    tfYUnitNo = new JTextField( 4 );
    tfYUnitNo.setToolTipText("Number of the unit on y-axis");


    rbXAct = new JRadioButton( "Activation", true );
    rbYAct = new JRadioButton( "Activation", true );
    cbAVE = new JCheckBox( "AVE", true );
    cbAVE.setToolTipText("Error/|Output Units|");

    jcbErrors = new JComboBox();
    jcbErrors.addItem( "\u03a3|t-o|" );
    jcbErrors.addItem( "\u03a3|t-o|²" );
    jcbErrors.addItem( "|t-o|" );
    jcbErrors.addActionListener( this );

    cbChangeColors = new JCheckBox("Change color", true);
    cbChangeColors.setToolTipText("Change the color of the line after the last pattern of the set");

    pXDetails = new JPanel();
    pXDetails.setLayout( new BoxLayout( pXDetails, BoxLayout.X_AXIS ) );
    setXUnitPanel();
    pYDetails = new JPanel();
    pYDetails.setLayout( new BoxLayout( pYDetails, BoxLayout.X_AXIS ) );
    setYUnitPanel();

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;
    gbc.weightx = 0;
    gbc.weighty = .5;
    gbc.gridx = gbc.gridy = 0;

    JPanel panel = new JPanel();
    panel.setLayout( gbl );

    gbl.setConstraints( lXaxis, gbc );
    panel.add( lXaxis );

    gbc.gridx = 1;
    gbl.setConstraints( jcbXValues, gbc );
    panel.add( jcbXValues );

    gbc.gridx = 2;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = .5;
    gbl.setConstraints( pXDetails, gbc );
    panel.add( pXDetails );
    gbc.fill = gbc.NONE;
    gbc.weightx = 0;

    gbc.gridy = 1;
    gbc.gridx = 0;
    gbl.setConstraints( lYaxis, gbc );
    panel.add( lYaxis );

    gbc.gridx = 1;
    gbl.setConstraints( jcbYValues, gbc );
    panel.add( jcbYValues );

    gbc.gridx = 2;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = .5;
    gbl.setConstraints( pYDetails, gbc );
    panel.add( pYDetails );

    return panel;
  }

  private JPanel getTest(){
    JLabel lSteps = new JLabel("Steps: ");
    tfSteps = new JTextField( "10", 4 );
    cbChangePattern = new JCheckBox("Change pattern", true);

    bTest = new JButton("Test");
    bTest.addActionListener( this );

    cbTrainNet = new JCheckBox("Train network", false );
    cbTrainNet.addActionListener( this );

    lTrainingSteps = new JLabel("Training steps:");
    lTrainingSteps.setEnabled( false );

    tfTrainingSteps = new JTextField( "1", 4 );
    tfTrainingSteps.setEnabled( false );

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;
    gbc.weightx = gbc.weighty = .5;
    gbc.gridx = gbc.gridy = 0;

    JPanel panel = new JPanel();
    panel.setLayout( gbl );

    gbc.anchor = gbc.WEST;
    gbl.setConstraints( lSteps, gbc );
    panel.add( lSteps );

    gbc.gridx = 1;
    gbc.anchor = gbc.CENTER;
    gbl.setConstraints( tfSteps, gbc );
    panel.add( tfSteps );

    gbc.gridx = 2;
    gbc.gridwidth = 2;
    gbc.anchor = gbc.WEST;
    gbl.setConstraints( cbTrainNet, gbc );
    panel.add( cbTrainNet );

    gbc.gridy = 1;
    gbc.gridx = 0;
    gbl.setConstraints( cbChangePattern, gbc );
    panel.add( cbChangePattern );

    gbc.gridx = 3;
    gbc.gridwidth = 1;
    gbl.setConstraints( lTrainingSteps, gbc );
    panel.add( lTrainingSteps );

    gbc.gridx = 4;
    gbl.setConstraints( tfTrainingSteps, gbc );
    panel.add( tfTrainingSteps );

    gbc.gridx = 5;
    gbc.anchor = gbc.SOUTHEAST;
    gbl.setConstraints( bTest, gbc );
    panel.add( bTest );

    return panel;
  }

  private JPanel getScale(){
    JLabel lXScale = new JLabel("X-scale value format");
    tfXScale = new JTextField(10);
    tfXScale.setToolTipText("Pattern for values on x-scale");
    tfXScale.setEnabled(false);
    cbXScaleDefault = new JCheckBox("default", true);
    cbXScaleDefault.addChangeListener(this);

    JLabel lYScale = new JLabel("Y-scale value format");
    tfYScale = new JTextField(10);
    tfYScale.setToolTipText("Pattern for values on y-scale");
    tfYScale.setEnabled(false);
    cbYScaleDefault = new JCheckBox("default", true);
    cbYScaleDefault.addChangeListener(this);

    bApplyPatterns = new JButton("Apply");
    bApplyPatterns.setToolTipText("Applies the current patterns on the scales");
    bApplyPatterns.addActionListener(this);

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;
    gbc.weightx = gbc.weighty = .5;
    gbc.gridx = gbc.gridy = 0;

    JPanel panel = new JPanel();
    panel.setLayout( gbl );

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbl.setConstraints(lXScale, gbc);
    panel.add(lXScale);
    gbc.gridx = 1;
    gbl.setConstraints(tfXScale, gbc);
    panel.add(tfXScale);
    gbc.gridx = 2;
    gbl.setConstraints(cbXScaleDefault, gbc);
    panel.add(cbXScaleDefault, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbl.setConstraints(lYScale, gbc);
    panel.add(lYScale);
    gbc.gridx = 1;
    gbl.setConstraints(tfYScale, gbc);
    panel.add(tfYScale);
    gbc.gridx = 2;
    gbl.setConstraints(cbYScaleDefault, gbc);
    panel.add(cbYScaleDefault, gbc);

    gbc.gridx = 3;
    gbl.setConstraints(bApplyPatterns, gbc);
    panel.add(bApplyPatterns);

    return panel;
  }

  /*
  the following methods set the different selected panels in the setup panel
  */

  private void setXUnitPanel(){
    pXDetails.removeAll();
    JLabel lUnitNo = new JLabel("Number: ");
    JRadioButton rbOut = new JRadioButton("Output");
    ButtonGroup bg = new ButtonGroup();
    bg.add( rbXAct );
    bg.add( rbOut );

    pXDetails.add( lUnitNo );
    pXDetails.add( tfXUnitNo );
    pXDetails.add( getRigidArea() );
    pXDetails.add( rbXAct );
    pXDetails.add( rbOut );
  }

  private void setXTimePanel(){
    pXDetails.removeAll();
  }

  private void setXPatNoPanel(){
    pXDetails.removeAll();
    pXDetails.add( cbChangeColors );
  }

  private void setYUnitPanel(){
    pYDetails.removeAll();
    JLabel lUnitNo = new JLabel("Number: ");
    JRadioButton rbOut = new JRadioButton("Output");
    ButtonGroup bg = new ButtonGroup();
    bg.add( rbYAct );
    bg.add( rbOut );

    pYDetails.add( lUnitNo );
    pYDetails.add( tfYUnitNo );
    pYDetails.add( getRigidArea() );
    pYDetails.add( rbYAct );
    pYDetails.add( rbOut );
  }

  private void setYErrorPanel(){
    pYDetails.removeAll();

    int sel = jcbErrors.getSelectedIndex();
    if( sel == -1 ) sel = 0;

    pYDetails.add( jcbErrors );
    pYDetails.add( Box.createHorizontalGlue() );
    switch( sel ){
      case 0 : jcbErrors.setToolTipText("Sum of absolute errors");
               pYDetails.add( cbAVE );
               break;
      case 1 : jcbErrors.setToolTipText("Sum of square errors");
               pYDetails.add( cbAVE );
               break;
      case 2 : jcbErrors.setToolTipText("Absolute error at a single unit");
               JLabel lUnitNo = new JLabel("Number: ");
               pYDetails.add( lUnitNo );
               pYDetails.add( tfYUnitNo );
    }
  }
  /**
   * method creates a rgigid area depending on the panel insets of the snns
   */
  private Component getRigidArea(){
    Insets i = snns.panel_insets;
    Dimension d = new Dimension( i.left + i.right, i.top + i.bottom );
    return Box.createRigidArea( d );
  }

}
