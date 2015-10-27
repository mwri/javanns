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

class LinkData{
  int source, target;
  double weight;

  /**
   * Constructor used by the delete() method of Link class
   * It saves the information to recreate the Link
   */
  public LinkData( Link link ){
    source = link.getSourceUnit().getNumber();
    target = link.getTargetUnit().getNumber();
    weight = link.getWeight();
  }

  /**
   * Constructor for a new LinkData Object to construct a new Link
   */
  public LinkData( int sourceNo, int targetNo, double weight ){
    source = sourceNo;
    target = targetNo;
    this.weight = weight;
  }
}
