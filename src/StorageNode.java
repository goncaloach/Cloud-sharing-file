import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class StorageNode {

    private CloudByte[] cloudBytes = new CloudByte[1000000];
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private int storagePort;
    private ServerSocket serverSocket;

    public StorageNode(String serverAddressText, int serverPort, int storagePort, String fileName) throws IOException {
        this.storagePort = storagePort;
        serverSocket = new ServerSocket(storagePort);
        connectToServer(serverAddressText,serverPort,storagePort);

        createCloudBytes(fileName);
        injectError();
    }

    private void connectToServer(String serverAddressText, int serverPort, int storagePort) throws IOException {
        InetAddress address = InetAddress.getByName(serverAddressText);
        socket = new Socket(address,serverPort);

        System.out.println("Socket:" + socket);

        //TODO
        //in = new ObjectInputStream(serverSocket.accept().getInputStream());
        //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    public StorageNode(String serverAddressText, int serverPort, int storagePort){
        //TODO
        //injectError();
    }

    private void createCloudBytes(String fileName){
        try {
            byte[] fileContents = Files.readAllBytes(new File(fileName).toPath());
            for (int i = 0; i < fileContents.length; i++)
                cloudBytes[i] = new CloudByte(fileContents[i]);
            System.out.println("Loaded data from file: "+cloudBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading file");
        }
    }

    private void injectError(){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String content = reader.readLine();
                String[] splited = content.split("\\s+");
                if (splited.length == 2 && splited[0].equals("ERROR")) {
                    int numByte = Integer.parseInt(splited[1]);
                    if(numByte<0 || numByte>999999)
                        throw new IllegalArgumentException("Second argument must be a number between 0 and 999999");
                    cloudBytes[numByte].makeByteCorrupt();
                    System.out.println("Injected Error into byte number: " + numByte);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private class DealWithClient extends  Thread{

        private ObjectInputStream in;
        private ObjectOutputStream out;

        public DealWithClient(Socket socket) throws IOException {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            //TODO
            System.out.println("run");
        }
    }


    public static void main(String[] args) throws IOException {
        if(args.length <3 || args.length >4)
            throw new IllegalArgumentException("The number of arguments must be 3 or 4");
        new StorageNode(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]),args[3]);
    }
}
