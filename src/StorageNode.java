import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StorageNode {

    private CloudByte[] cloudBytes = new CloudByte[1000000];

    public StorageNode(){
        createCloudBytes();
    }

    private void createCloudBytes(){
        try {
            byte[] fileContents = Files.readAllBytes(new File("data.bin").toPath());
            for (int i = 0; i < fileContents.length; i++) {
                cloudBytes[i] = new CloudByte(fileContents[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading file");
        }
    }





    public static void main(String[] args) {
        if(args.length <3 || args.length >4)
            throw new IllegalArgumentException("The number of arguments must be 3 or 4");

        StorageNode sn = new StorageNode();
    }
}
