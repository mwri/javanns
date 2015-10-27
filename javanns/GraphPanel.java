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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.* ;
import wsi.ra.chart2d.* ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * GraphPanel displays learning error and control elements (buttons etc.)
 *
 */
class GraphPanel extends JPanel implements ActionListener,
                                           NetworkListener,
                                           Printable {
  private ImageIcon
    iIncreaseX, iIncreaseY, iDecreaseX, iDecreaseY,
    iSSE, iSSE_V, iSSE_inv, iMSE, iMSE_V, iMSE_inv,
    iSSE_out, iSSE_out_V, iSSE_out_inv,
    iNewGraph, iGraphGrid, iGraphNoGrid;
  private FlatButton bIncreaseX, bIncreaseY, bDecreaseX, bDecreaseY, bError, bNewGraph, bGraphGrid;
  private GDC pMain;
  private JPanel pLeft, pBottom;
  private JPopupMenu pmError;                 // popup menu for choosing error to display
  private JMenuItem miSSE, miSSE_out, miMSE;  // menu items in popup window
  private boolean graphIsEmpty = true;
  private int err_type = 0, // 0 -> SSE, 1 -> MSE, 2 -> SSE_out
              log_entries = 10, // number of log entries per training session
              min_log_dist = 5; // minimal number of steps between two log entries
  private Network network;
  private Snns snns;

  JInternalFrame frame;
  InternalFrameListener frameListener =
    new InternalFrameAdapter(){
      public void internalFrameClosed(InternalFrameEvent e){ removeFromLists(); }
    };

  /**
   * Class constructor: creates panel with its elements (buttons etc.)
   *
   * @param snns application main frame
   */
  public GraphPanel(Snns snns) {
    this.snns = snns;
    network = snns.network;
    network.addListener( this );
    network.addTrainingListener( this );

    Point p;
    Dimension d;

    setBorder(new EmptyBorder(2,0,0,2));
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);

    iIncreaseX = snns.icons.getIcon("increaseXAxis.gif", "Increase X axis");
    iIncreaseY = snns.icons.getIcon("increaseYAxis.gif", "Increase Y axis");
    iDecreaseX = snns.icons.getIcon("decreaseXAxis.gif", "Decrease X axis");
    iDecreaseY = snns.icons.getIcon("decreaseYAxis.gif", "Decrease Y axis");

    iSSE       = snns.icons.getIcon("SSE.gif", "SSE");
    iSSE_V     = snns.icons.getIcon("SSE_V.gif", "SSE");
    iMSE       = snns.icons.getIcon("MSE.gif", "MSE");
    iMSE_V     = snns.icons.getIcon("MSE_V.gif", "MSE");
    iSSE_out   = snns.icons.getIcon("SSE_out.gif", "SSE_out");
    iSSE_out_V = snns.icons.getIcon("SSE_out_V.gif", "SSE_out");
    if(snns.isMetalLookAndFeel()) {
      iSSE_inv = iSSE;
      iMSE_inv = iMSE;
      iSSE_out_inv = iSSE_out;
    }
    else {
      iSSE_inv     = snns.icons.getIcon("SSE_inv.gif", "SSE");
      iMSE_inv     = snns.icons.getIcon("MSE_inv.gif", "MSE");
      iSSE_out_inv = snns.icons.getIcon("SSE_out_inv.gif", "SSE_out");
    }
    iNewGraph = snns.icons.getIcon("newGraph.gif", "New graph");
    iGraphGrid = snns.icons.getIcon("graphGrid.gif", "Grid on");
    iGraphNoGrid = snns.icons.getIcon("graphNoGrid.gif", "Grid off");

    bIncreaseX = new FlatButton(iIncreaseX);
    bIncreaseX.setToolTipText("Extend x-axis range");
    bIncreaseX.setAlignmentY(Component.CENTER_ALIGNMENT);
    bIncreaseX.addActionListener(this);

    bIncreaseY = new FlatButton(iIncreaseY);
    bIncreaseY.setToolTipText("Extend y-axis range");
    bIncreaseY.setAlignmentX(Component.CENTER_ALIGNMENT);
    bIncreaseY.addActionListener(this);

    bDecreaseX = new FlatButton(iDecreaseX);
    bDecreaseX.setToolTipText("Reduce x-axis range");
    bDecreaseX.setAlignmentY(Component.CENTER_ALIGNMENT);
    bDecreaseX.addActionListener(this);

    bDecreaseY = new FlatButton(iDecreaseY);
    bDecreaseY.setToolTipText("Reduce y-axis range");
    bDecreaseY.setAlignmentX(Component.CENTER_ALIGNMENT);
    bDecreaseY.addActionListener(this);

    bError = new FlatButton(iSSE_V);
    bError.setToolTipText("Error to display: SSE");
    bError.setAlignmentX(Component.CENTER_ALIGNMENT);
    bError.addActionListener(this);

    bNewGraph = new FlatButton(iNewGraph);
    bNewGraph.setToolTipText("Clear graph");
    bNewGraph.addActionListener(this);

    bGraphGrid = new FlatButton(iGraphGrid);
    bGraphGrid.setToolTipText("Turn grid on");
    bGraphGrid.addActionListener(this);

    pLeft = new JPanel();
    pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS) );
    pLeft.add(bIncreaseY);
    pLeft.add(bDecreaseY);
    pLeft.add( Box.createVerticalGlue() );
    pLeft.add(bError);
    pLeft.add( Box.createVerticalGlue() );

    MouseListener popupMouseListener = new MouseListener() {
      public void mouseEntered(MouseEvent e) {
        JMenuItem mi = (JMenuItem)e.getComponent();
        if(mi == miSSE) mi.setIcon(iSSE_inv);
        if(mi == miMSE) mi.setIcon(iMSE_inv);
        if(mi == miSSE_out) mi.setIcon(iSSE_out_inv);
        mi.repaint();
      }
      public void mouseExited (MouseEvent e) {
        JMenuItem mi = (JMenuItem)e.getComponent();
        if(mi == miSSE) mi.setIcon(iSSE);
        if(mi == miMSE) mi.setIcon(iMSE);
        if(mi == miSSE_out) mi.setIcon(iSSE_out);
        mi.repaint();
      }
      public void mouseClicked(MouseEvent e) {}
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
    };
    pmError = new JPopupMenu("Error");
    miSSE = new JMenuItem(iSSE);
    miSSE.addActionListener(this);
    miSSE.addMouseListener(popupMouseListener);
    pmError.add(miSSE);
    miMSE = new JMenuItem(iMSE);
    miMSE.addActionListener(this);
    miMSE.addMouseListener(popupMouseListener);
    pmError.add(miMSE);
    miSSE_out = new JMenuItem(iSSE_out);
    miSSE_out.addActionListener(this);
    miSSE_out.addMouseListener(popupMouseListener);
    pmError.add(miSSE_out);

    pBottom = new JPanel();
    pBottom.setLayout( new BoxLayout(pBottom, BoxLayout.X_AXIS) );
    JLabel label = new JLabel("Learning cycles");
    pBottom.add(bNewGraph);
    pBottom.add(bGraphGrid);
    pBottom.add( Box.createHorizontalGlue() );
    pBottom.add(label);
    pBottom.add( Box.createHorizontalGlue() );
    pBottom.add(bDecreaseX);
    pBottom.add(bIncreaseX);

    pMain = new GDC(this);
    //pMain.setBackground(new Color(248, 248, 248));
    pMain.setGridVisible( false );

    gbc.fill = gbc.BOTH;
    gbl.setConstraints(pLeft, gbc);
    add(pLeft);
    gbc.gridx = 1;
    gbc.weightx = gbc.weighty = 1;
    gbl.setConstraints(pMain, gbc);
    add(pMain);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.weightx = gbc.weighty = 0;
    gbl.setConstraints(pBottom, gbc);
    add(pBottom);

    frame = new JInternalFrame("Error graph", true, true, true, true);
    frame.addInternalFrameListener( frameListener );
    frame.setContentPane( this );
    frame.setSize(320, 240);
  }


  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/
  /**
   * Extends the error canvas if needed.
   *
   * @param x current x coordinate
   */
  public void extendIfNeeded(int x) {
    while(x > pMain.getXRange()) {
      bIncreaseX.setEnabled(pMain.increaseXRange());
      bDecreaseX.setEnabled(true);
    }
  }

  /**
   * Defines preferred size of the panel.
   *
   * @return preferred size of the panel
   */
  public Dimension getPreferredSize() { return new Dimension(500, 500); }

  /**
   * Defines minimum size of the panel.
   *
   * @return minimum size of the panel
   */
  public Dimension getMinimumSize() { return new Dimension(200, 150); }

