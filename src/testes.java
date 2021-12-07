public class testes {

    public static void main(String[] args) {
        ByteBlockRequestQueue list = new ByteBlockRequestQueue();
        for (int i = 0; i < 10000; i++) {
            list.addRequest(new ByteBlockRequest(i,i));
        }

        for (int i = 0; i < 1; i++) {
            ola c = new ola(list,i);
            c.start();
        }


    }
}
