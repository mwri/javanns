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
 * ColorSwatch is an icon in components foreground color
 *
 */
class ColorSwatch implements Icon {
  private int width;
  private int height;

  /**
   * Constructs a new 16x16 swatch.
   */
  public ColorSwatch() {
    width = 16;
    height = 16;
  }

  /**
   * Constructs a new swatch with given dimensions.
   *
   * @param w swatch width
   * @param h swatch height
   */
  public ColorSwatch(int w, int h) {
    width = w;
    height = h;
  }

  /**
   * Draws the color swatch in its component's foreground color.
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(c.getForeground());
    g.fillRect(x, y, width-2, height-2);
  }

  /**
   * Returns the swatch's width.
   */
  public int getIconWidth() { return width; }

  /**
   * Returns the swatch's height.
   */
  public int getIconHeight() { return height; }
};