/*----------------------------- interfaces -----------------------------------*/
// implementing ActionListener:
  /**
   * Event handler for GraphPanel. Shows appropriate controls and fields,
   *   initiazes actions like learning and opens help pages in an
   *   attached browser.
   *
   * @param e ActionEvent object
   */
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    int i, j;

    if(src == bIncreaseX) {
      bIncreaseX.setEnabled(pMain.increaseXRange());
      bDecreaseX.setEnabled(true);
    }
    if(src == bDecreaseX) {
      bIncreaseX.setEnabled(true);
      bDecreaseX.setEnabled(pMain.decreaseXRange());
    }
    if(src == bIncreaseY) {
      bIncreaseY.setEnabled(pMain.increaseYRange());
      bDecreaseY.setEnabled(true);
    }
    if(src == bDecreaseY) {
      bIncreaseY.setEnabled(true);
      bDecreaseY.setEnabled(pMain.decreaseYRange());
    }

    if(src == bError) {
      pmError.show(
        pLeft,
        bError.getLocation().x - 12,
        bError.getLocation().y + bError.mousePressedPos.y - 12
      );
    }

    if(src instanceof JMenuItem) {
      if(((JMenuItem)src) == miSSE) {
        bError.setIcon(iSSE_V);
        bError.setToolTipText("Error to display: SSE");
        pMain.setYDivisor(1);
        err_type = 0;
        repaint();
      }
      if(((JMenuItem)src) == miMSE) {
        bError.setIcon(iMSE_V);
        bError.setToolTipText("Error to display: MSE");
        pMain.setYDivisor(network.getNoOfSubpats());
        err_type = 1;
        repaint();
      }
      if(((JMenuItem)src) == miSSE_out) {
        bError.setIcon(iSSE_out_V);
        bError.setToolTipText("Error to display: SSE/no. of output units");
        pMain.setYDivisor(network.getNumberOfOutputUnits());
        err_type = 2;
        repaint();
      }
      miSSE.setIcon(iSSE);
      miMSE.setIcon(iMSE);
      miSSE_out.setIcon(iSSE_out);
    }

    if(src == bNewGraph) {
      pMain.clearGraph();
      graphIsEmpty = true;
    }

    if(src == bGraphGrid) {
      boolean grid = !pMain.isGridVisible();
      pMain.setGridVisible( grid );
      if( grid ) {
        bGraphGrid.setIcon(iGraphNoGrid);
        bGraphGrid.setToolTipText("Turn grid off");
      }
      else {
        bGraphGrid.setIcon(iGraphGrid);
        bGraphGrid.setToolTipText("Turn grid on");
      }
    }
  }

