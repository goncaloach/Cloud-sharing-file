import java.io.Serializable;

/**
 @author Gonçalo Henriques nº93205
 */

public class ByteBlockRequest implements Serializable {

    private int startIndex;
    private int length;

    public ByteBlockRequest(int startIndex, int length){
        this.startIndex=startIndex;
        this.length=length;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getLength() {
        return length;
    }
}
