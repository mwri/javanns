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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File ;  // for separator in Patterns

/**
 * PruningPanel controls network pruning
 *
 */
class PruningPanel extends JPanel implements ActionListener {
  Snns snns;
  JLabel label, lInitVal;
  NamedComboBox ncbPrunFunc;
  FlatButton bHelp;
  JTextField tMaxErrInc, tAccErr, tRetrCycles, tMinErr, tInitVal;
  JCheckBox cbRecLast, cbRefrDisplay, cbInputPr, cbHiddenPr;
  JButton bPrune;
  MasterControl master;

  /**
   * class constructor:
   *  creates panel with its elements (buttons, input fields etc)
   */
  public PruningPanel( MasterControl master ) {
    this.master = master;
    snns = master.snns;
    master.prunP = this;

    JPanel p = new JPanel();

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    p.setLayout(gbl);
    gbc.gridx = 0;
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 2, 2);
    gbc.gridy = 0;

    JLabel label = new JLabel("Method: ");
    gbl.setConstraints(label, gbc);
    p.add(label);

    ncbPrunFunc = new NamedComboBox();
    ncbPrunFunc.setPreferredSize(new Dimension(10, ncbPrunFunc.getPreferredSize().height));
    ncbPrunFunc.addItems( master.functions.getFunctionsOfType(Function.PRUNING) );
    gbc.gridx++;
    gbc.gridwidth = 2;
    gbl.setConstraints(ncbPrunFunc, gbc);
    p.add(ncbPrunFunc);
    ncbPrunFunc.addActionListener( this );
    label.setToolTipText("Pruning method to use");
    ncbPrunFunc.setToolTipText("Pruning method to use");

    if(ControlPanel.helpIcon == null)
      ControlPanel.helpIcon = snns.icons.getIcon("help.gif", "Help");
    bHelp = new FlatButton(ControlPanel.helpIcon);
    //bHelp = new FlatButton(new ImageIcon( cl.getResource("images/question.gif"), "Help"));
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(bHelp, gbc);
    p.add(bHelp);
    bHelp.setToolTipText("Information about the function");
    bHelp.addActionListener(this);
    bHelp.setVisible(((Function)ncbPrunFunc.getSelectedObject()).helpExists());

    label = new JLabel("    ");
    gbc.gridx++;
    gbl.setConstraints(label, gbc);
    p.add(label);

    label = new JLabel("Maximum error increase [%]: ");
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbl.setConstraints(label, gbc);
    p.add(label);

    tMaxErrInc = new JTextField("10", 6);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(tMaxErrInc, gbc);
    p.add(tMaxErrInc);
    label.setToolTipText("Maximum accepted increase of SSE");
    tMaxErrInc.setToolTipText("Maximum accepted increase of SSE");

    label = new JLabel("Accepted error: ");
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbl.setConstraints(label, gbc);
    p.add(label);

    tAccErr = new JTextField("5", 6);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(tAccErr, gbc);
    p.add(tAccErr);
    label.setToolTipText("Maximum accepted SSE");
    tAccErr.setToolTipText("Maximum accepted SSE");

    label = new JLabel("Cycles for retraining: ");
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbl.setConstraints(label, gbc);
    p.add(label);

    tRetrCycles = new JTextField("100", 6);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(tRetrCycles, gbc);
    p.add(tRetrCycles);
    label.setToolTipText("Maximum number of cycles for retraining");
    tRetrCycles.setToolTipText("Maximum number of cycles for retraining");

    label = new JLabel("Minimum error to train: ");
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbl.setConstraints(label, gbc);
    p.add(label);

    tMinErr = new JTextField("1", 6);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(tMinErr, gbc);
    p.add(tMinErr);
    label.setToolTipText("Stops training if SSE falls below it");
    tMinErr.setToolTipText("Stops training if SSE falls below it");

    lInitVal = new JLabel("Initial matrix value: ");
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbl.setConstraints(lInitVal, gbc);
    p.add(lInitVal);

    tInitVal = new JTextField("0.000001", 6);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    gbl.setConstraints(tInitVal, gbc);
    p.add(tInitVal);
    lInitVal.setToolTipText("Initial values on the Hesse-matrix diagonal");
    tInitVal.setToolTipText("Initial values on the Hesse-matrix diagonal");

    cbRecLast = new JCheckBox("Recreate last element", true);
    gbc.gridy -= 4;
    gbc.gridx += 3;
    gbl.setConstraints(cbRecLast, gbc);
    p.add(cbRecLast);
    cbRecLast.setToolTipText("Restore the last pruned element");

    cbRefrDisplay = new JCheckBox("Refresh display", false);
    gbc.gridy++;
    gbl.setConstraints(cbRefrDisplay , gbc);
    p.add(cbRefrDisplay);
    cbRefrDisplay.setToolTipText("Refresh display after each pruning step");

    cbInputPr = new JCheckBox("Prune input units", true);
    gbc.gridy++;
    gbl.setConstraints(cbInputPr, gbc);
    p.add(cbInputPr);
    cbInputPr.setToolTipText("Prune input units");

    cbHiddenPr = new JCheckBox("Prune hidden units", true);
    gbc.gridy++;
    gbl.setConstraints(cbHiddenPr, gbc);
    p.add(cbHiddenPr);
    cbHiddenPr.setToolTipText("Prune hidden units");

