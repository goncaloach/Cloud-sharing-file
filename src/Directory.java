import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Directory {

    private final int port;
    //private synchronizedQueue<NodeInformation > storageNodes; nao pode ser queue

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

        public DealWithClient(Socket socket) throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        }

        @Override
        public void run() {
            try {
                while (true){
                    String msgReceived = in.readLine();
                    String[] values = msgReceived.split("\\s+");

                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error while receiving data");
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
