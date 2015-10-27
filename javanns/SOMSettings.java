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
class SOMSettings extends JPanel{
  private JSlider sMaxDist;

  public SOMSettings(DisplaySettingsPanel parent){
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

    add( Box.createVerticalGlue() );

    JLabel lMaxDist = new JLabel("Maximum expected distance:");
    add( lMaxDist );

    add( Box.createVerticalGlue() );

    Hashtable hLabels = new Hashtable(4);
    hLabels.put( new Integer(-100), new JLabel("0.1") );
    hLabels.put( new Integer(0), new JLabel("1") );
    hLabels.put( new Integer(100), new JLabel("10") );
    hLabels.put( new Integer(200), new JLabel("100") );

    sMaxDist = new JSlider( SwingConstants.HORIZONTAL, -100, 200, 40 );
    sMaxDist.setLabelTable(hLabels);
    sMaxDist.setMajorTickSpacing (100);
    sMaxDist.setMinorTickSpacing (25);
    sMaxDist.setPaintTicks(true);
    sMaxDist.setPaintLabels(true);
    sMaxDist.setValueIsAdjusting(true);
    sMaxDist.setPreferredSize(new Dimension(120, sMaxDist.getPreferredSize().height));
    sMaxDist.setToolTipText("Chroma code of distances between units is interpolated up to this value");

    add( sMaxDist );

    add( Box.createVerticalGlue() );
  }

  void setValue( double v ){
    sMaxDist.setValue((int)( 100 * Math.log(v) / UnitsAndLinksSettings.log10 ) );
  }

  double getValue(){
    return Math.exp( sMaxDist.getValue() * UnitsAndLinksSettings.log10 / 100. );
  }
}
