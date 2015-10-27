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

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.io.* ;
import java.util.Date ;

// drucken:
import wsi.ra.print.TextPrinter ;
import java.awt.print.* ;
import java.util.Vector ;

/**
 * class LogPanel is a simple editor which is used by SNNS to show the status of
 * the net or the training success and makes it possible to save such results
 */
public class LogPanel extends JPanel implements LoaderAndSaver,
                                                ActionListener,
                                                Printable{
  Snns snns;
  JInternalFrame frame;
  JScrollPane spArea;
  WriteArea writer;
  JButton bClear, bClose, bState;
  File home_file = null;
  String file_title = "JavaNNS Log File";
  private boolean is_log_file = true;
  private TextPrinter printer;
  Object lock_key;

  public LogPanel( Snns snns ) {
    this.snns = snns;

    LogLayout ll = new LogLayout( this, snns.panel_insets, 6, 6 );
    setLayout( ll );

    writer = new WriteArea(30, 40, false);
    writer.tArea.addKeyListener(
      new KeyAdapter(){
        public void keyTyped( KeyEvent evt ){ writer.content_changed = true; }
      }
    );

    printer = new TextPrinter();
    printer.setLineWrap( true );

/*    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);
    gbc.insets = snns.panel_insets;

    gbc.gridx = 0;
    gbc.gridy = 0;

    gbc.fill = gbc.BOTH;*/
    spArea = new JScrollPane( writer.tArea );
//    gbl.setConstraints(spArea,gbc);
    add( spArea );
    spArea.setLocation( 0, 0 );

/*    Container cButtons = new Container();
    cButtons.setLayout(new BoxLayout(cButtons,BoxLayout.X_AXIS));

    int dx = 6;

    cButtons.add(Box.createHorizontalGlue());*/

    bClear = new JButton( "Clear" );
    bClear.addActionListener( this );
    add( bClear );
/*    cButtons.add( bClear );

    cButtons.add(Box.createRigidArea(new Dimension(dx,0)));*/

    bState = new JButton( "State" );
    bState.addActionListener( this );
    add( bState );
/*    cButtons.add( bState );

    cButtons.add(Box.createRigidArea(new Dimension(dx,0)));*/

    bClose = new JButton( "Close" );
    bClose.addActionListener( this );
    add( bClose );
/*    cButtons.add( bClose );

    gbc.gridy++;
    gbc.fill = gbc.HORIZONTAL;
    gbl.setConstraints(cButtons, gbc);
    add( cButtons );*/

    frame = new JInternalFrame("Log", true, true, true, true ){
      public void dispose(){ setVisible(false); } // necessary cause Snns calls the
                                                  // dispose method of the frames
                                                  // when all frames are closed
    };
    frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    frame.setContentPane( this );
    frame.pack();

    writer.tArea.append("opened at: " + new Date() + "\n" );
  }

  /**
   * appends a line in the editor
   * appends the given <code>String</code> to the editor and jumps to the next
   * line
   *
   * text the next line
   */
  public void append( String text ){ append( text, true ); }


  /**
   * appends a line in the editor
   *
   * @param text the next line
   * @param new_line if the Loganel should jump to the next line
   */
  public void append( String text, boolean new_line ){
    if( lock_key != null ) return;
    append_0( text, new_line );
  }

  private void append_0( String text, boolean new_line ){
    if( frame.isVisible() ) {
      writer.tArea.append( text );
      if( new_line ) writer.tArea.append( "\n" );
      writer.content_changed = true;
      int areaH = writer.tArea.getPreferredSize().height,
          viewH = spArea.getViewport().getExtentSize().height;
      if( viewH < areaH )
        spArea.getViewport().setViewPosition( new Point(0, areaH - viewH ) );
    }
  }

  public void append( String text, Object key ){
    append( text, true, key );
  }

  public void append( String text, boolean new_line, Object key ){
    if( lock_key == null || lock_key == key ) append_0( text, new_line );
  }

  public void enableOnly(Object key){
    if( lock_key != null ){
      if( lock_key != key ) throw
        new IllegalArgumentException("LogPanel is already locked by: "+lock_key);
      else return;
    }
    else lock_key = key;
  }

  public void release(Object key){
    if( lock_key == null ) return;
    if( lock_key != key ) throw
      new IllegalArgumentException("LogPanel cannot be released by another"
      +"\nobject than "+lock_key );
    else lock_key = null;
  }

/*------------------------- interfaces ---------------------------------------*/

// implementing LoaderAndSaver:

  public String getLASName(){ return "Log file"; }

  public String getKeyword(){ return file_title; }

  public String getFileExtension(){ return "log"; }

  public String getDescription(){
    return "JavaNNS log file *.log";
  }

  public JPanel getAccessory(){ return null;
  }

  public boolean hasHomeFile(){
    return (home_file != null);
  }

  public void save() throws IOException{
    if( home_file == null ) return;
    FileWriter fw = new FileWriter( home_file );
    if( is_log_file ){
      append( "saved at: " + new Date() );
      if( writer.tArea.getText().indexOf( file_title ) == -1 )
        fw.write( file_title + "\n" );
    }
    writer.tArea.write( fw );
    setTitle();
  }

  public void save( File file ) throws Exception{
    File old = home_file;
    home_file = file;
    try{ save(); }
    catch( IOException e ){
      home_file = old;
      setTitle();
      throw e;
    }
  }

  public void load( File file ) throws Exception{
    if( lock_key != null ) throw
      new Exception("LogPanel is locked by :"+lock_key);
    if( !file.exists() ) throw
      new Exception("File "+file.getName()+" doesn't exist");
    if( !writer.content_changed || clear() ){
      FileReader reader = new FileReader( file );
      writer.tArea.read( reader, null );
      home_file = file;
      if( !frame.isVisible() ) frame.setVisible( true );
      if( frame.isIcon() ) frame.setIcon( false );
      if( writer.tArea.getText().indexOf( file_title ) == -1 ) is_log_file = false;
      else writer.tArea.append( "opened at: " + new Date() + "\n" );
      setTitle();
    }
  }

  public boolean contentChanged(){ return writer.content_changed; }

  public File getFile(){
    return home_file;
  }

// implementing ActionListener:

  public void actionPerformed( ActionEvent evt ){
    Object src = evt.getSource();

    if( src == bClose ) frame.setVisible( false );

    else if( src == bState ) snns.network.showState();

    else if( src == bClear ) clear();
  }

  // implementing Printable:
  public int print(Graphics g, PageFormat pf, int pi){
    if( printer.isFinished() ) printer.setText( writer.tArea.getText() );
    return printer.print( g, pf, pi );
  }

/*-------------------- private methods ---------------------------------------*/

  private boolean clear(){
    //boolean really = true;
    //if( writer.content_changed ) really = snns.askForSaving(this);
    //if( really ){
      writer.content_changed = false;
      writer.tArea.setText("");
      is_log_file = true;
      home_file = null;
      setTitle();
      writer.tArea.append( "opened at: " + new Date() + "\n");
    //}
    //return really;
    return true;
  }

  private void setTitle(){
    String title = "Log";
    if( !is_log_file ) title = "No log file";
    if( home_file != null ) title += " - " + home_file.getName();
    frame.setTitle( title );
    frame.repaint();
  }
}

