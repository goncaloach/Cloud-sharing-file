import java.util.ArrayList;
import java.util.List;

/**
 @author Gonçalo Henriques nº93205
 */

public class synchronizedList<T> {

    private List<T> list = new ArrayList<>();

    public synchronized void add(T obj){
        list.add(obj);
    }

    public synchronized T remove(int index){
        try{
            return list.remove(index);
        }catch (Exception e){
            throw new IllegalStateException("Out of bounds");
        }
    }

    public synchronized T get(int index){
        try{
            return list.get(index);
        }catch (Exception e){
            throw new IllegalStateException("Out of bounds");
        }
    }

    public synchronized boolean isEmpty(){
        return list.isEmpty();
    }

    public synchronized int size(){
        return list.size();
    }

}
