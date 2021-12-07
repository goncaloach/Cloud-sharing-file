import java.io.IOException;

public class ola extends Thread{

    int num = 0;
    int id ;
    ByteBlockRequestQueue queue;

    public ola(ByteBlockRequestQueue queue, int id){
       this.queue = queue;
       this.id=id;
    }

    /*private  void decrement(){
        while (!list.isEmpty()){
            //if(list.getRequest() != null)
            try{
                list.getRequest();
                num++;
            }catch (IllegalStateException e){}
        }
    }*/

    private void decrement(){
        int blocksTransfered=0;
        System.out.println("Download Started");
        while (!queue.isEmpty()){
            try{
                ByteBlockRequest request = queue.getRequest();
                    for (int i = request.getStartIndex(); i < request.getStartIndex() + 100; i++)
                        System.out.println("");
                    blocksTransfered++;

            }catch (IllegalStateException e){} //list is empty
        }
        System.out.println("Transfer finished");
        System.out.println("Blocks Transfered:"+ blocksTransfered);
    }

    @Override
    public void run() {
        decrement();
        System.out.println("id:"+id+" values:"+num);
    }
}
