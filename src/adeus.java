public class adeus extends Thread{

    private Integer port;
    private synchronizedHashMap map;

    public adeus(synchronizedHashMap map,int port){
        this.port = port;
        this.map=map;
    }

    @Override
    public void run() {
        try{
            System.out.println(map.get(port));
        }catch (NullPointerException  e){
            System.out.println("no");
        }
    }

}
