public class testes {

    public static void main(String[] args) {
        ByteBlockRequestQueue list = new ByteBlockRequestQueue();
        for (int i = 0; i < 10000; i++) {
            list.addRequest(new ByteBlockRequest(i,i));
        }

        cyka c1 = new cyka(list,1);
        cyka c2 = new cyka(list,2);
        c1.start();
        c2.start();

    }
}
