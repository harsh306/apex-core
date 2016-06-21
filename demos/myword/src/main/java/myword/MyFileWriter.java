package myword;

import com.datatorrent.lib.io.fs.AbstractFileOutputOperator;

import java.util.HashMap;

/**
 * Created by harsh on 20/6/16.
 */
public class MyFileWriter extends AbstractFileOutputOperator<HashMap<String, Integer>> {
    public MyFileWriter (){
        filePath ="/harsh/test";
    }
    @Override
    protected String getFileName(HashMap<String, Integer> s) {
        return "wordcount.txt";
    }

    @Override
    protected byte[] getBytesForTuple(HashMap<String, Integer> s) {
        return s.toString().getBytes();
    }
}
