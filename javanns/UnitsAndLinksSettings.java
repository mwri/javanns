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
 * Panel with settings for units' and links' appearance in network window.
 *
 */
class UnitsAndLinksSettings extends JPanel {
  /*-------------------------------------------------------------------------*
   * public member variables
   *-------------------------------------------------------------------------*/

  /**
   * Possible labels for units
   */
  public static final String[] LABELS = {
    "<nothing>",
    "Activation", "Initial activation", "Output", "Bias",
    "Name", "Number", "z-Value", "Winner"
  };

  /**
   * Number of possible labels
   */
  public static final int N_LABELS = 9;


  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/

  // units:
  JSlider sMaxValue;
  Hashtable sliderTab;
  JComboBox cTop, cBase;

  // links:
  public static final double log10 = Math.log(10);
  private JSlider sTrigger;
  private JSlider sMaxWeight;
  JCheckBox cbShow, cbWeights, cbDirections, cbHideWeak;


  /**
   * Class constructor:
   *  creates panel with its elements (buttons, input fields...)
   */
  public UnitsAndLinksSettings(DisplaySettingsPanel parent) {
    int i;
    int fontSize = 7 * getFontMetrics(getFont()).getHeight() / 5;

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;

    gbc.insets.top = fontSize;
    gbc.insets.left = 16;
    gbc.insets.bottom = 0;
    gbc.insets.right = 16;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = gbc.NORTHWEST;

    // Left units panel, with combo boxes
    JLabel label = new JLabel("Top label: ");
    gbl.setConstraints(label, gbc);
    add(label);
    gbc.gridy++;
    gbc.insets.top = 0;
    cTop = new JComboBox();
    for(i=0; i<N_LABELS; i++) cTop.addItem(LABELS[i]);
    cTop.setToolTipText("Label shown above each unit");
    gbl.setConstraints(cTop, gbc);
    add(cTop);

    gbc.gridy++;
    gbc.insets.top = 16;
    label = new JLabel("Base label: ");
    gbl.setConstraints(label, gbc);
    add(label);
    gbc.gridy++;
    gbc.insets.top = 0;
    gbc.insets.bottom = 16;
    cBase = new JComboBox();
    for(i=0; i<N_LABELS; i++) cBase.addItem(LABELS[i]);
    cBase.setToolTipText("Label shown below each unit");
    gbl.setConstraints(cBase, gbc);
    add(cBase);

    // Right units panel, with slider
    JPanel pUR = new JPanel();
    pUR.setLayout( new BoxLayout(pUR, BoxLayout.Y_AXIS) );
    pUR.add( Box.createVerticalGlue() );
    label = new JLabel("Maximum expected value: ");
    pUR.add(label);

    sliderTab = new Hashtable();
    sliderTab.put(new Integer (0),   new JLabel ("0"));
    sliderTab.put(new Integer (50),  new JLabel ("0.5"));
    sliderTab.put(new Integer (100), new JLabel ("1"));
    sliderTab.put(new Integer (150), new JLabel ("1.5"));
    sliderTab.put(new Integer (200), new JLabel ("2"));
    sMaxValue = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
    sMaxValue.setLabelTable(sliderTab);
    sMaxValue.setMajorTickSpacing (50);
    sMaxValue.setMinorTickSpacing (10);
    sMaxValue.setPaintTicks(true);
    sMaxValue.setPaintLabels(true);
    sMaxValue.setValueIsAdjusting(true);
    sMaxValue.setPreferredSize(new Dimension(120, sMaxValue.getPreferredSize().height));
    sMaxValue.setToolTipText("Units' chroma code is interpolated up to this value");
    pUR.add(sMaxValue);
    gbc.gridx++;
    gbc.gridy -= 3;
    gbc.insets.top = fontSize;
    gbc.gridheight = 4;
    gbc.insets.bottom = 16;
    gbc.insets.left = 0;
    gbl.setConstraints(pUR, gbc);
    gbc.fill = gbc.BOTH;
    gbc.anchor = gbc.SOUTHEAST;
    add(pUR);


    gbc.fill = gbc.NONE;
    gbc.gridwidth = 2;
    gbc.gridheight = 4;
    gbc.gridx = 0;
    gbc.gridy += 4;

    gbc.insets.top = fontSize;
    gbc.insets.left = 16;
    gbc.insets.bottom = 0;
    gbc.insets.right = 16;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = gbc.SOUTHWEST;

    cbShow = new JCheckBox("Show");
    cbShow.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbWeights = new JCheckBox("Show weights");
    cbWeights.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbDirections = new JCheckBox("Show directions");
    cbDirections.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbHideWeak = new JCheckBox("Hide weak links");
    cbHideWeak.addChangeListener(
      new ChangeListener(){
        public void stateChanged(ChangeEvent evt){
          sTrigger.setEnabled( cbHideWeak.isSelected() );
        }
      }
    );
    cbHideWeak.setAlignmentX(Component.LEFT_ALIGNMENT);

    gbl.setConstraints(cbShow, gbc);
    cbShow.setToolTipText("Shows or hides links between units");
    add(cbShow);
    gbc.gridy++;
    gbc.insets.top = 0;
    gbl.setConstraints(cbWeights, gbc);
    cbWeights.setToolTipText("Shows or hides numerical value of links");
    add(cbWeights);
    gbc.gridy++;
    gbl.setConstraints(cbDirections, gbc);
    cbDirections.setToolTipText("Shows or hides arrowheads on links");
    add(cbDirections);
    gbc.gridy++;
    gbl.setConstraints(cbHideWeak, gbc);
    cbHideWeak.setToolTipText("Hides links of weak weight");
    add(cbHideWeak);


    JPanel pLR = new JPanel();
    pLR.setLayout( new BoxLayout(pLR, BoxLayout.Y_AXIS) );

    sliderTab = new Hashtable();
    sliderTab.put(new Integer (-100),   new JLabel ("0.1"));
    sliderTab.put(new Integer (0),  new JLabel ("1"));
    sliderTab.put(new Integer (100),  new JLabel ("10"));
    sliderTab.put(new Integer (200),  new JLabel ("100"));

    label = new JLabel("Maximum expected weight:");
    pLR.add(label);

    sMaxWeight = new JSlider(JSlider.HORIZONTAL, -100, 200, 50);
    sMaxWeight.setLabelTable(sliderTab);
    sMaxWeight.setMajorTickSpacing(100);
    sMaxWeight.setMinorTickSpacing(25);
    sMaxWeight.setPaintTicks(true);
    sMaxWeight.setPaintLabels(true);
    sMaxWeight.setValueIsAdjusting(true);
    sMaxWeight.setPreferredSize(new Dimension(10, sMaxWeight.getPreferredSize().height));
    sMaxWeight.setToolTipText("Links' chroma code is interpolated up to this value");
    pLR.add(sMaxWeight);

    pLR.add( Box.createVerticalGlue() );
    label = new JLabel("Weakest visible link:");
    pLR.add(label);

    sTrigger = new JSlider(JSlider.HORIZONTAL, -100, 200, 50);
    sTrigger.setLabelTable(sliderTab);
    sTrigger.setMajorTickSpacing(100);
    sTrigger.setMinorTickSpacing(25);
    sTrigger.setPaintTicks(true);
    sTrigger.setPaintLabels(true);
    sTrigger.setValueIsAdjusting(true);
    sTrigger.setPreferredSize(new Dimension(10, sTrigger.getPreferredSize().height));
    sTrigger.setToolTipText("Links weaker than this are not shown");
    pLR.add(sTrigger);

    gbc.insets.top = fontSize;
    gbc.insets.left = 0;
    gbc.insets.bottom = 16;
    gbc.gridx++;
    gbc.gridy -= 3;
    gbc.gridheight = 5;
    gbc.fill = gbc.BOTH;
    gbl.setConstraints(pLR, gbc);
    add(pLR);


    /* Bug or feature? We add bordered panels last, so they are first drawn and all other
       components are drawn over them. Cute trick to achieve border around groups of
       cells. Maybe a little bit risky, since it isn't guaranteed to work.
    */

    // Bordered panel for setting units
    JPanel pUnits = new JPanel(); // default flow layout
    pUnits.setBorder(
      BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Units")
    );
    gbc.gridx = gbc.gridy = 0;
    gbc.insets.top = gbc.insets.left = gbc.insets.bottom = gbc.insets.right = 0;
    gbc.gridwidth = 2;
    gbc.gridheight = 4;
    gbc.anchor = gbc.NORTHWEST;
    gbl.setConstraints(pUnits, gbc);
    add(pUnits);

    // Bordered panel for setting links
    JPanel pLinks = new JPanel();  // default flow layout
    pLinks.setBorder(
      BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Links")
    );
    gbc.gridheight = 5;
    gbc.gridy += 4;
    gbl.setConstraints(pLinks, gbc);
    add(pLinks);
  }

  void setTriggerValue( double v ){
    sTrigger.setValue((int)( 100 * Math.log(v) / log10 ) );
  }

  double getTriggerValue(){
    if( !cbHideWeak.isSelected() ) return 0.0;
    return Math.exp( sTrigger.getValue() * log10 / 100. );
  }

  void setMaxWeight( double v ){
    sMaxWeight.setValue((int)( 100 * Math.log(v) / log10 ) );
  }

  double getMaxWeight(){
    return Math.exp( sMaxWeight.getValue() * log10 / 100. );
  }
}
