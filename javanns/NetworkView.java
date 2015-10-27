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
import javax.swing.event.* ;
import java.awt.* ;
import java.awt.print.* ;
import java.awt.event.* ;
import java.awt.image.BufferedImage ;
import java.util.Vector ;
import wsi.ra.print.PagePrinter ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * NetworkView is window for displaying networks in 2D.
 *
 */
class NetworkView extends JPanel implements Scrollable, NetworkListener, Printable {
  private boolean under_construction = false;
  /*-------------------------------------------------------------------------*
   * public member variables
   *-------------------------------------------------------------------------*/
  public final static int LEFT=-1, TOP=-1, CENTER=0, RIGHT=1, BOTTOM=1;
  public final static Color DEFAULT_POS = Color.green;
  public final static Color DEFAULT_NEG = Color.red;
  public final static Color DEFAULT_NULL = new Color(0, 0, 0x99);

  Snns snns;
  NetworkViewSettings settings;
  int net_view_no; // the number of the network view

  // descriptors for all units
  int font_size, font_height, font_ascent;
  FontMetrics font_metrics;

  // grid position of the mouse
  private int grid_x, grid_y;

  // for drag and drop
  boolean drawingRect;
  Point startPoint, endPoint, delta = new Point();
  Rectangle rectangle;



  MouseListener mouseListener =
    new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        Point p = e.getPoint();
        Unit u = network.getUnitAtXY( new int[]{ grid_x, grid_y } );

        if( u != null && isPointOnUnit(p,u) ){ // auf eine Unit geklickt
          int mod = e.getModifiers();
          if( (mod & e.BUTTON1_MASK) == 0 ){ // nicht mit linker Maustaste
            network.selectUnit(u);
            Point p1 = scrollPane.getViewport().getViewPosition();
            snns.popup.show( frame, p.x - p1.x + 100, p.y - p1.y );
          }
          else{ // mit linker Maustaste
            if( network.unitsSelected() ){
              if( (mod & e.CTRL_MASK) == 0 ){
                network.deselectUnits();
                network.selectUnit(u);
              }
              else{
                if( network.isUnitSelected(u) ){
                  network.deselectUnit(u);
                }
                else network.selectUnit(u);
              }
            }
            else network.selectUnit(u);
            requestFocus();
          }
        }

