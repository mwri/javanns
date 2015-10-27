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
import javax.swing.event.*;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.*;
import java.io.IOException ;
import java.util.Vector ;
import java.util.Properties;
import java.io.*;
import java.awt.print.* ;
import java.net.URL;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * Snns is the main class which initializes the GUI
 *
 */
class Snns extends JFrame implements ActionListener,
                                     NetworkListener,
                                     LoaderAndSaver,
                                     NetworkViewListener {

  /**
   * applet from which the JavaNNS was called. <code>null</code> if the
   *  JavaNNS is used as a stand-alone application
   */
  public static JApplet applet = null;

  /*-------------------------------------------------------------------------*
   * Settings for different distributions (Unix v/s Windows; book or no book)
   *-------------------------------------------------------------------------*/

  /**
   * Current version number of the release
   */
  public static String JavaNNS_VERSION = "1.1";

  /**
   * filenames of JavaNNS
   */
  static final String JAR_FILE_NAME = "JavaNNS.jar";

  // WICHTIG : Im jar file darf es keine weitere Datei geben, die im Namen die
  // Zeichenkette SNNS_jkr enthaelt, da ansonsten die Methode loadLibrary
  // in Network durcheinandergeraten koennte
  static final String LIBRARY_NAME = "SNNS_jkr";

  /**
   * Filename of the javanns fn-declaration file
   * usually part of the jar-file
   */
  static final String FN_DECL_FILE_NAME = "FnDeclaration.txt";

  /**
   * the JavaNNS help index file
   * JavaNNS looks for that file in the URL specified by SNN_BOOK_URL_KEY
   * of the JavaNNS properties
   */
  static final String HELP_INDEX_FILE_NAME = "JavaNNSindex.html";


  /**
   * path of the icon and images directory
   */
  static final String ICON_PATH = "images";


  static JavaNNSProperties properties;

  /*-------------------------------------------------------------------------*
   * private member variables
   *-------------------------------------------------------------------------*/

  /**
   * Use metal look-and-feel or not:
   */
  private static boolean METAL_LOOK_AND_FEEL = true;


  // Diverse menu items, for use in action listener
  private JMenuItem
    mNew, mOpen, mClose, mSave, mSaveAs, mSaveData, mPrint, mExit, // File
    mNetwork, m3DNetwork, mGraph, mAnalyzer, mProjection, mWeights, mViewSettings, mEditUnits,
      cbmiStatusPanel, mProperties,// View
    mControl, mCascade, mKohonen, mLog,// Tools
    mAddPattern, mModifyPattern, mDeletePattern, mNewPatternSet, // Pattern
    mCascadeW, mCloseW,// mArrangeIcons, // Window
    mContents, mSearch, mAbout; // Help

  // edit menu:
  private JMenuItem mUndo, mRedo, mTop, mBottom;
    // delete menu:
    private JMenu mDelete;
    private JMenuItem mDeleteLinks, mDeleteUnits;

  // Create submenu of the Tools menu:
  private JMenu mCreate;
  private JMenuItem mLayer, mConnect;

  JPopupMenu popup;
  private JMenuItem mPopEditU, mPopEditL, mPopDelU, mPopDelL;

  // used by WindowsMenuItems, so it has to be a member
  private JMenu mWindows;

  // main pane
  private JDesktopPane desktop;

  // all functions
  Functions functions;

  // unit detail
  UnitDetailPanel unitDetail;

  Insets panel_insets = new Insets(3, 4, 3, 4); // Insets for all additional panels
  IconGrabber icons;
  MasterControl master;
  StatusPanel statusPanel;
  public LogPanel pLog;
  public FileManager fileManager;
  public Network network;
  public PatternSets patternSets;
  File configHomeFile;

  private Vector net_views = new Vector();
  private int net_views_count;// more history than number of current net views
  private NetworkView last_view; // the last active network view

  /*-------------------------------------------------------------------------*
   * constructor: creates main application frame
   *-------------------------------------------------------------------------*/

  /**
   * Class constructor. Creates main application frame and loads specified
   *   network and patterns.
   *
   * @param files to load
   */
  public Snns( String[] args ) throws Exception{
    super("JavaNNS");
    icons = new IconGrabber(this);
    setIconImage(icons.getImage("network.gif"));

    properties = new JavaNNSProperties(this);

    network = new Network( this );
    network.addListener( this );
    patternSets = new PatternSets( this, 5 );

    JPanel contentPane = new JPanel( new BorderLayout() );
    statusPanel = new StatusPanel( this );
    contentPane.add( statusPanel, BorderLayout.NORTH );

    desktop = new JDesktopPane(); //a specialized layered pane
    //setContentPane(desktop);
    contentPane.add( desktop, BorderLayout.CENTER );
    setContentPane( contentPane );
    desktop.putClientProperty("JDesktopPane.dragMode", "outline");

    setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
    addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) { close();}
      }
    );

    setMenuBar();

    pLog = new LogPanel( this );
    addInternalFrame( pLog.frame, false );

    fileManager = new FileManager( this );
    fileManager.setDefaultLAS( pLog );

    // the function set :
    functions = new Functions(this);
    master = new MasterControl(this);
    addInternalFrame(master.frame, false);


    // check the arguments :
    LoaderAndSaver[] las = new LoaderAndSaver[]{ this, network, patternSets.getCover() };
    for( int i=0; i<args.length; i++ ) {
      if( args[i].equals("-view") ) openNetworkView();
      else if( args[i].equals("-controlPanel") )
        master.frame.setVisible(true);
      else if( args[i].equals("-errorGraph") ) {
        GraphPanel gp = new GraphPanel( this );
        addInternalFrame( gp.frame, true );
      }
      else if( args[i].equals("-analyzer") ){
        AnalyzerPanel ap = new AnalyzerPanel( this );
        addInternalFrame( ap.frame, true );
      }
      else if( args[i].equals("-log") )
        pLog.frame.setVisible( true );
      else if(args[i].equals("-help") || args[i].equals("-?")) {
        System.out.println(
          "\n" +
          "Java Neural Network Simulator " + JavaNNS_VERSION + "\n" +
          "Copyright (c) 1990-1995 IPVR, University of Stuttgart\n" +
          "Copyright (c) 1996-2002 WSI, University of Tuebingen\n" +
          "http://www-ra.informatik.uni-tuebingen.de/forschung/JavaNNS/\n\n" +
          "Usage:  java -jar JavaNNS.jar [-options] [files]\n\n" +
          "where options include:\n" +
          "  -view           open a network view upon starting\n" +
          "  -errorGraph     open an error graph window\n" +
          "  -controlPanel   open the control panel\n" +
          "  -analyzer       open the analyzer tool\n" +
          "  -log            show the log window\n" +
          "  -?  or  -help   show this help message\n"
        );
        System.exit(0);
      }
      else
        //try{ fileManager.load( new File( args[i] ), las ); }
        try {
          // this is a workaround for JVM not respecting the user.dir
          // when set in the applet
        fileManager.load(new File(System.getProperty("user.dir", "")
                                  + System.getProperty("file.separator", "/")
                                  + args[i]),
                         las); }
        catch( Exception e ){ showException( e, fileManager ); }

    }

    // unit details :
    unitDetail = new UnitDetailPanel( this, true );
    addInternalFrame( unitDetail.frame, false );

    PrintStream out = new PrintStream( pLog.writer );
    //System.out.println("System.out und System.err wird auf LogPanel umgeleitet");
    System.setErr( out );
    System.setOut( out );

    if( net_views.size() == 0 ) openNetworkView();
  }


  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  /**
   * Stand-alone application starting point. Calls
   *  {@link #appletMain(JApplet, String[])} with <code>applet</code>
   *  parameter set to <code>null</code>
   *
   * @param args [0]:network, [1]:pattern, [2]:configuration file to load.
   *   All args are optional.
   */
  public static void main(String[] args) { appletMain(null, args); }

  /**
   * Application starting point if called from an applet. Sets application
   *   look-and-feel and displays the application main frame.
   *
   * @param apl caller applet
   * @param args [0]:network, [1]:pattern, [2]:configuration file to load.
   *   All args are optional.
   * @return main application frame
   */
  public static Snns appletMain(JApplet apl, String[] args) {
    applet = apl;
    String laf;
    try {
      if(isMetalLookAndFeel()) laf = UIManager.getCrossPlatformLookAndFeelClassName();
      else laf = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(laf);
    } catch(Exception e1) { System.out.println("No Look-and-Feel!"); }
    try{
      Snns frame = new Snns( args );
      frame.setSize(Integer.parseInt(properties.getProperty(properties.SCR_WIDTH_KEY)),
                    Integer.parseInt(properties.getProperty(properties.SCR_HEIGHT_KEY))
      );
      frame.setVisible(true);
      frame.cascadeWindows();
      return frame;
    }
    catch( Exception ex ){
      JOptionPane.showMessageDialog( apl, ex, "JavaNNS couldn't start", JOptionPane.ERROR_MESSAGE );
      ex.printStackTrace();
      if( apl != null ) apl.stop();
      else System.exit(0);
      return null;
    }
  }


  /**
   * Formats a double to a maximum of <code>m</code> digits behind the point.
   * Discards any trailing zeros.
   *
   * @param x The double to be formated.
   * @param m The maximum number of digits behind the decimal point.
   * @return  String containing formated number.
   */
  public static String maxFixPoint(double x, int m) {
    if(m <= 0) return String.valueOf((int)x);
    String s;
    if(Math.abs(x) < Math.pow(10, -m)) s = "0.00000000000000000000000000000000000";
    else s = String.valueOf(x);
    int i = s.indexOf('.');
    if(i<0) return s;
    if(s.length() >= i+m+1) s = s.substring(0, i+m+1);
    while( s.endsWith("0") ) s = s.substring(0, s.length()-1);
    if(s.endsWith(".")) s = s.substring(0, s.length()-1);
    return s;
  }

  /**
   * Formats a double to <code>m</code> digits behind the point.
   * Displays trailing zeros, if any.
   *
   * @param x The double to be formated.
   * @param m Number of digits behind the decimal point.
   * @return  String containing formated number.
   */
  public static String fixPoint(double x, int m) {
    if(m <= 0) return String.valueOf((int)x);
    String s;
    if(Math.abs(x) < Math.pow(10, -m)) s = "0.00000000000000000000000000000000000";
    else s = String.valueOf(x);
    int i = s.indexOf('.');
    if(i<0) {
      s = s + ".00000000000000000000000000000000000";
      i = s.indexOf('.');
    }
    if(s.length() >= i+m+1) return s.substring(0, i+m+1);
    while( s.length() < i+m+1 ) s = s +"0";
    return s;
  }

  /**
   * adds a new frame to the desktop
   *
   * @param the internal frame
   */
  public void addInternalFrame( final JInternalFrame frame, boolean visible ){
    /*System.out.println("Snns.addInternalFrame(): "+evt.getMessage());
    int no = desktop.getComponentCount();
    System.out.println("Snns contains "+no+" components:");
    for( int i=0; i<no; i++ ) {
      java.awt.Component c = desktop.getComponent(i);
      if( c instanceof JInternalFrame ) {
        JInternalFrame f = (JInternalFrame)c;
        java.awt.Container cont = f.getContentPane();
        if( cont instanceof JScrollPane ){
          JScrollPane sp = (JScrollPane)cont;
          JViewport vp = sp.getViewport();
          if( vp.getView() instanceof NetworkView ){
            NetworkView nv = (NetworkView)vp.getView();
            System.out.println("NetworkView["+nv.net_view_no+"]");
          }
          else System.out.println( sp.getViewport().getClass() );
        }
        else if( cont instanceof JTabbedPane )
          System.out.println("Control panel");
        else if( cont instanceof UnitDetailPanel ){
          UnitDetailPanel u = (UnitDetailPanel)cont;
          String text = "Unit details";
          if( u.frame.isVisible() ) text += " shown";
          else text += " hidden";
          System.out.println( text );
        }
        else if( cont instanceof GraphPanel )
          System.out.println("Error graph panel");
        else System.out.println( f.getContentPane().getClass() );
      }
      else System.out.println( c.getClass() );
    }*/

    // this object modifies the windows menu
    new WindowMenuItem(frame, mWindows);

    desktop.add( frame );
    if( visible ){
      frame.setVisible( true );
      frame.toFront();
      try {
        frame.setIcon(false);
        frame.setSelected(true);
      }
      catch (java.beans.PropertyVetoException e) {
        //System.out.println(frame.toString()+"coudn't get focused.");
        showException( e );
      }
    }
  }

  /**
   * Gives information about LookAndFeel the JavaNNS is using.
   *
   * @return <code>true</code> if using Swing (Metal) LookAndFeel
   */
  public static boolean isMetalLookAndFeel() { return METAL_LOOK_AND_FEEL; }

  private void showException( String message ){
    showException( new Exception( message ) );
  }
  private void showException( Exception e ){ showException( e, this ); }
  /**
   * method shows the message of an exception in a message dialog
   *
   * @param the exception
   */
  public void showException( Exception e, Object source ){
    JOptionPane.showMessageDialog( this,
                                   e.toString(),
                                   "Error message",
                                   JOptionPane.ERROR_MESSAGE );
    if( pLog != null )
      e.printStackTrace( new PrintStream( pLog.writer ) );
  }
  /**
   * method returns the last active network view or null
   */
  public NetworkView getLastSelectedView(){
    if( last_view != null && last_view.frame.isSelected() )
      return last_view;
    else if( net_views.size() > 0 )
      return (NetworkView)net_views.lastElement();
    return null;
  }

  /**
   * method returns the current NetworkViewSettings, that means either the
   * settings of the currently active NetworkView, of the last created NetworkView
   * or default settings
   *
   * @return the NetworkViewSettings
   */
  public NetworkViewSettings getCurrentSettings(){
    NetworkView view = getLastSelectedView();
    if( view != null ) return view.settings;
    return NetworkViewSettings.getDefaultSettings();
  }

  /**
   * method asks the user whether an <code>LoaderAndSaver</code> object should
   * be saved or not
   *
   * @param las the <code>LoaderAndSaver</code> object to save
   * @return <code>false</code> if something went wrong or if the
   *         CANCEL_OPTION has been selected
   */
  public boolean askForSaving( LoaderAndSaver las ){
    boolean ok = true;
    String text = "Save changes to "+ las.getLASName() + "?";
    int choice = JOptionPane.showOptionDialog( this, text, "FileManager",
                                               JOptionPane.YES_NO_CANCEL_OPTION,
                                               JOptionPane.QUESTION_MESSAGE,
                                               null, null, null );
    if( choice == JOptionPane.YES_OPTION ){
      try{
        if( las.hasHomeFile() ) las.save();
        else ok = fileManager.showSaveDialog( las );
      }
      catch( Exception e ){
        showException( e );
        ok = false;
      }
    }
    else if( choice == JOptionPane.CANCEL_OPTION ) ok = false;
    return ok;
  }

