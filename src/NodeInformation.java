import java.net.InetAddress;

/**
 @author Gonçalo Henriques nº93205
 */

//Class used to represent a StorageNode's address and port

public class NodeInformation{
    private final InetAddress address;
    private final int port;

    public NodeInformation(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return  "node "+address.getHostAddress()+" "+ port;
    }
}