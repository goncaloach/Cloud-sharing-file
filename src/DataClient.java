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

    public DataClient(String addressText, int port){
        /*try {
            InetAddress address = InetAddress.getByName(addressText);
            Socket socket = new Socket(address, port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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
        txtConsultPosition.setText("0");

        frame.add(new JLabel("Length:"));
        JTextField txtLength = new JTextField(10);
        frame.add(txtLength);
        txtLength.setText("1");

        JButton consultButton = new JButton("Consult");
        frame.add(consultButton);

        JTextArea txtArea = new JTextArea();
        frame.add(txtArea);
        txtArea.setText("The answers will apear here:  ");

        consultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //txtConsultPosition.setText();
                //txtLength.setText();
                //txtArea.setText();
            }
        });
    }

    private boolean isIntegerInBounds( String input ) {
        try {
            int num = Integer.parseInt(input);
            if(num<0 || num>999999){
                System.err.println("Second argument must be a number between 0 and 999999");
                return  false;
            }
            return true;
        }
        catch( Exception e ) {
            System.err.println("Second argument must be a number between 0 and 999999");
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


        DataClient dc = new DataClient(args[0],Integer.parseInt(args[1]));
        dc.openGUI();
    }
}
