import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class testes {

    public static void main(String[] args) throws UnknownHostException {

        synchronizedHashMap map = new synchronizedHashMap();

        for (int i = 0; i < 10000; i++) {
            map.put(8080+i, new NodeInformation(InetAddress.getByName(null),8080+i,false));
        }

        for (int i = 0; i < 7; i++) {
            new adeus(map,8088).start();
        }

    }



}