// implementing NetworkListener:
  /**
   * method receives new learning errors of the network
   */
  public void networkChanged( NetworkEvent evt ){
    if( evt.id == NetworkEvent.NETWORK_TRAINED ){
      if( evt.arg == null ) return;
      TrainingResult res = (TrainingResult)evt.arg;
      double[] sse = res.sse;
      boolean val = res.val_sse != null;
      int steps = sse.length,
          tot_num = pMain.addValues( sse ),
          start = tot_num - steps,
          log_dist = res.steps_total / log_entries;

      if( log_dist < min_log_dist ) log_dist = min_log_dist;

      extendIfNeeded(tot_num);
      if( val ) pMain.addValValues( res.val_sse, start+1 );
      int x = start / log_dist + 1;
      start = x * log_dist;
      graphIsEmpty = false;

      String err_txt = "SSE";
      double n = 1.0;
      switch( err_type ){
        case 1 : err_txt = "MSE";
                 n = network.getNoOfSubpats();
                 break;
        case 2 : err_txt = "SSE / | output units |";
                 n = network.getNumberOfOutputUnits();
                 break;
      }
      for( int i=start; i<=tot_num; i += log_dist ){
        String text = "Step "+i+" "+err_txt+":\t"+sse[i-tot_num+steps-1]/n;
        if( val ) text += "\tvalidation:\t"+res.val_sse[i-tot_num+steps-1]/n;
        snns.pLog.append( text );
      }

      if( res.final_result && tot_num%log_dist != 0 ){
        String text = "Step "+tot_num+" "+err_txt+":\t"+sse[steps-1]/n;
        if( val ) text += "\tvalidation:\t"+res.val_sse[steps-1]/n;
        snns.pLog.append( text );
      }
    }

    else if( evt.id == NetworkEvent.NETWORK_INITIALIZED ||
             evt.id == NetworkEvent.NEW_NETWORK_LOADED ) {
      if( !graphIsEmpty ) pMain.nextCurve();
    }
  }
// implementing Printable:
  public int print( Graphics g, PageFormat pf, int pi ){
    return pMain.print( g, pf, pi );
  }

  private void removeFromLists(){
    network.removeListener( this );
  }
}