/*------------------------------ private methods -----------------------------*/


  /**
   * Opens a frame for viewing the network with default settings
   */
  private UIDisplayType getDefaultDisplayType() {
    UIDisplayType dt = new UIDisplayType();
    dt.position = new Point(0, 0);
    dt.displayNo = net_views_count + 1;
    dt.dimension = new Dimension(400, 300);
    dt.settings = null;
    return dt;
  }

  private void openNetworkView(){
    openNetworkView( getDefaultDisplayType() );
  }

  /**
   * Opens a frame for viewing the network.
   *
   * @param display type
   */
  private void openNetworkView( UIDisplayType dt ) {
    //System.out.println("Snns.openNetworkView");
    NetworkView nv = new NetworkView( this,  dt );
    nv.addListener( this );
    net_views.addElement( nv );
    net_views_count++;
    addInternalFrame( nv.frame, true );
  }

  /**
   * method removes all network views and sets the network view counter to zero
   */
  private void deleteNetworkViews(){
    NetworkView view;
    for( int i=net_views.size()-1; i>-1; i-- ){
      view = (NetworkView)net_views.elementAt( i );
      view.frame.dispose();
    }
    net_views_count = 0;
  }

  private void close(){
    Vector las = new Vector();

    if( network.contentChanged() ) las.add(network);

    if( pLog.contentChanged() ) las.add(pLog);

    Vector sets = patternSets.sets;
    for( int i=0; i<sets.size(); i++ ){
      PatternSet set = (PatternSet)sets.get(i);
      if( set.contentChanged() ) las.add(set);
    }

    if( properties.contentChanged() ) las.add(properties);

    if( las.size() > 0 ){
      LoaderAndSaver[] lasar = new LoaderAndSaver[ las.size() ];
      las.toArray(lasar);
      if( !(new LASSaveDialog()).show( this, (LoaderAndSaver[])lasar ) ) return;
    }
    if(applet == null) System.exit(0);
    else dispose();
  }

  /**
   * Creates menu bar in the application main frame.
   */
  private void setMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    // File menu
    JMenu menu;
    menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    menu.getAccessibleContext().setAccessibleDescription("File menu");
    menuBar.add(menu);

    mNew = new JMenuItem("New...", KeyEvent.VK_N);
    mNew.setIcon(icons.getIcon("New16.gif"));
    mNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    mNew.getAccessibleContext().setAccessibleDescription("Create new network");
    mNew.addActionListener(this);
    menu.add(mNew);

    mOpen = new JMenuItem("Open...", KeyEvent.VK_O);
    mOpen.setIcon(icons.getIcon("Open16.gif"));
    mOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    mOpen.getAccessibleContext().setAccessibleDescription("Open file...");
    mOpen.addActionListener(this);
    menu.add(mOpen);

    menu.addSeparator();

    mClose = new JMenuItem("Close", KeyEvent.VK_C);
    mClose.setIcon(icons.getIcon("empty16.gif"));
    mClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
    mClose.getAccessibleContext().setAccessibleDescription("Close");
    mClose.addActionListener(this);
    menu.add(mClose);

    mSave = new JMenuItem("Save", KeyEvent.VK_S);
    mSave.setIcon(icons.getIcon("Save16.gif"));
    mSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    mSave.getAccessibleContext().setAccessibleDescription("Save network");
    mSave.addActionListener(this);
    menu.add(mSave);

    mSaveAs = new JMenuItem("Save as...", KeyEvent.VK_A);
    mSaveAs.setIcon(icons.getIcon("SaveAs16.gif"));
    mSaveAs.addActionListener(this);
    menu.add(mSaveAs);

    mSaveData = new JMenuItem("Save data...", KeyEvent.VK_D);
    mSaveData.setIcon(icons.getIcon("empty16.gif"));
    mSaveData.addActionListener(this);
    menu.add(mSaveData);

    menu.addSeparator();

    mPrint = new JMenuItem("Print...", KeyEvent.VK_P);
    mPrint.setIcon(icons.getIcon("Print16.gif"));
    mPrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    mPrint.getAccessibleContext().setAccessibleDescription("Print");
    mPrint.addActionListener(this);
    menu.add(mPrint);

    menu.addSeparator();

    mExit = new JMenuItem("Exit", KeyEvent.VK_X);
    mExit.setIcon(icons.getIcon("empty16.gif"));
    mExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    mExit.getAccessibleContext().setAccessibleDescription("Exit");
    mExit.addActionListener(this);
    menu.add(mExit);

    // Edit menu
    JMenu mEdit = new JMenu("Edit");
    mEdit.setMnemonic( KeyEvent.VK_E );
    menuBar.add( mEdit );

    mUndo = new JMenuItem("Undo", KeyEvent.VK_U);
    mUndo.setIcon(icons.getIcon("Undo16.gif"));
    mUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
    mUndo.getAccessibleContext().setAccessibleDescription("Undo");
    mUndo.setEnabled(false);
    mEdit.add(mUndo);
    mUndo.addActionListener(this);

    mRedo = new JMenuItem("Redo", KeyEvent.VK_R);
    mRedo.setIcon(icons.getIcon("Redo16.gif"));
    mRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
    mRedo.getAccessibleContext().setAccessibleDescription("Redo");
    mRedo.setEnabled(false);
    mEdit.add(mRedo);
    mRedo.addActionListener(this);

    NetworkUndoRedo nur = new NetworkUndoRedo( this, 10 );
    nur.addMenus(mUndo, mRedo);

    mEdit.addSeparator();

    mTop = new JMenuItem("Top Labels");
    mTop.setIcon(icons.getIcon("empty16.gif"));
    mTop.setEnabled( false );
    mEdit.add( mTop );
    mTop.addActionListener( this );

    mBottom = new JMenuItem("Bottom Labels");
    mBottom.setIcon(icons.getIcon("empty16.gif"));
    mBottom.setEnabled( false );
    mEdit.add( mBottom );
    mBottom.addActionListener( this );

    mEdit.addSeparator();

    mEditUnits = new JMenuItem("Units Properties...");
    mEditUnits.setIcon(icons.getIcon("unitProperties.gif"));
    mEditUnits.setEnabled( false );
    mEditUnits.addActionListener( this );
    mEdit.add( mEditUnits );

    // Submenu "Delete":
    mDelete = new JMenu("Delete");
    mDelete.setMnemonic(KeyEvent.VK_D);
    mDelete.setIcon(icons.getIcon("Delete16.gif"));
    mDelete.setEnabled( false );
    mEdit.add( mDelete );

      mDeleteUnits = new JMenuItem("Units");
      mDeleteUnits.addActionListener( this );
      mDelete.add( mDeleteUnits );

      mDeleteLinks = new JMenuItem("Links");
      mDeleteLinks.addActionListener( this );
      mDelete.add( mDeleteLinks );

    // View menu
    menu = new JMenu("View");
    menu.setMnemonic(KeyEvent.VK_V);
    menu.getAccessibleContext().setAccessibleDescription("View menu");
    menuBar.add(menu);

    mNetwork = new JMenuItem("Network", KeyEvent.VK_N);
    mNetwork.setIcon(icons.getIcon("network.gif"));
    mNetwork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
    mNetwork.getAccessibleContext().setAccessibleDescription("Network window");
    mNetwork.addActionListener(this);
    menu.add(mNetwork);

    mViewSettings = new JMenuItem("Display Settings", KeyEvent.VK_D);
    mViewSettings.setIcon(icons.getIcon("viewSettings.gif"));
    mViewSettings.setEnabled( false );
    mViewSettings.addActionListener( this );
    menu.add( mViewSettings );

    /*m3DNetwork = new JMenuItem("3D-Network", KeyEvent.VK_3);
    m3DNetwork.setIcon(new ImageIcon( cl.getResource("images/empty16.gif") ));
    m3DNetwork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
    m3DNetwork.getAccessibleContext().setAccessibleDescription("3D Network window");
    m3DNetwork.addActionListener(this);
    menu.add(m3DNetwork);
    m3DNetwork.setEnabled(false);*/

    menu.addSeparator();

    mGraph = new JMenuItem("Error Graph", KeyEvent.VK_E);
    mGraph.setIcon(icons.getIcon("errorGraph.gif"));
    mGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    mGraph.getAccessibleContext().setAccessibleDescription("Error graph window");
    mGraph.addActionListener(this);
    menu.add(mGraph);

    mWeights = new JMenuItem("Weights", KeyEvent.VK_W);
    mWeights.setIcon(icons.getIcon("weight.gif"));
    mWeights.getAccessibleContext().setAccessibleDescription("Weights");
    mWeights.addActionListener(this);
    menu.add(mWeights);

    mProjection = new JMenuItem("Projection", KeyEvent.VK_P);
    mProjection.setIcon(icons.getIcon("projection.gif"));
    mProjection.getAccessibleContext().setAccessibleDescription("Projection");
    mProjection.addActionListener( this );
    mProjection.setEnabled( false );
    menu.add( mProjection );

    mKohonen = new JMenuItem("Kohonen", KeyEvent.VK_K);
    mKohonen .setIcon(icons.getIcon("empty16.gif"));
    mKohonen.getAccessibleContext().setAccessibleDescription("Kohonen");
    mKohonen.addActionListener(this);
    menu.add(mKohonen);

    mLog = new JMenuItem("Log", KeyEvent.VK_L);
    mLog.setIcon(icons.getIcon("History16.gif"));
    mLog.getAccessibleContext().setAccessibleDescription("Log Panel");
    mLog.addActionListener(this);
    menu.add(mLog);

    menu.addSeparator();

    cbmiStatusPanel = new JCheckBoxMenuItem("Status panel", true);
    cbmiStatusPanel.addActionListener(this);
    menu.add(cbmiStatusPanel);

    if( properties.editable() ){
      mProperties = new JMenuItem("Properties...");
      mProperties.getAccessibleContext().setAccessibleDescription("Properties Editor");
      mProperties.addActionListener(this);
      menu.add(mProperties);
    }

    // Tools menu
    menu = new JMenu("Tools");
    menu.setMnemonic(KeyEvent.VK_T);
    menu.getAccessibleContext().setAccessibleDescription("Tools menu");
    menuBar.add(menu);

    mControl = new JMenuItem("Control Panel", KeyEvent.VK_C);
    mControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    mControl.getAccessibleContext().setAccessibleDescription("Control panel");
    mControl.addActionListener(this);
    menu.add(mControl);

    mCascade = new JMenuItem("Cascade & TACOMA", KeyEvent.VK_S);
    mCascade.getAccessibleContext().setAccessibleDescription("Cascade correlation and TACOMA");
    mCascade.addActionListener(this);
    menu.add(mCascade);

    mAnalyzer = new JMenuItem("Analyzer", KeyEvent.VK_A);
    mAnalyzer.getAccessibleContext().setAccessibleDescription("Analyzer");
    mAnalyzer.addActionListener(this);
    menu.add(mAnalyzer);

    // Submenu "Create"
    mCreate = new JMenu("Create");
    mCreate.setMnemonic(KeyEvent.VK_R);
    mCreate.addActionListener(this);
    menu.add(mCreate);

      mLayer = new JMenuItem("Layers...", KeyEvent.VK_L);
      mLayer.addActionListener( this );
      mCreate.add(mLayer);

      mConnect = new JMenuItem("Connections...");
      mConnect.setEnabled( false );
      mConnect.addActionListener( this );
      mCreate.add(mConnect);

    // Pattern menu
    menu = new JMenu("Pattern");
    menu.setMnemonic(KeyEvent.VK_P);
    menu.getAccessibleContext().setAccessibleDescription("Pattern menu");
    menuBar.add(menu);

    mAddPattern = new JMenuItem("Add", KeyEvent.VK_A);
    mAddPattern.setIcon(icons.getIcon("addPattern.gif"));
