import java.net.InetAddress;

public class NodeInformation{
    private final InetAddress address;
    private final int port;
    private boolean readyToServe;

    public NodeInformation(InetAddress address, int port, boolean readyToServe) {
        this.address = address;
        this.port = port;
        this.readyToServe = readyToServe;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isReadyToServe() {
        return readyToServe;
    }

    public void setReadyToServe(){
        this.readyToServe = true;
    }
}