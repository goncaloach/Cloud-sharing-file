import java.net.InetAddress;

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