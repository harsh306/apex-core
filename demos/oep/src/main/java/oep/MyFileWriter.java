package oep;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.lib.io.fs.AbstractFileOutputOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by harsh on 17/6/16.
 */
public class MyFileWriter extends AbstractFileOutputOperator<String> {

    public MyFileWriter() {
        filePath = "/harsh/test/";
    }

    @Override
    protected String getFileName(String s) {
        if(s.contains("even"))
        {
            return "outeven.txt";
        }else if(s.contains("odd")){
            return "ooutodd.txt";
        }else{
            return "outprime.txt";
        }

    }

    @Override
    protected byte[] getBytesForTuple(String s) {
        return s.getBytes();
    }
}
