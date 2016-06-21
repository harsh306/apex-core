package chromo;

/**
 * Created by harsh on 18/6/16.
 */
public class ChromoType {
    private  int ChrNum,start,end;
    public ChromoType(){}

    public ChromoType(int chrNum,int start,int end){
        this.ChrNum=chrNum;
        this.start=start;
        this.end=end;
    }

    public void setChrNum(int chrNum) {
        ChrNum = chrNum;
    }

    public int getChrNum() {
        return ChrNum;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