//    mAddPattern.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    mAddPattern.getAccessibleContext().setAccessibleDescription("Add new pattern");
    mAddPattern.addActionListener(this);
    menu.add(mAddPattern);

    mModifyPattern = new JMenuItem("Modify", KeyEvent.VK_M);
    mModifyPattern.setIcon(icons.getIcon("modifyPattern.gif"));
//    mModifyPattern.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    mModifyPattern.getAccessibleContext().setAccessibleDescription("Modify current pattern");
    mModifyPattern.addActionListener(this);
    menu.add(mModifyPattern);

    mDeletePattern = new JMenuItem("Delete", KeyEvent.VK_D);
    mDeletePattern.setIcon(icons.getIcon("deletePattern.gif"));
//    mDeletePattern.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    mDeletePattern.getAccessibleContext().setAccessibleDescription("Delete current pattern");
    mDeletePattern.addActionListener(this);
    menu.add(mDeletePattern );

    menu.addSeparator();

    mNewPatternSet = new JMenuItem("New Set", KeyEvent.VK_N);
    mNewPatternSet .setIcon(icons.getIcon("newPatternSet.gif"));
//    mNewPatternSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    mNewPatternSet.getAccessibleContext().setAccessibleDescription("Delete current pattern");
    mNewPatternSet.addActionListener(this);
    menu.add(mNewPatternSet);
    mNewPatternSet.setEnabled(false);

    // Window menu
    mWindows = new JMenu("Window");
    mWindows.setMnemonic(KeyEvent.VK_W);
    mWindows.getAccessibleContext().setAccessibleDescription("Window menu");
    menuBar.add(mWindows);

    mCascadeW = new JMenuItem("Cascade", KeyEvent.VK_C);
    mCascadeW.getAccessibleContext().setAccessibleDescription("Cascade windows");
    mCascadeW.addActionListener(this);
    mWindows.add(mCascadeW);

    mCloseW = new JMenuItem("Close all", KeyEvent.VK_A);
    mCloseW.getAccessibleContext().setAccessibleDescription("Close all windows");
    mCloseW.addActionListener(this);
    mWindows.add(mCloseW);
