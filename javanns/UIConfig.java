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
import java.math.*;
import java.awt.*;
import java.io.*;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * Parses the whole SNNS network configuration file.
 *
 */

class UIConfig {

  static final int CURRENT_FORMAT      = 6;
  static final int UI_NO_LEARN_PARAMS  = 5;
  static final int UI_NO_UPDATE_PARAMS = 5;
  static final int UI_NO_INIT_PARAMS   = 5;

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/

 	public int                   format;
 	public String                ui_pathname;
 	public String                ui_filenameNET;
 	public String                ui_filenamePAT;
 	public String                ui_filenameRES;
 	public String                ui_filenameCFG;
 	public String                ui_filenameTXT;
  public int                   layers;
  public String                ui_layerNames[];
  public int                   learn_parameters;
  public double                ui_learnParameters[];
  public int                   update_parameters;
  public double                ui_updateParameters[];
  public int                   init_parameters;
  public double                ui_initParameters[];
  public int                   displays;
  public UIDisplayType         displayTypes[];
  public boolean               ui_configHas3DSection;
  public D3StateType           d3_state;
  public PrinterConfiguration printer_config;

/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Parses the network configuration file.
   *
   * @param configBuffer configuration file contents
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer) throws IOException {
    format                = 0;
    ui_configHas3DSection = false;
    String  line;

    // format
    format = configBuffer.readInteger();
    if(format < 1 || format > CURRENT_FORMAT)
      throw(new IOException("Unsupported configuration file format."));

    configBuffer.readNextLine(); // skip empty line

    if( format >= 2 ) {
      line = configBuffer.readNextLine(); // filenames
      if(line.equals("filenames:")) {
        ui_pathname = configBuffer.readText();
        ui_filenameNET = configBuffer.readText();
        ui_filenamePAT = configBuffer.readText();
        if(format >= 6) {
          ui_filenameRES = configBuffer.readText();
        }
        // Check .cfg specs, maybe following lines belong into above 'if'
        ui_filenameCFG = configBuffer.readText();
        ui_filenameTXT = configBuffer.readText();
      }
    }
    configBuffer.readNextLine(); // skip empty line

    layers = configBuffer.readInteger();
    ui_layerNames = new String[ layers ];
    for(int layer = 0; layer < layers; layer++) {
      ui_layerNames[ layer ] = configBuffer.readText();
    }
    configBuffer.readNextLine(); // skip empty line

    if( format >= 4 ) {
      int noParams = configBuffer.readInteger();
      learn_parameters = Math.min ( noParams, UI_NO_LEARN_PARAMS );
      ui_learnParameters = new double [ learn_parameters ];
      for(int parameter = 0; parameter < learn_parameters; parameter++) {
        ui_learnParameters [ parameter ] = configBuffer.readDouble();
      }
    }
    configBuffer.readNextLine(); // skip empty line

    if(format >= 4) {
      int noParams = configBuffer.readInteger();
      update_parameters = Math.min ( noParams, UI_NO_UPDATE_PARAMS );
      ui_updateParameters = new double [ update_parameters ];
      for (int parameter = 0; parameter < update_parameters; parameter++) {
        ui_updateParameters [ parameter ] = configBuffer.readDouble();
      }
    }
    configBuffer.readNextLine(); // skip empty line

    int noParams = configBuffer.readInteger ();
    init_parameters = Math.min ( noParams, UI_NO_INIT_PARAMS );
    ui_initParameters = new double [ init_parameters ];
    for(int parameter = 0; parameter < init_parameters; parameter++) {
      ui_initParameters [ parameter ] = configBuffer.readDouble();
    }
    configBuffer.readNextLine(); // skip empty line

    displays = configBuffer.readInteger();
    configBuffer.readNextLine();  // skip empty line

    displayTypes = new UIDisplayType[ displays ];
    for(int display = displays-1; display >= 0; display--) {
      displayTypes[ display ] = new UIDisplayType();
      displayTypes[ display ].readConfiguration(configBuffer, format);
      configBuffer.readNextLine(); // skip empty line
    }

    if(format >= 5) {
      line = configBuffer.readNextLine();
      if(line.equals("3D-Configuration")) {
        ui_configHas3DSection = true;
        configBuffer.readNextLine(); // skip empty line
        d3_state = new D3StateType();
        d3_state.readConfiguration(configBuffer);
      }
    }

    if(format >= 6) {
      configBuffer.readNextLine(); // skip empty line
      line = configBuffer.readNextLine();
      if(line.equals("Printer-Configuration")) {
        configBuffer.readNextLine(); // skip empty line
        printer_config = new PrinterConfiguration();
        printer_config.readConfiguration(configBuffer);
      }
    }
  }
}
