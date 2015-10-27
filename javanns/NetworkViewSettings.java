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
 * Parses the part of the configuration file that is defined as
 * SetupDataType.
 *
 */
class NetworkViewSettings implements Cloneable {

/*-------------------------------------------------------------------------*
 * public member variables
 *-------------------------------------------------------------------------*/
  /**
   * Codes for unit label types
   */
  public final static int  NOTHING=0, ACT=1, INIT_ACT=2, OUTPUT=3, BIAS=4,
    NAME=5, NUMBER=6, Z_VAL=7, WINNER=8;

  /**
   * Default colors for text and background
   */
  public final static Color DEFAULT_TEXT = Color.black;
  public final static Color DEFAULT_BGD = Color.white;
  public final static Color DEFAULT_POS = Color.green;
  public final static Color DEFAULT_NEG = Color.red;
  public final static Color DEFAULT_NULL = new Color(0, 0, 0x99);


  /**
   * Grid coordinates of the window upper left corner
   */
  public Point                 origin;

  /**
   * Grid size in pixels
   */
  public int                   grid_size;

  public int                   unitsInX;
  public int                   unitsInY;

  /**
   * Frozen window is not being updated
   */
  public boolean               frozen;
  public boolean               raster;          /* draw points on positions - not used*/

  /**
   * Window update needed?
   */
  public boolean               refreshNeeded;

  public int                   subNetNo;        /* visible in this display */
  public int                   flags;
  public int                   layers;          /* visible in this display */
  public int                   updateType;

  /**
   * Base label type for units
   */
  public int             base_label_type;

  /**
   * Top label type for units
   */
  public int             top_label_type;

  /**
   * Show links (connections between units) in network?
   */
  public boolean         show_links;

  /**
   * Show link directions as arrows?
   */
  public boolean         show_directions;

  /**
   * Show link values?
   */
  public boolean         show_weights;

  /**
   * Minimum link weights to be visible
   */
  public double          link_trigger;

  /**
   * Maximum link weights to be displayed with graded colors
   */
  public double          link_max;

  /**
   * Maximum neuron value to be displayed with graded colors
   */
  public double          unit_max;

  /**
   * Maximum distance between units in SOM networks to be displayed with graded
   * colors
   */
   public double         dist_max;

  public String          siteName;

  /**
   * String containing color table indices for display colors
   */
  public String          color_index;

  /**
   * Color for displaying text in window
   */
  public Color           text_color;

  /**
   * Window background color
   */
  public Color           background_color;

  /**
   * Color of selected units
   */
  public Color           selection_color;

  // descriptors for all units
  public Color neg_color, pos_color, null_color;
  int width, height, font_size;
  Font font;

  public NetworkViewSettings(){
    neg_color = DEFAULT_NEG;
    pos_color = DEFAULT_POS;
    null_color = DEFAULT_NULL;
    width = height = 16;
    font_size = 11;
    font = new Font("Dialog", Font.PLAIN, font_size);
    //font_metrics = getFontMetrics(font);
    //font_height = font_metrics.getHeight();
    //font_ascent = font_metrics.getAscent();
  }
/*-------------------------------------------------------------------------*
 * public methods
 *-------------------------------------------------------------------------*/

  /**
   * Parses those lines in the configuration files that content the fields
   * of the SetupDataType.
   *
   * @param configBuffer configuration file contents
   * @param format format of the configuration file
   * @throws IOException if can't read the file
   */
  public void readConfiguration(LineReader configBuffer, int format)
    throws IOException {

    boolean bool;

    origin= new Point(
      configBuffer.readInteger(),
      configBuffer.readInteger()
    );

    grid_size = configBuffer.readInteger();
    frozen = configBuffer.readBoolean();
    raster = configBuffer.readBoolean();
    subNetNo = configBuffer.readInteger();
    flags = configBuffer.readInteger();
    layers = configBuffer.readInteger();
    updateType = configBuffer.readInteger();

    bool = configBuffer.readBoolean();
    top_label_type = configBuffer.readInteger();
    if(!bool || top_label_type < 0 || top_label_type > WINNER)
      top_label_type = NOTHING;

    bool = configBuffer.readBoolean();
    base_label_type = configBuffer.readInteger();
    if(!bool || base_label_type < 0 || base_label_type > WINNER)
      base_label_type = NOTHING;

    show_links = configBuffer.readBoolean();
    show_directions = configBuffer.readBoolean();
    show_weights = configBuffer.readBoolean();

    double pos=0, neg=0;
    pos = configBuffer.readDouble();
    neg = configBuffer.readDouble();
    if(pos < -neg) link_trigger = pos;
    else link_trigger = -neg;

    unit_max = 1 / configBuffer.readDouble();

    if(format >= 3) {
      link_max = configBuffer.readDouble();
      siteName = configBuffer.readText();

      color_index = String.valueOf(configBuffer.readInteger());
      while(color_index.length() < 6) color_index = "0" + color_index;
      try {
        text_color = index2color(color_index.substring(0, 2));
        background_color = index2color(color_index.substring(2, 4));
        selection_color = index2color(color_index.substring(4, 6));
      }
      catch(Exception ex) {
        throw(new IOException("Error in configuration file."));
      }
    }
  }


