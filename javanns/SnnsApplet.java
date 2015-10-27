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




/**
 *  Filename: $RCSfile: SnnsApplet.java,v $
 *  Purpose:  Applet embedding the SNNS Java GUI
 *  Language: Java
 *  Compiler: JDK 1.2.2
 *  Authors:  Igor Fischer
 *  Version:  $Revision: 1.1.2.2 $
 *            $Date: 2005/02/03 17:46:43 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

/*==========================================================================*
 * PACKAGE
 *==========================================================================*/
package javanns;


//package my.project.name;


/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * SnnsApplet is applet embeding the SNNS GUI. It is used for linking the
 * SNNS with a browser.
 *
 */
public class SnnsApplet extends JApplet implements ActionListener {
  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/
  private Snns snns = null;
  private JButton bStart, bStop;
  private JLabel lResult;

 /**
   * Class constructor. Does nothing.
   *
   */
  public SnnsApplet() {}

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

 /**
   * Creates a button to start the SNNS application
   *
   */
  public void init() {
    lResult = new JLabel("Result: ");
//    setBackground(Color.white);
    bStart = new JButton("starten");
    bStart.addActionListener(this);
    bStop = new JButton("Stop");
    bStop.addActionListener(this);
    getContentPane().add("West", bStart);
    getContentPane().setBackground(Color.white);
//    getContentPane().add("Center", lResult);
//    getContentPane().add("East", bStop);

    lResult.setText(lResult.getText() + "Init; ");
  }

  /**
   * Reacts to pressing the <b>Start</b> button. Calls the SNNS application
   *   and passes it network, pattern and configuration file names, as
   *   given in parameter tag in the HTML page.
   *
   * @param e the ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    JButton src = (JButton)(e.getSource());

    if(src == bStart) {
      Vector vp = new Vector(10);
      String p = getParameter("startingPath");
      if(p != null) System.setProperty("user.dir", p);
      for(int i=0; i<100; i++) {
        p = getParameter("param" + (i+1));
        if(p != null) vp.addElement(p);
        else break;
      }
      String[] args = new String[vp.size()];
      for(int i=0; i<args.length; i++) args[i] = (String)vp.elementAt(i);
//      lResult.setText(lResult.getText() + args[0] + "; " + args[1] + "; " + args[2] + "; ");

      snns = Snns.appletMain(this, args);
      if(snns == null) lResult.setText(lResult.getText() + "Fail");
      else lResult.setText(lResult.getText() + "Success");
    }
  }

/*
  public void destroy() {
    if(program!=null) program.stop();
    program=null;
  }
*/
}

 
 
