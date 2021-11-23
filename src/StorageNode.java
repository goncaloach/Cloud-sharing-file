import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class StorageNode {

    //TODO sincronizar o array de cloudBytes
    //TODO comunicar com o diretorio (pedir n nodes)
    //TODO tirar atributos desnecessarios


    private CloudByte[] cloudBytes = new CloudByte[1000000];


    private BufferedReader in;
    private PrintWriter out;
    private Socket directorySocket;


    private int thisPort;
    private ServerSocket thisServerSocket;

    public StorageNode(String directoryAddressText, int serverPort, int thisPort, String fileName) throws IOException {
        this.thisPort = thisPort;
        thisServerSocket = new ServerSocket(thisPort);

        connectToDirectory(directoryAddressText,serverPort,thisPort);

        createCloudBytes(fileName);

        new ReadInputsFromConsole().start();

        startServing();

        System.out.println("algo de errado nao estao certo");
    }

    /*public StorageNode(String serverAddressText, int serverPort, int thisPort){
        //TODO
    }*/

    private void connectToDirectory(String directoryAddressText, int serverPort, int storagePort) throws IOException {
        InetAddress address = InetAddress.getByName(directoryAddressText);
        directorySocket = new Socket(address,serverPort);

        in = new BufferedReader(new InputStreamReader(directorySocket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(directorySocket.getOutputStream())), true);

        InetAddress snAdress = InetAddress.getByName(null);
        String msg = "INSC "+snAdress+ " "+ storagePort;
        System.out.println("Sending to Directory: "+msg);
        out.println(msg);
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


    private class ReadInputsFromConsole extends Thread{

        private boolean checkArguments(String [] strArgs) {
            if(strArgs.length != 2 || !strArgs[0].equals("ERROR")){
                System.err.println("Wrong Arguments");
                return false;
            }
            try {
                int num = Integer.parseInt(strArgs[1]);
                if(num<1 || num>1000000){
                    System.err.println("Second Argument must be a number between 1 and 1000000");
                    return false;
                }
                return true;
            }
            catch( Exception e ) {
                System.err.println("Second Argument must be a number");
                return false;
            }
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                try {
                    String content = reader.readLine();
                    String[] strArgs = content.split("\\s+");

                    if(checkArguments(strArgs)){
                        int numByte = Integer.parseInt(strArgs[1])-1;

                        //TODO falta sincronizar
                        cloudBytes[numByte].makeByteCorrupt();
                        System.out.println("Injected Error into byte number " + (numByte+1)+ ": "+cloudBytes[numByte]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void startServing() throws IOException {
        try {
            System.out.println("Awaiting connections...");
            while (true) {
                Socket clientSocket = thisServerSocket.accept();
                new DealWithClient(clientSocket).start();
            }
        } finally {
            thisServerSocket.close();
        }
    }


    private class DealWithClient extends Thread{

        private ObjectInputStream in;
        private ObjectOutputStream out;

        public DealWithClient(Socket s) throws IOException {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        }

        @Override
        public void run() {
            while (true){
                try {
                    ByteBlockRequest msgReceived = (ByteBlockRequest) in.readObject();
                    int startIndex = msgReceived.getStartIndex();
                    int length = msgReceived.getLength();

                    CloudByte[] msgResponse = new CloudByte[length];

                    //TODO sincronizar
                    for (int i = startIndex-1, j=0; i < startIndex-1+length; i++, j++){
                        //TODO verificar se o byte esta corrupt
                        msgResponse[j]=cloudBytes[i];
                    }
                        //TODO verificar se o byte esta corrupt

                    out.reset();
                    out.writeObject(msgResponse);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        if(args.length <3 || args.length >4)
            throw new IllegalArgumentException("The number of arguments must be 3 or 4");
        new StorageNode(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]),args[3]);
    }
}