  /**
   * Computes neuron or link color, depending on its value and selected state.
   *
   * @param v neuron activation
   * @return neuron color
   */
  public Color getColor(double v) {
    Color ci = null_color,
          ca = pos_color,
          cn = neg_color;
    int red, green, blue;

    if(v >= 0) {
      red = (int)( ci.getRed() + v * (ca.getRed()-ci.getRed()) );
      red = limit(red);
      green = (int)( ci.getGreen() + v * (ca.getGreen()-ci.getGreen()) );
      green = limit(green);
      blue = (int)( ci.getBlue() + v * (ca.getBlue()-ci.getBlue()) );
      blue = limit(blue);
    }
    else {
      red = (int)( ci.getRed() - v * (cn.getRed()-ci.getRed()) );
      red = limit(red);
      green = (int)( ci.getGreen() - v * (cn.getGreen()-ci.getGreen()) );
      green = limit(green);
      blue = (int)( ci.getBlue() - v * (cn.getBlue()-ci.getBlue()) );
      blue = limit(blue);
    }
    return new Color(red, green, blue);
  }

  /**
   * Makes sure that color channel (R, G or B) remains in [0, 255]
   *
   * @param color unsafe color channel
   * @return safe color channel
   */
  private int limit(int color) {
    if(color<0) return 0;
    if(color>255) return 255;
    return color;
  }

  /**
   * returns the default network view settings.
   *
   * @return default network view settings
   */
  public static NetworkViewSettings getDefaultSettings() {
    NetworkViewSettings s = new NetworkViewSettings();

    // default settings for units
    s.unit_max = 1;
    s.top_label_type = NAME;
    s.base_label_type = OUTPUT;
    s.grid_size = 50;
    s.text_color = DEFAULT_TEXT;
    s.background_color = DEFAULT_BGD;
    s.selection_color = Color.yellow;

    // default settings for links
    s.link_trigger = 0;
    s.link_max = 10;
    s.show_links =
      s.show_weights =
      s.show_directions = true;

    // default SOM settings
    s.dist_max = 2;
    return s;
  }

  /**
   * Computes color from index in color table
   *
   * @param s String containing index in color table
   * @return Color
   */
  public static Color index2color(String s) {
    return index2color(Integer.valueOf(s).intValue());
  }

  /**
   * Computes color from index in color table
   *
   * @param i index in color table
   * @return Color
   */
  public static Color index2color(int i) {
    if(i>63 || i<0) return Color.black;
    if(i>=56) switch(i) {
      case 56: return Color.black;
      case 57: return Color.red;
      case 58: return Color.green;
      case 59: return Color.blue;
      case 60: return Color.yellow;
      case 61: return Color.magenta;
      case 62: return Color.cyan;
      case 63: return Color.white;
    }
    if(i>=48) {
      int j = (int)((i-47)*85/3.);
      return new Color(j, j, j);
    }
    else {
      float h, s, b;
      h = (i&7)/8.F;
      if(i>=32) {
        b = 1;
        s = (2 - ( (i&8)>>3 )) / 4.F;
      }
      else {
        s = 1;
        b = (( (i&24)>>3 ) + 1) / 4.F;
      }
      return Color.getHSBColor(h, s, b);
    }
  }

  public Object clone() {
    try { return super.clone(); }
    catch(CloneNotSupportedException ex) { return null; }
  }
}