/*
    mArrangeIcons = new JMenuItem("Arrange Icons", KeyEvent.VK_I);
    mArrangeIcons.getAccessibleContext().setAccessibleDescription("Arrange Icons");
    mArrangeIcons.addActionListener(this);
    mWindows.add(mArrangeIcons);
*/
    mWindows.addSeparator();

    // Help menu
    menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);
    menu.getAccessibleContext().setAccessibleDescription("Help menu");
    menuBar.add(menu);

    mContents = new JMenuItem("Contents", KeyEvent.VK_C);
    mContents.setIcon(icons.getIcon("Help16.gif"));
    mContents.getAccessibleContext().setAccessibleDescription("Help contents");
    mContents.addActionListener(this);
    menu.add(mContents);
    //mContents.setEnabled(false);

    mSearch = new JMenuItem("Search...", KeyEvent.VK_S);
    mSearch.setIcon(icons.getIcon("empty16.gif"));
    mSearch.getAccessibleContext().setAccessibleDescription("Search help");
    mSearch.addActionListener(this);
    //menu.add(mSearch);
    //mSearch.setEnabled(false);

    menu.addSeparator();

    mAbout = new JMenuItem("About...", KeyEvent.VK_A);
    mAbout.setIcon(icons.getIcon("About16.gif"));
    mAbout.getAccessibleContext().setAccessibleDescription("About JavaNNS");
    mAbout.addActionListener(this);
    menu.add(mAbout);

    // Popup menu:
    popup = new JPopupMenu("Edit&Info");

    mPopDelU = new JMenuItem("Delete Units");
    mPopDelU.addActionListener( this );
    popup.add( mPopDelU );
    mPopDelU.setEnabled( false );

    mPopDelL = new JMenuItem("Delete Links");
    mPopDelL.addActionListener( this );
    popup.add( mPopDelL );
    mPopDelL.setEnabled( false );

    popup.addSeparator();

    mPopEditU = new JMenuItem("Edit Units...");
    mPopEditU.addActionListener( this );
    popup.add( mPopEditU );
    mPopEditU.setEnabled( false );

    mPopEditL = new JMenuItem("Edit Links...");
    mPopEditL.addActionListener( this );
    popup.add( mPopEditL );
    mPopEditL.setEnabled( false );
  }

  /**
   * updates the menu when a certain event ocurred
   *
   * @param the event
   */
  private void updateTheMenu( Event evt ){
    if( evt instanceof NetworkViewEvent ){
      NetworkView view = getLastSelectedView();
      if( view != null ){
        if( view.isDirectEdit() ){
          mTop.setEnabled( false );
          mBottom.setEnabled( false );
        }
        else{
          mTop.setEnabled( true );
          mBottom.setEnabled( true );
        }
        mProjection.setEnabled( ProjectionPanel.projectionIsPossible( network ) );
        mViewSettings.setEnabled( true );
        NetworkViewSettings settings = view.settings;
        String label;
        switch( settings.top_label_type ) {
          case NetworkViewSettings.ACT:       label = "Activation values"; break;
          case NetworkViewSettings.INIT_ACT:  label = "Initial activation"; break;
          case NetworkViewSettings.OUTPUT:    label = "Output values"; break;
          case NetworkViewSettings.BIAS:      label = "Bias"; break;
          case NetworkViewSettings.NAME:      label = "Names"; break;
          case NetworkViewSettings.NUMBER:    label = "Numbers"; break;
          case NetworkViewSettings.Z_VAL:     label = "Z-values"; break;
//        case NetworkViewSettings.WINNER:    label = ;
          default:      label = "Top labels";
        }
        mTop.setText( label );
        if( settings.top_label_type == NetworkViewSettings.NUMBER ) mTop.setEnabled( false );

        switch( settings.base_label_type ) {
          case NetworkViewSettings.ACT:       label = "Activation values"; break;
          case NetworkViewSettings.INIT_ACT:  label = "Initial activation"; break;
          case NetworkViewSettings.OUTPUT:    label = "Output values"; break;
          case NetworkViewSettings.BIAS:      label = "Bias"; break;
          case NetworkViewSettings.NAME:      label = "Names"; break;
          case NetworkViewSettings.NUMBER:    label = "Numbers"; break;
          case NetworkViewSettings.Z_VAL:     label = "Z-values"; break;
//        case NetworkViewSettings.WINNER:    label = ;
          default:      label = "Bottom labels";
        }
        mBottom.setText( label );
        if( settings.base_label_type == NetworkViewSettings.NUMBER ) mBottom.setEnabled( false );
      }
      else{
        mTop.setText("Top labels");
        mTop.setEnabled( false );
        mBottom.setText("Bottom labels");
        mBottom.setEnabled( false );
        mViewSettings.setEnabled( false );
        mProjection.setEnabled( false );
      }
    }
    else if( evt instanceof NetworkEvent ){
      NetworkEvent e = (NetworkEvent)evt;

      if( e.id == NetworkEvent.UNIT_VALUES_EDITED ){
        mTop.setEnabled( true );
        mBottom.setEnabled( true );
      }

      else if( e.id == NetworkEvent.SELECTED_UNITS_CHANGED ){
        int no = network.getSelectedUnitsCount();
        mEditUnits.setEnabled( ( no > 0 ) );
        mDelete.setEnabled( ( no > 0 ) );

        mPopEditU.setEnabled( ( no > 0 ) );
        mPopEditL.setEnabled( LinkEdit.linkSelected( network ) );
        mPopDelU.setEnabled( ( no > 0 ) );
        mPopDelL.setEnabled( ( no > 1 ) );
        // projection :
        mProjection.setEnabled( ProjectionPanel.projectionIsPossible( network ) );
      }

      else if( e.id == NetworkEvent.UNITS_DELETED     ||
               e.id == NetworkEvent.UNITS_CREATED     ||
               e.id == NetworkEvent.NEW_NETWORK_LOADED  )
        mConnect.setEnabled( ( network.getNumberOfUnits() > 1 ) );
    }
  }


