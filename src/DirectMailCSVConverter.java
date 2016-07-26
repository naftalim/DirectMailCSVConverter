import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Created by naftali on 7/14/16.
 */
public class DirectMailCSVConverter {

    public static void main(String args[])
    {
        EventQueue.invokeLater( () -> {
            MainFrame frame = new MainFrame();
            frame.setTitle("DirectMail CSV to Excel Tool");
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);


                });
    }
}
