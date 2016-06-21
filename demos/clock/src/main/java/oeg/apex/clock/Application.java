/**
 * Put your copyright and license info here.
 */
package oeg.apex.clock;

import com.datatorrent.api.Context;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.DAG;
import com.datatorrent.api.DAG.Locality;
import com.datatorrent.lib.io.ConsoleOutputOperator;

@ApplicationAnnotation(name="MyFirstApplication")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    dag.setAttribute(Context.DAGContext.STREAMING_WINDOW_SIZE_MILLIS,1000);
    TimeGenerator timeGenerator = dag.addOperator("timeGenerator", TimeGenerator.class);
    timeGenerator.setNumTuples(3600);
    HandsAngle handsAngle = dag.addOperator("handsangle", HandsAngle.class);


    ConsoleOutputOperator cons = dag.addOperator("console", new ConsoleOutputOperator());

    dag.addStream("time to angle",timeGenerator.out,handsAngle.inputPort ).setLocality(Locality.CONTAINER_LOCAL);
    dag.addStream("anglecalc", handsAngle.outputPort, cons.input).setLocality(Locality.CONTAINER_LOCAL);
  }
}