/*----------------------------- interfaces -----------------------------------*/
  /**
   * Implementation of NetworkListener interface.
   * Asks if also configuration should be loaded with a new network.
   * Changes cursor if needed.
   *
   * @param evt event that was fired
   */
  public void networkChanged( NetworkEvent evt ){
    int type = evt.id;
    Cursor crs = null;

    if( type == NetworkEvent.NEW_NETWORK_LOADED ){
      updateTheMenu( evt );
      // looking for configuration file
      File configFile, netFile = network.getFile();
      String configPath, netPath;
      try{
        netPath = netFile.getCanonicalPath();
        int index = netPath.lastIndexOf(".");
        configPath = netPath.substring(0, index) + "." + getFileExtension();
        configFile = new File( configPath );
      }
      catch( Exception e ){ showException( e ); return; }

      if( !configFile.exists() ){
        if( net_views.size() == 0 ) openNetworkView();
        return;
      }

      //if( configuration.hasHomeFile() && configuration.getFile().getName().equals( configFile.getName() ) ) return;
      int choice = JOptionPane.showOptionDialog( this, "Load corresponding configuration file?", "Loading " + netFile,
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE, null, null, null);
      if( choice == JOptionPane.OK_OPTION ) {
        deleteNetworkViews();
        try{ load( configFile ); }
        catch(Exception e){ showException(e); }
      }
    }
    else if(
      type == NetworkEvent.SELECTED_UNITS_CHANGED ||
      type == NetworkEvent.UNIT_VALUES_EDITED     ||
      type == NetworkEvent.UNITS_CREATED          ||
      type == NetworkEvent.UNITS_DELETED
    ) updateTheMenu( evt );
    else if(type == NetworkEvent.LONG_TASK_STARTED)
      crs = new Cursor(Cursor.WAIT_CURSOR);
    else if(type == NetworkEvent.LONG_TASK_OVER)
      crs = new Cursor(Cursor.DEFAULT_CURSOR);

    if(crs != null) {
      setCursor(crs);
      int no = desktop.getComponentCount();
      for(int i=0; i<no; i++) {
        java.awt.Component c = desktop.getComponent(i);
        if(c instanceof JInternalFrame) ((JInternalFrame)c).setCursor(crs);
      }
    }
  }

  // implementing action listener :
  /**
   * Handles menu events in Snns main frame.
   *
   * @param e ActionEvent object
   */
  // action performer for Snns. Mainly handling of menus
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    //String s = "Action: " + source.getText();
    //System.out.println(s);
    JInternalFrame[] frames = desktop.getAllFrames();


    if(source == mExit) close();

    // File menu:
    else if( source == mNew ) {
      network.deleteNetwork();
      patternSets.deleteAllPatternSets();
      net_views_count = 0;
      if( net_views.size() == 0 ) openNetworkView();
    }

    else if(source == mOpen) {
      try{
        fileManager.showOpenDialog(
          new LoaderAndSaver[]{ network, patternSets.getCover(), this, pLog }
        );
      }
      catch( Exception e2 ){ showException( e2 ); }
    }

    else if(source == mSave) {
      if( network.hasHomeFile() )
        try{ network.save(); }
        catch( Exception ex ){
        showException( ex );
      }
      else actionPerformed( new ActionEvent( mSaveAs, 0, "" ) );
    }

    else if(source == mSaveAs) {
      try{ fileManager.showSaveDialog( network ); }
      catch( Exception e2 ){
        showException( e2 );
      }
    }

    else if(source == mSaveData) {
      try{
        fileManager.showSaveDialog(
          new LoaderAndSaver[]{ patternSets.getCurrent(), this, pLog, new Result( network ) }
        );
      }
      catch( Exception e2 ){
        showException( e2 );
      }
    }

    else if(source == mPrint) {
      int i=0;
      boolean found = false;
      Printable painter = null;

      // da NetworkView selbst und nicht die contantPane Printable implementiert
      // muessen die NetworkViews extra durchgegangen werden
      NetworkView view = getLastSelectedView();
      if( view != null && view.frame.isSelected() ){
        found = true;
        painter = view;
      }

      while( !found && i < frames.length ){
        if( frames[i].isSelected() ){
          found = true;
          Container cp = frames[i].getContentPane();
          if( cp instanceof Printable ) painter = (Printable)cp;
        }
        else i++;
      }
      /*  Container cp = desktop.getSelectedFrame().getContentPane();
        if( cp instanceof Printable ) painter = (Printable)cp;*/

      if( found && painter != null ){
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable( painter );
        if ( printJob.printDialog() ) {
          try { printJob.print(); }
          catch (Exception ex) { showException( ex ); }
        }
      }
    }

    // Edit menu
    else if( source == mTop ){
      mTop.setEnabled( false );
      mBottom.setEnabled( false );
      last_view.editTopLabels();
    }
    else if( source == mBottom ){
      mTop.setEnabled( false );
      mBottom.setEnabled( false );
      last_view.editBottomLabels();
    }
    else if( source == mLayer ) {
      boolean found = false;
      int i=0;
      while( !found && i<frames.length )
        if( frames[i].getContentPane() instanceof LayerPanel ) {
          found = true;
          try{ frames[i].setIcon( false ); }
          catch( Exception ex ){
            showException( "The LayerPanel couldn't be deiconified" );
          }
          frames[i].toFront();
        }
        else i++;
      if( !found ) {
        LayerPanel lp = new LayerPanel( this );
        addInternalFrame( lp.frame, true );
      }
    }

    else if( source == mConnect ){
      boolean found = false;
      int i=0;
      while( !found && i<frames.length )
        if( frames[i].getContentPane() instanceof LinkPanel ) {
          found = true;
          try{ frames[i].setIcon( false ); }
          catch( Exception ex ){
            showException( "The LinkPanel couldn´t be deiconified" );
          }
          frames[i].toFront();
        }
        else i++;
      if( !found ) {
        LinkPanel lp = new LinkPanel( this );
        addInternalFrame( lp.frame, true );
      }
    }
    // Edit / Delete menu:
    else if( source == mDeleteUnits ||
             source == mPopDelU       ) network.deleteUnits();
    else if( source == mDeleteLinks ||
             source == mPopDelL       ) network.deleteLinks();
    else if( source == mPopEditL ){
      try{
        LinkEdit le = new LinkEdit( this );
        addInternalFrame( le.frame, true );
      }
      catch( Exception ex ){ showException( ex ); }
    }

    // View menu:
    else if( source == mNetwork )
      openNetworkView();

    else if( source == mViewSettings ){
      NetworkView view = last_view;
      String title = "Display settings of view "+view.frame.getTitle();
      JDialog sd = new JDialog(this, title, true);
      sd.setLocation( 200, 200 );
      DisplaySettingsPanel dsp = new DisplaySettingsPanel(this, view );
      sd.setContentPane(dsp);
      sd.addWindowListener(new DisplaySettingsListener(dsp));
      sd.pack();
      sd.setResizable( false );
      sd.setVisible(true); //necessary as of kestrel
    }

    else if( source == mProjection ){
      ProjectionPanel p = new ProjectionPanel( this );
      addInternalFrame( p.frame, true );
    }

    else if( source == mWeights ){
      WeightPanel wp = new WeightPanel( this );
      addInternalFrame(wp.frame, true);
    }

    else if( source == mAnalyzer ){
      AnalyzerPanel ap = new AnalyzerPanel( this );
      addInternalFrame( ap.frame, true );
    }

    else if( source == mEditUnits ||
             source == mPopEditU    ) unitDetail.showDetails();

    else if(source == mGraph) {
      // open or restore the error graph window
      boolean found = false;
      int i=0;
      while( !found && i<frames.length )
        if( frames[i].getContentPane() instanceof GraphPanel ) {
          found = true;
          try{ frames[i].setIcon( false ); }
          catch( Exception ex ){
            showException( "The GraphPanel couldn't be deiconified" );
          }
          frames[i].toFront();
        }
        else i++;
      if( !found ) {
        GraphPanel gp = new GraphPanel( this );
        addInternalFrame( gp.frame, true );
      }
    }

    else if(source == cbmiStatusPanel)
      statusPanel.setVisible( cbmiStatusPanel.isSelected() );

    else if(source == mProperties) properties.edit();

    // Tools menu
    else if(source == mControl) {
      // open or restore the control panel
      /*boolean found = false;
      int i=0;
      while( !found && i<frames.length )
        if(frames[i].getTitle().equals(master.frame_title)) {
          found = true;
          try{ frames[i].setIcon( false ); }
          catch( Exception ex ){
            showException("The ControlPanel couldn't be deiconified");
          }
          frames[i].toFront();
        }
        else i++;
      if( !found ) addInternalFrame( master.frame, true );*/
      master.frame.setVisible(true);
      master.frame.toFront();
    }

    else if(source == mCascade) {
      boolean found = false;
      int i=0;
      while( !found && i<frames.length )
        if( frames[i].getTitle().equals( CCPanel.TITLE ) ) {
          found = true;
          try{ frames[i].setIcon( false ); }
          catch( Exception ex ){
            showException( "The CascadePanel couldn't be deiconified" );
          }
          frames[i].toFront();
        }
        else i++;
      if( !found ) {
        CCPanel ccp = new CCPanel( this );
        addInternalFrame( ccp.frame, true );
      }
    }

    else if( source == mKohonen ){
      SOMPanel sp = new SOMPanel( this,  getDefaultDisplayType() );
      sp.addListener( this );
      net_views.addElement( sp );
      net_views_count++;
      addInternalFrame( sp.frame, true );
    }
    else if( source == mLog ){
      pLog.frame.setVisible( true );
      try{ pLog.frame.setIcon( false ); }
      catch( Exception ex ){
        showException( "The status panel couldn't be deiconified" );
      }
      pLog.frame.toFront();
    }

    // Patterns menu:
    if(source == mAddPattern || source == mModifyPattern || source == mDeletePattern) {
      if( last_view != null && last_view.getLabelsToEdit() == NetworkViewSettings.ACT )
          last_view.evaluateDirectEdit( false );

      if(source == mAddPattern)
        try{ network.createPattern(); }
        catch( Exception ex ){ showException( ex ); }
      else if(source == mModifyPattern) network.modifyPattern();
      else
        try{ network.deletePattern(); }
        catch( Exception ex ) {
        showException( ex );
      }
    }

  // window menu:
    else if( source == mCascadeW ) cascadeWindows();

    else if( source == mCloseW ){
      for( int i=0; i<frames.length; i++ )
        frames[i].dispose();
    }

  // help menu:
    else if(source == mAbout) {
      JOptionPane.showMessageDialog(
        this,
        "<html><body><font face='Arial, Helvetica, sans-serif'><center>" +
          "<h1>Java Neural Network Simulator " + JavaNNS_VERSION + "</h1>" +
          "based on the <b>Stuttgart Neuronal Network Simulator 4.2</b> kernel<br>" +
          "<h2>Credits:<br><small>(in order of participation)</small></h2></center>" +
          "<b>SNNS:</b> Andreas Zell, G&uuml;nter Mamier, Michael Vogt, Niels Mache,<br>" +
          "Tilman Sommer, Ralf H&uuml;bner, Michael Schmalzl, Tobias Soyez, Sven<br>" +
          "D&ouml;ring, Dietmar Posselt, Kai-Uwe Herrmann, Artemis Hatzigeorgiou;<br>" +
          "<b>external contributions:</b> Martin Riedmiller, Heike Speckmann, Martin<br>" +
          "Reczko, Jamie DeCoster, Jochen Biederman, Joachim Danz, Christian<br>" +
          "Wehrfritz, Randolf Werner, Michael Berthold, Hans Rudolph<br>" +
          "<p><b>Java User Interface:</b> Igor Fischer, Fabian Hennecke, Christian Bannes<br>" +
          "<p>Copyright &copy; 1990-1995 IPVR, University of Stuttgart<br>" +
          "Copyright &copy; 1996-2002 WSI, University of T&uuml;bingen<br></font></body></html>",
        "About JavaNNS",
        JOptionPane.INFORMATION_MESSAGE
      );
    }
    else if(source == mContents) {
      try {
        if(applet != null) {
          URL helpURL = new URL(properties.getProperty(properties.USER_MANUAL_URL_KEY));
          applet.getAppletContext().showDocument(helpURL, "Help");
        }
        else {
          String[] command =  new String[]{
            properties.getProperty(properties.BROWSER_NAME_KEY),
            properties.getProperty(properties.USER_MANUAL_URL_KEY)};
          Runtime.getRuntime().exec( command );
        }
      }
      catch(Exception ex) { showException(ex); }
    }
  }

