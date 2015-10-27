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


/*-------------------------------------------------------------------------
  IMPORTS
 *-----------------------------------------------------------------------*/

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File ;  // for separator in Patterns

/*------------------------- class declaration --------------------------------*/
/**
 * MasterControl is the main class of the control panel.
 * It manages to init the network or to train it.
 */
class MasterControl{
  Snns snns;
  Network network;
  Functions functions;
  JTabbedPane controlPane;
  UpdatePanel up;
  InitializingPanel ip;
  PatternsPanel pp;
  SubPatternPanel sp;
  LearnPanel lp;
  PruningPanel prunP;
  Function[] prunableFn;
  JInternalFrame frame;

  public static final String frame_title = "Control Panel";

  /**
   * constructor with Snns as parameter
   *
   * @param snns
   */
  public MasterControl(Snns snns) {
    this.snns = snns;
    network = snns.network;
    functions = snns.functions;
    frame = new JInternalFrame(frame_title, false, true, false, true){
      public void dispose(){ setVisible(false); } // necessary cause Snns calls the
                                                  // dispose method of the frames
                                                  // when all frames are closed
    };
    frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

    up = new UpdatePanel(this);
    ip = new InitializingPanel(this);
    pp = new PatternsPanel(this);
    sp = new SubPatternPanel(this);
    lp = new LearnPanel(this);
    prunP = new PruningPanel(this);

    controlPane = new JTabbedPane();

    controlPane.addTab("Initializing", ip);
    controlPane.addTab("Updating", up);
    controlPane.addTab("Learning", lp);
    controlPane.addTab("Pruning", prunP);
    controlPane.addTab("Patterns", pp);
    controlPane.addTab("Subpatterns", sp);
    controlPane.setEnabledAt(controlPane.indexOfTab("Subpatterns"), false);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new CenterLayout());
    controlPane.setBorder(new EmptyBorder(3, 3, 2, 2));
    mainPanel.add(controlPane);

    frame.setContentPane(mainPanel);
    frame.pack();

    prunableFn = snns.functions.getFunctionsOfType( Function.PR_LEARN );
  }

  /*------------------------ public methods ------------------------------------*/

  /**
   * Initializes the network.
   */
  public void init() {
    try{
      Function f = ip.getFunction();
      network.setFunction( f, ip.getParameters() );
      network.initNet();
    }
    catch(Exception e) { showException(e); }
  }

  /**
   * Trains the network by using all patterns.
   *
   * @param steps training steps (cycles, epochs)
   * @exception throws an Exception when it wasn´t possible to set one of the currently
   *            choosen functions
   */
  public void learn(int steps, ThreadChief tc) throws Exception{
    network.setFunction( up.getFunction(), up.getParameters() );
    network.setFunction( pp.getFunction(), pp.getParameters() );
    network.setFunction( lp.getFunction(), lp.getParameters() );
    network.trainNet(tc, steps, lp.cbShuffle.isSelected(), sp.cbShuffle.isSelected());
  }

  /**
   * Trains the network with the current pattern patterns.
   *
   * @param steps training steps (cycles, epochs)
   */
  public void learnCurrent(int steps, ThreadChief tc) {
    //System.out.println("MasterControl.learn( int ): "+steps);
    try {
      network.setFunction( up.getFunction(), up.getParameters() );
      network.setFunction( pp.getFunction(), pp.getParameters() );
      network.setFunction( lp.getFunction(), lp.getParameters() );
      network.trainNet_CurrentPattern(tc, steps, lp.cbShuffle.isSelected(), sp.cbShuffle.isSelected());
    }
    catch( Exception e ){ showException( e ); }
  }

  /**
   * Updates the tabs. If selected learning function can be used for pruning
   * enables the Pruning tab. If selected pattern has subpatterns, enables
   * the Subpatterns tab
   */
  public void updateTabs() {
    //System.out.println("MasterControl.updateTabs()");
    if(prunableFn == null) return;
    Function fn = lp.getFunction();
    boolean  fl = false;
    int i;
    for(i=0; i<prunableFn.length; i++) if(fn.equals(prunableFn[i])) break;
    if(i < prunableFn.length) fl = true;
    controlPane.setEnabledAt(controlPane.indexOfTab("Pruning"), fl);

    fl = false;
    KernelInterface.KernelPatternInfo kpi = network.getPatInfo();
    if(kpi != null &&
      (kpi.in_number_of_dims != 0 || kpi.out_number_of_dims != 0)) fl = true;
    controlPane.setEnabledAt(controlPane.indexOfTab("Subpatterns"), fl);
  }


  private void showException( Exception e ){ snns.showException( e, this ); }
}
