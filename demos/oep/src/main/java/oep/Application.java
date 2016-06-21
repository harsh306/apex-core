/**
 * Put your copyright and license info here.
 */
package oep;

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
    OddEvenPrimeClassifier oe = dag.addOperator("oec",OddEvenPrimeClassifier.class);
    MyFileWriter mf =dag.addOperator("mf" , MyFileWriter.class);

   // ConsoleOutputOperator cons = dag.addOperator("console", new ConsoleOutputOperator());

    dag.addStream("randomNumber to odd_or_even", randomGenerator.out, oe.input);//setLocality
    dag.addStream("odd_even to console", oe.outputeven,mf.input);
   // dag.addStream("odd_odd to console", oe.outputodd,mf.input);
    //dag.addStream("odd_prime to console", oe.outputprime,mf.input);


  }
}
