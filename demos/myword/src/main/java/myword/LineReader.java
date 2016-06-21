package myword;

import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.lib.io.fs.AbstractFileInputOperator;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by harsh on 14/6/16.
 */
public class LineReader extends AbstractFileInputOperator<String> {
    public transient final DefaultOutputPort<String> output=new DefaultOutputPort<String>();
    private  transient BufferedReader br;

    @Override
    protected InputStream openFile(Path path) throws IOException {
        InputStream is = super.openFile(path);
        br =new BufferedReader(new InputStreamReader(is));
        return is;
    }
    @Override
    protected void closeFile(InputStream is) throws IOException{

        super.closeFile(is);
        br.close();
    }



    @Override
    protected void emit(String tuple) {
        output.emit(tuple);
        // TODO Auto-generated method stub

    }

    @Override
    protected String readEntity() throws IOException {
        // TODO Auto-generated method stub
        return br.readLine();

    }

}