        else{ // neben die Units geklickt
          network.deselectUnits();
          if( isDirectEdit() ) evaluateDirectEdit(true);
        }
      }

      public void mousePressed(MouseEvent e){
        Point p = e.getPoint();
        Unit u = network.getUnitAtXY(new int[]{grid_x,grid_y});
        startPoint = p;
        if( u == null || !network.isUnitSelected(u) ){ // es werden neue Units gewaehlt
          endPoint = p;
          drawingRect = true;
          buildRectangle();
        }
        else if( isPointOnUnit(p) )
          endPoint = (Point)p.clone(); // es werden Units verschoben
      }

      public void mouseReleased(MouseEvent e){
        if( drawingRect ){ // es wurden neue Units gewaehlt
          Vector us = getUnitsInRectangle();
          if( us.size() > 0 ){
            if( network.unitsSelected() && (e.getModifiers() & e.CTRL_MASK) == 0 )
              network.deselectUnits();
            network.selectUnits(us);
            requestFocus();
          }
          drawingRect = false;
          repaint(rectangle);
        }
        else {
          updatePositions(); // es wurden Units verschoben
          endPoint = null;
        }
      }
    };

  MouseMotionListener mouseMotionListener =
    new MouseMotionAdapter(){
        public void mouseDragged(MouseEvent e){
          Point p = e.getPoint();
          if( !drawingRect ) {
            if( endPoint != null ) checkStepSize(p); // ggf. repaint durchfuehren
          }
          else{ // Selektionsrechteck anpassen
            drawingRect = false;
            repaint( rectangle );
            drawingRect = true;
            endPoint = p;
            buildRectangle();
          }
        }
        public void mouseMoved(MouseEvent e){// ToolTips anpassen
          mouseMovedTo(e.getPoint());
        }
    };

  JInternalFrame frame;

  // listeners :
  Vector listeners = new Vector();

  InternalFrameListener frameListener =
    new InternalFrameAdapter(){
      public void internalFrameClosed(InternalFrameEvent e) { close(); }
      public void internalFrameActivated(InternalFrameEvent evt){
        requestFocus();
        fireEvent( NetworkViewEvent.VIEW_ACTIVATED );
      }
      public void internalFrameDeactivated( InternalFrameEvent evt ){
        fireEvent( NetworkViewEvent.VIEW_DEACTIVATED );
      }
    };

  KeyListener keyListener =
    new KeyAdapter(){
      public void keyReleased(KeyEvent e){
        int code = e.getKeyCode();
        if( code == e.VK_DELETE ) network.deleteUnits() ;
        else if( code == e.VK_A && (e.getModifiers() & e.CTRL_MASK) != 0 )
            network.selectAll();
      }
    };

  // for direct editing :
  private JTextField[] labels;
  private boolean edit_top_labels = false,
                  edit_bottom_labels = false;
  private int labels_to_edit;

  // the network the view should show
  Network network;

  JScrollPane scrollPane;
  Point view_position = null; // used to move the viewport to currently created units
  /*-------------------------------------------------------------------------*
   * constructor
   *-------------------------------------------------------------------------*/

  /**
   * Class constructor. Applies given settings for network display.
   *
   * @param the snns kernel
   * @param display type ( size, colors, ... )
   */
  public NetworkView( Snns snns, UIDisplayType dt) {
    this.snns = snns;
    setLayout( null );
    network = snns.network;
    network.addListener( this );

    addMouseListener( mouseListener );
    addMouseMotionListener( mouseMotionListener );
    setRequestFocusEnabled(true);
    addKeyListener( keyListener );

    if( dt != null ) setPreferredSize( dt.dimension );

    if(dt != null && dt.settings != null) settings = dt.settings;
    else settings = NetworkViewSettings.getDefaultSettings();

    font_metrics = getFontMetrics(settings.font);
    font_height = font_metrics.getHeight();
    font_ascent = font_metrics.getAscent();

    scrollPane = new JScrollPane( this,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    JViewport vp = scrollPane.getViewport();
    vp.setBackground( settings.background_color );

    JPanel pMain = new JPanel();
    JPanel pPalette = new PalettePanel( this );
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    pMain.setLayout( gbl );
    gbl.setConstraints( pPalette, gbc );
    pMain.add( pPalette );
    gbc.weightx = gbc.weighty = .5;
    gbc.gridx = 1;
    gbc.fill = gbc.BOTH;
    gbl.setConstraints( scrollPane, gbc );
    pMain.add( scrollPane );

    net_view_no = dt.displayNo;
    frame = new JInternalFrame( network.getName() + " <" + dt.displayNo + ">",
      true, //resizable
      true, //closable
      true, //maximizable
      true);//iconifiable
    frame.addInternalFrameListener( frameListener );
    frame.setContentPane( pMain );
    frame.setSize( dt.dimension );
    frame.setLocation( dt.position );
//    frame.setBackground( settings.background_color );
    update();
  }


  /**
   * Class constructor. Initializes default settings for network display.
   *
   * @param snns The SNNS kernel
   */
  public NetworkView(Snns snns) {
    this(snns, null);
  }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  public void settingsChanged(){
    //System.out.println("NetworkView.settingsChanged()");
    if( isDirectEdit() ) fillLabels();
    scrollPane.getViewport().setBackground(settings.background_color);
    fireEvent( NetworkViewEvent.SETTINGS_CHANGED );
  }

/*---------------------- drawing methods -------------------------------------*/
  /**
   * Displays the current network, according to the settings.
   *
   * @param g graphic context of the window
   */
  public void paint( Graphics g ){
    if( under_construction ) System.out.println("NetworkView.paint(): "+net_view_no);
    super.paint( g );
    Color old_color = g.getColor();
    Font oldFont = g.getFont();
    g.setFont(settings.font);
    Dimension min_size = getMinimumSize();// er sollte auch schrumpfen koennen, falls Units geloescht oder verschoben werden

    // first draw all links...
    if(settings.show_links)
      for( Unit unit = network.getFirstUnit(); unit != null; unit = network.getNextUnit() ){
        Vector links = unit.getAllIncomingLinks();
        for( int i=0; i<links.size(); i++ ) drawLink( g, (Link)links.elementAt( i ) );
      }

    // ...and then the units
    Unit winner = null;
    if( settings.top_label_type == NetworkViewSettings.WINNER ||
        settings.base_label_type == NetworkViewSettings.WINNER )
      winner = network.getWinnerUnit();

    for(Unit unit = network.getFirstUnit(); unit != null; unit = network.getNextUnit() ) {
      Point p = drawNeuron(g, unit, winner, null );
      if( p.x + settings.grid_size> min_size.width )
        min_size.width = p.x + settings.grid_size;
      if( p.y + settings.grid_size> min_size.height )
        min_size.height = p.y + settings.grid_size;
    }

    // when drawing the rectangle
    if( drawingRect ){
      g.setColor( settings.selection_color );
      g.drawRect( rectangle.x, rectangle.y, rectangle.width - 1, rectangle.height - 1);
    }

    // while dragging
    if( network.unitsSelected() && (delta.x != 0 || delta.y != 0) ){
      g.setColor( settings.null_color );
      int[] pos;
      for( Unit unit = network.getFirstUnit(); unit != null; unit = network.getNextUnit() )
        if( network.isUnitSelected( unit ) ){
          pos = unit.getPosition();
          g.drawRect( X(pos[0]) - (settings.width >> 1) + delta.x,
                      Y(pos[1]) - (settings.width >> 1) + delta.y,
                      settings.width, settings.height );
        }
    }

    g.setColor(old_color);
    g.setFont(oldFont);

    Dimension old = getPreferredSize();

    if( !old.equals( min_size ) ) {
      setPreferredSize( min_size );
      setSize( min_size );
      getParent().validate();
    }

    if( view_position != null ){
      scrollPane.getViewport().setViewPosition( view_position );
      view_position = null;
    }
  }

  /**
   * Updates the display settings and repaints the window.
   */
  public void update() {
    setForeground(settings.text_color);
    setBackground(settings.background_color);
    frame.setBackground(settings.background_color);
    repaint();
  }

  /**
   * method returns true if the view is in direct edit mode
   */
  public boolean isDirectEdit(){
    return ( edit_bottom_labels || edit_top_labels );
  }

  /**
   * method returns the NetworkViewSettings id number of the labels which
   * are currently edited
   */
  public int getLabelsToEdit(){
    if( !isDirectEdit() ) return -1;
    return labels_to_edit;
  }

/*-------------------- private drawing methods--------------------------------*/
  /**
   * Draws a neuron in the window's graphic context. Neuron properties are
   *   specified by member variables, which are set in
   *   {@link #paint(Graphics)}.
   *
   * @param g graphic context
   * @param unit
   * @param the winner unit
   * @param the color of the unit
   *        ( if set to <code>null<\code> the color belonging to the activation value
   *          will be taken )
   * @return the point right below the unit
   */
  protected Point drawNeuron(Graphics g, Unit unit, Unit winner, Color color ) {
    if( under_construction ) System.out.println("NetworkView.drawNeuron()");
    String top_label = "", bottom_label = "";
    double act = unit.getActivation();
    if( !edit_top_labels ){
      switch(settings.top_label_type) {
        case NetworkViewSettings.ACT:       top_label = Snns.maxFixPoint(act, 3); break;
        case NetworkViewSettings.INIT_ACT:  top_label = Snns.maxFixPoint(unit.getInitAct(), 3); break;
        case NetworkViewSettings.OUTPUT:    top_label = Snns.maxFixPoint(unit.getOutput(), 3); break;
        case NetworkViewSettings.BIAS:      top_label = Snns.maxFixPoint(unit.getBias(), 3); break;
        case NetworkViewSettings.NAME:      top_label = unit.getName(); break;
        case NetworkViewSettings.NUMBER:    top_label = String.valueOf(unit.getNumber()); break;
        case NetworkViewSettings.Z_VAL:     top_label = String.valueOf( unit.getPosition()[2] ); break;
        case NetworkViewSettings.WINNER:
          if( unit.equals( winner ) )  top_label = "Winner";break;
        default:      top_label = "";
      }
    }
    if( !edit_bottom_labels ) {
      switch(settings.base_label_type) {
        case NetworkViewSettings.ACT:       bottom_label = Snns.maxFixPoint(act, 3); break;
        case NetworkViewSettings.INIT_ACT:  bottom_label = Snns.maxFixPoint(unit.getInitAct(), 3); break;
        case NetworkViewSettings.OUTPUT:    bottom_label = Snns.maxFixPoint(unit.getOutput(), 3); break;
        case NetworkViewSettings.BIAS:      bottom_label = Snns.maxFixPoint(unit.getBias(), 3); break;
        case NetworkViewSettings.NAME:      bottom_label = unit.getName(); break;
        case NetworkViewSettings.NUMBER:    bottom_label = String.valueOf(unit.getNumber()); break;
        case NetworkViewSettings.Z_VAL:     bottom_label = String.valueOf( unit.getPosition()[2] ); break;
        case NetworkViewSettings.WINNER:
          if( unit.equals( winner ) )  bottom_label = "Winner";break;
        default:      bottom_label = "";
      }
    }

    int[] pos = unit.getPosition();
    Point p = new Point( X(pos[0]), Y(pos[1]) );

    if( network.isUnitSelected( unit ) )
      g.setColor( settings.selection_color );
    else if( color == null )
      g.setColor(settings.getColor(act / settings.unit_max));
    else g.setColor( color );

    g.fillRect(p.x-(settings.width >> 1), p.y-(settings.height >> 1), settings.width, settings.height);
    g.drawRect(p.x-(settings.width >> 1), p.y-(settings.height >> 1), settings.width, settings.height);
    if( edit_top_labels ) {
      Dimension d = labels[ unit.getNumber() - 1 ].getSize();
      labels[ unit.getNumber() - 1 ].setLocation( p.x - d.width / 2, p.y - (settings.height>>1) - d.height );
    }
    else drawLabel(g, top_label, p.x, p.y-(settings.height >> 1)-1, CENTER, BOTTOM);
    if( edit_bottom_labels ){
      Dimension d = labels[ unit.getNumber() - 1 ].getSize();
      labels[ unit.getNumber() - 1 ].setLocation( p.x - d.width / 2, p.y + (settings.height>>1) );
    }
    else drawLabel(g, bottom_label, p.x, p.y+(settings.height >> 1)+1, CENTER, TOP);
    if( network.isSelectedSourceUnit( unit ) ) {
      g.setColor( settings.getColor( 0 ) );
      g.drawRect(p.x-(settings.width >> 1)-1, p.y-(settings.height >> 1)-1, settings.width+2, settings.height+2);
      g.setColor( settings.selection_color );
      g.drawRect(p.x-(settings.width >> 1), p.y-(settings.height >> 1), settings.width, settings.height);
    }
    else {
      g.setColor(getBackground());
      g.drawRect(p.x-(settings.width >> 1)-1, p.y-(settings.height >> 1)-1, settings.width+2, settings.height+2);
    }

    Point p2 = new Point();
    p2.x = p.x + Math.max( font_metrics.stringWidth( top_label ), font_metrics.stringWidth( bottom_label ) ) / 2;
    p2.y = p.y + (settings.height >> 1) + 1 + font_metrics.getHeight();
    return p2;
  }

  /**
   * Draws a link between two neurons in the window's graphic context.
   *   The destination neuron is specified by member variables, which
   *   are set in <code>paint</code>.
   *
   * @param g       graphic context
   * @param link
   */
  private void drawLink( Graphics g, Link link ){
    double weight = link.getWeight();
    // ignore very weak weights
    if(weight > -settings.link_trigger && weight < settings.link_trigger) return;


    double scaled_w;
    scaled_w = weight / settings.link_max;
    g.setColor( settings.getColor( scaled_w ) );

    int[] p = link.getSourceUnit().getPosition();
    Point from = new Point( X(p[0]), Y(p[1]) );
    p = link.getTargetUnit().getPosition();
    Point to = new Point( X(p[0]), Y(p[1]));

    if( link.isSelfConnection() ){
      g.drawOval(from.x, from.y - settings.height, settings.width, settings.height);
      if( settings.show_directions ){
        g.drawLine( from.x - settings.width / 4, from.y - 3 * settings.height / 4, from.x, from.y - settings.height / 2 );
        g.drawLine( from.x + settings.width / 4, from.y - 3 * settings.height / 4, from.x, from.y - settings.height / 2 );
      }
      if(settings.show_weights)
        drawLabel(g, Snns.maxFixPoint(weight, 3),
                  from.x + (int)(.5 * ( 1 + .5 * Math.sqrt( 2 ) ) * settings.width),
                  from.y - (int)(.5 * ( 1 + .5 * Math.sqrt( 2 ) ) * settings.height),
                  LEFT, BOTTOM);
      return;
    }

    if(settings.show_directions) {
      int dx = to.x-from.x,
        dy = to.y-from.y;
      if(Math.abs(dx) <= Math.abs((double)dy*settings.width/settings.height)) {
        // upper or lower edge of the target neuron
        int sgn = dy < 0 ? -1 : 1;
        to.x -= dx*settings.height / Math.abs(dy)/2;
        to.y -= sgn * settings.height/2;
      }
      else {
        // left or right edge of the target neuron
        int sgn = dx < 0 ? -1 : 1;
        to.x -= sgn * settings.width/2;
        to.y -= dy*settings.width / Math.abs(dx)/2;
      }
      dx = to.x-from.x;
      dy = to.y-from.y;
      double R = Math.sqrt(settings.width*settings.width +settings.height*settings.height)/3;
      double d = Math.sqrt(dx*dx + dy*dy);
      double tx = to.x - R*dx/d,
             ty = to.y - R*dy/d;
      int x1 = (int)(tx - R*dy/d/3),
          y1 = (int)(ty + R*dx/d/3),
          x2 = (int)(tx + R*dy/d/3),
          y2 = (int)(ty - R*dx/d/3);
      g.drawLine(to.x, to.y, x1, y1);
      g.drawLine(to.x, to.y, x2, y2);
//      g.drawLine(x1, y1, x2, y2);
//      g.drawLine((int)tx, (int)ty, from.x, from.y);
      g.drawLine(to.x, to.y, from.x, from.y);
    }
    else g.drawLine(from.x, from.y, to.x, to.y);
    if(settings.show_weights)
      drawLabel(g, Snns.maxFixPoint(weight, 3), (to.x+from.x)>>1, (to.y+from.y)>>1, CENTER, CENTER);
  }

  /**
   * Displays a label (neuron or link) at specified coordinates, with
   *   specified alignment.
   *
   * @param g        graphic context
   * @param str      label to display
   * @param x        x coordinate in window
   * @param y        y coordinate in window
   * @param align_h  horizontal alignment of the label
   * @param align_v  vertical alignment of the label
   */
  private void drawLabel(Graphics g, String str, int x, int y, int align_h, int align_v) {
    int w = font_metrics.stringWidth(str) + 2;
    int h = font_ascent + 2;

    switch(align_h) {
      case CENTER: x -= (w>>1); break;
      case RIGHT: x -= w; break;
    }

    switch(align_v) {
      case CENTER: y -= (h>>1); break;
      case BOTTOM: y -= h; break;
    }

    int x2 = x + 1;
    int y2 = y + font_ascent - 1;

    if(str != null && !str.equals("")) {
      g.setColor(getBackground());
      g.fillRect(x, y, w, h);
      g.drawRect(x, y, w, h);
      g.setColor(getForeground());
      g.drawString(str, x2, y2);
    }
  }

/*------------------- listeners & events -------------------------------------*/

  /**
   * method adds a new NetworkViewListener
   *
   * @param listener
   */
  public void addListener( NetworkViewListener l ){
    //System.out.println("NetworkView.addListener:"+l);
    if( !listeners.contains( l ) ) listeners.add( l );
  }

  /**
   * methods gives information to all listeners
   *
   * @param event type
   */
  void fireEvent(int type){
    NetworkViewEvent evt = new NetworkViewEvent(this, type);
    //System.out.println("NetworkView.fireEvent:"+evt.getMessage());
    for( int i=listeners.size()-1; i>-1; i-- )
      ( (NetworkViewListener)listeners.get( i ) ).networkViewChanged( evt );
  }

  /**
   * method removes a certain listener from the list
   */
  public void removeListener(NetworkViewListener l){
    //System.out.println("NetworkView.removeListener:"+l);
    listeners.remove( l );
  }

  /**
   * shows text fields instead of labels at the top of each unit
   * to edit names and values directly
   */
  public void editTopLabels(){
    edit_top_labels = true;
    labels_to_edit = settings.top_label_type;
    prepareLabels();
    repaint();
  }

  /**
   * shows text fields instead of labels at the bottom of each unit
   * to edit names and values directly
   */
  public void editBottomLabels(){
    edit_bottom_labels = true;
    labels_to_edit = settings.base_label_type;
    prepareLabels();
    repaint();
  }

  /**
   * tries to send the edited labels back to the network
   * shows an error message if something was wrong
   */
  public void evaluateDirectEdit( boolean delete_edit_labels ){
    if( under_construction ) System.out.println("NetworkView.evaluateDirectEdit()");
    try{ labels2units( delete_edit_labels ); }
    catch( Exception e2 ){
      snns.showException( e2, this );
      return;
    }
    if( delete_edit_labels ) {
      deleteLabels();
      edit_top_labels = edit_bottom_labels = false;
    }
    network.fireEvent( NetworkEvent.UNIT_VALUES_EDITED );
  }

  public void dispose(){ close(); }

/*----------------------- protected methods ----------------------------------*/
  /**
   * Converts given grid x-coordinate to pixel x-coordinate.
   *
   * @param x x-coordinate, in grid units
   * @return corresponding x-coordinate in pixels
   */
  protected int X(int x){ return settings.grid_size * x + (settings.width) + 1; }

  /**
   * Converts given grid Y coordinate to pixel Y coordinate.
   *
   * @param y y-coordinate, in grid units
   * @return corresponding y-coordinate in pixels
   */
  protected int Y(int y){ return settings.grid_size * y + (settings.height>>1) + 1 + font_height; }

/*----------------------- private methods ------------------------------------*/

  /**
   * Checks if there is a unit at given coordinates.
   *
   * @param p point with queried coordinates.
   * @return <code>true</code> if there is a unit at given point
   */
  private boolean isPointOnUnit( Point p ){
    Unit unit = network.getUnitAtXY( new int[]{ grid_x, grid_y } );
    if( unit == null ) return false;
    return isPointOnUnit( p, unit );
  }

  /**
   * Checks if the given point lies on the given unit.
   *
   * @param p point where the unit is expected
   * @param unit unit expected at point
   * @return <code>true</code> if the point lies on the unit.
   */
  private boolean isPointOnUnit(Point p, Unit unit){
    int[] pos = unit.getPosition();
    if( p.x < X(pos[0]) - settings.width / 2 || p.x > X(pos[0]) + settings.width / 2 ) return false;
    if( p.y < Y(pos[1]) - settings.height / 2 || p.y > Y(pos[1]) + settings.height / 2 ) return false;
    return true;
  }

  private Vector getUnitsInRectangle(){
    Vector units = new Vector();
    for( Unit unit = network.getFirstUnit(); unit!=null; unit = network.getNextUnit() )
      if( isUnitInRectangle( unit ) ) units.addElement( unit );
    return units;
  }

  /**
   * Checks if a certain unit lies in the selection rectangle
   *
   * @param Unit
   * @return true if this unit is selected
   */
  private boolean isUnitInRectangle( Unit unit ){
    int[] pos = unit.getPosition();
    if( !rectangle.contains( X(pos[0]), Y(pos[1]) ) ) return false;
    return true;
  }

  /**
   * moves dragged units away
   *
   * @return true if a move was necessary
   */
  private boolean updatePositions(){
    if( delta.x == 0 && delta.y == 0 ) return false;
    int dx = delta.x / settings.grid_size,
        dy = delta.y / settings.grid_size;
    network.moveUnits( network.getSelectedUnits(), new int[]{ dx, dy } );
    delta.x = delta.y = 0;
    return true;
  }


  /**
   * builds a rectangle around the given coordinates and paints it
   */
  private void buildRectangle(){
    int x, y, width, height;
    x = Math.min( startPoint.x, endPoint.x );
    y = Math.min( startPoint.y, endPoint.y );
    width = Math.abs( startPoint.x - endPoint.x ) + 1;
    height = Math.abs( startPoint.y - endPoint.y ) + 1;
    rectangle = new Rectangle( x, y, width, height);
    repaint( rectangle );
  }

  /**
   * checks if mouse made a significant move while dragging units
   * decides to repaint the network
   */
  private void checkStepSize( Point p ){
    int dirX = ( p.x > endPoint.x )? 1 : -1,
        dirY = ( p.y > endPoint.y )? 1 : -1,
        dx = ( p.x - endPoint.x + dirX * (settings.grid_size>>1)) / settings.grid_size,
        dy = ( p.y - endPoint.y + dirY * (settings.grid_size>>1)) / settings.grid_size;
    if( dx!= 0 || dy != 0 ){
      endPoint.x += dx * settings.grid_size;
      endPoint.y += dy * settings.grid_size;
      delta.x = endPoint.x - startPoint.x;
      delta.y = endPoint.y - startPoint.y;
      repaint();
    }
  }

  private void mouseMovedTo(Point p){
    grid_x = (p.x - 1 - settings.width + (settings.grid_size>>1)) / settings.grid_size;
    grid_y = (p.y - font_height - 1 + ((settings.grid_size - settings.height)>>1))/ settings.grid_size;
    setToolTipText( "(" + grid_x + ", " + grid_y  + ")" );
  }

  protected void close(){
    network.removeListener( this );
    fireEvent( NetworkViewEvent.VIEW_CLOSED );
  }

/*-------------------- private methods for direct edit -----------------------*/

  private void prepareLabels(){
    labels = new JTextField[ network.getNumberOfUnits() ];
    for( int i=0; i<labels.length; i++ ) {
      labels[ i ] = new JTextField( 5 );
      labels[ i ].setHorizontalAlignment(JTextField.CENTER);
      labels[ i ].setFont(settings.font);
      labels[ i ].setBorder( BorderFactory.createLineBorder(settings.text_color) );
      Dimension d = labels[ i ].getPreferredSize();
      labels[ i ].setSize( d.width, font_ascent+2 );
      labels[ i ].setVisible( true );
      add( labels[ i ] );

      labels[i].addKeyListener(
        new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == e.VK_ESCAPE) {
              deleteLabels();
              edit_top_labels = edit_bottom_labels = false;
              fireEvent(NetworkViewEvent.SETTINGS_CHANGED);
              network.fireEvent( NetworkEvent.UNIT_VALUES_EDITED );
              repaint();
            }
            else if(e.getKeyCode() == e.VK_ENTER) evaluateDirectEdit(true);
          }
        }
      );
    }
    fillLabels();
  }

  private void fillLabels(){
    if( labels == null ) return;
    String label;
    Unit unit;
    for( int i=0; i<labels.length; i++ ) {
      unit = network.getUnitNumber( i + 1 );
      switch( labels_to_edit ) {
        case NetworkViewSettings.ACT:       label = Snns.maxFixPoint(unit.getActivation(), 3); break;
        case NetworkViewSettings.INIT_ACT:  label = Snns.maxFixPoint(unit.getInitAct(), 3); break;
        case NetworkViewSettings.OUTPUT:    label = Snns.maxFixPoint(unit.getOutput(), 3); break;
        case NetworkViewSettings.BIAS:      label = Snns.maxFixPoint(unit.getBias(), 3); break;
        case NetworkViewSettings.NAME:      label = unit.getName(); break;
        case NetworkViewSettings.NUMBER:    label = String.valueOf(unit.getNumber()); break;
        case NetworkViewSettings.Z_VAL:     label = String.valueOf( unit.getPosition()[2] ); break;
//      case NetworkViewSettings.WINNER:    label = ;
        default:      label = "";
      }
      labels[ i ].setText( label );
    }
  }

  /**
   * method gives the units their new values
   */
  private void labels2units( boolean delete_edit_labels ) throws Exception{
    if( under_construction ) System.out.println("NetworkView.labels2units()");
    switch( labels_to_edit ) {
      case NetworkViewSettings.NAME:      names2units( delete_edit_labels ); return;
      case NetworkViewSettings.Z_VAL:     zPos2units( delete_edit_labels ); return;
    }
    double v = 0.0;
    Unit unit;
    for( int i=0; i<labels.length; i++ ){
      unit = network.getUnitNumber( i + 1 );
      try{ v = Double.valueOf( labels[ i ].getText() ).doubleValue(); }
      catch( Exception e ){
        throw new Exception("Value at unit " + ( i + 1 ) + " has to be a double value");
      }
      switch( labels_to_edit ){
        case NetworkViewSettings.ACT:       unit.setActivation( v ); break;
        case NetworkViewSettings.INIT_ACT:  unit.setInitAct( v ); break;
        case NetworkViewSettings.OUTPUT:    unit.setOutput( v ); break;
        case NetworkViewSettings.BIAS:      unit.setBias( v ); break;
      }
    }
  }

  private void names2units( boolean delete_edit_labels ){
    String name;
    Unit unit;
    for( int i=0; i<labels.length; i++ ){
      unit = network.getUnitNumber( i + 1 );
      unit.setName( labels[ i ].getText() );
    }
  }

  private void zPos2units( boolean delete_edit_labels ) throws Exception{
    int[] pos;
    Unit unit;
    for( int i=0; i<labels.length; i++ ){
      unit = network.getUnitNumber( i + 1 );
      pos = unit.getPosition();
      try{ pos[2] = Integer.parseInt( labels[ i ].getText() ); }
      catch( Exception e ){
        throw new Exception("Z-Position of unit " + ( i + 1 ) + " has to be an integer value");
      }
      unit.setPosition( pos );
    }
  }

  private void deleteLabels(){
    if( under_construction ) System.out.println("NetworkView.deleteLabels()");
    for( int i=0; i<labels.length; i++ )
      remove( labels[ i ] );
    labels = null;
  }

