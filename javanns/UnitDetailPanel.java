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

import java.util.Vector ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.awt.* ;
import java.awt.event.* ;


/*-------------------------- class declaration -------------------------------*/

class UnitDetailPanel extends JPanel implements ActionListener, NetworkListener{
  private Snns snns;
  private Network network;
  private JTextField tName, tAct, tInitAct, tBias, tSubnet, tLayer, tNumbers;
  private NamedComboBox cbxType, cbxActFn, cbxOutFn;
  private JButton bApply, bCancel;
  JInternalFrame frame = null;
  private boolean listen = false, diff_set = false;
  private Unit[] units;
  private static final String DIFF = "different";

  public UnitDetailPanel( Snns snns, boolean own_frame ) {
    this.snns = snns;
    network = snns.network;
    Functions functions = snns.functions;
    if( own_frame ) network.addListener( this );

    GridBagLayout gbl = new GridBagLayout();
    setLayout( gbl );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = snns.panel_insets;

    gbc.gridx = gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.anchor = gbc.WEST;

    if( own_frame ){
      JLabel lNumbers = new JLabel("Numbers: ");
      gbl.setConstraints( lNumbers, gbc);
      add( lNumbers );

      gbc.gridx = 2;
      gbc.fill = gbc.HORIZONTAL;
      tNumbers = new JTextField( 10 );
      gbl.setConstraints( tNumbers, gbc);
      add( tNumbers );

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.fill = gbc.NONE;

      JLabel lName = new JLabel("Names: ");
      gbl.setConstraints( lName, gbc);
      add( lName );

      gbc.gridx = 2;
      gbc.fill = gbc.HORIZONTAL;
      tName = new JTextField( 10 );
      gbl.setConstraints( tName, gbc);
      add( tName );

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.fill = gbc.NONE;
    }

    JLabel lType = new JLabel("Unit type: ");
    gbl.setConstraints( lType, gbc);
    add( lType );

    gbc.gridx = 2;
    gbc.fill = gbc.HORIZONTAL;
    cbxType = new NamedComboBox();
    cbxType.addItems( UnitTTypes.getTypes() );
    cbxType.setSelectedIndex( 0 );
    gbl.setConstraints( cbxType, gbc);
    add( cbxType );

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.fill = gbc.NONE;
    JLabel lActFn = new JLabel("Activation function: ");
    gbl.setConstraints( lActFn, gbc);
    add( lActFn );

    gbc.gridx = 2;
    gbc.fill = gbc.HORIZONTAL;
    cbxActFn = new NamedComboBox();
    cbxActFn.addItems( functions.getFunctionsOfType( Function.ACTIVATION_FN ) );
    cbxActFn.setSelectedIndex( 0 );
    gbl.setConstraints( cbxActFn, gbc);
    add( cbxActFn );

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.fill = gbc.NONE;
    JLabel lOutFn = new JLabel("Output function: ");
    gbl.setConstraints( lOutFn, gbc);
    add( lOutFn );

    gbc.gridx = 2;
    gbc.fill = gbc.HORIZONTAL;
    cbxOutFn = new NamedComboBox();
    cbxOutFn.addItems( functions.getFunctionsOfType( Function.OUTPUT_FN ) );
    cbxOutFn.setSelectedIndex( 0 );
    gbl.setConstraints( cbxOutFn, gbc);
    add( cbxOutFn );


    if( own_frame ){
      gbc.gridy++;
      gbc.gridx = 0;
      gbc.fill = gbc.NONE;
      JLabel lAct = new JLabel("Activation: ");
      gbl.setConstraints( lAct, gbc);
      add( lAct );

      gbc.gridx = 2;
      gbc.fill = gbc.HORIZONTAL;
      tAct = new JTextField( "0.0", 10 );
      gbl.setConstraints( tAct, gbc);
      add( tAct );

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.fill = gbc.NONE;
      JLabel lInitAct = new JLabel("Initial activation: ");
      gbl.setConstraints( lInitAct, gbc);
      add( lInitAct );

      gbc.gridx = 2;
      gbc.fill = gbc.HORIZONTAL;
      tInitAct = new JTextField( "0.0", 10 );
      gbl.setConstraints( tInitAct, gbc);
      add( tInitAct );

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.fill = gbc.NONE;
      JLabel lBias = new JLabel("Bias: ");
      gbl.setConstraints( lBias, gbc);
      add( lBias );

      gbc.gridx = 2;
      gbc.fill = gbc.HORIZONTAL;
      tBias = new JTextField( "0.0", 10 );
      gbl.setConstraints( tBias, gbc);
      add( tBias );
    }

    gbc.gridwidth = 1;

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.fill = gbc.NONE;
    gbc.anchor = gbc.EAST;
    JLabel lLayer = new JLabel("Layer number: ");
    gbl.setConstraints( lLayer, gbc);
    add( lLayer );

    gbc.gridx = 1;
    tLayer = new JTextField( String.valueOf( network.getMaxLayerNo() + 1 ), 4 );
    gbl.setConstraints( tLayer, gbc);
    add( tLayer );

    gbc.gridx = 2;
    gbc.anchor = gbc.EAST;
    JLabel lSubnet = new JLabel("Subnet number: ");
    gbl.setConstraints( lSubnet, gbc);
    add( lSubnet );

    gbc.gridx = 3;
    gbc.anchor = gbc.WEST;
    tSubnet = new JTextField( "0", 4 );
    gbl.setConstraints( tSubnet, gbc);
    add( tSubnet );

    gbc.gridwidth = 2;
    gbc.anchor = gbc.CENTER;
    if( own_frame ){
      gbc.gridy++;
      gbc.gridx = 0;
      bApply = new JButton("Apply");
      bApply.addActionListener( this );
      gbl.setConstraints( bApply, gbc);
      add( bApply );

      gbc.gridx = 2;
      bCancel = new JButton("Close");
      bCancel.addActionListener( this );
      gbl.setConstraints( bCancel, gbc);
      add( bCancel );
    }

    if( own_frame ){
      frame = new JInternalFrame("Edit units", false, false, false, false){
        public void dispose(){ close(); }
      };
      frame.addInternalFrameListener(
        new InternalFrameAdapter(){
          public void internalFrameClosed(InternalFrameEvent e) { close(); }
        }
      );
      frame.setContentPane( this );
      frame.pack();
      frame.setVisible( false );
    }
  }

/*-------------------------- public methods ----------------------------------*/
  /**
   * shows the unit details to the currently selected unit
   * only to use, when the panel was constructed with own frame
   *
   * @return <code>true<\code> if it was possible
   */
  public boolean showDetails(){
    Unit[] units = network.getSelectedUnits();
    return showDetails( units );
  }
  /**
   * shows the unit details to certain units
   * only to use, when panel was constructed with own frame
   *
   * @return <code>true<\code> if it was possible
   */
  public boolean showDetails( Unit[] us ){
    if( us.length < 1 ) return false;
    units = us;
    listen = true;

    String num = String.valueOf( units[0].getNumber() );
    for( int i=1; i<units.length; i++ )
      num += ";" + String.valueOf( units[i].getNumber() );
    tNumbers.setText( num );

    if( units.length == 1 ){
      Unit unit = units[0];
      tName.setText( unit.getName() );
      cbxType.setSelectedIndex( unit.getType() );
      cbxActFn.setSelectedItem( unit.getActFnName() );
      cbxOutFn.setSelectedItem( unit.getOutFnName() );
      tAct.setText( String.valueOf( unit.getActivation() ) );
      tInitAct.setText( String.valueOf( unit.getInitAct() ) );
      tBias.setText( String.valueOf( unit.getBias() ) );
      tLayer.setText( String.valueOf( unit.getLayer() ) );
      tSubnet.setText( String.valueOf( unit.getSubnetNo() ) );

    }
    else {
      if( !diff_set ){
        cbxType.addItem( DIFF, null );
        cbxActFn.addItem( DIFF, null );
        cbxOutFn.addItem( DIFF, null );
        diff_set = true;
      }
      boolean dname = false, dtype = false, dactfn = false, doutfn = false,
              dact = false, dinit = false, dbias = false, dlayer = false,
              dsubnet = false;
      UnitData data = new UnitData( units[0] );

      for( int i=1; i<units.length; i++ ){
        UnitData comp = new UnitData( units[i] );
        dname = data.name.equals( comp.name )? dname : true;
        dtype = (data.type == comp.type)? dtype : true;
        dactfn = data.act_fn_name.equals( comp.act_fn_name )? dactfn : true;
        doutfn = data.out_fn_name.equals( comp.out_fn_name )? doutfn : true;
        dact = (data.act == comp.act)? dact : true;
        dinit = (data.init_act == comp.init_act)? dinit : true;
        dbias = (data.bias == comp.bias)? dbias : true;
        dlayer = (data.layer == comp.layer)? dlayer : true;
        dsubnet = (data.subnet == comp.subnet)? dsubnet : true;
      }

      if( dname ) tName.setText( DIFF );
      else tName.setText( data.name );

      if( dtype ) cbxType.setSelectedItem( DIFF );
      else cbxType.setSelectedIndex( data.type );

      if( dactfn ) cbxActFn.setSelectedItem( DIFF );
      else cbxActFn.setSelectedItem( data.act_fn_name );

      if( doutfn ) cbxOutFn.setSelectedItem( DIFF );
      else cbxOutFn.setSelectedItem( data.out_fn_name );

      if( dact ) tAct.setText( DIFF );
      else tAct.setText( String.valueOf( data.act ) );

      if( dinit ) tInitAct.setText( DIFF );
      else tInitAct.setText( String.valueOf( data.init_act ) );

      if( dbias ) tBias.setText( DIFF );
      else tBias.setText( String.valueOf( data.bias ) );

      if( dlayer ) tLayer.setText( DIFF );
      else tLayer.setText( String.valueOf( data.layer ) );

      if( dsubnet ) tSubnet.setText( DIFF );
      else tSubnet.setText( String.valueOf( data.subnet ) );
    }

    frame.setVisible( true );
    return true;
  }

