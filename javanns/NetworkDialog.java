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



import java.awt.FlowLayout ;
import java.awt.Dimension ;
import javax.swing.*;
import java.io.File ;
import java.io.IOException ;
import java.awt.event.* ;
import java.awt.Color ;
import java.awt.Container ;

public class NetworkDialog extends JDialog implements ActionListener{
  Network network;
  JButton bSave, bCancel;
  JTextField tName;
  JLabel lNetworkName, lError;
  JPanel dialog = new JPanel();

  
  public NetworkDialog(JFrame frame, String title, boolean modal) {
    super(frame, title, modal);
    try  {
      jbInit();
      pack();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public NetworkDialog( Snns snns ) {
    this( (JFrame)snns, "Network name", true);
    network = snns.network ;

    lNetworkName = new JLabel("Network name: ");
    dialog.add( lNetworkName );

    tName = new JTextField(12);

    File file = network.getFile();
    if( file != null ){
      String name = file.getName();
      int index = name.lastIndexOf(".");
      tName.setText( ( index == -1 )? name : name.substring(0, index)  );
    }
    dialog.add( tName );

    bCancel = new JButton("Cancel");
    bCancel.addActionListener(this);
    dialog.add(bCancel);

    bSave = new JButton("Save");
    bSave.addActionListener(this);
    dialog.add( bSave );

    lError = new JLabel("Couldn't get the Path");
    lError.setForeground( Color.red );
    dialog.add( lError );
    lError.setVisible( false );

    setSize( 200, 150);

    setVisible( true );
  }

  void jbInit() throws Exception {
    dialog.setLayout( new FlowLayout( FlowLayout.CENTER, 10, 15 ) );
    getContentPane().add( dialog );
  }
  public void actionPerformed(ActionEvent e){
    Object src = e.getSource();
    if(src == bSave)  {
      network.setName( tName.getText() );
      try{ network.save(); }
      catch( IOException e2 ){ System.out.println( e2.getMessage() ); }
    }
    else if(src == bCancel) dispose();
  }
}

