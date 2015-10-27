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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * FlatButton is a JButton that looks flat as long as mouse is not over it
 *
 */
class FlatButton extends JButton implements MouseListener {
  public Point mouseClickedPos, mousePressedPos, mouseReleasedPos;
  private Border noBorder = new EmptyBorder(2,2,2,2);
  private Border hiliteBorder = new EtchedBorder();

  /**
   * Creates a button with no set text or icon.
   *
   */
  public FlatButton() {
    super();
    constructorRest();
  }

  /**
   * Creates a button with an icon.
   *
   * @param icon the Icon image to display on the button
   */
  public FlatButton(Icon icon) {
    super(icon);
    constructorRest();
  }

  /**
   * Creates a button with text.
   *
   * @param text the text of the button
   */
  public FlatButton(String text) {
    super(text);
    constructorRest();
  }

  /**
   * Creates a button with initial text and an icon.
   *
   * @param text the text of the button
   * @param icon the Icon image to display on the button
   */
  public FlatButton(String text, Icon icon) {
    super(text, icon);
    constructorRest();
  }

  /**
   * The common part for all constructors. Makes button flat
   *   and without focus marking.
   */
  private void constructorRest() {
    setBorder(noBorder);
    addMouseListener(this);
    setFocusPainted(false);
  }


  public void mouseEntered(MouseEvent e) {
    ((JButton)e.getComponent()).setBorder(hiliteBorder);
    ((JButton)e.getComponent()).repaint();
  }
  public void mouseExited (MouseEvent e) {
    ((JButton)e.getComponent()).setBorder(noBorder);
    ((JButton)e.getComponent()).repaint();
  }
  public void mouseClicked(MouseEvent e) { mouseClickedPos = e.getPoint(); }
  public void mousePressed(MouseEvent e) { mousePressedPos = e.getPoint(); }
  public void mouseReleased(MouseEvent e) { mouseReleasedPos = e.getPoint(); }
}
