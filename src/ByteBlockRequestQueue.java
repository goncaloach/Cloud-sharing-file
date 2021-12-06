import java.util.LinkedList;
import java.util.Queue;

public class ByteBlockRequestQueue {

    private Queue<ByteBlockRequest> queue = new LinkedList<>();

    public synchronized void addRequest(ByteBlockRequest request){
        queue.add(request);
    }

    public synchronized ByteBlockRequest getRequest(){
        if(isEmpty())
            throw new IllegalStateException("Out of bounds");
        return queue.remove();
    }

    public synchronized boolean isEmpty(){
        return queue.isEmpty();
    }

    public synchronized int size(){
        return queue.size();
    }
}
