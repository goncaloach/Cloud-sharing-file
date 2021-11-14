import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataClient {

    public DataClient(String adress, int port){
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


    public static void main(String[] args) {
        if(args.length != 2)
            throw new IllegalArgumentException("The number of arguments must be 2");
        DataClient dc = new DataClient(args[0],Integer.parseInt(args[1]));
        dc.openGUI();
    }
}
