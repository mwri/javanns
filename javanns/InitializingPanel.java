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
 * InitializingPanel controls network initializing
 *
 */
class InitializingPanel extends ControlPanel{
  JButton bInit;


  /**
   * Creates panel with its elements (buttons, input fields etc)
   */
  public InitializingPanel( MasterControl master ){
    super(master, "Initializing function: ", Function.INIT ) ;
    master.ip = this;

    bInit = new JButton("Init");
    add(bInit);
    bInit.addActionListener(this);
    bInit.setToolTipText("Initialize network");
    Dimension d = bInit.getPreferredSize();
    bInit.setSize(d.width * 2, d.height);
    lw = toGrid((maxX - 2*d.width)/2);
    move(bInit, lw, y + 2);
  }


  /**
   * Event handler for InitializingPanel. Shows appropriate controls and fields
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e);
    if( e.getSource() == bInit ) master.init();
  }
}
