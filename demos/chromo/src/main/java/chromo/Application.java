/**
 * Put your copyright and license info here.
 */
package chromo;

import com.datatorrent.lib.algo.UniqueValueCount;
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
    // Sample DAG with 2 operators
    // Replace this code with the DAG you want to build

    RandomNumberGenerator randomGenerator = dag.addOperator("randomGenerator", RandomNumberGenerator.class);
    randomGenerator.setNumTuples(500);
    Coverage coverage = dag.addOperator("coverage",Coverage.class);

    //MyFileWriter mf = dag.addOperator("myfile", MyFileWriter.class);
    UniqueValueCount uc =dag.addOperator("uq", new UniqueValueCount());
    ConsoleOutputOperator cons = dag.addOperator("console", new ConsoleOutputOperator());

    dag.addStream("randomData to chromo_input", randomGenerator.out, coverage.input);
    dag.addStream("ch to unique", coverage.output,uc.input);
    dag.addStream("coverage to file",uc.output,cons.input);

  }
}
