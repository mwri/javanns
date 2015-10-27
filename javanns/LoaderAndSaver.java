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



import java.io.File;
import java.io.IOException ;
import javax.swing.JPanel ;

/**
 * LoaderAndSaver is an interface for aome useful information about a part of
 * JavaNNS which can be loaded and saved, has an original file, a certain
 * typical file extension or a keyword inside the file types.
 * The accessory is used to give more info to the kernel before saving it.
 */
public interface LoaderAndSaver {
  /**
   * the name of the part of JavaNNS, such as "spirals network"
   */
  String getLASName();
  /**
   * the keyword which should be found inside the file,
   * for example "SNNS network definition file"
   */
  String getKeyword();
  /**
   * the usual file extension of the files which contain information about
   * this part of the network like "net"
   */
  String getFileExtension();
  /**
   * general description about the part like "Network files *.net"
   * it is used by the FileManager in the file filter selection combo box
   */
  String getDescription();
  /**
   * more information about what is to save
   * this is currently used by network, because of its name, and by the result
   * files, if the result should be appended or not, or if it should include
   * input or output values of the patterns
   */
  JPanel getAccessory();
  /**
   * this method should return true, if the content of this part of the net has
   * been changed since it has been loaded
   */
  boolean contentChanged();

  /**
   * this method returns true, if the LoaderAndSaver has been loaded from a file
   * or was already saved before
   */
  boolean hasHomeFile();

  /**
   * method saves the LoaderAndSaver in its home file
   */
  void save() throws IOException;

  /**
   * method saves the LoaderAndSaver in the given file
   */
  void save( File file ) throws Exception;
  /**
   * method loads the given file by the LoaderAndSaver
   * when you implement this method and the LoaderAndSaver had already some
   * content, you should ask, if this content should be saved before the new is
   * loaded
   */
  void load( File file ) throws Exception;
  /**
   * method returns the home file when it exists
   */
  File getFile();
  }