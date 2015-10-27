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
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

/**
   * DLLPathDialog opens an JDialog when JavaNNS couldn´t find the library.
   * This may be a result, that there exists no such library, or it is not
   * in one of the systems library paths.
   * This dialog enables the user to specify where he wants to write the
   * library to.
   */
class DLLPathDialog extends JDialog implements ActionListener{
    boolean ok = false;
    JTextArea taExpl;
    JTextField tfPath;
    JButton bOk, bBrowse;
    static String libName;

    javax.swing.filechooser.FileFilter ffDirLib = new javax.swing.filechooser.FileFilter(){
      public boolean accept(File f){
        if( f.isDirectory() ) return true;
        // falls in dem Ordner ein Dateiname auftaucht, welcher den dll-Namen enthaelt,
        // wird das File angezeigt
        String name = f.getName();
        if(name.equals(libName)) return true;
        return false;
      }
      public String getDescription() { return "Directories & " + libName; }
    };


    public DLLPathDialog(Snns snns, String dll_path, String libName){
      super(snns,"Installing library", true);
      this.libName = libName;

      Container c = getContentPane();
      GridBagLayout gbl = new GridBagLayout();
      c.setLayout(gbl);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(6, 5, 6, 5);

      String text =
          "<html><head><title>Installing Library</title></head><body>"
        + "<h1><font face='Arial, Helvetica, sans-serif'>Installing "
        + "Library</font></h1>"
        + "<p><font face='Arial, Helvetica, sans-serif'>"
        + "JavaNNS needs the library <b><code>" + libName + "</code></b> "
        + "in order to work properly. "
        + "The library will now be installed into the directory specified "
        + "below. This is by default your current working directory, but "
        + "you are free to choose any other directory instead. It is of "
        + "an advantage if the library is installed in the system library "
        + "path, especially if you share this machine with other users. "
        + "</font></p><p><font face='Arial, Helvetica, sans-serif'>"
        + "JavaNNS remembers the location of the library by storing it "
        + "in the file <b><code>JavaNNS.properties</code></b>, which is "
        + "generated and placed "
        + "into your home directory. If you wish to change properties, "
        + "use the <b><i>Properties</i></b> editor in the JavaNNS "
        + "<b><i>View</i></b> menu. "
        + "</font></p><p><font face='Arial, Helvetica, sans-serif'>"
        + "If JavaNNS has been run previously on your machine, it is "
        + "possible that the library already exists. In that case, "
        + "the path below shows where it was last found. If you know "
        + "where the library "
        + "is placed, you can enter the path to it into the text field "
        + "below or browse into the directory containing it. This will "
        + "prevent JavaNNS from installing another copy of the same file. "
        + "</font></p><p><font face='Arial, Helvetica, sans-serif'>"
        + "You can delete the library or the "
        + "<b><code>JavaNNS.properties</code></b> file any time you want. "
        + "In that case, this dialog will appear again the next time you "
        + "start JavaNNS.</font></body></html>";

      JEditorPane epExpl = new JEditorPane("text/html", text) {
        public Dimension getPreferredScrollableViewportSize() {
          return new Dimension(0, 320);
        }
      };
      epExpl.setEditable(false);
      epExpl.setBackground(getBackground());
      JScrollPane spExpl = new JScrollPane(epExpl);
      gbc.gridwidth = 3;
      gbc.fill = gbc.HORIZONTAL;
      gbl.setConstraints(spExpl,gbc);
      c.add(spExpl);

      JLabel lPath = new JLabel("Library Path: ");
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.fill = gbc.NONE;
      gbl.setConstraints(lPath, gbc);
      c.add(lPath);

      if(dll_path == null) dll_path = System.getProperty("user.dir", "");
      tfPath = new JTextField(dll_path, 30);
      gbc.gridx = 1;
      gbl.setConstraints(tfPath,gbc);
      c.add(tfPath);

      bBrowse = new JButton("Browse...");
      bBrowse.setMnemonic(KeyEvent.VK_B);
      bBrowse.addActionListener(this);
      gbc.anchor = gbc.EAST;
      gbc.gridx = 2;
      gbl.setConstraints(bBrowse,gbc);
      c.add(bBrowse);

      Dimension path_dim = tfPath.getPreferredSize(),
                search_dim = bBrowse.getPreferredSize();
      path_dim.height = search_dim.height;
      tfPath.setPreferredSize(path_dim);

      gbc.gridy = 2;
      bOk = new JButton("OK");
      bOk.setMnemonic(KeyEvent.VK_O);
      bOk.addActionListener(this);
      bOk.setPreferredSize(search_dim);
      gbl.setConstraints(bOk,gbc);
      c.add(bOk);

      pack();
      setResizable(false);
      Dimension sd = Toolkit.getDefaultToolkit().getScreenSize(),
                dd = getPreferredSize();
      setLocation((sd.width-dd.width)/2,(sd.height-dd.height)/2);
      setVisible(true);
    }


    public void actionPerformed(ActionEvent evt){
      Object src = evt.getSource();
      if( src == bOk ) {ok = true; setVisible(false);}
      else {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir",""));
        fc.setDialogType(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(ffDirLib);
        if( fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ){
          File f = fc.getSelectedFile();
          tfPath.setText(f.getParentFile().getPath());
          fc.setVisible(false);
        }
      }
    }

    public String getPath(){ return tfPath.getText(); }
  }