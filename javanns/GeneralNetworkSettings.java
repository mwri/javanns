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
 * GeneralNetworkSettings.
 *
 */
class GeneralNetworkSettings extends JPanel {
  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/
  JTextField tGrid, tSubnet, tZ;
  JButton bMaxValueColor, bMinValueColor, bZeroValueColor;
  JButton bTextColor, bBackgroundColor, bSelectedColor;


  /**
   * Class constructor:
   *  creates panel with its elements (buttons, input fields...)
   */
  public GeneralNetworkSettings(DisplaySettingsPanel parent) {
    int i;

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.insets.bottom = 8;
    gbc.insets.left = 16;
    gbc.anchor = gbc.NORTHWEST;

    JLabel label = new JLabel("Grid size: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Subnet: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("z-Value: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Text color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Background color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy -= 4;
    gbc.gridx++;
    gbc.insets.left = 8;
    tGrid = new JTextField(4);
    tGrid.setToolTipText("Grid for placing units, in pixels");
    gbl.setConstraints(tGrid, gbc);
    add(tGrid);

    gbc.gridy++;
    tSubnet = new JTextField(4);
    tSubnet.setToolTipText("???");
    gbl.setConstraints(tSubnet, gbc);
    add(tSubnet);

    gbc.gridy++;
    tZ = new JTextField(4);
    tZ.setToolTipText("???");
    gbl.setConstraints(tZ, gbc);
    add(tZ);

    gbc.gridy++;
    bTextColor = new JButton(new ColorSwatch(18, 18));
    bTextColor.setPreferredSize(new Dimension(20, 20));
    bTextColor.addActionListener(parent);
    bTextColor.setFocusPainted(false);
    bTextColor.setToolTipText("Foreground color");
    gbl.setConstraints(bTextColor, gbc);
    add(bTextColor);

    gbc.gridy++;
    bBackgroundColor = new JButton(new ColorSwatch(18, 18));
    bBackgroundColor.setPreferredSize(new Dimension(20, 20));
    bBackgroundColor.addActionListener(parent);
    bBackgroundColor.setFocusPainted(false);
    bBackgroundColor.setToolTipText("Background color");
    gbl.setConstraints(bBackgroundColor, gbc);
    add(bBackgroundColor);

    gbc.gridx++;
    gbc.gridy -= 3;
    gbc.insets.left = 24;
    label = new JLabel("Max. value color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Min. value color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Zero value color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridy++;
    label = new JLabel("Selection color: ");
    gbl.setConstraints(label, gbc);
    add(label);

    gbc.gridx++;
    gbc.gridy-=3;
    gbc.insets.left = 8;
    gbc.insets.right = 16;
    bMaxValueColor = new JButton(new ColorSwatch(18, 18));
    bMaxValueColor.setPreferredSize(new Dimension(20, 20));
    bMaxValueColor.addActionListener(parent);
    bMaxValueColor.setFocusPainted(false);
    bMaxValueColor.setToolTipText("Chroma code for maximum (positive) values");
    gbl.setConstraints(bMaxValueColor, gbc);
    add(bMaxValueColor);

    gbc.gridy++;
    bMinValueColor = new JButton(new ColorSwatch(18, 18));
    bMinValueColor.setPreferredSize(new Dimension(20, 20));
    bMinValueColor.addActionListener(parent);
    bMinValueColor.setFocusPainted(false);
    bMinValueColor.setToolTipText("Chroma code for minimum (negative) values");
    gbl.setConstraints(bMinValueColor, gbc);
    add(bMinValueColor);

    gbc.gridy++;
    bZeroValueColor = new JButton(new ColorSwatch(18, 18));
    bZeroValueColor.setPreferredSize(new Dimension(20, 20));
    bZeroValueColor.addActionListener(parent);
    bZeroValueColor.setFocusPainted(false);
    bZeroValueColor.setToolTipText("Chroma code for zero values");
    gbl.setConstraints(bZeroValueColor, gbc);
    add(bZeroValueColor);

    gbc.gridy++;
    bSelectedColor = new JButton(new ColorSwatch(18, 18));
    bSelectedColor.setPreferredSize(new Dimension(20, 20));
    bSelectedColor.addActionListener(parent);
    bSelectedColor.setFocusPainted(false);
    bSelectedColor.setToolTipText("Chroma code for selected neurons");
    gbl.setConstraints(bSelectedColor, gbc);
    add(bSelectedColor);
  }
}
