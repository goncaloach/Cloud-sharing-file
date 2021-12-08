import java.util.ArrayList;

public class adeus extends Thread{

    private int id;
    private CountDownLatch cdl;

    public adeus(int id, CountDownLatch cdl){

        this.id = id;
        this.cdl=cdl;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Thread id:"+id+" interrupted");
        }
        cdl.countDown();
        System.out.println("Thread id:"+id+" finished");
    }

    @Override
    public String toString() {
        return "thread{" +
                "ola sou a thread id=" + id +
                '}';
    }
}
