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
import java.awt.Dimension ;
import java.awt.Point ;
import java.io.File ;
import java.io.IOException ;

class ResultAccessory extends JPanel{
  JLabel lStartPattern, lEndPattern ;
  JTextField tStartPattern, tEndPattern;
  ButtonGroup bgMode;
  JRadioButton rbAppend, rbCreate;
  JCheckBox cbInclIn, cbInclOut;

  private int rand = 6, w, h, dh;


  public ResultAccessory(){
    //setBorder( BorderFactory.createLoweredBevelBorder() );
    setLayout( null );

    lStartPattern = new JLabel("Start pattern: ");
    tStartPattern = new JTextField("1", 4);

    lEndPattern = new JLabel("End pattern: ");
    tEndPattern = new JTextField( "0", 4 );

    cbInclIn = new JCheckBox("Include input patterns");

    cbInclOut = new JCheckBox("Include output patterns");

    bgMode = new ButtonGroup();
    rbCreate = new JRadioButton("create", true );
    bgMode.add( rbCreate );
    rbAppend = new JRadioButton("append", false );
    bgMode.add( rbAppend );

    Dimension d = rbCreate.getPreferredSize();
    w = d.width;
    d = rbAppend.getPreferredSize();
    w += d.width + 3 * rand;

    d = tStartPattern.getPreferredSize();
    dh = d.height + 2 * rand ;

    moveLeft( lStartPattern, 0 );
    moveRight( tStartPattern, 0 );
    moveLeft( lEndPattern, 1 );
    moveRight( tEndPattern, 1 );
    moveLeft( cbInclIn, 2 );
    moveLeft( cbInclOut, 3 );
    moveLeft( rbCreate, 4 );
    h = moveRight( rbAppend, 4 ) + rand;
    //setSize( getPreferredSize() );
    //setVisible( true );
  }
  public void setLastPatternCount( int lp ){
    tEndPattern.setText( String.valueOf( lp ) );
  }
  public Dimension getPreferredSize(){
    return new Dimension(w, h);
  }
  public void setEnabled( boolean really ){
    for(int i=0; i<getComponentCount(); i++)
      this.getComponent( i ).setEnabled( really );
  }
  public int startPattern(){
    return Integer.parseInt( tStartPattern.getText() );
  }
  public int endPattern(){
    return Integer.parseInt( tEndPattern.getText() );
  }
  public boolean inclInputPatterns(){
    return cbInclIn.isSelected();
  }
  public boolean inclOutputPatterns(){
    return cbInclOut.isSelected();
  }
  public String getFileMode(){
    return rbAppend.isSelected()? "append" : "create";
  }
  private void moveLeft( JComponent c, int y){
    add( c );
    c.setSize( c.getPreferredSize() );
    c.setLocation( rand, rand + y * dh);
  }
  private int moveRight( JComponent c, int y ){
    add( c );
    Dimension d = c.getPreferredSize();
    c.setSize( d );
    c.setLocation( w - rand - d.width, rand + y * dh);
    return rand + y * dh + d.height;
  }
}
