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
 * ein Versuch, Threads auf eine einheitliche Art abzufangen
 */
public abstract class ThreadChief {

  /**
   *  gibt an, wie lange ein Thread pausieren sollte ( in ms )
   */
  public int sleep = 10;

 /**
  * Anzahl der Schritte, bis eine Pause eingelegt werden soll
  */
  public int awake = 100;

 /**
  * Sstop zeigt an, ob der Thread angehalten werden soll.
  */
  public volatile boolean stop = false;

  /**
   * hier meldet der Thread dass er fertig ist und liefert ggf. irgendwas
   */
  abstract void stopped(Object retour);
}