    bPrune = new JButton("Prune");
    gbc.gridy += 2;
    gbl.setConstraints(bPrune, gbc);
    p.add(bPrune);
    bPrune.setToolTipText("Perform network pruning");
    bPrune.addActionListener( this );

    add(p);
    /*
    Function f = (Function)ncbPrunFunc.getSelectedObject();
    bHelp.setVisible( f.helpExists() );
    boolean flag = false;
    if(f.getKernelName().equals("OptimalBrainSurgeon")) flag = true;
    tInitVal.setEnabled(flag);
    lInitVal.setEnabled(flag);
    */
    actionPerformed( new ActionEvent(ncbPrunFunc, 0, ""));
  }

  /**
   * Event handler for PruningPanel. Checks the entry data and performs
   * pruning.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if(src == bPrune) {
      String prune_func, learn_func;
      double pmax_error_incr, paccepted_error, pmin_error_to_stop, pinit_matrix_value;
      boolean precreatef, pinput_pruningf, phidden_pruningf;
      int pfirst_train_cyc, pretrain_cyc;

      SimpleFunction f = (SimpleFunction)ncbPrunFunc.getSelectedObject();
      prune_func = f.kernel_name;
      f = (SimpleFunction) (master.lp.cFunction.getSelectedObject());
      learn_func = f.kernel_name;

      try{ pmax_error_incr = Double.valueOf( tMaxErrInc.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Maximum error increase has to be a double value");
        return;
      }

      try{ paccepted_error = Double.valueOf( tAccErr.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Accepted error has to be a double value");
        return;
      }

      try{ pmin_error_to_stop = Double.valueOf( tMinErr.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Minimum error to stop has to be a double value");
        return;
      }

      try{ pinit_matrix_value = Double.valueOf( tInitVal.getText() ).doubleValue(); }
      catch( Exception e ){
        showException("Initial value for matrix has to be a double value");
        return;
      }

      precreatef = cbRecLast.isSelected();
      pinput_pruningf = cbInputPr.isSelected();
      phidden_pruningf = cbHiddenPr.isSelected();

      try{ pfirst_train_cyc = Integer.parseInt( master.lp.tCycles.getText() ); }
      catch( Exception e ){
        showException("The number of learn cycles for first training has to be an integer value");
        return;
      }

      try{ pretrain_cyc = Integer.parseInt( tRetrCycles.getText() ); }
      catch( Exception e ){
        showException("The number of learn cycles for retraining has to be an integer value");
        return;
      }

      Network net = master.network;

      Function oldLearnFn = net.functions[ Function.LEARN ],
               prLearnFn = new Function(snns, Function.LEARN, "PruningFeedForward");
      double[] oldParam = net.parameters[ Function.LEARN ];
      try { net.setFunction(prLearnFn, master.lp.getParameters()); }
      catch(Exception e){
        showException("Could not set the necessary pruning learn function");
        return;
      }
      net.setPruningFunc(prune_func, learn_func,
                          pmax_error_incr, paccepted_error,
                          precreatef, pfirst_train_cyc,
                          pretrain_cyc, pmin_error_to_stop,
                          pinit_matrix_value, pinput_pruningf,
                          phidden_pruningf);
      try { net.initNet(); }
      catch(Exception e) {
        showException("Could not initialize the network before pruning.");
        return;
      }
      /*
      new Thread() {
        Network net;
        public void start(Network net) {
          this.net = net;
          start();
        }
        public void run() {
          net.fireEvent(NetworkEvent.LONG_TASK_STARTED);
          try { net.pruneNet(cbRefrDisplay.isSelected()); }
          catch(Exception e) {
            net.fireEvent(NetworkEvent.LONG_TASK_OVER);
            showException(e.toString());
            return;
          }
          net.fireEvent(NetworkEvent.LONG_TASK_OVER);
        }
      }.start(net);*/
      new PrunThread().start( net );
      if( oldLearnFn != null ){
        try { net.setFunction( oldLearnFn, oldParam ); }
        catch(Exception e) {
          showException("Could not set the old learn function: "+oldLearnFn.show_name);
        }
      }
    }

    if(src == bHelp) {
      try{ ((Function)ncbPrunFunc.getSelectedObject()).showHelp(); }
      catch( Exception ex ){ showException( ex.toString() ); }
    }

    if(src == ncbPrunFunc) {
      Function f = (Function)ncbPrunFunc.getSelectedObject();
      bHelp.setVisible(f.helpExists());

      boolean flag = false;
      if(f.getKernelName().equals("OptimalBrainSurgeon")) flag = true;
      tInitVal.setEnabled(flag);
      lInitVal.setEnabled(flag);
    }
  }

  private void showException( String text ){
    snns.showException( new Exception( text ), this );
  }

  class PrunThread extends Thread{
    Network net;
    public void start(Network net) {
      this.net = net;
      start();
    }
    public void run() {
      net.fireEvent(NetworkEvent.LONG_TASK_STARTED);
      try { net.pruneNet(cbRefrDisplay.isSelected()); }
      catch(Exception e) {
        net.fireEvent(NetworkEvent.LONG_TASK_OVER);
        showException(e.toString());
        return;
      }
      net.fireEvent(NetworkEvent.LONG_TASK_OVER);
    }
  }

}
