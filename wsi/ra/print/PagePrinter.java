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
 *  Filename: $RCSfile: PagePrinter.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.2.3 $
 *            $Date: 2005/02/03 17:53:02 $
 *            $Author: hoensela $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.print;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.* ;
import java.awt.print.* ;
import java.awt.image.BufferedImage ;
import javax.swing.* ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class PagePrinter
{
  Component c;
  Graphics g;
  PageFormat pf;
  public boolean fit_in_possible = true;

  public PagePrinter( Component c, Graphics g, PageFormat pf ) {
    this.c = c;
    this.g = g;
    this.pf = pf;
  }

  public int print(){
    Dimension old = c.getSize();

    int x = (int)pf.getImageableX(),
        y = (int)pf.getImageableY();

    g.translate( x, y );

    double w = (int)pf.getImageableWidth(),
           h = (int)pf.getImageableHeight();

    if( old.width > w || old.height > h ){
      boolean rec_turn = false, rec_fit_in = false;
      if( ( old.width > old.height && h > w ) ||
          ( old.width < old.height && h < w ) ) {
        rec_turn = true;
        if( old.width > h || old.height > w ) rec_fit_in = true;
      }
      else rec_fit_in = true;

      JLabel[] text = new JLabel[4];
      text[0] = new JLabel("The component which should be printed");
      text[1] = new JLabel("is too large.");
      text[2] = new JLabel("You can choose if the component should be");
      JCheckBox cbFitIn = new JCheckBox("fitted-in", rec_fit_in),
                cbTurn = new JCheckBox("turned", rec_turn );
      text[3] = new JLabel("(Recommended choice is pre-selected)");

      if( !fit_in_possible ){
        cbFitIn.setEnabled( false );
        cbFitIn.setSelected( false );
      }

      GridBagLayout gbl = new GridBagLayout();
      JPanel panel = new JPanel( gbl );
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = gbc.gridy = 0;
      gbc.weightx = gbc.weighty = .5;

      gbc.gridwidth = 2;
      gbl.setConstraints( text[0], gbc );
      panel.add( text[0] );

      gbc.gridy++;
      gbl.setConstraints( text[1], gbc );
      panel.add( text[1] );

      gbc.gridy++;
      gbl.setConstraints( text[2], gbc );
      panel.add( text[2] );

      gbc.gridy++;
      gbc.gridwidth = 1;
      gbl.setConstraints( cbFitIn, gbc );
      panel.add( cbFitIn );

      gbc.gridx++;
      gbl.setConstraints( cbTurn, gbc );
      panel.add( cbTurn );
      gbc.gridx = 0;
      gbc.gridwidth = 2;
      gbc.gridy++;
      gbl.setConstraints( text[3], gbc);
      panel.add( text[3] );

      int choice = JOptionPane.showOptionDialog( c, panel, "Fit-in",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null, null, null );

      if( choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION )
        return Printable.NO_SUCH_PAGE;

      else if( choice == JOptionPane.OK_OPTION ){

        if( cbTurn.isSelected() ){
          BufferedImage img;
          if( cbFitIn.isSelected() ){
            double m = Math.min( h / (double)old.width, w / (double)old.height );
            img = (BufferedImage)c.createImage( (int)( old.height * m ), (int)( old.width * m ) );
            Graphics2D g2 = img.createGraphics();
            g2.rotate( Math.toRadians( 90 ) );
            g2.translate( 0, - old.height * m );
            c.setSize( (int)( old.width * m ), (int)( old.height * m ) );
            c.paint( g2 );
            c.setSize( old );
          }
          else{
            img = (BufferedImage)c.createImage( old.height, old.width );
            Graphics2D g2 = img.createGraphics();
            g2.rotate( Math.toRadians( 90 ) );
            g2.translate( 0, - old.height );
            c.paint( g2 );
          }
          g.drawImage( img, 0, 0, c.getBackground(), c );
        }

        else{
          if( cbFitIn.isSelected() ){
            double m = Math.min( w / (double)old.width, h / (double)old.height );
            c.setSize( (int)( old.width * m ), (int)( old.height * m ) );
            c.paint( g );
            c.setSize( old );
          }
          else c.paint( g );
        }
      }
    }
    else  c.paint( g );
    return Printable.PAGE_EXISTS;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