  /**
   * method returns the name of the unit
   *
   * @return the name
   */
  public String getName(){
    String name = tName.getText();
    if( name.equals("") ) name = "noName";
    return name;
  }
  /**
   * method returns the topological unit type
   *
   * @see UnitTTypes
   * @return the int id of the type
   */
  public int getType(){
    return ( (UnitTType)cbxType.getSelectedObject() ).getNumber();
  }
  /**
   * method returns the current activation of the unit
   *
   * @return the activation
   */
  public double getActivation() throws Exception{
    if( tAct == null ) return Unit.DEFAULT_ACT;
    double act;
    try{ act = Double.valueOf( tAct.getText() ).doubleValue(); }
    catch(Exception e){ throw new Exception( "Activation has to be a double value" ); }
    return act;
  }
  /**
   * method returns the name of the unit activation function
   *
   * @return the name of the activation function
   */
  public String getActFnName(){
    Function f = (Function)cbxActFn.getSelectedObject();
    return f.kernel_name;
  }

  /**
   * method returns the name of the unit output function
   *
   * @return the name of the output function
   */
  public String getOutFnName(){
    Function f = (Function)cbxOutFn.getSelectedObject();
    return f.kernel_name;
  }
  /**
   * method returns the initial activation value of the unit
   *
   * @return initial activation
   */
  public double getInitAct() throws Exception{
    if( tInitAct == null ) return Unit.DEFAULT_INIT_ACT ;
    double init_act;
    try{ init_act = Double.valueOf( tInitAct.getText() ).doubleValue(); }
    catch(Exception e){ throw new Exception( "Initial activation hes to be an double value"); }
    return init_act;
  }

