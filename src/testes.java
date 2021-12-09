import java.util.ArrayList;

public class testes {

    public static void main(String[] args) {
        CountDownLatch cld = new CountDownLatch(2);
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            threads.add(new adeus(i,cld,i));
        }

        threads.forEach(Thread::start);

        try {
            cld.await();
            System.out.println("final do await");
            //threads.forEach(Thread::interrupt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



}