/*------------------------ interfaces ----------------------------------------*/
// implementing NetworkListener :
  /**
   * repaints the view when the network has changed
   */
  public void networkChanged( NetworkEvent evt ){
    if( under_construction ) System.out.println("NetworkView.networkChanged(): "+evt);
    int type = evt.id;

    if( type == NetworkEvent.NETWORK_NAME_CHANGED ){
      frame.setTitle( network.getName() + " <" + net_view_no + ">" );
      frame.repaint();
    }

    else if( type == NetworkEvent.NETWORK_DELETED      ||
             type == NetworkEvent.NEW_NETWORK_LOADED   ){
      frame.setTitle( network.getName() + " <" + net_view_no + ">" );
      if( isDirectEdit() ) deleteLabels();
      frame.repaint();
    }

    else if( type == NetworkEvent.UNITS_CREATED ){
      if( isDirectEdit() ){
        deleteLabels();
        prepareLabels();
      }
      Unit[] units = (Unit[])evt.arg;
      int i, x = 0, y = 0, h, v;
      int[] pos;
      for( i=0; i<units.length; i++ ){
        pos = units[i].getPosition();
        if( pos[0] > x ) x = pos[0];
        if( pos[1] > y ) y = pos[1];
      }
      JViewport viewPort = scrollPane.getViewport();
      Rectangle r = viewPort.getViewRect();
      Point p;
      x = X(x); y = Y(y);
      if( !r.contains( x, y ) ){
        h = x - r.width + (settings.grid_size>>1);
        if( h < 0 ) h = 0;
        v = y - r.height + (settings.grid_size>>1);
        if( v < 0 ) v = 0;
        view_position = new Point( h, v );
      }
      repaint();
    }

    else if( type == NetworkEvent.UNITS_DELETED ){
      if( isDirectEdit() ){
        deleteLabels();
        prepareLabels();
      }
      repaint();
    }

    else if( type == NetworkEvent.PATTERN_CHANGED ){
      if( isDirectEdit() ) fillLabels();
      repaint();
    }

    else if( evt.id == NetworkEvent.NETWORK_INITIALIZED ||
             evt.id == NetworkEvent.UNIT_VALUES_EDITED  ||
             evt.id == NetworkEvent.SELECTED_UNITS_CHANGED ||
             evt.id == NetworkEvent.LINKS_CREATED ||
             evt.id == NetworkEvent.LINKS_DELETED ||
             evt.id == NetworkEvent.NETWORK_UPDATED ||
             evt.id == NetworkEvent.UNITS_MOVED ||
             evt.id == NetworkEvent.NETWORK_TRAINED ||
             evt.id == NetworkEvent.NETWORK_PRUNED ||
             evt.id == NetworkEvent.SUBPATTERN_CHANGED) repaint();
  }

  public Dimension getMinimumSize(){
    Dimension min = super.getMinimumSize();
    if( min.width < 400 ) min.width = 400;
    if( min.height < 300 ) min.height = 300;
    return min;
  }

