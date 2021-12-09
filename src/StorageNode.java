import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class StorageNode {

    //TODO corrigir erros
    //O diretorio so atende um cliende de cada vez?
    //TODO deadlock! qnd o node 3 copia do node 1 ou 2 e estes tem erros
    //possivel correcao o diretorio mostrar so os nodes que estao disponiveis para servir

    private CloudByte[] cloudBytes = new CloudByte[1000000];
    private BufferedReader inDirectory;
    private PrintWriter outDirectory;
    private final int port;

    public StorageNode(String directoryAddressText, int directoryPort, int nodePort, String fileName) {
        this.port = nodePort;
        try {
            connectToDirectory(directoryAddressText, directoryPort, nodePort);
        } catch (IOException e) {
            System.err.println("Error while connecting to Directory");
            e.printStackTrace();
            System.exit(1);
        }

        createCloudBytes(fileName);
        new injectErrorsFromConsole().start();

        try {
            startServing();
        } catch (IOException e) {
            System.err.println("Error while connecting to Clients");
            e.printStackTrace();
        }
        System.out.println("You were not supposed to reach here :/");
    }

    public StorageNode(String directoryAddressText, int directoryPort, int nodePort) {
        this.port = nodePort;
        try {
            connectToDirectory(directoryAddressText, directoryPort, nodePort);
        } catch (IOException e) {
            System.err.println("Error while connecting to Directory");
            e.printStackTrace();
            System.exit(1);
        }

        long start = System.nanoTime();
        getCloudBytesFromStorageNodes();
        long timeElapsed = (System.nanoTime() - start) / 1000000L;
        System.out.println("Time elapsed:"+timeElapsed+" milliseconds");

        new injectErrorsFromConsole().start();

        try {
            startServing();
        } catch (IOException e) {
            System.err.println("Error while connecting to Clients");
            e.printStackTrace();
        }
        System.out.println("You were not supposed to reach here v2 :/");
    }

    //DONE
    private void connectToDirectory(String directoryAddressText, int directoryPort, int nodePort) throws IOException {
        InetAddress directoryAddress = InetAddress.getByName(directoryAddressText);
        Socket directorySocket = new Socket(directoryAddress, directoryPort);

        this.inDirectory = new BufferedReader(new InputStreamReader(directorySocket.getInputStream()));
        this.outDirectory = new PrintWriter(new BufferedWriter(new OutputStreamWriter(directorySocket.getOutputStream())), true);

        InetAddress nodeAddress = InetAddress.getByName(null);
        String msg = "INSC " + nodeAddress + " " + nodePort;
        System.out.println("Sending to Directory: " + msg);
        outDirectory.println(msg);
    }

    //DONE
    private void createCloudBytes(String fileName) {
        try {
            byte[] fileContents = Files.readAllBytes(new File(fileName).toPath());
            for (int i = 0; i < fileContents.length; i++)
                cloudBytes[i] = new CloudByte(fileContents[i]);
            System.out.println("Loaded data from file: " + cloudBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading file");
        }
    }

    //DONE
    private class injectErrorsFromConsole extends Thread {

        private boolean checkArguments(String[] strArgs) {
            if (strArgs.length != 2 || !strArgs[0].equals("ERROR")) {
                System.err.println("Wrong Arguments");
                return false;
            }
            try {
                int num = Integer.parseInt(strArgs[1]);
                if (num < 1 || num > 1000000) {
                    System.err.println("Second Argument must be a number between 1 and 1000000");
                    return false;
                }
                return true;
            } catch (Exception e) {
                System.err.println("Second Argument must be a number");
                return false;
            }
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] strArgs = line.split("\\s+");
                if (checkArguments(strArgs)) {
                    int numByte = Integer.parseInt(strArgs[1]) - 1;
                    cloudBytes[numByte].makeByteCorrupt();
                    System.out.println("Injected Error into byte number " + (numByte + 1) + ": " + cloudBytes[numByte]);
                }
            }
        }

    }

    //DONE
    private void getCloudBytesFromStorageNodes(){
        ArrayList<NodeInformation> storageNodes = getListOfStorageNodes();
        if(storageNodes.size()==0){
            System.err.println("Not enough nodes connected do directory");
            System.exit(1);
        }
        synchronizedQueue<ByteBlockRequest> queue = new synchronizedQueue<>();
        for (int i = 0; i < 10000; i++)
            queue.add(new ByteBlockRequest(i*100,100));
        ArrayList<Thread> threads = new ArrayList<>();
        for (NodeInformation storageNode : storageNodes)
            threads.add(new getStorageNodeData(storageNode.getAddress(), storageNode.getPort(), queue));
        threads.forEach(t->t.start());
        threads.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    //DONE
    private ArrayList<NodeInformation> getListOfStorageNodes()  {
        ArrayList<NodeInformation> list = new ArrayList<>();
        outDirectory.println("nodes");
        try {
            while (true){
                String line = inDirectory.readLine();
                if (line.equals("end"))
                    break;
                String[] values = line.split("\\s+");
                if(values[1].equals("localhost/127.0.0.1"))     //InetAddress.getByName can't recognize this address
                    values[1]="localhost";
                if(!values[2].equals(""+port))
                    list.add(new NodeInformation(InetAddress.getByName(values[1]),Integer.parseInt(values[2]),false));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while trying to receive list of StorageNodes");
        }
        System.out.println("List of StorageNodes to contact:");
        list.forEach(i-> System.out.println(i.getAddress()+"  "+i.getPort()));
        System.out.println("Download Started...");
        return list;
    }

    //DONE
    private class getStorageNodeData extends Thread{

        private ObjectInputStream in;
        private ObjectOutputStream out;
        private synchronizedQueue<ByteBlockRequest> queue;
        private int blocksTransferred;
        private final InetAddress address;
        private final int port;

        public getStorageNodeData(InetAddress address, int port,synchronizedQueue<ByteBlockRequest> queue){
            this.address=address;
            this.port=port;
            this.queue=queue;
            try {
                Socket socket = new Socket(address, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error while trying to connect with StorageNodes");
            }
        }

        @Override
        public void run() {
            while (!queue.isEmpty()){
                try{
                    ByteBlockRequest request = queue.get();
                    try {
                        out.writeObject(request);
                        CloudByte[] dataReceived = (CloudByte[]) in.readObject();
                        for (int i = request.getStartIndex(),j = 0; i < request.getStartIndex() + 100; i++, j++)
                            cloudBytes[i] = dataReceived[j];
                        blocksTransferred++;
                    } catch (IOException | ClassNotFoundException ioException) {
                        System.err.println("Error while sending or receiving data");
                        ioException.printStackTrace();
                    }
                }catch (IllegalStateException e){} //list is empty
            }
            System.out.println("Transfer finished from StorageNode Address:"+address+" Port:"+port +" \n"+
                    "Blocks Transferred:"+ blocksTransferred +" = "+(blocksTransferred *100)+" bytes");
        }
    }

    //DONE
    private void startServing() throws IOException {
        ServerSocket storageNodeServerSocket = new ServerSocket(port);
        try {
            System.out.println("Awaiting connections...");
            while (true) {
                Socket clientSocket = storageNodeServerSocket.accept();
                new DealWithClient(clientSocket).start();
            }
        }
        catch (IOException e) {
            System.err.println("Error while serving clients");
            e.printStackTrace();
        }
    }

    //DONE
    private class DealWithClient extends Thread {

        private ObjectInputStream in;
        private ObjectOutputStream out;

        public DealWithClient(Socket s) throws IOException {
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    ByteBlockRequest msgReceived = (ByteBlockRequest) in.readObject();
                    int startIndex = msgReceived.getStartIndex();
                    int length = msgReceived.getLength();
                    CloudByte[] msgResponse = new CloudByte[length];
                    for (int i = startIndex; i < startIndex + length; i++) {
                        if (!cloudBytes[i].isParityOk()) {
                            System.out.println("Error in byte number " + (i+1) + ": " + cloudBytes[i]);
                            correctError(i);
                        }
                    }
                    for (int i = startIndex, j = 0; i < startIndex + length; i++, j++)
                        msgResponse[j] = cloudBytes[i];
                    out.reset();
                    out.writeObject(msgResponse);
                } catch (IOException | ClassNotFoundException e) {
                    //System.err.println("Error while transferring data");
                   // e.printStackTrace();
                }
            }
        }
    }

    //TODO
    private class errorCorrector extends Thread{

        private CountDownLatch cdl;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private synchronizedQueue<CloudByte> cbsReceived;
        private final InetAddress address;
        private final int port;
        private final ByteBlockRequest cloudByteRequested;

        public errorCorrector(InetAddress address, int port, CountDownLatch cdl,
                              ByteBlockRequest cloudByteRequested,synchronizedQueue<CloudByte> cbsReceived){
            this.cbsReceived=cbsReceived;
            this.cloudByteRequested = cloudByteRequested;
            this.cdl=cdl;
            this.address=address;
            this.port=port;
            try {
                Socket socket = new Socket(address, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error while trying to connect with StorageNodes (errorCorrector)");
            }
        }

        @Override
        public void run() {
            try {
                out.writeObject(cloudByteRequested);
                CloudByte[] dataReceived = (CloudByte[]) in.readObject();
                cbsReceived.add(dataReceived[0]);
                System.out.println("CloudByte received:"+dataReceived[0]+" From: "+address + " Port:"+port);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error while sending or receiving data (errorCorrector)");
                e.printStackTrace();
            }
            System.out.println("countdown from StorageNode Address:"+address+" Port:"+port);
            cdl.countDown();
        }
    }

    //TODO
    private void correctError(int position){
        ArrayList<NodeInformation> storageNodes = getListOfStorageNodes();
        if(storageNodes.size()<2){
            System.out.println("Not enough StorageNodes to correct error");
            return;
        }
        synchronizedQueue<CloudByte> cbsReceived = new synchronizedQueue<>();
        CountDownLatch cdl = new CountDownLatch(2);
        ArrayList<Thread> threads = new ArrayList<>();
        ByteBlockRequest cloudByteRequested = new ByteBlockRequest(position,1);
        for (NodeInformation storageNode : storageNodes)
            threads.add(new errorCorrector(storageNode.getAddress(), storageNode.getPort(), cdl, cloudByteRequested,cbsReceived));
        threads.forEach(Thread::start);
        try {
            cdl.await();
            CloudByte cb1 = cbsReceived.get();
            CloudByte cb2 = cbsReceived.get();
            if(cb1.equals(cb2)){
                System.out.print("CloudByte:"+cloudBytes[cloudByteRequested.getStartIndex()] +" corrected to -> ");
                System.out.println(cb1);
                cloudBytes[cloudByteRequested.getStartIndex()]=cb1;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error while waiting for errorCorrectors");
        }
        /*threads.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });*/
    }

    //TODO check args
    public static void main(String[] args) throws IOException {
        if(args.length < 3 || args.length > 4)
            throw new IllegalStateException("Number of arguments must be 3 or 4");

        switch (args.length){
            case 4:
                try{
                    int directoryPort = Integer.parseInt(args[1]);
                    int nodePort = Integer.parseInt(args[2]);
                    if(!args[3].equals("data.bin")){
                        System.err.println("File not found");
                        return;
                    }
                    if(directoryPort<1024 || directoryPort> 65535 ||
                            nodePort<1024 || nodePort> 65535 || directoryPort==nodePort){
                        System.err.println("Wrong port values");
                        return;
                    }
                }catch (Exception e){
                    System.err.println("Second and third argument must be Integers");
                    return;
                }
                new StorageNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                break;
            case 3:
                try{
                    int directoryPort = Integer.parseInt(args[1]);
                    int nodePort = Integer.parseInt(args[2]);
                    if(directoryPort<1024 || directoryPort> 65535 ||
                            nodePort<1024 || nodePort> 65535 || directoryPort==nodePort){
                        System.err.println("Wrong port values");
                        return;
                    }
                }catch (NumberFormatException e){
                    System.err.println("Second and third argument must be Integers");
                    return;
                }
                new StorageNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                break;
        }
    }
}
