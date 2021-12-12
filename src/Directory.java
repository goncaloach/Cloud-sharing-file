import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 @author Gonçalo Henriques nº93205
 */

public class Directory {

    private final int port;
    private syncNodeInfoHashMap storageNodes = new syncNodeInfoHashMap();

    public Directory(int port){
        this.port=port;
    }

    public void startServing() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        try {
            System.out.println("Starting service...");
            while(true){
                Socket clientSocket = serverSocket.accept();
                new DealWithClient(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error while connecting with client");
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }

    }

    private class DealWithClient extends Thread{

        private BufferedReader in;
        private PrintWriter out;
        private int port;
        private InetAddress address;
        private final Socket socket;

        public DealWithClient(Socket socket) throws IOException {
            this.socket=socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        }

        @Override
        public void run() {
            try {
                while (true){
                    String msgReceived = in.readLine();
                    System.out.println("Message received: "+msgReceived);
                    String[] values = msgReceived.split("\\s+");
                    switch (values[0]){
                        case "nodes":
                            if(values.length!=1){
                                System.err.println("Wrong Arguments");
                                break;
                            }
                            String list;
                            System.out.println("List of nodes:");
                            for (Integer i : storageNodes.keySet()) {
                                list="node "+storageNodes.get(i).getAddress()+" " +storageNodes.get(i).getPort();
                                System.out.println(list);
                                out.println(list);
                            }
                            out.println("end");
                            break;
                        case "INSC":
                            if(values.length!=3){
                                System.err.println("Wrong Arguments");
                                break;
                            }
                            if(values[1].equals("localhost/127.0.0.1"))     //InetAddress.getByName can't recognize this address
                                values[1]="localhost";
                            try{
                                this.port = Integer.parseInt(values[2]);
                                try {
                                    storageNodes.get(port);
                                    System.err.println("Client already registered");
                                    port=-1; //Create invalid port to be removed
                                    socket.close();
                                } catch (NullPointerException e){} //port doesn't exist
                                this.address = InetAddress.getByName(values[1]);
                                storageNodes.put(port,new NodeInformation(address,port));
                                System.out.println("Client Registered: "+storageNodes.get(port));
                            }catch (Exception e){
                                System.err.println("Wrong values");
                                e.printStackTrace();
                            }
                            break;
                        default:
                            System.err.println("Wrong arguments");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error while receiving data");
            } finally {
                try {
                    try{
                        System.out.println("StorageNode removed: "+storageNodes.get(port));
                        storageNodes.remove(port);
                    }catch (NullPointerException e){} //Non existent StorageNode
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    try{
                        System.out.println("StorageNode removed: "+storageNodes.get(port));
                        storageNodes.remove(port);
                    }catch (NullPointerException exp){} //Non existent StorageNode
                }
            }

        }
    }

    public static void main(String[] args) {
        if(args.length != 1)
            throw new IllegalStateException("Number of arguments must be one");
        try{
            int port = Integer.parseInt(args[0]);
            if(port<1024 || port> 65535){
                System.err.println("Argument must be a value between 1024 and 65535");
                return;
            }
        }catch (Exception e){
            System.err.println("Argument must be an Integer");
        }
        try {
            new Directory(Integer.parseInt(args[0])).startServing();
        } catch (IOException e) {
            System.err.println("Error while starting server");
            e.printStackTrace();
        }
    }

}
