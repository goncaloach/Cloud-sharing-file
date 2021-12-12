import java.util.LinkedList;
import java.util.Queue;

/**
 @author Gonçalo Henriques nº93205
 */

public class synchronizedQueue<T> {

    private Queue<T> queue = new LinkedList<>();

    public synchronized void add(T obj){
        queue.add(obj);
    }

    public synchronized T get(){
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
