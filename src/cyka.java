import java.util.NoSuchElementException;

public class cyka extends Thread{

    int num = 0;
    int id ;
    ByteBlockRequestQueue list;

    public cyka(ByteBlockRequestQueue list, int id){
       this.list=list;
       this.id=id;
    }

    private void decrement(){
        try{

        }catch (NoSuchElementException e ){}
        while (!list.isEmpty()){
            list.getRequest();
            num++;
        }
    }

    @Override
    public void run() {
        decrement();
        System.out.println("id:"+id+" values:"+num);
    }
}
