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


import javax.swing.* ;
import java.awt.* ;

/**
 * panel for entering width, height and position of a new layer of units
 */
class LayerBoundsPanel extends JPanel{
  JTextField tWidth, tHeight;
  PositionPanel pPos;

  public LayerBoundsPanel( Insets insets ){
    setBorder( BorderFactory.createTitledBorder("Size & Position") );

    JLabel lWidth = new JLabel("Width: ");
    tWidth = new JTextField("1", 4);

    JLabel lHeight = new JLabel("Height: ");
    tHeight = new JTextField("1", 4);

    JLabel lPos = new JLabel("Top left position: ");
    pPos = new PositionPanel();

    GridBagLayout gbl = new GridBagLayout();
    setLayout( gbl );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = insets;
    gbc.weightx = gbc.weighty = 0.5;
    gbc.anchor = gbc.WEST;

    gbl.setConstraints( lWidth, gbc );
    add( lWidth );

    gbc.gridx = 1;
    gbl.setConstraints( tWidth, gbc );
    add( tWidth );

    gbc.gridx = 2;
    gbl.setConstraints( lHeight, gbc );
    add( lHeight );

    gbc.gridx = 3;
    gbl.setConstraints( tHeight, gbc );
    add( tHeight );

    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.gridx = 0;
    gbl.setConstraints( lPos, gbc );
    add( lPos );

    gbc.gridx = 2;
    gbl.setConstraints( pPos, gbc );
    add( pPos );
  }

  public int getWidth2() throws Exception{
    int w;
    try{ w = Integer.parseInt( tWidth.getText() ); }
    catch(Exception e){ throw new Exception( "Width must be an integer value" ); }
    return w;
  }
  public int getHeight2() throws Exception{
    int h;
    try{ h = Integer.parseInt( tHeight.getText() ); }
    catch(Exception e){ throw new Exception( "Height must be an integer value" ); }
    return h;
  }
  public void setXPos( int x ){ pPos.setXPos( x ); }

  public int getXPos() throws Exception{ return pPos.getXPos(); }

  public int getYPos() throws Exception{ return pPos.getYPos(); }

  public int getZPos() throws Exception{ return pPos.getZPos(); }

  private class PositionPanel extends JPanel{
    JTextField tXPos, tYPos, tZPos;

    public PositionPanel(){
      tXPos = new JTextField("1", 4);
      tXPos.setToolTipText("x value");
      tYPos = new JTextField("1", 4);
      tYPos.setToolTipText("y value");
      tZPos = new JTextField("1", 4);
      tZPos.setToolTipText("z value");

      setLayout( new GridLayout(1, 3, 6, 4) );

      add( tXPos );
      add( tYPos );
      add( tZPos );
    }

    public void setXPos( int x ){
      tXPos.setText( String.valueOf( x ) );
    }
    public int getXPos() throws Exception{
      int x;
      try{ x = Integer.parseInt( tXPos.getText() ); }
      catch(Exception e){ throw new Exception( "X - Coordinate must be an integer value" ); }
      return x;
    }
    public int getYPos() throws Exception{
      int y;
      try{ y = Integer.parseInt( tYPos.getText() ); }
      catch(Exception e){ throw new Exception( "Y - Coordinate must be an integer value" ); }
      return y;
    }
    public int getZPos() throws Exception{
      int z;
      try{ z = Integer.parseInt( tZPos.getText() ); }
      catch(Exception e){ throw new Exception( "Z - Coordinate must be an integer value" ); }
      return z;
    }
  }
}