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

import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;



/**
 * The status bar, informing the user which files are currently
 * open and/or active.
 */
public class StatusPanel extends JToolBar implements NetworkListener {
  Snns snns;
  Network net;
  PatternSets sets;

  JLabel[] lNet, lTrPat, lValPat ;

  /**
   * Creates the stautus bar with initialy no files loaded.
   *
   * @param snns the main program class
   */
  public StatusPanel(Snns snns) {
    this.snns = snns;
    net = snns.network;
    sets = snns.patternSets;

    net.addListener( this );

    ImageIcon icon = snns.icons.getIcon("network.gif");
    lNet = new JLabel[] {
      new JLabel("Network: ", icon, SwingConstants.LEFT),
      new JLabel("default")
    };
    add( lNet[0] );
    add( lNet[1] );

    addSeparator();

    icon = snns.icons.getIcon("patternSet.gif");
    lTrPat = new JLabel[] {
      new JLabel("Training pattern set: ", icon, SwingConstants.LEFT),
      new JLabel("<none>")
    };
    add( lTrPat[0] );
    add( lTrPat[1] );

    addSeparator();

    lValPat = new JLabel[] {
      new JLabel("Validation pattern set: ", icon, SwingConstants.LEFT),
      new JLabel("<none>")
    };
    add( lValPat[0] );
    add( lValPat[1] );
  }


  /**
   * Implementation of the NetworkListener. Updates the stautus bar
   * fields when network or pattern set change.
   *
   * @param evt the event that caused the call to this method
   */
  public void networkChanged(NetworkEvent evt) {
    //System.out.println("StatusPanel.networkChanged: "+evt);
    switch( evt.id ){
      case NetworkEvent.NETWORK_DELETED :
      case NetworkEvent.NETWORK_NAME_CHANGED :
      case NetworkEvent.NEW_NETWORK_LOADED :
        lNet[1].setText( net.getName() );
        break;
      case NetworkEvent.PATTERN_SET_CHANGED :
        PatternSet set = sets.getCurrent();
        if( set != null ) lTrPat[1].setText( set.getName() );
        else lTrPat[1].setText("<none>");
        set = sets.getValidationSet();
        if( set != null ) lValPat[1].setText( set.getName() );
        else lValPat[1].setText("<none>");
        break;
    }
  }
}