// implementing LoaderAndSaver:
  public String getLASName() { return "Configuration"; }
  public JPanel getAccessory(){ return null; }
  public boolean hasHomeFile(){ return ( configHomeFile != null );}

  public void save() throws IOException{
    if( hasHomeFile() ) save( configHomeFile);
    else throw new IOException("No home file defined");
  }

  public File getFile(){ return configHomeFile; }

  /**
   * returns the keyword of configuration files
   */
  public String getKeyword(){ return "configuration file" ; }

  /**
   * returns the usual file ending of the configuration files
   */
  public String getFileExtension(){ return "cfg"; }

  /**
   * returns the description of the configuration file type for the fileManager
   */
  public String getDescription(){ return "Configuration file *.cfg"; }

  /**
   * should save the configuration
   */
  public void save( File file ) throws IOException{
    String path = file.getCanonicalPath();
    if( !FileManager.getFileExtension( file ).equals("cfg") ) path += ".cfg";
    System.out.println( "not yet implemented." );
  }

  /**
   * removes the old configuration and loads a new one
   * with new network views
   */
  public void load( File file ) throws IOException{
    FileInputStream configFile = new FileInputStream( file );
    byte buffer[] = new byte [ configFile.available() ];
    configFile.read( buffer );

    LineReader configBuffer = new LineReader( buffer );
    UIConfig configuration = new UIConfig();
    configuration.readConfiguration( configBuffer );
    int display_count = configuration.displays;
    UIDisplayType dt;
    for(int i=0; i<display_count; i++) {
      dt = configuration.displayTypes[ i ];
      dt.displayNo = net_views.size() + 1;
      openNetworkView( dt );
    }
    configHomeFile = file;
  }

  public boolean contentChanged(){ return false; }

// implementing NetworkViewListener:
  /**
   * method receives messages from the network views
   *
   * @param network view event
   */
  public void networkViewChanged( NetworkViewEvent evt ){
    //System.out.println("Snns.networkViewChanged("+evt.getMessage()+")");
    NetworkView view = evt.getView();

    if( evt.id == NetworkViewEvent.VIEW_CLOSED ) {
      net_views.remove( view );
      if( view == last_view ) last_view = null;
    }
    else if( evt.id == NetworkViewEvent.VIEW_ACTIVATED ){
      if( !net_views.contains( view ) ) net_views.add( view );
      last_view = view;
    }
    updateTheMenu( evt );
  }

  public void cascadeWindows() {
    JInternalFrame[] frames = desktop.getAllFrames();
    if( frames.length == 0 ) return;
    JInternalFrame frame;
    int index = 0, delta = 0, i;
    for( i = 0; i<frames.length; i++ ){
      frame = frames[i];
      if( frame.isVisible() ){
        if( index == 0 )
          delta = frame.getContentPane().getLocationOnScreen().y
                - frame.getLocationOnScreen().y;
        frame.setLocation(index*delta, index*delta);
        frame.toFront();
        desktop.setSelectedFrame(frame);
        index++;
      }
    }
  }
}
