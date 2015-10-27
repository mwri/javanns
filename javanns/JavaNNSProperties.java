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

/**
 * Title: JavaNNSProperties.java
 * Description:  Properties object for the JavaNNS
 *               reads some information for the use of JavaNNS
 *               contains an editor
 * Copyright:    Copyright (c) 1998
 * Company:      WSI
 * @author       Fabian Hennecke
 * @version
 */

import java.util.Properties;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

public class JavaNNSProperties extends Properties implements ActionListener,
                                                             LoaderAndSaver{

  /**
   * JavaNNS properties file written in the home directory of the
   * user
   */
  public static final String PROPERTIES_FILENAME = "JavaNNS.properties";

  /**
   * the name of the properties file containig the default properties
   * inside the jar file
   */
  public static final String DEFAULTS_FILENAME = "Default.properties";

  /**
   * JavaNNS property key for book directory
   */
  public static final String SNN_BOOK_URL_KEY = "SNN book URL";

  /**
   * JavaNNS property key for browser
   */
  public static final String BROWSER_NAME_KEY = "Browser name";

  /**
   * JavaNNS property key for initial JavaNNS main window width
   */
  public static final String SCR_WIDTH_KEY = "Screen width";

  /**
   * JavaNNS property key for initial JavaNNS main window height
   */
  public static final String SCR_HEIGHT_KEY = "Screen height";

  /**
   * JavaNNS property key for initial JavaNNS main window height
   */
  public static final String USER_MANUAL_URL_KEY = "User Manual URL";

  /**
   * JavaNNS property key for initial JavaNNS main window height
   */
  public static final String LIBRARY_PATH_KEY = "SNNS_jkr library path";


  Snns snns;
  JComboBox cbProps;
  JTextField tfValue;
  JButton[] options;
  JOptionPane pane;
  JDialog dialog;
  Properties edit_props;
  private boolean content_changed, editable;
  private String old_key;

  public JavaNNSProperties(Snns snns) throws Exception{
    this.snns = snns;

    // first it tries to load the default properties file from the jar file
    // or from the server
    // if this is not possible, JavaNNS can not start
    if( !loadDefaults() ) throw
      new Exception("Couldn´t load the default properties.\nWrong release!");

    // then it tries to load the individual properties from the user home directory
    // if this isn´t possible, it makes a deep copy of the default properties
    if( !loadProperties() ) copyDefaults();

    // then it checks, whether it´s possible to save manipulated properties
    try{
      if( System.getSecurityManager() != null )
        System.getSecurityManager().checkWrite(PROPERTIES_FILENAME);
      editable = true;
    }
    catch(SecurityException ex){}
  }

  /**
   * method initiliazes the UI with combo box, text field, buttons...
   */
  void initUI(){
    cbProps = new JComboBox();
    cbProps.addActionListener(this);
    tfValue = new JTextField(35);

    Container c = new Container();
    c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));

    Container cRow = new Container();
    cRow.setLayout(new BoxLayout(cRow,BoxLayout.X_AXIS));
    JLabel lProp = new JLabel("Property", JLabel.LEFT);
    cRow.add(lProp);
    cRow.add(cbProps);
    cRow.add(Box.createHorizontalGlue());
    c.add(cRow);

    c.add(Box.createRigidArea(new Dimension(0,8)));

    cRow = new Container();
    cRow.setLayout(new BoxLayout(cRow,BoxLayout.X_AXIS));
    JLabel lValue = new JLabel("Value", JLabel.LEFT);
    Dimension dLabel = lValue.getPreferredSize();
    dLabel.width *= 2;
    lProp.setPreferredSize(dLabel);
    lValue.setPreferredSize(dLabel);
    cRow.add(lValue);
    cRow.add(tfValue);

    c.add(cRow);

    options = new JButton[3];
    options[0] = new JButton("Default");
    options[0].setToolTipText("Set default property value");
    options[0].setMnemonic(KeyEvent.VK_D);
    options[0].addActionListener(this);

    options[1] = new JButton("Ok");
    options[1].setToolTipText("Set property values");
    options[1].setMnemonic(KeyEvent.VK_O);
    options[1].addActionListener(this);

    options[2] = new JButton("Cancel");
    options[2].setToolTipText("Cancel changes");
    options[2].setMnemonic(KeyEvent.VK_C);
    options[2].addActionListener(this);

    pane = new JOptionPane(c, JOptionPane.INFORMATION_MESSAGE,
      JOptionPane.YES_NO_CANCEL_OPTION, null, options);
  }

  /**
   * method returns the default value to the given key
   */
  public String getDefaultValue(String key){
    return defaults.getProperty(key);
  }

  /**
   * method returns true when it should be possible to save changes in the settings
   * to the home directory of the user
   */
  public boolean editable(){ return editable; }

  /**
   * method sets the editor dialog visible
   * it updates the key entries in the combo box and creates the dialog
   */
  public void edit(){
    if( pane == null ) initUI();

    edit_props = (Properties)clone();
    cbProps.removeAllItems();

    // alphabetisch sortieren:
    TreeSet ts = new TreeSet(String.CASE_INSENSITIVE_ORDER);
    ts.addAll(keySet());

    Iterator it = ts.iterator();
    while(it.hasNext()) cbProps.addItem(it.next());

    old_key = (String)cbProps.getItemAt(0);
    tfValue.setText(edit_props.getProperty(old_key));

    dialog = pane.createDialog(snns,"Properties editor");
    dialog.setVisible(true);
  }

  /**
   * method checks an action on the GUI
   * when the combo box item changed, it sets the new value text to the text field
   * when the default button was clicked it sets - if possible - the default value
   * to the text field
   * when the ok button was pressed, it puts the edited values to the properties
   * and closes the dialog
   * and when the cancel button was pressed, the dialog is simply closed
   */
  public void actionPerformed(ActionEvent evt){
    Object src = evt.getSource();

    if( src == cbProps ){
      if( old_key != null ){
        input2edited();
        updatetfValue();
      }
    }

    else if( src == options[0] ){
      String def_val = defaults.getProperty((String)cbProps.getSelectedItem());
      if( def_val != null ) tfValue.setText(def_val);
    }

    else if( src == options[1] ){
      input2edited();
      edited2props();
      close();
    }

    else if( src == options[2] ) close();
  }

  /**
   * method sets the currently edited value to the choosen key
   */
  private void input2edited(){
    edit_props.setProperty(old_key,tfValue.getText());
  }

  /**
   * method updates the text in the text field for the values to
   * the curently selected key of the combo box
   */
  private void updatetfValue(){
    Object o = cbProps.getSelectedItem();
    if( o != null ) {
      old_key = (String)o;
      tfValue.setText(edit_props.getProperty(old_key));
    }
  }

  /**
   * method sets the edited values to the properties
   */
  private void edited2props(){
    Enumeration keys = edit_props.keys();
    while(keys.hasMoreElements()){
      String key = (String)keys.nextElement();
      setProperty(key,edit_props.getProperty(key));
    }
  }

  private void close(){
    old_key = null;
    edit_props = null;
    dialog.setVisible(false);
  }

  /**
   * this method sets the given value to the given key in the properties of JavaNNS
   * if the new value was different from the old one, the flag content_changed is
   * set to true, so the user will be asked if the properties should be saved when
   * he/she finishs working with JavaNNS
   */
  public synchronized Object setProperty(String key, String value){
    String old_val = (String)getProperty(key);
    if( old_val == null || !old_val.equals(value) )
      content_changed = true;
    return super.setProperty(key, value);
  }

  /**
   * some necessary methods for implementing LoaderAndSaver
   */
  public String getLASName(){ return "JavaNNS Properties"; }

  public String getKeyword(){ return "JavaNNS Properties File"; }

  public String getFileExtension(){ return "prp"; }

  public String getDescription(){ return "JavaNNS Properties files *.prp"; }

  public JPanel getAccessory(){ return null; }

  public boolean contentChanged(){ return content_changed; }

  public boolean hasHomeFile(){ return true; }

  public void save() throws IOException{
    String path = System.getProperty("user.home")
                  + File.separator
                  + PROPERTIES_FILENAME;
    FileOutputStream os = new FileOutputStream(path);
    store(os, getKeyword());
    os.close();
    content_changed = false;
  }

  public void save( File file ) throws Exception{
    throw new Exception("Properties may only be saved to their original file");
  }

  public void load( File file ) throws Exception{
    throw new Exception("Properties already loaded");
  }

  public File getFile(){
    return new File(PROPERTIES_FILENAME);
  }

  /**
   * the default properties have to be set in the jar file or have been
   * loaded from the server
   * if they can not be found the program cannot run
   *
   * @return method returns <code>true|/code> when the default
   *         properties file could be loaded
   */
  private boolean loadDefaults(){
    defaults = new Properties();
    ClassLoader cl = getClass().getClassLoader();
    InputStream is = cl.getResourceAsStream(DEFAULTS_FILENAME);
    if(is == null) return false;
    try{
      defaults.load(is);
      is.close();
    }
    catch(IOException ex){ return false; }
    return true;
  }

  /**
   * to load the individual properties file is optional
   *
   * @return method returns <code>true|/code> when the individual
   *         properties file could be loaded
   */
  private boolean loadProperties(){
    String path = System.getProperty("user.home")
                  + File.separator
                  + PROPERTIES_FILENAME;
    File prps = new File(path);
    if( prps.exists() ){
      try{
        FileInputStream is = new FileInputStream(prps);
        load(is);
        is.close();
        return true;
      }
      catch(IOException ex){ return false; }
    }
    return false;
  }

  private void copyDefaults(){
    Enumeration keyEnum = defaults.keys();
    while( keyEnum.hasMoreElements() ){
      String key = (String)keyEnum.nextElement();
      setProperty(key, defaults.getProperty(key));
    }
  }
}
