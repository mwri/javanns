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
 * Title:        SubPatternPanel<p>
 * Description:  Panel for subpattern navigation and control<p>
 * Copyright:    Copyright (c) 2001<p>
 * Company:      WSI<p>
 * @author Igor Fischer
 * @version
 */
package javanns;


import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;

public class SubPatternPanel
             extends JPanel
             implements ActionListener, NetworkListener {

  public static final int SET_TRAINING = 1;
  public static final int SET_SHOW     = 2;
  public static final int SET_TRAINING_SHOW = SET_TRAINING | SET_SHOW;
  Snns snns;
  MasterControl master;
  Network network;
  KernelInterface.KernelPatternInfo kpi;
  JCheckBox cbShuffle;
  DimensionPanel dp1, dp2;

  class DimensionPanel extends JPanel {
    FlatButton bFirst, bPrev, bNext, bLast;
    JLabel lSize =            new JLabel("Size", JLabel.CENTER);
    JLabel lStep =            new JLabel("Step", JLabel.CENTER);
    JLabel lPosition =        new JLabel("Position", JLabel.CENTER);
    JLabel lTotal =           new JLabel("Total", JLabel.CENTER);
    JLabel lInput =           new JLabel("Input: ", JLabel.LEFT);
    JLabel lOutput =          new JLabel("Output: ", JLabel.LEFT);
    JTextField tSizeIn =      new JTextField("1", 4);
    JTextField tSizeOut =     new JTextField("1", 4);
    JTextField tStepIn =      new JTextField("1", 4);
    JTextField tStepOut =     new JTextField("1", 4);
    JTextField tPositionIn =  new JTextField("1", 4);
    JTextField tPositionOut = new JTextField("1", 4);
    JTextField tTotalIn =     new JTextField("1", 4);
    JTextField tTotalOut =    new JTextField("1", 4);

    public DimensionPanel(int dimension) {
      setBorder(
        BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder(), "Dimension " + dimension
        )
      );
      Color bgd = getBackground();
      tSizeIn.setHorizontalAlignment(JTextField.RIGHT);
      tSizeOut.setHorizontalAlignment(JTextField.RIGHT);
      tStepIn.setHorizontalAlignment(JTextField.RIGHT);
      tStepOut.setHorizontalAlignment(JTextField.RIGHT);
      tPositionIn.setEditable(false);
      tPositionIn.setBackground(bgd);
      tPositionIn.setHorizontalAlignment(JTextField.CENTER);
      tPositionOut.setEditable(false);
      tPositionOut.setBackground(bgd);
      tPositionOut.setHorizontalAlignment(JTextField.CENTER);
      tTotalIn.setEditable(false);
      tTotalIn.setBackground(bgd);
      tTotalIn.setHorizontalAlignment(JTextField.CENTER);
      tTotalOut.setEditable(false);
      tTotalOut.setBackground(bgd);
      tTotalOut.setHorizontalAlignment(JTextField.CENTER);

      bFirst = new FlatButton(snns.icons.getIcon("leftEndArrow.gif", "First"));
      bFirst.setToolTipText("Show first subpattern of the current pattern");
      bPrev  = new FlatButton(snns.icons.getIcon("leftArrow.gif", "Previous"));
      bPrev.setToolTipText("Show previous subpattern of the current pattern");
      bNext  = new FlatButton(snns.icons.getIcon("rightArrow.gif", "Next"));
      bNext.setToolTipText("Show next subpattern of the current pattern");
      bLast  = new FlatButton(snns.icons.getIcon("rightEndArrow.gif", "Last"));
      bLast.setToolTipText("Show last subpattern of the current pattern");

      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      setLayout(gbl);

      Insets ins = new Insets(3, 3, 2, 2);
      Insets bIns = new Insets(0, 0, 0, 0);
      gbc.fill = gbc.HORIZONTAL;
      gbc.insets = ins;
      gbc.gridy = 0;
      gbc.gridx = 1;

      // Title line:
      gbl.setConstraints(lSize, gbc);
      add(lSize);
      gbc.gridx++;
      gbl.setConstraints(lStep, gbc);
      add(lStep);
      gbc.gridx+=3;
      gbl.setConstraints(lPosition, gbc);
      add(lPosition);
      gbc.gridx+=3;
      gbl.setConstraints(lTotal, gbc);
      add(lTotal);
      gbc.gridx = 0;
      gbc.gridy++;

      // Input line:
      gbl.setConstraints(lInput, gbc);
      add(lInput);
      gbc.gridx++;
      gbl.setConstraints(tSizeIn, gbc);
      add(tSizeIn);
      gbc.gridx++;
      gbl.setConstraints(tStepIn, gbc);
      add(tStepIn);
      gbc.gridx++;

      gbc.gridheight = 2;
      gbc.insets = new Insets(0, 15, 0, 0);
      gbl.setConstraints(bFirst, gbc);
      add(bFirst);
      gbc.gridx++;
      gbc.insets = bIns;
      gbl.setConstraints(bPrev, gbc);
      add(bPrev);
      gbc.gridx++;
      gbc.gridheight = 1;
      gbc.insets = ins;

      gbl.setConstraints(tPositionIn, gbc);
      add(tPositionIn);
      gbc.gridx++;

      gbc.gridheight = 2;
      gbc.insets = bIns;
      gbl.setConstraints(bNext, gbc);
      add(bNext);
      gbc.gridx++;
      gbc.insets = new Insets(0, 0, 0, 15);
      gbl.setConstraints(bLast, gbc);
      add(bLast);
      gbc.gridx++;
      gbc.gridheight = 1;
      gbc.insets = ins;

      gbl.setConstraints(tTotalIn, gbc);
      add(tTotalIn);
      gbc.gridx = 0;

      // Output line:
      gbc.gridy++;
      gbl.setConstraints(lOutput, gbc);
      add(lOutput);
      gbc.gridx++;
      gbl.setConstraints(tSizeOut, gbc);
      add(tSizeOut);
      gbc.gridx++;
      gbl.setConstraints(tStepOut, gbc);
      add(tStepOut);
      gbc.gridx += 3;
      gbl.setConstraints(tPositionOut, gbc);
      add(tPositionOut);
      gbc.gridx += 3;
      gbl.setConstraints(tTotalOut, gbc);
      add(tTotalOut);
    }


    /**
     * Shows or hides input or output fields according to
     * passed parameters.
     *
     * @param in  flag controling the visibility of the input fields
     * @param out flag controling the visibility of the output fields
     */
    public void updateVisibility(boolean in, boolean out) {
      lInput.setVisible(in);
      lOutput.setVisible(out);
      tSizeIn.setVisible(in);
      tSizeOut.setVisible(out);
      tStepIn.setVisible(in);
      tStepOut.setVisible(out);
      tPositionIn.setVisible(in);
      tPositionOut.setVisible(out);
      tTotalIn.setVisible(in);
      tTotalOut.setVisible(out);
    }
  }


  /**
   * Constructs a panel for navigating and controlling subpatterns.
   *
   * @param master master control panel embedding this panel
   */
  public SubPatternPanel(MasterControl master) {
    this.master = master;
    snns = master.snns;
    network = master.network;
    network.addListener( this );
    kpi = network.getPatInfo();

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);

    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 2, 2);
    gbc.anchor = gbc.NORTHWEST;
    gbc.gridy = gbc.gridx = 0;

    dp1 = new DimensionPanel(1);
    dp2 = new DimensionPanel(2);

    cbShuffle = new JCheckBox("Shuffle", false);
    cbShuffle.setToolTipText("Shuffle subpatterns during learning");
    gbl.setConstraints(cbShuffle, gbc);
    add(cbShuffle);
    gbc.gridx++;

    gbl.setConstraints(dp1, gbc);
    add(dp1);
    gbc.gridy++;
    gbl.setConstraints(dp2, gbc);
    add(dp2);

    dp1.bFirst.addActionListener(this);
    dp1.bPrev.addActionListener(this);
    dp1.bNext.addActionListener(this);
    dp1.bLast.addActionListener(this);
    dp2.bFirst.addActionListener(this);
    dp2.bPrev.addActionListener(this);
    dp2.bNext.addActionListener(this);
    dp2.bLast.addActionListener(this);
  }


  /**
   * Event handler for UpdatePanel.
   *
   * @param evt ActionEvent object
   */
  public void actionPerformed( ActionEvent evt ) {
    Object src = null;
    if(evt != null) src = evt.getSource();

    int[] insize  = new int[kpi.input_dim];
    int[] instep  = new int[kpi.input_dim];
    int[] inpos   = new int[kpi.input_dim];
    int[] inmax   = new int[kpi.input_dim];
    int[] outsize = new int[kpi.output_dim];
    int[] outstep = new int[kpi.output_dim];
    int[] outpos  = new int[kpi.output_dim];
    int[] outmax  = new int[kpi.output_dim];

    try {
      switch(kpi.input_dim) {
      case 2:
        insize[1]  = Integer.parseInt(dp2.tSizeIn.getText());
        instep[1]  = Integer.parseInt(dp2.tStepIn.getText());
        inpos[1]   = Integer.parseInt(dp2.tPositionIn.getText());
        inmax[1]   = Integer.parseInt(dp2.tTotalIn.getText())
          - Integer.parseInt(dp2.tSizeIn.getText()) + 1;
        if(src == dp2.bFirst) inpos[1] = 1;
        if(src == dp2.bPrev && inpos[1]-instep[1] > 0) inpos[1] -= instep[1];
        if(src == dp2.bNext && inpos[1]+instep[1] <= inmax[1]) inpos[1] += instep[1];
        if(src == dp2.bLast) inpos[1] = inmax[1];

      case 1:
        insize[0]  = Integer.parseInt(dp1.tSizeIn.getText());
        instep[0]  = Integer.parseInt(dp1.tStepIn.getText());
        inpos[0]   = Integer.parseInt(dp1.tPositionIn.getText());
        inmax[0]   = Integer.parseInt(dp1.tTotalIn.getText())
          - Integer.parseInt(dp1.tSizeIn.getText()) + 1;
        if(src == dp1.bFirst) inpos[0] = 1;
        if(src == dp1.bPrev && inpos[0]-instep[0] > 0) inpos[0] -= instep[0];
        if(src == dp1.bNext && inpos[0]+instep[0] <= inmax[0]) inpos[0] += instep[0];
        if(src == dp1.bLast) inpos[0] = inmax[0];
      }

      switch(kpi.output_dim) {
      case 2:
        outsize[1] = Integer.parseInt(dp2.tSizeOut.getText());
        outstep[1] = Integer.parseInt(dp2.tStepOut.getText());
        outpos[1]  = Integer.parseInt(dp2.tPositionOut.getText());
        outmax[1]  = Integer.parseInt(dp2.tTotalOut.getText())
          - Integer.parseInt(dp2.tSizeOut.getText()) + 1;
        if(src == dp2.bFirst) outpos[1] = 1;
        if(src == dp2.bPrev && outpos[1]-outstep[1] > 0) outpos[1] -= outstep[1];
        if(src == dp2.bNext && outpos[1]+outstep[1] <= outmax[1]) outpos[1] += outstep[1];
        if(src == dp2.bLast) outpos[1] = outmax[1];

      case 1:
        outsize[0] = Integer.parseInt(dp1.tSizeOut.getText());
        outstep[0] = Integer.parseInt(dp1.tStepOut.getText());
        outpos[0]  = Integer.parseInt(dp1.tPositionOut.getText());
        outmax[0]  = Integer.parseInt(dp1.tTotalOut.getText())
          - Integer.parseInt(dp1.tSizeOut.getText()) + 1;
        if(src == dp1.bFirst) outpos[0] = 1;
        if(src == dp1.bPrev && outpos[0]-outstep[0] > 0) outpos[0] -= outstep[0];
        if(src == dp1.bNext && outpos[0]+outstep[0] <= outmax[0]) outpos[0] += outstep[0];
        if(src == dp1.bLast) outpos[0] = outmax[0];
      }

      if(src == dp1.bFirst || src == dp1.bPrev ||
         src == dp1.bNext  || src == dp1.bLast ||
         src == dp2.bFirst || src == dp2.bPrev ||
         src == dp2.bNext  || src == dp2.bLast)
           network.defShowSubPat(insize, outsize, inpos, outpos);
      network.defTrainSubPat(insize, outsize, instep, outstep);
    }
    catch(Exception e) { showException(e); return; }

    switch(kpi.input_dim) {
      case 2:
      dp2.tPositionIn.setText(String.valueOf(inpos[1]));

      case 1:
      dp1.tPositionIn.setText(String.valueOf(inpos[0]));
    }
    switch(kpi.output_dim) {
      case 2:
      dp2.tPositionOut.setText(String.valueOf(outpos[1]));

      case 1:
      dp1.tPositionOut.setText(String.valueOf(outpos[0]));
    }
  }


  /**
   * Implements the NetworkListener interface. Changes subpattern
   * data when another pattern was selected.
   *
   * @param evt NetworkEvent that has happened
   */
  public void networkChanged(NetworkEvent evt) {
    if( evt.id == NetworkEvent.PATTERN_SET_LOADED  ||
        evt.id == NetworkEvent.PATTERN_SET_CREATED ||
        evt.id == NetworkEvent.PATTERN_SET_CHANGED ||
        evt.id == NetworkEvent.PATTERN_CHANGED     ||
        evt.id == NetworkEvent.PATTERN_CREATED     ||
        evt.id == NetworkEvent.PATTERN_DELETED     ||
        evt.id == NetworkEvent.SUBPATTERN_CHANGED ){
      if(dp1 == null || dp2 == null) return;
      kpi = network.getPatInfo();
      if(kpi == null) return;

      boolean flagIn1  = false;
      boolean flagIn2  = false;
      boolean flagOut1 = false;
      boolean flagOut2 = false;
      boolean flagD1 = false;
      boolean flagD2 = false;

      if(kpi.in_number_of_dims >= 1 || kpi.out_number_of_dims >= 1)
        flagD1 = true;
      if(kpi.in_number_of_dims == 2 || kpi.out_number_of_dims == 2)
        flagD2 = true;

      if(flagD1 && kpi.in_number_of_dims  > 0) flagIn1  = true;
      if(flagD1 && kpi.out_number_of_dims > 0) flagOut1 = true;
      if(flagD2 && kpi.in_number_of_dims  > 1) flagIn2  = true;
      if(flagD2 && kpi.out_number_of_dims > 1) flagOut2 = true;

      dp1.setVisible(flagD1);
      if(flagD1) dp1.updateVisibility(flagIn1, flagOut1);
      dp2. setVisible(flagD2);
      if(flagD2) dp2.updateVisibility(flagIn2, flagOut2);

      switch(kpi.input_dim) {
        case 2:
        dp2.tTotalIn.setText(String.valueOf(kpi.input_dim_sizes[1]));
        if(evt.id != NetworkEvent.SUBPATTERN_CHANGED) {
          dp2.tPositionIn.setText("1");
        }

        case 1:
        dp1.tTotalIn.setText(String.valueOf(kpi.input_dim_sizes[0]));
        if(evt.id != NetworkEvent.SUBPATTERN_CHANGED) {
          dp1.tPositionIn.setText("1");
        }
        break;

        default:
        dp1.tTotalIn.setText("0");
        dp1.tPositionIn.setText("0");
      }

      switch(kpi.output_dim) {
        case 2:
        dp2.tTotalOut.setText(String.valueOf(kpi.output_dim_sizes[1]));
        if(evt.id != NetworkEvent.SUBPATTERN_CHANGED) {
          dp2.tPositionOut.setText("1");
        }

        case 1:
        dp1.tTotalOut.setText(String.valueOf(kpi.output_dim_sizes[0]));
        if(evt.id != NetworkEvent.SUBPATTERN_CHANGED) {
          dp1.tPositionOut.setText("1");
        }
        break;

        default:
        dp1.tTotalOut.setText("0");
        dp1.tPositionOut.setText("0");
      }
    }
  }


  /**
   * Sets subpattern training scheme.
   */
  public void setSubPatternScheme() {
    //emulate actionEvent to get entry fields copied and checked
    actionPerformed(null);
  }


  private void showException(String text){
    showException(new Exception(text));
  }


  private void showException(Exception e){
    master.snns.showException(e, this );
  }
}