// implementing Scrollable :
    /**
     * Returns the preferred size of the viewport for a view component.
     * For example the preferredSize of a JList component is the size
     * required to acommodate all of the cells in its list however the
     * value of preferredScrollableViewportSize is the size required for
     * JList.getVisibleRowCount() rows.   A component without any properties
     * that would effect the viewport size should just return
     * getPreferredSize() here.
     *
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize(){
      return getPreferredSize();
    }


    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation.  Ideally,
     * components should handle a partially exposed row or column by
     * returning the distance required to completely expose the item.
     * <p>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction
     * @see JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction){
      if( orientation == SwingConstants.VERTICAL ) return settings.height;
      else return settings.width;
    }


    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one block
     * of rows or columns, depending on the value of orientation.
     * <p>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a block scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "block" increment for scrolling in the specified direction.
     * @see JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction){
      return settings.grid_size ;
    }


    /**
     * Return true if a viewport should always force the width of this
     * Scrollable to match the width of the viewport.  For example a noraml
     * text view that supported line wrapping would return true here, since it
     * would be undesirable for wrapped lines to disappear beyond the right
     * edge of the viewport.  Note that returning true for a Scrollable
     * whose ancestor is a JScrollPane effectively disables horizontal
     * scrolling.
     * <p>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables width to match its own.
     */
    public boolean getScrollableTracksViewportWidth(){ return false;  }

    /**
     * Return true if a viewport should always force the height of this
     * Scrollable to match the height of the viewport.  For example a
     * columnar text view that flowed text in left to right columns
     * could effectively disable vertical scrolling by returning
     * true here.
     * <p>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     */
    public boolean getScrollableTracksViewportHeight(){ return false; }

  // implementing Printable:
  public int print( Graphics g, PageFormat pf, int pi ){
    if( pi >= 1 ) return Printable.NO_SUCH_PAGE;
    scrollPane.setHorizontalScrollBarPolicy(
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    );
    scrollPane.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    );
    frame.getContentPane().validate();
    PagePrinter printer = new PagePrinter( frame.getContentPane(),g , pf );
    printer.fit_in_possible = false;
    int ret = printer.print();
    scrollPane.setHorizontalScrollBarPolicy(
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    scrollPane.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
    );
    frame.getContentPane().validate();
    return ret;
  }
}


