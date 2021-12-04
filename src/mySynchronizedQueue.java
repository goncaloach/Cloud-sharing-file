public class mySynchronizedQueue<Item> {

    //Synchronized Queue using a linked-list implementation

    private class Node{
        Item item;
        Node next;
    }

    private Node first = null;
    private Node last = null;

    public synchronized boolean isEmpty(){
        return first == null;
    }

    public synchronized void enqueue(Item item){
        Node oldLast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        if(isEmpty())
            first = last;
        else
            oldLast.next = last;
    }

    public synchronized Item dequeue(){
        if(isEmpty())
            return null;
        Item item = first.item;
        first = first.next;
        if(isEmpty())
            last=null;
        return item;
    }

}
