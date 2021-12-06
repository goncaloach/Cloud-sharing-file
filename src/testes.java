import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class testes {

    public static void main(String[] args) {
        ByteBlockRequestQueue list = new ByteBlockRequestQueue();
        for (int i = 0; i < 10000; i++) {
            list.addRequest(new ByteBlockRequest(i,i));
        }

        for (int i = 0; i < 1; i++) {
            cyka c = new cyka(list,i);
            c.start();
        }


    }
}