  /**
   * method returns the bias value of the unit
   *
   * @return the bias value
   */
  public double getBias() throws Exception{
    if( tBias == null ) return Unit.DEFAULT_BIAS;
    double bias;
    try{ bias = Double.valueOf( tBias.getText() ).doubleValue(); }
    catch(Exception e){ throw new Exception( "Bias has to be a double value"); }
    return bias;
  }

  /**
   * method sets the number in the layer number text field
   *
   * @param no the layer number
   */
  public void setLayer( int no ){
    tLayer.setText(String.valueOf(no));
  }

  /**
   * method returns the number of the layer the unit lies in
   *
   * @return the numberof the layer
   */
  public int getLayer() throws Exception{
    int l;
    try{ l = Integer.parseInt( tLayer.getText() ); }
    catch(Exception e){ throw new Exception( "Layer number must be an integer value" ); };
    if( l < 1 ) throw new Exception( "Layer number must be bigger than 1");
    return l;
  }
  /**
   * method returns the number of the subnet the unit lies in
   *
   * @return the number of the subnet
   */
  public int getSubnet() throws Exception{
    int s;
    try{ s = Integer.parseInt( tSubnet.getText() ); }
    catch(Exception e){ throw new Exception( "Subnet number has to be an integer value" ); }
    return s;
  }
  /**
   * method returns the number of the subnet the unit lies in
   *
   * @return the number of the subnet
   */
  public Vector getNumbers() throws Exception{
    String num = tNumbers.getText();
    int i = num.indexOf(";");
    Vector nos = new Vector();
    try{
      while( i > -1 ){
        String no = num.substring(0, i);
        nos.add( new Integer( no.trim() ) );
        num = num.substring( i + 1 );
        i = num.indexOf(";");
      }
      if( num.length() > 0 ) nos.add( new Integer( num.trim() ) );
    }
    catch( Exception e ){
      throw new Exception("Numbers of units have to be separated by ;");
    }
    return nos;
  }
  /**
   * method sets the network multi selctionable again
   * and hides the frame
   */
  public void close(){
    listen = false;
    frame.setVisible( false );
    if( diff_set ){
      cbxType.removeLastItem();
      cbxActFn.removeLastItem();
      cbxOutFn.removeLastItem();
      diff_set = false;
    }
  }

/*------------------------ interfaces ----------------------------------------*/
// implementing NetworkListener :
  public void networkChanged(NetworkEvent evt){
    if( listen ){
      switch(evt.id){
        case NetworkEvent.UNIT_VALUES_EDITED    :
        case NetworkEvent.SELECTED_UNITS_CHANGED:
          if( frame == null ){
            tLayer.setText( String.valueOf( ( network.getMaxLayerNo() + 1 ) ) );
          }
          break;
        case NetworkEvent.NETWORK_DELETED       :
        case NetworkEvent.NEW_NETWORK_LOADED    : close(); break;
        case NetworkEvent.UNITS_CREATED         :
        case NetworkEvent.UNITS_DELETED         :
          if( !showDetails() ) close();
          else if( frame == null ){
            tLayer.setText( String.valueOf( ( network.getMaxLayerNo() + 1 ) ) );
          }
      }
    }
    /*int type = evt.id;
    if( frame == null ){
      tLayer.setText( String.valueOf( ( network.getMaxLayerNo() + 1 ) ) );
    }
    else if( listen ){
      if( type == NetworkEvent.NEW_NETWORK_LOADED ||
          type == NetworkEvent.NETWORK_DELETED      )
        close();
      else if( type == NetworkEvent.SELECTED_UNITS_CHANGED )
        showDetails();
      else if( type == NetworkEvent.UNITS_CREATED ||
               type == NetworkEvent.UNITS_DELETED   )
        if( !showDetails() ) close();
    }*/
  }

// implementing ActionListener :
  public void actionPerformed(ActionEvent evt){
    Object src = evt.getSource();
    if( src == bApply ){
      boolean nname, ntype, nactfn, noutfn, nact, nbias, ninit, nlayer, nsubnet;
      nname = !getName().equals( DIFF );
      ntype = !cbxType.getSelectedItem().equals( DIFF );
      nactfn = !cbxActFn.getSelectedItem().equals( DIFF );
      noutfn = !cbxOutFn.getSelectedItem().equals( DIFF );
      nact = !tAct.getText().equals( DIFF );
      nbias = !tBias.getText().equals( DIFF );
      ninit = !tInitAct.getText().equals( DIFF );
      nlayer = !tLayer.getText().equals( DIFF );
      nsubnet = !tSubnet.getText().equals( DIFF );

      String name = "", actFn = "", outFn = "";
      int type = 0, layer = 0, subnet = 0;
      double act = 0, init = 0, bias = 0;
      Vector nos;
      try{
        if( nlayer ) layer = getLayer();
        if( nsubnet ) subnet = getSubnet();
        if( nact ) act = getActivation();
        if( ninit ) init = getInitAct();
        if( nbias ) bias = getBias();
        nos = getNumbers();
      }
      catch( Exception e ){ snns.showException( e, this ); return; }
      if( nname ) name = getName();
      if( ntype ) type = getType();
      if( nactfn ) actFn = getActFnName();
      if( noutfn ) outFn = getOutFnName();

      units = new Unit[ nos.size() ];
      for( int i=0; i<nos.size(); i++ ){
        units[i] = network.getUnitNumber( ((Integer)nos.get(i)).intValue() );
        if( units[i] == null ){
          snns.showException( new Exception("Wrong unit numbers"), this );
          return;
        }
      }
      for( int i=0; i<units.length; i++ ){
        Unit unit = units[i];
        if( nname ) unit.setName( name );
        if( ntype ) unit.setType( type );
        if( nact ) unit.setActivation( act );
        if( nactfn ) unit.setActFnName( actFn );
        if( noutfn ) unit.setOutFnName( outFn );
        if( nbias ) unit.setBias( bias );
        if( ninit ) unit.setInitAct( init );
        if( nlayer ) unit.setLayer( layer );
        if( nsubnet ) unit.setSubnetNo( subnet );
      }
      network.fireEvent( NetworkEvent.UNIT_VALUES_EDITED, units );
    }
    else if( src == bCancel ) close();
  }
}
