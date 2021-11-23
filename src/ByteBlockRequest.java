public class ByteBlockRequest {

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
