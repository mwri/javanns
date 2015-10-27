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


/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;



/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * Listener for DisplaySettingsPanel events.
 *
 */
class DisplaySettingsListener implements WindowListener {
  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/
  DisplaySettingsPanel panel;

  /*-------------------------------------------------------------------------*
   * constructor
   *-------------------------------------------------------------------------*/

  /**
   * Class constructor.
   *
   * @param dsp DisplySettingsPanel
   */
  public DisplaySettingsListener(DisplaySettingsPanel dsp) {
    super();
    panel = dsp;
  }

  /**
   * Does the same as Cancel.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowClosing(WindowEvent e) {
    panel.view.settings   = (NetworkViewSettings)panel.orig_settings.clone(); // to do: make deep copy
    panel.view.settings.pos_color  = panel.orig_pos_color;
    panel.view.settings.neg_color  = panel.orig_neg_color;
    panel.view.settings.null_color = panel.orig_null_color;
    panel.view.update();
  }

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowOpened(WindowEvent e) {}

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowClosed(WindowEvent e) {}

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowActivated(WindowEvent e) {}

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowDeactivated(WindowEvent e) {}

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowIconified(WindowEvent e) {}

  /**
   * Dummy, does nothing.
   *
   * @param e Event that caused the call of this method.
   */
  public void windowDeiconified(WindowEvent e) {}
}
