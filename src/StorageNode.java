import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class StorageNode {

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
        searchForErrors();
        try {
            startServing();
        } catch (IOException e) {
            System.err.println("Error while connecting to Clients");
            e.printStackTrace();
            System.exit(1);
        }
        System.err.println("Something went wrong");
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
        System.out.println("Time elapsed:" + timeElapsed + " milliseconds");
        new injectErrorsFromConsole().start();
        searchForErrors();
        try {
            startServing();
        } catch (IOException e) {
            System.err.println("Error while connecting to Clients");
            e.printStackTrace();
            System.exit(1);
        }
        System.err.println("Something went wrong v2");
    }

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

    private void getCloudBytesFromStorageNodes() {
        ArrayList<NodeInformation> storageNodes = getListOfStorageNodes();
        if (storageNodes.size() == 0) {
            System.err.println("Not enough nodes connected do directory");
            System.exit(1);
        }
        synchronizedQueue<ByteBlockRequest> queue = new synchronizedQueue<>();
        for (int i = 0; i < 10000; i++)
            queue.add(new ByteBlockRequest(i * 100, 100));
        ArrayList<Thread> threads = new ArrayList<>();
        for (NodeInformation storageNode : storageNodes)
            threads.add(new getStorageNodeData(storageNode.getAddress(), storageNode.getPort(), queue));
        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private ArrayList<NodeInformation> getListOfStorageNodes() {
        ArrayList<NodeInformation> list = new ArrayList<>();
        outDirectory.println("nodes");
        try {
            while (true) {
                String line = inDirectory.readLine();
                if (line.equals("end"))
                    break;
                String[] values = line.split("\\s+");
                if (values[1].equals("localhost/127.0.0.1"))     //InetAddress.getByName can't recognize this address
                    values[1] = "localhost";                      //My version of Directory doesn't need this if
                if (!values[2].equals("" + port))
                    list.add(new NodeInformation(InetAddress.getByName(values[1]), Integer.parseInt(values[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while trying to receive list of StorageNodes");
        }
        System.out.println("List of StorageNodes to contact:");
        list.forEach(i -> System.out.println(i.getAddress() + "  " + i.getPort()));
        System.out.println("Download Started...");
        return list;
    }

    private class getStorageNodeData extends Thread {

        private ObjectInputStream in;
        private ObjectOutputStream out;
        private synchronizedQueue<ByteBlockRequest> queue;
        private int blocksTransferred;
        private final InetAddress address;
        private final int port;

        public getStorageNodeData(InetAddress address, int port, synchronizedQueue<ByteBlockRequest> queue) {
            this.address = address;
            this.port = port;
            this.queue = queue;
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
            while (!queue.isEmpty()) {
                try {
                    ByteBlockRequest request = queue.get();
                    try {
                        out.writeObject(request);
                        CloudByte[] dataReceived = (CloudByte[]) in.readObject();
                        for (int i = request.getStartIndex(), j = 0; i < request.getStartIndex() + 100; i++, j++)
                            cloudBytes[i] = dataReceived[j];
                        blocksTransferred++;
                    } catch (IOException | ClassNotFoundException ioException) {
                        System.err.println("Error while sending or receiving data");
                        ioException.printStackTrace();
                        System.exit(1);
                    }
                } catch (IllegalStateException e) {
                } //list is empty
            }
            System.out.println("Transfer finished from StorageNode Address:" + address + " Port:" + port + " \n" +
                    "Blocks Transferred:" + blocksTransferred + " = " + (blocksTransferred * 100) + " bytes");
        }
    }

    private void startServing() throws IOException {
        ServerSocket storageNodeServerSocket = new ServerSocket(port);
        try {
            System.out.println("Awaiting connections...");
            while (true) {
                Socket clientSocket = storageNodeServerSocket.accept();
                new DealWithClient(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error while serving clients");
            e.printStackTrace();
        }
    }

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
                            System.out.println("Error in byte number " + (i + 1) + ": " + cloudBytes[i]);
                            correctError(i);
                        }
                    }
                    for (int i = startIndex, j = 0; i < startIndex + length; i++, j++)
                        msgResponse[j] = cloudBytes[i];
                    out.reset();
                    out.writeObject(msgResponse);
                } catch (IOException | ClassNotFoundException e) {
                    //System.err.println("Error while transferring data");
                    //e.printStackTrace();
                }
            }
        }
    }

    private class errorCorrector extends Thread {

        private CountDownLatch cdl;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private synchronizedList<CloudByte> cbsReceived;
        private final InetAddress address;
        private final int port;
        private final ByteBlockRequest cloudByteRequested;

        public errorCorrector(InetAddress address, int port, CountDownLatch cdl,
                              ByteBlockRequest cloudByteRequested, synchronizedList<CloudByte> cbsReceived) {
            this.cbsReceived = cbsReceived;
            this.cloudByteRequested = cloudByteRequested;
            this.cdl = cdl;
            this.address = address;
            this.port = port;
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
                System.out.println("CloudByte received:" + dataReceived[0] + " From: " + address + " Port:" + port);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error while sending or receiving data (errorCorrector)");
                e.printStackTrace();
            }
            cdl.countDown();
        }
    }

    private void correctError(int position) {
        ArrayList<NodeInformation> storageNodes = getListOfStorageNodes();
        if (storageNodes.size() < 2) {
            System.out.println("Not enough StorageNodes to correct error");
            return;
        }
        synchronizedList<CloudByte> cbsReceived = new synchronizedList<>();
        CountDownLatch cdl = new CountDownLatch(2);
        ArrayList<Thread> threads = new ArrayList<>();
        ByteBlockRequest cloudByteRequested = new ByteBlockRequest(position, 1);
        for (NodeInformation storageNode : storageNodes)
            threads.add(new errorCorrector(storageNode.getAddress(), storageNode.getPort(), cdl, cloudByteRequested, cbsReceived));
        threads.forEach(Thread::start);
        try {
            cdl.await();
            try {
                for (int i = 0; i < storageNodes.size(); i++) {
                    for (int j = i + 1; j < storageNodes.size(); j++) {
                        CloudByte cb1 = cbsReceived.get(i);
                        CloudByte cb2 = cbsReceived.get(j);
                        if (cb1.equals(cb2)) {
                            System.out.println("CloudByte:" + cloudBytes[cloudByteRequested.getStartIndex()] + " corrected to -> " + cb1);
                            cloudBytes[cloudByteRequested.getStartIndex()] = cb1;
                            return;
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("empty");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error while waiting for errorCorrectors");
        }
    }

    private class queueFiller implements Runnable {

        private synchronizedQueue<Integer> queue;

        public queueFiller(synchronizedQueue<Integer> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            queue = fillQueue(queue);
        }
    }

    private synchronizedQueue<Integer> fillQueue(synchronizedQueue<Integer> queue) {
        for (int i = 0; i < 1000000; i++)
            queue.add(i);
        return queue;
    }

    private void searchForErrors() {
        synchronizedQueue<Integer> queue = new synchronizedQueue<>();
        queue = fillQueue(queue);
        CyclicBarrier barrier = new CyclicBarrier(2, new queueFiller(queue));
        errorSentinel[] threads = new errorSentinel[2];
        for (int i = 0; i < 2; i++) {
            threads[i] = new errorSentinel(i, queue, barrier);
            threads[i].start();
        }
    }

    private class errorSentinel extends Thread {

        private final int id;
        private synchronizedQueue<Integer> queue;
        private CyclicBarrier barrier;

        public errorSentinel(int id, synchronizedQueue<Integer> queue, CyclicBarrier barrier) {
            this.id = id;
            this.queue = queue;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            while (true) {
                loop();
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void loop() {
            while (!queue.isEmpty()) {
                try {
                    Integer index = queue.get();
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!cloudBytes[index].isParityOk()) {
                        correctError(index);
                        System.out.println("id:" + id + " cb:" + cloudBytes[index] + " index:" + index);
                    }
                } catch (IllegalStateException e) {
                } //queue is empty
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3 || args.length > 4)
            throw new IllegalStateException("Number of arguments must be 3 or 4");

        switch (args.length) {
            case 4:
                try {
                    int directoryPort = Integer.parseInt(args[1]);
                    int nodePort = Integer.parseInt(args[2]);
                    if (!args[3].equals("data.bin")) {
                        System.err.println("File not found");
                        return;
                    }
                    if (directoryPort < 1024 || directoryPort > 65535 ||
                            nodePort < 1024 || nodePort > 65535 || directoryPort == nodePort) {
                        System.err.println("Wrong port values");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Second and third argument must be Integers");
                    return;
                }
                new StorageNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                break;
            case 3:
                try {
                    int directoryPort = Integer.parseInt(args[1]);
                    int nodePort = Integer.parseInt(args[2]);
                    if (directoryPort < 1024 || directoryPort > 65535 ||
                            nodePort < 1024 || nodePort > 65535 || directoryPort == nodePort) {
                        System.err.println("Wrong port values");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Second and third argument must be Integers");
                    return;
                }
                new StorageNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                break;
        }
    }
}
