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

/**
 * PatternsPanel controls patterns in SNNS. It enables adding, modifying and
 *  deleting patterns and creating new pattern sets.
 *
 */
class PatternsPanel extends ControlPanel implements NetworkListener {
  Network network;
  PatternSets sets;

  JTextField tPattern;
  FlatButton bAdd, bModify, bDelete;
  FlatButton bNew1, bNew2, bEmpty1, bEmpty2;
  FlatButton bFirst, bPrev, bNext, bLast;
  NamedComboBox cSet1, cSet2;

  private int cSet1_lastSelected = -1;
  private int cSet2_lastSelected = -1;
  private boolean isRearanging = false;

  /**
   * Class constructor. Creates the GUI.
   *
   */
  public PatternsPanel(MasterControl master){
    super(master, "Remapping function: ", Function.REMAPPING);
    master.pp = this;
    network = master.network;
    network.addListener( this );
    sets = snns.patternSets;

    JLabel label;
    Point pr;
    int i;


    // Training pattern set
    label = new JLabel("Training set: ");
    add(label);
    p = movePref(label, 1, y);

    cSet1 = new NamedComboBox();
    add(cSet1);
    cSet1.addActionListener(this);
    cSet1.setToolTipText("Choose training set");
    cSet1.setSize(new Dimension(20*grid, cSet1.getPreferredSize().height));
    p = move(cSet1, p.x, y);
    for(i=0; i<sets.size(); i++) cSet1.addItem( sets.getSet(i) );

    // Validation pattern set
    int train_y = y;
    y = p.y + 1;
    int valid_y = y;
    label = new JLabel("Validation set: ");
    add(label);
    p = movePref(label, 1, y);

    int valid_x = p.x;
    cSet2 = new NamedComboBox();
    add(cSet2);
    cSet2.addActionListener(this);
    cSet2.setToolTipText("Choose validation set");
    cSet2.setSize(new Dimension(20*grid, cSet2.getPreferredSize().height));
    p = move(cSet2, p.x, y);
    y = train_y;
    p = move(cSet1, valid_x, y);
    valid_x = p.x;
    for(i=0; i<sets.size(); i++) cSet2.addItem(sets.getSet(i));

    // Training pattern set: buttons
    bEmpty1 = new FlatButton(snns.icons.getIcon("deletePatternSet.gif", "Remove training set"));
    add(bEmpty1);
    bEmpty1.addActionListener(this);
    bEmpty1.setToolTipText("Remove pattern set");
    p = movePref(bEmpty1, p.x, y);

    bNew1 = new FlatButton(snns.icons.getIcon("newPatternSet.gif", "Create new pattern set"));
    add(bNew1);
    bNew1.addActionListener(this);
    bNew1.setToolTipText("Create new pattern set");
    p = movePref(bNew1, p.x, y);


    // Pattern editing
    bDelete = new FlatButton(snns.icons.getIcon("deletePattern.gif", "Delete current pattern"));
    add(bDelete);
    bDelete.addActionListener(this);
    bDelete.setToolTipText("Delete the current pattern");
    pr = movePrefRevX(bDelete, 1, y);

    bModify = new FlatButton(snns.icons.getIcon("modifyPattern.gif", "Modify pattern"));
    add(bModify);
    bModify.addActionListener(this);
    bModify.setToolTipText("Modify the current pattern to reflect current unit activations");
    pr = movePrefRevX(bModify, pr.x, y);

    bAdd = new FlatButton(snns.icons.getIcon("addPattern.gif", "Create new pattern"));
    add(bAdd);
    bAdd.addActionListener(this);
    bAdd.setToolTipText("Add a new pattern, made up of current in/out activations");
    pr = movePrefRevX(bAdd, pr.x, y);


    // Validation pattern set: buttons
    y = valid_y;
    bEmpty2 = new FlatButton(snns.icons.getIcon("deletePatternSet.gif", "Remove pattern set"));
    add(bEmpty2);
    bEmpty2.addActionListener(this);
    bEmpty2.setToolTipText("Remove pattern set");
    p = movePref(bEmpty2, valid_x, y);

    bNew2 = new FlatButton(snns.icons.getIcon("newPatternSet.gif", "Create new pattern set"));
    add(bNew2);
    bNew2.addActionListener(this);
    bNew2.setToolTipText("Create new pattern set");
    p = movePref(bNew2, p.x, y);


    // Navigation through patterns
    bLast = new FlatButton(snns.icons.getIcon("rightEndArrow.gif", "Last"));
    add(bLast);
    bLast.addActionListener(this);
    bLast.setToolTipText("Show last pattern in set");
    pr = movePrefRevX(bLast, 1, y);

    bNext = new FlatButton(snns.icons.getIcon("rightArrow.gif", "Next"));
    add(bNext);
    bNext.addActionListener(this);
    bNext.setToolTipText("Show next pattern");
    pr = movePrefRevX(bNext, pr.x, y);

    tPattern = new JTextField(4);
    Dimension d1 = tPattern.getPreferredSize();
    Dimension d2 = bLast.getPreferredSize();
    if(network != null) tPattern.setText( String.valueOf(network.getCurrentPatternNo())) ;
    else tPattern.setText("0");
    tPattern.setSize(new Dimension(d1.width, d2.height));
    tPattern.setHorizontalAlignment(JTextField.CENTER);
    tPattern.addActionListener( this );
    tPattern.addMouseListener(
      new MouseAdapter(){
        public void mouseClicked(MouseEvent e){
          actionPerformed( new ActionEvent( tPattern, 0, "" ) );
        }
      }
    );
    add(tPattern);
    tPattern.setToolTipText("Current pattern number");
    pr = moveRevX(tPattern, pr.x, y);

    bPrev = new FlatButton(snns.icons.getIcon("leftArrow.gif", "Previous"));
    add(bPrev);
    bPrev.addActionListener(this);
    bPrev.setToolTipText("Show previous pattern");
    pr = movePrefRevX(bPrev, pr.x, y);

    bFirst = new FlatButton(snns.icons.getIcon("leftEndArrow.gif", "First"));
    add(bFirst);
    bFirst.addActionListener(this);
    bFirst.setToolTipText("Show first pattern in set");
    pr = movePrefRevX(bFirst, pr.x, y);
  }


