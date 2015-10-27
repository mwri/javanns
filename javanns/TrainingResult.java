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


class TrainingResult {
  double[] sse, val_sse = null;
  boolean final_result = false;
  int steps_total;

  public TrainingResult( int size, boolean incl_validation ){
    steps_total = size;
    sse = new double[ size ];
    if( incl_validation ) val_sse = new double[ size ];
  }

  public void add( int index, double err ){
    sse[ index ] = err;
  }

  public void add( int index, double err, double val_err ){
    sse[ index ] = err;
    val_sse[ index ] = val_err;
  }

  public void clear(){
    int s = sse.length;
    sse = new double[ s ];
    if( val_sse != null ) val_sse = new double[ s ];
  }

  public void cut( int size ){
    double[] v = new double[ size ];
    System.arraycopy( sse, 0, v, 0, size );
    sse = v;
    if( val_sse != null ){
      v = new double[ size ];
      System.arraycopy( val_sse, 0, v, 0, size );
      val_sse = v;
    }
  }

  public TrainingResult getPart( int pos, int size ){
    TrainingResult part = new TrainingResult( size, val_sse != null );
    part.steps_total = steps_total;
    System.arraycopy( sse, pos, part.sse, 0, size );
    if( val_sse != null )
      System.arraycopy( val_sse, pos, part.val_sse, 0, size );
    return part;
  }
}
