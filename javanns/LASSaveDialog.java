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



//Titel:        JavaSNNS
//Version:
//Copyright:    Copyright (c) 1998
//Autor:       Fabian Hennecke
//Firma:      WSI
//Beschreibung:  Ihre Beschreibung

package javanns;


import java.awt.Container ;
import java.awt.event.* ;
import javax.swing.* ;

/**
 * this dialog is used to ask the user if some of the LoaderAndSavers whose
 * contents changed should be saved before the program exits
 */
public class LASSaveDialog implements ActionListener{
  Snns snns;
  LoaderAndSaver[] las;
  boolean ret_val;
  JDialog dialog;
  JButton[] option;
  JCheckBox[] cbs;

  /**
   * method returns false if the saving has been canceled
   */
  public boolean show( Snns snns, LoaderAndSaver[] las ){
    if( las == null || las.length == 0 ) return true;
    this.snns = snns;
    this.las = las;

    Container c = new Container();
    c.setLayout( new BoxLayout(c, BoxLayout.Y_AXIS ) );
    cbs = new JCheckBox[ las.length ];
    for( int i=0; i<las.length; i++ ){
      cbs[i] = new JCheckBox( las[i].getLASName(),true );
      c.add( cbs[i] );
    }

    option = new JButton[]{
        new JButton("Ok"),
        new JButton("Save All"),
        new JButton("Save None"),
        new JButton("Cancel") };
    for( int i=0; i<4; i++ ) option[i].addActionListener( this );
    option[0].setMnemonic(KeyEvent.VK_O);
    option[1].setMnemonic(KeyEvent.VK_A);
    option[2].setMnemonic(KeyEvent.VK_N);
    option[3].setMnemonic(KeyEvent.VK_C);


    JOptionPane pane = new JOptionPane( c, JOptionPane.QUESTION_MESSAGE,
      JOptionPane.OK_CANCEL_OPTION, null, option );

    dialog = pane.createDialog( snns, "Save Changes" );
    dialog.setVisible( true );

    return ret_val;
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    boolean save = false;
    ret_val = true;

    if(src == option[1]) {
      for(int i=0; i<cbs.length; i++) cbs[i].setSelected(true);
      save = true;
    }
    else if(src == option[2]) {
      for(int i=0; i<cbs.length; i++) cbs[i].setSelected(false);
      save = false;
    }
    else if(src == option[0]) save = true;
    else if(src == option[3]) ret_val = false;

    if(save) for(int i=0; i<cbs.length; i++) {
      if(cbs[i].isSelected()) {
        LoaderAndSaver ls = las[i];
        try {
          if(ls.hasHomeFile()) ls.save();
          else if(!snns.fileManager.showSaveDialog(ls)) {
            ret_val = false;
          }
        }
        catch(Exception ex) {
          snns.showException(ex, this);
          ret_val = false;
        }
      }
    }
    dialog.setVisible( false );
  }
}
