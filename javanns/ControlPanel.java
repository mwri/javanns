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

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

class ControlPanel extends JPanel implements ActionListener {
  public Point p;
  static ImageIcon helpIcon = null;

  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/
  FlatButton bHelp;
  NamedComboBox cFunction;
  JLabel lParam[] = new JLabel[5];
  JTextField tParam[] = new JTextField[5];
  int grid, maxX, maxY, ID;
  int[] x_coord ;
  int y, lw, oldY;
  int origX=1, origY=1;
  Snns snns;
  MasterControl master;

  /**
   * Class constructor:
   * creates panel with the elements:
   *  - a NamedComboBox containing the functions of the given type
   *  - the given label on the left hand side of the ComboBox
   *  - a panel for the parameters of the functions
   */
  public ControlPanel(MasterControl master, String lbl, int type) {
    this.master = master;
    snns = master.snns;

    int i, j, x, oldMaxX;
    setLayout(null);
    grid = getFontMetrics(getFont()).getHeight() >> 1;
    lw = toGrid( master.functions.maximumLabelWidth );

    JLabel label = new JLabel(lbl);
    add(label);
    p = movePref(label, origX, origY);

    cFunction = new NamedComboBox();
    cFunction.addItems( master.functions.getFunctionsOfType( type ) );
    add(cFunction);
    p = movePref(cFunction, p.x, origY);
    cFunction.addActionListener( this );

    if(helpIcon == null) helpIcon = snns.icons.getIcon("help.gif","Help");

    bHelp = new FlatButton(helpIcon);
    //bHelp = new FlatButton(new ImageIcon( cl.getResource("images/question.gif"), "Help"));
    add(bHelp);
    bHelp.setToolTipText("Information about the function");
    bHelp.addActionListener(this);
    p = movePref(bHelp, p.x, origY);

    oldY = p.y;
    oldMaxX = maxX;
    JPanel pPanel = new JPanel();
    pPanel.setLayout(null);
    pPanel.setBorder(
      BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parameters")
      );
    add(pPanel);

    x = 2; y = 3; maxX = 0;
    x_coord = new int[5];
    for(i=0; i<5; i++) {
      if(i == 3) { x = 2; y = p.y + 1; }
      lParam[i] = new JLabel("-", SwingConstants.RIGHT);
      tParam[i] = new JTextField("", 6);
      pPanel.add(lParam[i]);
      pPanel.add(tParam[i]);
      lParam[i].setSize(lw*grid, lParam[i].getPreferredSize().height);
      p = move(lParam[i], x, y );
      x = p.x + 1;
      x_coord[i] = x;
      p = movePref(tParam[i], x, y);
      x = p.x;
    }
    pPanel.setSize(maxX+2*grid, (p.y+2)*grid);
    maxX = oldMaxX;
    y = oldY + 1;
    p = move(pPanel, 1, y);
    y = p.y + 1;
    actionPerformed( new ActionEvent( cFunction, 0, "" ) );
  }


  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  /**
   * Converts grid coordinate to pixel coordinate
   *
   * @param x coordinate in grid units
   * @return coordinate in pixels
   */
  public int G(int x) { return grid*x; }

  /**
   * Moves a component to specified grid coordinates
   *
   * @param c component to move
   * @param x x-coordinate, in grid units
   * @param y y-coordinate, in grid units
   * @return grid coordinate of the position right and below given component.
   */
  public Point move(JComponent c, int x, int y) {
    int X = grid*x;
    int Y = grid*y;
    c.setLocation(X, Y);
    Dimension d = c.getSize();
    if(X + d.width > maxX) maxX = X + d.width;
    if(Y + d.height > maxY) maxY = Y + d.height;
    return new Point( toGrid(X+d.width), toGrid(Y+d.height) );
  }

  /**
   * Moves a component to specified grid coordinates and resizes it to its
   *   preferred size.
   *
   * @param c component to move
   * @param x x-coordinate, in grid units
   * @param y y-coordinate, in grid units
   * @return grid coordinate of the position right and below given component
   */
  public Point movePref(JComponent c, int x, int y) {
    int X = grid*x;
    int Y = grid*y;
    c.setLocation(X, Y);
    c.setSize(c.getPreferredSize());
    Dimension d = c.getSize();
    if(X + d.width > maxX) maxX = X + d.width;
    if(Y + d.height > maxY) maxY = Y + d.height;
    return new Point( toGrid(X+d.width), toGrid(Y+d.height) );
  }

