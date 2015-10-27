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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.print.*;
import wsi.ra.chart2d.*;

/**
 * the analyzer gets the information which is displayed by the analyzer panel
 * it trains and tests the net
 */
class Analyzer implements Runnable{
  Snns snns;
  Network net;
  AnalyzerPanel ap;
  Thread thread;
  ThreadChief trainer_tc;

  public Analyzer(AnalyzerPanel panel){
    ap = panel;
    snns = ap.snns;
    net = snns.network;
    trainer_tc = new ThreadChief(){
      public void stopped(Object o){
        if( o instanceof Exception ) {
          snns.showException((Exception)o, this);
          ap.tc.stop = true;
        }
      }
    };

    thread = new Thread( this );
    thread.start();
  }

  public void run(){
    try{
      int steps = ap.getSteps(), training_steps = 0, patNo;
      if( ap.cbTrainNet.isSelected() ) training_steps = ap.getTrainingSteps();
      boolean change_pattern = ap.cbChangePattern.isSelected(),
              x_act = ap.rbXAct.isSelected(), y_act = ap.rbYAct.isSelected();
      int selX = ap.jcbXValues.getSelectedIndex(),
          selY = ap.jcbYValues.getSelectedIndex(),
          err = ap.jcbErrors.getSelectedIndex();

      // falls die Funktionen oder die Parameter veraendert wurden:
      Function f = snns.master.lp.getFunction();
      double[] p = snns.master.lp.getParameters();
      net.setFunction(f, p);
      f = snns.master.up.getFunction();
      p = snns.master.up.getParameters();
      net.setFunction(f, p);

      String[] lab = new String[]{"Pattern", "", ""};
      patNo = net.getCurrentPatternNo();

      if( selY == 0 ){  // --> unit
        Unit y_unit = ap.getYUnit();
        if( y_unit == null ) return;
        lab[2] = "Output";
        if( y_act ) lab[2] = "Activation";
        lab[2] += " of unit["+y_unit.getNumber()+"]";
        ap.sbArea.y_label = lab[2];

        if( selX == 0 ) { // unit ---> unit
          Unit x_unit = ap.getXUnit();
          lab[1] = x_act?"Activation":"Output";
          lab[1] += " of unit["+x_unit.getNumber()+"]";
          ap.sbArea.x_label = lab[1];
          snns.pLog.append( lab[0] + "\t" + lab[1] + "\t" + lab[2], ap );

          for( int i=0; i<steps; i++ ){
            if( checkThread(i) ) { ap.tc.stopped(null); return; }

            if( i > 0 && change_pattern ) patNo = setNextPattern();
            if( training_steps > 0 ) net.trainNet( trainer_tc, training_steps, false, false);
            else net.updateNet();

            double x, y;
            if( x_act ) x = x_unit.getActivation();
            else x = x_unit.getOutput();
            if( y_act ) y = y_unit.getActivation();
            else y = y_unit.getOutput();
            ap.addPoint( x, y );
            snns.pLog.append( patNo + "\t" + x + "\t" + y, ap );
          }
        }

        else if( selX == 1 ) {  // time ---> unit
          lab[1] = "Time";
          ap.sbArea.x_label = lab[1];
          snns.pLog.append( lab[0] + "\t" + lab[1] + "\t" + lab[2], ap );
          for( int i=0; i<steps; i++ ){
            if( checkThread(i) ) { ap.tc.stopped(null); return; }

            if( i > 0 && change_pattern ) patNo = setNextPattern();
            if( training_steps > 0 ) net.trainNet( trainer_tc, training_steps, false, false);
            else net.updateNet();

            double y;
            if( y_act ) y = y_unit.getActivation();
            else y = y_unit.getOutput();
            ap.addPoint( ap.time, y );
            snns.pLog.append( patNo + "\t" + ap.time + "\t" + y, ap );
            ap.time++;
          }
        }

        else { // pattern number ---> unit
          ap.sbArea.x_label = lab[0];
          snns.pLog.append( lab[0] + "\t" + lab[2], ap );
          for( int i=0; i<steps; i++ ){
            if( checkThread(i) ) { ap.tc.stopped(null); return; }

            if( i > 0 && change_pattern ) patNo = setNextPattern();
            if( patNo == 1 ) ap.jump();
            if( training_steps > 0 ) net.trainNet( trainer_tc, training_steps, false, false);
            else net.updateNet();

            double y;
            if( y_act ) y = y_unit.getActivation();
            else y = y_unit.getOutput();
            ap.addPoint( patNo, y );
            snns.pLog.append( patNo + "\t" + y, ap );
          }

        }
      }
      else { // --> error
        if( training_steps == 0 )
          throw new Exception("Error would be constant without training");
        double errV;
        boolean ave = ap.cbAVE.isSelected();
        lab[2] = ap.jcbErrors.getSelectedItem().toString();
        if( err < 2 && ave ) lab[2] += " / | output units |";
        ap.sbArea.y_label = lab[2];

        if( selX == 0 ){ // unit ---> error
          Unit x_unit = ap.getXUnit();
          if( x_unit == null ) return;
          lab[1] = x_act?"Activation":"Output";
          lab[1] += " of unit["+x_unit.getNumber()+"]";
          ap.sbArea.x_label = lab[1];
          snns.pLog.append( lab[0] + "\t" + lab[1] + "\t" + lab[2], ap );

          if( err == 0 ){ // unit ---> absolute sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              net.trainNet( trainer_tc, training_steps, false, false);
              double x = x_act? x_unit.getActivation():x_unit.getOutput();
              errV = net.getError( false, ave );
              ap.addPoint( x, errV );
              snns.pLog.append( patNo + "\t" + x + "\t" + errV, ap );
            }
          }
          else if( err == 1 ){ // unit ---> square sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              net.trainNet( trainer_tc, training_steps, false, false);
              double x = x_act? x_unit.getActivation():x_unit.getOutput();
              errV = net.getError( true, ave );
              ap.addPoint( x, errV );
              snns.pLog.append( patNo + "\t" + x + "\t" + errV, ap );
            }
          }
          else { // unit ---> single abs error
            int y_no = ap.getYUnit().getNumber();
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              net.trainNet( trainer_tc, training_steps, false, false);
              double x = x_act? x_unit.getActivation():x_unit.getOutput();
              errV = net.getError( y_no );
              ap.addPoint( x, errV );
              snns.pLog.append( patNo + "\t" + x + "\t" + errV, ap );
            }
          }
        }
        else if( selX == 1 ){ // time ---> error
          lab[1] = "Time";
          ap.sbArea.x_label = lab[1];
          snns.pLog.append( lab[0] + "\t" + lab[1] + "\t" + lab[2], ap );
          if( err == 0 ){ // time ---> absolute sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( false, ave );
              ap.addPoint( ap.time, errV );
              snns.pLog.append( patNo + "\t" + ap.time + "\t" + errV, ap );
              ap.time++;
            }
          }
          else if( err == 1 ){ // time ---> square sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) {
                setNextPattern();
                patNo = net.getCurrentPatternNo();
              }
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( true, ave );
              ap.addPoint( ap.time, errV );
              snns.pLog.append( patNo + "\t" + ap.time + "\t" + errV, ap );
              ap.time++;
            }
          }
          else { // time ---> single abs error
            int y_no = ap.getYUnit().getNumber();
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( y_no );
              ap.addPoint( ap.time, errV );
              snns.pLog.append( patNo + "\t" + ap.time + "\t" + errV, ap );
              ap.time++;
            }
          }
        }
        else { // pattern number ---> error
          ap.sbArea.x_label = lab[0];
          snns.pLog.append( lab[0] + "\t" + lab[2], ap );
          if( err == 0 ){ // p.n. ---> absolute sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }

              if( i > 0 && change_pattern ) patNo = setNextPattern();
              if( patNo == 1 ) ap.jump();
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( false, ave );
              ap.addPoint( patNo, errV );
              snns.pLog.append( patNo + "\t" + errV, ap );
            }
          }
          else if( err == 1 ){ // p.n. ---> square sum
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              if( patNo == 1 ) ap.jump();
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( true, ave );
              ap.addPoint( patNo, errV );
              snns.pLog.append( patNo + "\t" + errV, ap );
            }
          }
          else { // p.n. ---> single abs error
            int y_no = ap.getYUnit().getNumber();
            for( int i=0; i<steps; i++ ){
              if( checkThread(i) ) { ap.tc.stopped(null); return; }
              if( i > 0 && change_pattern ) patNo = setNextPattern();
              if( patNo == 1 ) ap.jump();
              net.trainNet( trainer_tc, training_steps, false, false);
              errV = net.getError( y_no );
              ap.addPoint( patNo, errV );
              snns.pLog.append( patNo + "\t" + errV, ap );
            }
          }
        }
      }
      ap.tc.stopped(null);
    }
    catch(Exception e){ ap.tc.stopped(e); }
  }

  private int setNextPattern() throws Exception{
    if( !snns.network.setNextPattern( true ) )
      if( !snns.network.setFirstPattern( true ) )
        throw new Exception("No pattern set defined");
    return snns.network.getCurrentPatternNo();
  }

  private boolean checkThread(int step) throws InterruptedException{
    if( ap.tc.stop ) return true;
    if( step%ap.tc.awake == 0 ) thread.sleep( ap.tc.sleep );
    return false;
  }
}
