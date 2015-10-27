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

/*---------------------------- imports ---------------------------------------*/
import java.io.* ;
import java.util.* ;

import java.util.jar.*;
import javax.swing.* ;
import java.awt.* ;

// imports for NetTrainer:
import java.awt.event.*;


class NetTrainer implements Runnable {
  Network net;
  int pattern = -1, index = 0;
  boolean shuffle, subShuffle;
  Exception exc = null;
  TrainingResult res;
  ThreadChief tc;

  private int steps;
  private Thread thread;

  public NetTrainer(Network net,
                    ThreadChief tc,
                    int steps,
                    boolean shuffle,
                    boolean subShuffle) {
    this(net, tc, steps, shuffle, subShuffle, -1);
  }

  public NetTrainer(Network net,
                    ThreadChief tc,
                    int steps,
                    boolean shuffle,
                    boolean subShuffle,
                    int pattern) {
    this.net = net;
    this.tc = tc;
    this.steps = steps;
    this.pattern = pattern;
    this.shuffle = shuffle;
    this.subShuffle = subShuffle;
    thread = new Thread( this );
    thread.start();
  }

  public void run() {
    PatternSet val_set = net.snns.patternSets.getValidationSet();
    val_set = (net.snns.patternSets.getCurrent() == val_set)? null : val_set;
    boolean lis_ex = ( net.training_listeners.size() > 0 );

    KernelInterface ki = net.ki;
    if( !lis_ex ) {
      while( index < steps - tc.awake ){
        if( tc.stop ) {
          tc.stopped(null);
          net.fireEvent(NetworkEvent.NETWORK_TRAINED);
          return;
        }
        try {
          if(pattern >= 0) ki.trainNet(pattern, tc.awake);
          else ki.trainNet( tc.awake );
        }
        catch( Exception e ){
          exc = e;
          tc.stopped(e);
          net.fireEvent(NetworkEvent.NETWORK_TRAINED);
          return;
        }
        index += tc.awake;
        try{ thread.sleep( tc.sleep ); }
        catch( InterruptedException ex ){ net.snns.showException( ex, this ); }
      }
      if( tc.stop ) {
        tc.stopped(null);
        net.fireEvent(NetworkEvent.NETWORK_TRAINED);
        return;
      }
      try {
        if(pattern >= 0) ki.trainNet(pattern, steps-index);
        else ki.trainNet(steps-index);
      }
      catch( Exception e ){exc = e;}
      tc.stopped(exc);
      net.fireEvent(NetworkEvent.NETWORK_TRAINED);
    }
    else {
     res = new TrainingResult( steps, ( val_set != null ) );
      index = 0;
      int width = 0;
      while( index < steps ) {
        if(index < steps - tc.awake) width = tc.awake;
        else width = steps - index;
        try {
          if(pattern >= 0) ki.trainNetFixedTime(pattern, width, 1);
          else ki.trainNetFixedTime(width, 1);
        }
        catch( Exception e ){ exc = e; close(); return; }
        if( val_set == null )
        for(int i = 0; i < ki.steps_done; ++i)
          res.add( index + i, ki.sseArr[i] );
        else {
          double validationErr = net.validate( val_set, shuffle, subShuffle );
          for(int i = 0; i < ki.steps_done; ++i)
            res.add( index +i, ki.sseArr[i], validationErr );
        }
        if( tc.stop ) {close(); return; }
        if( ki.steps_done > 0 ){
          TrainingResult part = res.getPart( index, ki.steps_done);
          net.fireEvent( NetworkEvent.NETWORK_TRAINED, part );
          try{ thread.sleep( tc.sleep ); }
          catch( InterruptedException ex ){ net.snns.showException( ex, this ); }
          index += ki.steps_done;
        }
      }
      TrainingResult part;
     // int re = (index-1)%tc.awake+1;
     // if( re > 0 ) part = res.getPart( index - re, re );
     // else part = new TrainingResult( 0, val_set != null );
      part = new TrainingResult( 0, val_set != null );
     //* part.final_result = true;
      tc.stopped( part );
      net.fireEvent( NetworkEvent.NETWORK_TRAINED, part );
      //net.snns.appendToLog(index+" steps trained");
    }
  }

  public boolean wasOK(){ return exc == null; }

  private void close(){
    //net.snns.appendToLog(index+" steps trained");
    if( index != steps ) res = res.getPart(0, index);
    res.final_result = true;
    tc.stopped( res );
  }
}
