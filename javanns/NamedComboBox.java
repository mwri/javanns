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


import javax.swing.JComboBox;
import java.util.Vector ;


/**
 * Comment this!
 */
public class NamedComboBox extends JComboBox {
  private Vector objects = new Vector();

  /**
   * Helper class. It is used if not a NamedObject is
   *  provided, but a name and an object.
   */
  private class TmpNamedObject implements NamedObject {
    private Object object;
    private String name;

    public TmpNamedObject(String s, Object o) {
      name = s;
      object = o;
    }

    public String getName() { return name; }
    public boolean equals(NamedObject o) {
      return name.equals(o.getName());
    }
  }


  /**
   * Comment this!
   */
  public void addItem(String name, Object o) {
    TmpNamedObject obj = new TmpNamedObject(name, o);
    addItem(obj);
  }

  /**
   * Comment this!
   */
  public void addItem(NamedObject o){
    objects.addElement(o);
    super.addItem(o.getName());
  }

  /**
   * Comment this!
   */
  public void addItems(NamedObject[] os) {
    for(int i=0; i<os.length; i++) addItem(os[i]);
  }

  /**
   * Comment this!
   */
  public void addItems(Vector os) {
    for(int i=0; i<os.size(); i++) addItem((NamedObject)os.elementAt(i));
  }

  /**
   * Comment this!
   */
  public Object getSelectedObject() {
    int i = getSelectedIndex();
    if(i<0) return null;
    return objects.elementAt(i);
  }

  /**
   * Comment this!
   */
  public Object getObjectAt(int i) {
    return objects.elementAt(i);
  }

  /**
   * Comment this!
   */
  public void setSelectedItem(String name) {
    int l = objects.size();
    int i;
    for(i=0; i<l; i++)
      if(((NamedObject)objects.elementAt(i)).getName().equals(name)) break;
    if(i<l)  setSelectedIndex(i);
  }

  /**
   * Comment this!
   */
  public void setSelectedItem(NamedObject o) {
    if( o == null ) return;
    int index = objects.indexOf(o);
    if( index == -1 ) {
      boolean found = false;
      while(index < objects.size()-1 && ! found ) {
        index++;
        if(o.equals((NamedObject)objects.elementAt(index))) found = true;
      }
      if(!found) return;
    }
    setSelectedIndex(index);
  }

  /**
   * Comment this!
   */
  public void removeAllItems() {
    super.removeAllItems();
    objects.removeAllElements();
  }

  public void removeLastItem(){
    int index = objects.size() - 1;
    if( index == -1 ) return;
    objects.removeElementAt( index );
    super.removeItemAt( index );
  }

  /**
   * Comment this!
   */
  public int length() { return objects.size(); }
}
