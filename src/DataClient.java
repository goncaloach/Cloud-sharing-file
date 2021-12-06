import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class DataClient {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private JTextArea txtArea;

    public DataClient(String addressText, int port){
        try {
            InetAddress address = InetAddress.getByName(addressText);
            Socket socket = new Socket(address, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGUI() {
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600,140);
        frame.setVisible(true);

        frame.setLayout(new FlowLayout());

        frame.add(new JLabel("Consult Position:"));
        JTextField txtConsultPosition = new JTextField(10);
        frame.add(txtConsultPosition);
        txtConsultPosition.setText("1");

        frame.add(new JLabel("Length:"));
        JTextField txtLength = new JTextField(10);
        frame.add(txtLength);
        txtLength.setText("1");

        JButton consultButton = new JButton("Consult");
        frame.add(consultButton);

        txtArea = new JTextArea();
        frame.add(txtArea);
        txtArea.setText("The answers will apear here:  ");

        consultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(checkValues(txtConsultPosition.getText(),txtLength.getText())){
                    int position = Integer.parseInt(txtConsultPosition.getText())-1;
                    int length = Integer.parseInt(txtLength.getText());
                    length = truncateLength(position,length);

                    ByteBlockRequest msgRequest = new ByteBlockRequest(position,length);

                    try {
                        out.writeObject(msgRequest);
                        new MessageReceiver().start();
                    } catch (IOException ioException) {
                        System.err.println("Error while sending data");
                        ioException.printStackTrace();
                    }
                }
            }
        });
    }

    private class MessageReceiver extends Thread{

        @Override
        public void run() {
            try {
                CloudByte[] cloudBytes = (CloudByte[]) in.readObject();
                String cloudBytesTXT="";
                for (int i = 0; i < cloudBytes.length; i++){
                    cloudBytesTXT=cloudBytesTXT.concat(cloudBytes[i]+"  ");
                }

                txtArea.setText(cloudBytesTXT);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Error while receiving data");
                ex.printStackTrace();
            }
        }
    }

    private int truncateLength(int pos, int len){
        if(pos+len>1000000)
            return 1000000-pos;
        return len;
    }

    private boolean checkValues(String txtPosition, String txtLength) {
        try {
            int position = Integer.parseInt(txtPosition);
            int length = Integer.parseInt(txtLength);
            if(position<1 || position>1000000 || length<1 || length>1000000){
                System.err.println("Position and Length must be numbers between 1 and 1000000");
                return false;
            }
            return true;
        }
        catch( Exception e ) {
            System.err.println("Values must be numbers");
            return false;
        }
    }

    public static void main(String[] args) {
        if(args.length != 2)
            throw new IllegalArgumentException("The number of arguments must be 2");
        try{
            int num = Integer.parseInt(args[1]);
            DataClient dc = new DataClient(args[0],num);
            dc.openGUI();
        }catch( Exception e ) {
            throw new IllegalArgumentException("Second argument must be a number");
        }

    }
}
