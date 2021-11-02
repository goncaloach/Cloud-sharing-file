import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataClient {

    private JFrame frame;

    public DataClient(){
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.setSize(440,140);
        frame.setVisible(true);
    }

    private void addFrameContent() {
        frame.setLayout(new FlowLayout());

        frame.add(new JLabel("Consult Position:"));
        JTextField txtConsultPosition = new JTextField(7);
        frame.add(txtConsultPosition);
        txtConsultPosition.setText("0");

        frame.add(new JLabel("Length:"));
        JTextField txtLength = new JTextField(7);
        frame.add(txtLength);
        txtLength.setText("0");

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
        DataClient dc = new DataClient();
    }
}
