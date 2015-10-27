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
 * <p>Title: javanns.IconGrabber.java</p>
 * <p>Description: Java source file for JavaNNS</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Tuebingen WSI</p>
 * @author Fabian Hennecke
 * @version 1.0
 */
import java.awt.Toolkit;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.io.File;
import java.net.URL;

/**
 * Class IconGrabber looks for icons and images in the image directory
 * of JavaNNS
 */
public class IconGrabber {
  Snns snns;
  static final ClassLoader cl = ClassLoader.getSystemClassLoader();
  static final String path = Snns.ICON_PATH + '/';
  static final Toolkit toolkit = Toolkit.getDefaultToolkit();

  public IconGrabber(Snns snns) {
    this.snns = snns;
   }

  public ImageIcon getIcon(String name){
    return getIcon(name,name);
  }

  public ImageIcon getIcon(String name, String description){
    URL url = getURL(name);
    if( url == null ) return null;
    return new ImageIcon( url, description );
  }

  public Image getImage(String name){
    URL url = getURL(name);
    if( url == null ) return null;
    return toolkit.createImage(url);
  }

  URL getURL(String name){
    URL url = cl.getResource(path+name);
    if( url == null ) {
      System.err.println("Cannot find icon "+name+" in directory "+Snns.ICON_PATH);
      return null;
    }
    return url;
  }
}