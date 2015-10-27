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
 * DisplaySettingsPanel allows adjusting appearance of network window.
 *   It encloses General, Units&Links, and Layers Pane.
 *
 */
class DisplaySettingsPanel extends JPanel implements ActionListener {
  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/
  private UnitsAndLinksSettings pUnitsLinks;
  private GeneralNetworkSettings pGeneral;
  private SOMSettings pSOM;
  private JPanel pLayers;
  private boolean isSOM;
  JButton bPreview, bOK, bDefault, bCancel;

  JPopupMenu cc;
  JButton ccCaller;

  Snns snns;
  NetworkView view;
  NetworkViewSettings orig_settings;
  Color orig_pos_color, orig_neg_color, orig_null_color;


  /**
   * Class constructor:
   *  creates panel with its elements (buttons, input fields...)
   */
  public DisplaySettingsPanel(Snns snns, NetworkView nv) {
    this.snns = snns;
    view = nv;
    if( view instanceof SOMPanel ) isSOM = true;

    setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

    JTabbedPane tabs = new JTabbedPane();
    pGeneral = new GeneralNetworkSettings(this);
    tabs.add("General", pGeneral);
//    pLayers = new LayersSettings(this);
//    tabs.add("Layers", pLayers);
    if( isSOM ){
      pSOM = new SOMSettings( this );
      tabs.add("SOM", pSOM);
    }
    else{
      pUnitsLinks = new UnitsAndLinksSettings(this);
      tabs.add("Units & Links", pUnitsLinks);
    }

    add(tabs);


    bPreview = new JButton("Preview");
    bPreview.addActionListener(this);
    bPreview.setToolTipText("See how it looks like, without commiting changes");
    Dimension d = bPreview.getPreferredSize();

    bOK = new JButton("OK");
    bOK.addActionListener(this);
    bOK.setToolTipText("Commit changes");
    bOK.setPreferredSize(d);

    bDefault = new JButton("Default");
    bDefault.addActionListener(this);
    bDefault.setToolTipText("Back to default settings");
    bDefault.setPreferredSize(d);

    bCancel = new JButton("Cancel");
    bCancel.addActionListener(this);
    bCancel.setToolTipText("Close this window withot commiting changes");
    bCancel.setPreferredSize(d);

    JPanel p = new JPanel();
    p.setLayout( new FlowLayout(FlowLayout.CENTER, 8, 8));
    p.add(bOK);
    p.add(bPreview);
    p.add(bDefault);
    p.add(bCancel);
    add(p);

    orig_settings = (NetworkViewSettings)view.settings.clone();
    orig_pos_color = orig_settings.pos_color;
    orig_neg_color = orig_settings.neg_color;
    orig_null_color = orig_settings.null_color;

    settings2ui();
  }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  /**
   * Event handler.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();

    if(src == bOK) {
      ui2settings();
      view.update();
      JDialog parent = (JDialog) getParent().getParent().getParent(); // !!!
      parent.setVisible(false);
      parent.dispose();
    }
    if(src == bPreview) {
      ui2settings();
      view.update();
    }
    if(src == bCancel) {
      view.settings   = (NetworkViewSettings)orig_settings.clone(); // to do: make deep copy
      view.settings.pos_color  = orig_pos_color;
      view.settings.neg_color  = orig_neg_color;
      view.settings.null_color = orig_null_color;
      view.update();
      JDialog parent = (JDialog) getParent().getParent().getParent(); // !!!
      parent.setVisible(false);
      parent.dispose();
    }
    if(src == bDefault) {
      view.settings   = NetworkViewSettings.getDefaultSettings();
      view.settings.pos_color  = view.DEFAULT_POS;
      view.settings.neg_color  = view.DEFAULT_NEG;
      view.settings.null_color = view.DEFAULT_NULL;
      settings2ui();
      repaint();
      view.settingsChanged();
      view.update();
    }

    if(src == pGeneral.bMaxValueColor || src == pGeneral.bMinValueColor ||
       src == pGeneral.bZeroValueColor || src == pGeneral.bSelectedColor ||
       src == pGeneral.bTextColor || src == pGeneral.bBackgroundColor) {
      ccCaller = (JButton)src;
      cc = new JPopupMenu("Color");
      cc.setLayout(new GridLayout(8, 8));
      ColorSwatch colorSwatch = new ColorSwatch(16, 16);
      for(int i=0; i<64; i++) {
        FlatButton b = new FlatButton(colorSwatch);
        b.setForeground(NetworkViewSettings.index2color(i));
        b.setPreferredSize(new Dimension(16, 16));
        b.addActionListener(this);
        cc.add(b);
      }
      JDialog parent = (JDialog) getParent().getParent().getParent(); // !!!
      int x = ccCaller.getLocation().x;
      if(x >= parent.getSize().width/2) x -= cc.getPreferredSize().width;
      else x += ccCaller.getSize().width;
      cc.show(ccCaller.getParent(), x, ccCaller.getLocation().y);
    }

    if(src instanceof FlatButton) {
      if(ccCaller == pGeneral.bSelectedColor) {
        view.settings.selection_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.selection_color);
        cc.setVisible(false);
      }
      else if(ccCaller == pGeneral.bMaxValueColor) {
        view.settings.pos_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.pos_color);
        cc.setVisible(false);
      }
      else if(ccCaller == pGeneral.bMinValueColor) {
        view.settings.neg_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.neg_color);
        cc.setVisible(false);
      }
      else if(ccCaller == pGeneral.bZeroValueColor) {
        view.settings.null_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.null_color);
        cc.setVisible(false);
      }
      else if(ccCaller == pGeneral.bTextColor) {
        view.settings.text_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.text_color);
        cc.setVisible(false);
      }
      else if(ccCaller == pGeneral.bBackgroundColor) {
        view.settings.background_color = ((FlatButton)(src)).getForeground();
        ccCaller.setForeground(view.settings.background_color);
        cc.setVisible(false);
      }
    }
  }

  /*-------------------------------------------------------------------------*
   * private methods
   *-------------------------------------------------------------------------*/