  /**
   * Moves a component to specified grid coordinates, measured
   *   from the right bottom corner of the container.
   *
   * @param c component to move
   * @param x x-coordinate, in grid units
   * @param y y-coordinate, in grid units
   * @return grid coordinate of the position left and above given component.
   */
  public Point moveRevX(JComponent c, int x, int y) {
    Dimension size = getPreferredSize();
    Dimension d = c.getSize();
    int X = size.width - 1 - d.width - grid*x;
    int Y = grid*y;
    c.setLocation(X, Y);
    return new Point( prevGrid(X, size.width), toGrid(Y) );
  }

  /**
   * Moves a component to specified grid coordinates, measured
   *   from the right bottom corner of the container, and resizes
   *   it to its preferred size.
   *
   * @param c component to move
   * @param x x-coordinate, in grid units
   * @param y y-coordinate, in grid units
   * @return grid coordinate of the position right and below given component
   */
  public Point movePrefRevX(JComponent c, int x, int y) {
    Dimension size = getPreferredSize();
    c.setSize(c.getPreferredSize());
    Dimension d = c.getSize();
    int X = size.width - 1 - d.width - grid*x;
    int Y = grid*y;
    c.setLocation(X, Y);
    return new Point( prevGrid(X, size.width), toGrid(Y) );
  }

  /**
   * Resizes a component to its preferred size
   *
   * @param c component to resize
   * @return new component size
   */
  public Dimension resize(JComponent c) {
    Dimension d= c.getPreferredSize();
    c.setSize(d);
    return d;
  }

  /**
   * Computes next grid-aligned coordinate of the given coordinate.
   *
   * @param i pixel coordinate to align
   * @return the nearest greater or same grid coordinate
   */
  public int toGrid(int i) {
    return (grid+i-1) / grid;
  }

  /**
   * Computes previous grid-aligned coordinate of the given coordinate.
   *
   * @param i pixel coordinate to align
   * @return the nearest smaller or same grid coordinate
   */
  public int prevGrid(int i, int size) {
    return (grid + (size-i)-1 ) / grid;
  }

  /**
   * Computes size of this panel that allows all it components to be shown
   *
   * @return preferred size of this panel
   */
  public Dimension getPreferredSize() {
    return new Dimension( maxX+grid, maxY+grid );
  }

  /**
   * method returns chosen function
   *
   * @return function
   */
  public Function getFunction(){
    int no = cFunction.getSelectedIndex();
    if( no == -1 ) no = 0;
//    return (Function)cFunction.getObjectAt(no);
    return (Function)cFunction.getSelectedObject();
  }


  /**
   * method returns chosen parameters
   *
   * @return double[] parameters
   */
  public double[] getParameters(){
    Function f = getFunction();
    int i;
    double[] p = new double[5];
    for(i=0; i<5; i++) p[i] = 0;
    if(f == null) return p;
    for(i=0; i<f.nP; i++){
      try{ p[i] = Double.valueOf(tParam[i].getText()).doubleValue(); }
      catch(Exception e2) { }
    }
    return p;
  }


  /**
   * Event handler for ControlPanel. Shows appropriate controls and fields
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    int i;

    if(src == cFunction) {
      if( cFunction.getSelectedIndex() == -1 ) return;
      Function f = (Function)cFunction.getSelectedObject();
      Parameter p;
      for(i=0; i<f.nP; i++) {
        p = f.parameter[i];
        lParam[i].setText( p.parStr );
        lParam[i].setVisible(true);
        lParam[i].setToolTipText( p.toolTip );
        tParam[i].setText( String.valueOf(p.getDefaultValue()) );
        tParam[i].setVisible(true);
        tParam[i].setToolTipText( p.toolTip );
      }
      for(; i<5; i++){
        lParam[i].setVisible(false);
        tParam[i].setText("0.0");
        tParam[i].setVisible(false);
      }
      bHelp.setVisible(f.helpExists());

      if( master.frame.isVisible() )
        snns.pLog.append( f.show_name + " selected" );
      repaint();
    }

    else if(src == bHelp)
      try{ ((Function)cFunction.getSelectedObject()).showHelp(); }
      catch( Exception ex ){ showException( ex ); }
  }

  protected void showException( Exception e ){ snns.showException( e, this ); }
}

