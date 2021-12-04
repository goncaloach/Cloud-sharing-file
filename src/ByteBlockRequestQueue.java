import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ByteBlockRequestQueue {

    private Queue<ByteBlockRequest> queue = new LinkedList<>();
    private Lock lock =new ReentrantLock();

    public synchronized void addRequest(ByteBlockRequest request){
        queue.add(request);
    }

    public synchronized ByteBlockRequest getRequest(){
        if(isEmpty())
            return null;
        return queue.remove();
    }

    public synchronized boolean isEmpty(){
        return queue.isEmpty();
    }
}
