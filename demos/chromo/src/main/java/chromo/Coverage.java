package chromo;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by harsh on 18/6/16.
 */
public class Coverage extends BaseOperator {
   public String position="qqq";
    public HashMap<String,ArrayList<Integer>> hashMap = new HashMap<String,ArrayList<Integer>>();

    public transient final DefaultInputPort<ChromoType> input= new DefaultInputPort<ChromoType>() {
        @Override
        public void process(ChromoType ct) {

            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            for (int i = ct.getStart(); i < ct.getEnd(); i++) {
                position = (String.valueOf((ct.getChrNum()) + " " + String.valueOf(i)));
                output.emit(position);
                //      output.emit(String.valueOf(ct.getChrNum()));

            }
            // System.out.println("hiiiii0000000");

        }
    };
        public transient final DefaultOutputPort<String> output =new DefaultOutputPort<String>();
}
