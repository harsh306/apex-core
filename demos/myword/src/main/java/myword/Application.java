/**
 * Put your copyright and license info here.
 */
package myword;

import com.datatorrent.lib.algo.UniqueCounter;

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

    LineReader lr= dag.addOperator("LineReader", LineReader.class);
    Parser parser =dag.addOperator("Parser", Parser.class);
    UniqueCounter<String> counter =dag.addOperator("Counter",new UniqueCounter());

    //randomGenerator.setNumTuples(500);

 // ConsoleOutputOperator cons = dag.addOperator("console", new ConsoleOutputOperator());
    MyFileWriter mf = dag.addOperator("mf", MyFileWriter.class);

    //dag.addStream("randomData", randomGenerator.out, cons.input).setLocality(Locality.CONTAINER_LOCAL);
    dag.addStream("LineReader to Parser", lr.output, parser.input).setLocality(Locality.THREAD_LOCAL);
    dag.addStream("Parser to Counter", parser.output, counter.data).setLocality(Locality.THREAD_LOCAL);
    dag.addStream("Counter to mf",counter.count,mf.input).setLocality(Locality.THREAD_LOCAL);;
  }
}