  /**
   * Event handler for PatternsPanel.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    //System.out.println("PatternsPanel.actionPerformed: " + e );
    super.actionPerformed(e);
    if(isRearanging) return;

    PatternSet s;
    Object src = e.getSource();
    try{
      if(src == bFirst) network.setFirstPattern(false);
      else if(src == bPrev) network.setPreviousPattern(false);
      else if(src == bNext) network.setNextPattern(false);
      else if(src == bLast) network.setLastPattern(false);
    }
    catch( Exception ex ){ showException( ex ); }

    // training set: create new pattern set
    if(src == bNew1) {
      if(cSet2 != null) cSet2.setEditable(false);
      if(cSet1.isEditable()) createPatternSet(cSet1);
      else {
        cSet1.setEditable(true);
        cSet1.getEditor().setItem("<new set>");
        cSet1.getEditor().selectAll();
      }
    }
    // training set: change current pattern set
    else if(src == cSet1) {
      if(cSet2 != null) cSet2.setEditable(false);
      PatternSet ps = (PatternSet)cSet1.getSelectedObject();
      if(ps == null && cSet1.isEditable()) createPatternSet(cSet1);
      else if(ps != null && sets.size() > 0)
        sets.setCurrent( ps );
      cSet1.setEditable(false);
      master.updateTabs();
    }
    // training set: remove pattern set
    else if(src == bEmpty1 && sets.size() > 0) {
      s = (PatternSet)cSet1.getSelectedObject();
      sets.deleteSet(s);
      s = sets.getCurrent();
      if( s != null ) cSet1.setSelectedItem( s );
      master.updateTabs();
    }

    // validation set: create new pattern set
    else if(src == bNew2) {
      if(cSet1 != null) cSet1.setEditable(false);
      if(cSet2.isEditable()) createPatternSet(cSet2);
      else {
        cSet2.setEditable(true);
        cSet2.getEditor().setItem("<new set>");
        cSet2.getEditor().selectAll();
      }
      master.updateTabs();
    }
    // validation set: change current pattern set
    else if(src == cSet2) {
      if(cSet1 != null) cSet1.setEditable(false);
      PatternSet ps = (PatternSet)cSet2.getSelectedObject();
      if(ps == null && cSet2.isEditable()) createPatternSet(cSet2);
      else if(ps != null && sets.size() > 0)
        sets.setValidationSet( ps );
      cSet2.setEditable(false);
    }
    // validation set: remove pattern set
    else if(src == bEmpty2 && sets.size() > 0) {
      s = (PatternSet)cSet2.getSelectedObject();
      sets.deleteSet(s);
      s = sets.getCurrent();
      if( s!= null ) cSet2.setSelectedItem( s );
    }

    else if(src == tPattern){
      int old = network.getCurrentPatternNo();
      boolean did_it = false;
      try{ did_it = network.setPattern( Integer.parseInt(tPattern.getText()), false ); }
      catch( Exception ex ){}
      if( !did_it ) tPattern.setText( String.valueOf(old));
    }

    else {
      if( snns != null ){
        NetworkView view = snns.getLastSelectedView();
        if( view != null && view.getLabelsToEdit() == NetworkViewSettings.ACT )
          view.evaluateDirectEdit( false );
      }

      if(src == bAdd)
        try{ network.createPattern(); }
        catch( Exception ex ){ showException( ex ); }
      else if(src == bModify) network.modifyPattern();
      else if(src == bDelete)
        try{ network.deletePattern(); }
        catch( Exception ex ){ showException( ex ); }
    }

    if(src != cSet1 && src != cSet2 && src != bNew1 && src != bNew2) {
      if(cSet1 != null) cSet1.setEditable(false);
      if(cSet2 != null) cSet2.setEditable(false);
    }
  }


  /**
   * Creates a new pattern set with the name given in the ComboBox.
   *
   * @return <code>true</code> if set successfully created.
   */
  public boolean createPatternSet(NamedComboBox cb) {
    //System.out.println("PatternPanel.createPatternSet(...)");
    if(cb.getItemCount() >= 5) return false;     // TO DO: check through kernel!
    String s = (String)cb.getEditor().getItem();
    cb.setEditable(false);
    sets.createSet(s);
    return true;
  }

