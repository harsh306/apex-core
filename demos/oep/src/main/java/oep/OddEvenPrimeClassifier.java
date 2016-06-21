package oep;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.InputOperator;
import com.datatorrent.common.util.BaseOperator;

/**
 * Created by harsh on 17/6/16.
 */
public class OddEvenPrimeClassifier extends BaseOperator  {
    public final transient DefaultInputPort<Integer> input = new DefaultInputPort<Integer>() {
        @Override
        public void process(Integer num1) {
            int num = num1.intValue();
            String numStr = String.valueOf(num);
            if (num%2==0){
                //even
                outputeven.emit(numStr+"even");
                if(num == 2)
                {
                    outputprime.emit(numStr+"prime");
                }
            }
            else{
                //odd
                outputodd.emit(numStr+"odd");
                for (int i=2; i<num;i++){
                    if(num%i!=0){
                        //prime
                        String numString = String.valueOf(num);
                        outputprime.emit(numStr+"prime");
                    }
                }

            }

        }
    };
    public final transient DefaultOutputPort<String> outputeven = new DefaultOutputPort<String>();
    public final transient DefaultOutputPort<String> outputodd = new DefaultOutputPort<String>();
    public final transient DefaultOutputPort<String> outputprime = new DefaultOutputPort<String>();
}
