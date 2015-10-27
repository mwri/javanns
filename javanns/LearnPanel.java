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

/*----------------- class declaration ----------------------------------------*/

/**
 * LearnPanel controls network learning
 *
 */
class LearnPanel extends ControlPanel implements NetworkListener{

/*------------- private member variables -------------------------------------*/
  JButton bInit, bLearnAll, bLearnCurrent;
  JTextField tCycles, tSteps;
  JCheckBox cbShuffle;
  JLabel label;
  volatile boolean is_learning = false;

  ThreadChief tc_all = new ThreadChief(){
    public void stopped(Object o){
      is_learning = false;
      bLearnAll.setText("Learn All");
    }
  },
  tc_single = new ThreadChief(){
    public void stopped(Object o){
      is_learning = false;
      bLearnCurrent.setText("Learn Current");
    }
  };


  /**
   * class constructor:
   *  creates panel with its elements (buttons, input fields etc)
   */
  public LearnPanel( MasterControl master ) {
    super(master, "Learning function: ", Function.LEARN);
    master.lp = this;
    master.network.addListener( this );
    Dimension d;

    label = new JLabel("Cycles: ");
    add(label);
    label.setToolTipText("Learning cycles");
    p = movePref(label, x_coord[0] - toGrid(label.getPreferredSize().width)+1, y);
    tCycles = new JTextField("100", 6);
    add(tCycles);
    tCycles.setToolTipText("Number of learning cycles");
    p = movePref(tCycles, x_coord[0]+1, y);
    label = new JLabel("Steps: ");
    add(label);
    label.setToolTipText("Update steps");
    p = movePref(label, x_coord[1] - toGrid(label.getPreferredSize().width)+1, y);
    tSteps = new JTextField("1", 6);
    add(tSteps);
    tSteps.setToolTipText("Number of update steps");
    p = movePref(tSteps, x_coord[1]+1, y);

    cbShuffle = new JCheckBox("Shuffle");
    add(cbShuffle);
    cbShuffle.setToolTipText("Shuffle patterns");
    p = movePref(cbShuffle, x_coord[2]+1, y);
    y = p.y + 1;

    bLearnCurrent = new JButton("Learn Current");
    add(bLearnCurrent);
    bLearnCurrent.addActionListener(this);
    bLearnCurrent.setToolTipText("Learn only current pattern");
    d = resize(bLearnCurrent);
    bInit = new JButton("Init");
    add(bInit);
    bInit.addActionListener(this);
    bInit.setToolTipText("Initialize network");
    bInit.setSize(d);
    bLearnAll = new JButton("Learn All");
    add(bLearnAll);
    bLearnAll.addActionListener(this);
    bLearnAll.setToolTipText("Learn all patterns");
    bLearnAll.setSize(d);
    if(maxX < 3*d.width) {
      p = move(bInit, origX, y);
      p = move(bLearnCurrent, p.x, y);
      p = move(bLearnAll, p.x, y);
    }
    else {
      lw = toGrid((maxX - 3*d.width) / 4);
      p = move(bInit, lw, y);
      p = move(bLearnCurrent, p.x+lw-1, y);
      p = move(bLearnAll, p.x+lw-1, y);
    }

    newNet();
  }

  /**
   * Event handler for LearnPanel. Shows appropriate controls and fields,
   *   initializes actions like learning and opens help pages in an
   *   attached browser.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    Object src = e.getSource();

    if(src == bInit) master.init();

    if(src == bLearnAll) {
      is_learning = !is_learning;
      if(is_learning) {
        tc_all.stop = false;
        bLearnAll.setText("Stop");
        try{ master.learn(Integer.parseInt(tCycles.getText()), tc_all); }
        catch(Exception ex) {
          bLearnAll.setText("Learn All");
          snns.showException(ex, this);
        }
      }
      else tc_all.stop = true;
    }

    if(src == bLearnCurrent) {
      is_learning = !is_learning;
      if(is_learning) {
        tc_single.stop = false;
        bLearnCurrent.setText("Stop");
        try{ master.learnCurrent(Integer.parseInt(tCycles.getText()), tc_single); }
        catch(Exception ex) {
          bLearnCurrent.setText("Learn Current");
          snns.showException(ex, this);
        }
      }
      else tc_single.stop = true;
    }

    if(src == cFunction) master.updateTabs();
  }


  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.NEW_NETWORK_LOADED ) newNet();
  }


  private void newNet(){
    if( master.network == null ) return;
    Function fn = master.network.getFunction( Function.LEARN );
    cFunction.setSelectedItem( fn );
  }
}
