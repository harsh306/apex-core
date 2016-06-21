package chromo;

import com.datatorrent.lib.io.fs.AbstractFileOutputOperator;

/**
 * Created by harsh on 17/6/16.
 */
public class MyFileWriter extends AbstractFileOutputOperator<String> {

    public MyFileWriter() {
        filePath = "/harsh/test/";
    }

    @Override
    protected String getFileName(String s) {

            return "chromo_out.txt";
}

    @Override
    protected byte[] getBytesForTuple(String s) {
        return s.getBytes();
    }
}