  // implementing NetworkListener:
  /**
   * Changes the current pattern number when another pattern
   * was selected
   */
  public void networkChanged(NetworkEvent evt) {
    if(tPattern == null) return;
    Network network = evt.getNetwork();

    if( evt.id == NetworkEvent.PATTERN_CHANGED ||
        evt.id == NetworkEvent.PATTERN_CREATED ||
        evt.id == NetworkEvent.PATTERN_DELETED  )
      tPattern.setText( String.valueOf(network.getCurrentPatternNo())) ;

    else if(evt.id == NetworkEvent.PATTERN_SET_LOADED  ||
            evt.id == NetworkEvent.PATTERN_SET_CREATED ||
            //evt.id == NetworkEvent.PATTERN_SET_DELETED ||
            evt.id == NetworkEvent.PATTERN_SET_CHANGED  ) {
      rearangeSets();
      tPattern.setText( String.valueOf(network.getCurrentPatternNo()));
      PatternSet s = sets.getCurrent();
      cSet1.setSelectedItem( s );
      s = sets.getValidationSet();
      cSet2.setSelectedItem( s );
    }
  }

  private void rearangeSets(){
    isRearanging = true;
    PatternSet ps;
    cSet1.removeAllItems();
    cSet2.removeAllItems();
    for(int i=0; i<sets.size(); i++) {
      ps = sets.getSet(i);
      cSet1.addItem( ps );
      cSet2.addItem( ps );
    }
    isRearanging = false;
  }
}
