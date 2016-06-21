/**
 * Put your copyright and license info here.
 */
package oeg.apex.clock;

import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.InputOperator;
import com.datatorrent.common.util.BaseOperator;
import java.util.Date;

/**
 * This is a simple operator that emits random number.
 */
public class TimeGenerator extends BaseOperator implements InputOperator
{
  private int numTuples = 300;
  private transient int count = 0;

  public final transient DefaultOutputPort<String > out = new DefaultOutputPort<String>();

  @Override
  public void beginWindow(long windowId)
  {
    count = 0;
  }

  @Override
  public void emitTuples()
  {
    if (count++ < numTuples) {
      Date date = new Date();
      String d[]=date.toString().split(" ");
      String t[]=d[3].split(":");
      out.emit(t[1].toString()+" "+t[2].toString() );
    }
  }

  public int getNumTuples()
  {
    return numTuples;
  }

  /**
   * Sets the number of tuples to be emitted every window.
   * @param numTuples number of tuples
   */
  public void setNumTuples(int numTuples)
  {
    this.numTuples = numTuples;
  }
}
