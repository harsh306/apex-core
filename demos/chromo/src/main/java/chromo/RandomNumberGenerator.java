/**
 * Put your copyright and license info here.
 */
package chromo;

import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.InputOperator;
import com.datatorrent.common.util.BaseOperator;

/**
 * This is a simple operator that emits random number.
 */
public class RandomNumberGenerator extends BaseOperator implements InputOperator
{
  private int numTuples = 100;
  private transient int count = 0;

  public final transient DefaultOutputPort<ChromoType> out = new DefaultOutputPort<ChromoType>();

  @Override
  public void beginWindow(long windowId)
  {
    count = 0;
  }

  @Override
  public void emitTuples()
  {
    if (count++ < numTuples) {
      int ChrNum= Integer.valueOf((int)(Math.random()*100));
      int start= Integer.valueOf((int)(Math.random()*100));
      int end = start+5;
      ChromoType ct = new ChromoType(ChrNum,start,end);
      //System.out.println(String.valueOf(ct.getEnd()));
      out.emit(ct);
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
