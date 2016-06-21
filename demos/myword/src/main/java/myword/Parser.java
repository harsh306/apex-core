package myword;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

/**
 * Created by harsh on 14/6/16.
 */
public class Parser extends BaseOperator {
    public transient final DefaultInputPort<String> input =new DefaultInputPort<String>() {

        @Override
        public void process(String tuple) {
            // TODO Auto-generated method stub
            String [] words  = tuple.split(" " );
            for (String word: words){
                output.emit(word);
            }

        }
    };
    public transient final DefaultOutputPort<String> output= new DefaultOutputPort<String>();
}