  /**
   * Sets the UI elements (buttons, sliders etc.) according to the
   *   network settings
   */
  private void settings2ui() {
    pGeneral.tGrid.setText(String.valueOf(view.settings.grid_size));
    pGeneral.tSubnet.setText(String.valueOf(view.settings.subNetNo));
    //pGeneral.tZ.setText(String.valueOf(view.settings.zValue));
    pGeneral.bMaxValueColor.setForeground(view.settings.pos_color);
    pGeneral.bMinValueColor.setForeground(view.settings.neg_color);
    pGeneral.bZeroValueColor.setForeground(view.settings.null_color);
    pGeneral.bTextColor.setForeground(view.settings.text_color);
    pGeneral.bBackgroundColor.setForeground(view.settings.background_color);
    pGeneral.bSelectedColor.setForeground(view.settings.selection_color);
    if( isSOM )
      pSOM.setValue( view.settings.dist_max );
    else{
      pUnitsLinks.cTop.setSelectedIndex(view.settings.top_label_type);
      pUnitsLinks.cBase.setSelectedIndex(view.settings.base_label_type);
      pUnitsLinks.sMaxValue.setValue( (int)(100 * view.settings.unit_max) );
      pUnitsLinks.cbShow.setSelected(view.settings.show_links);
      pUnitsLinks.cbWeights.setSelected(view.settings.show_weights);
      pUnitsLinks.cbDirections.setSelected(view.settings.show_directions);
      pUnitsLinks.setTriggerValue( view.settings.link_trigger );
      pUnitsLinks.setMaxWeight( view.settings.link_max );
    }
  }

  /**
   * Reads the status of the UI elements (buttons, sliders etc.) and
   *   adjusts the network settings accordingly
   */
  private void ui2settings() {
    NetworkViewSettings settings = view.settings;
    settings.grid_size        = Integer.parseInt(pGeneral.tGrid.getText());
    settings.subNetNo         = Integer.parseInt(pGeneral.tSubnet.getText());
    //settings.zValue           = Integer.parseInt(pGeneral.tZ.getText());
    settings.text_color       = pGeneral.bTextColor.getForeground();
    settings.background_color = pGeneral.bBackgroundColor.getForeground();
    settings.selection_color  = pGeneral.bSelectedColor.getForeground();
    settings.pos_color   = pGeneral.bMaxValueColor.getForeground();
    settings.neg_color   = pGeneral.bMinValueColor.getForeground();
    settings.null_color  = pGeneral.bZeroValueColor.getForeground();
    if( isSOM )
      view.settings.dist_max = pSOM.getValue();
    else{
      settings.top_label_type   = pUnitsLinks.cTop.getSelectedIndex();
      settings.base_label_type  = pUnitsLinks.cBase.getSelectedIndex();
      settings.unit_max         = pUnitsLinks.sMaxValue.getValue()/100.;
      settings.show_links       = pUnitsLinks.cbShow.isSelected();
      settings.show_weights     = pUnitsLinks.cbWeights.isSelected();
      settings.show_directions  = pUnitsLinks.cbDirections.isSelected();
      settings.link_trigger     = pUnitsLinks.getTriggerValue();
      settings.link_max         = pUnitsLinks.getMaxWeight();
    }
    view.settingsChanged();
  }
}
