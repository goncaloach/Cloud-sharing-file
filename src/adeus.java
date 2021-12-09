import java.util.ArrayList;

public class adeus extends Thread{

    private int id;
    private CountDownLatch cdl;
    private int time;

    public adeus(int id, CountDownLatch cdl, int i){
        this.time = i*10;
        this.id = id;
        this.cdl=cdl;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000+time);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Thread id:"+id+" interrupted");
            return;
        }
        System.out.println("Thread id:"+id+" finished");
        cdl.countDown();
    }

    @Override
    public String toString() {
        return "thread{" +
                "ola sou a thread id=" + id +
                '}';
    }
}
