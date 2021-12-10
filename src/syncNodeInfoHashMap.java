import java.util.Collection;
import java.util.HashMap;

public class syncNodeInfoHashMap {

    private HashMap<Integer,NodeInformation> hashMap = new HashMap();

    public synchronized void put(Integer port, NodeInformation node){
        hashMap.put(port,node);
    }

    public synchronized NodeInformation get(Integer port){
        NodeInformation node = hashMap.get(port);
        if(node.equals(null))
            throw new NullPointerException();
        return node;
    }

    public synchronized boolean isEmpty(){
        return hashMap.isEmpty();
    }

    public synchronized int size(){
        return hashMap.size();
    }

    public synchronized void edit(Integer index, NodeInformation node){
        hashMap.replace(index,node);
    }

    public synchronized void remove(Integer port){
        hashMap.remove(port);
    }

    public synchronized Collection<Integer> keySet(){
        return hashMap.keySet();
    }
}
