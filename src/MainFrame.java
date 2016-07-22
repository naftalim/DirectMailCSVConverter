import com.opencsv.CSVReader;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;


import static java.util.Arrays.asList;

/**
 * Created by naftali on 7/21/16.
 */
public class MainFrame extends JFrame {

    java.util.List<String> requiredColumnHeaders = asList("seqno", "ADDRESS1", "CITY", "STATE", "ZIP", "phone1", "VIN", "MAKE", "MODEL", "YEAR", "email", "year2", "make2", "model2","first", "last");
    JFileChooser fileChooser = new JFileChooser();
    List<String[]> csvList;
    HashMap<String, Integer> csvColumnIndex = new HashMap<>();

    MainFrame(){

        //setSize(400, 600);
        add(new MainPanel());
        pack();
    }
    class MainPanel extends JPanel {
        ArrayList<TextField> textFields = new ArrayList<>();
        MainPanel(){
            setLayout(new MigLayout());
            for(String requiredColumnHeader: requiredColumnHeaders){
                addInputRow(requiredColumnHeader);
            }
            JPanel buttonPanel = new JPanel();
            JButton button = new JButton("upload CSV");
            button.addActionListener(new GetCsvFile());
            buttonPanel.add(button);
            add(buttonPanel, "span");
            JButton button1 = new JButton("test");
            button1.addActionListener(new TestCsvIndex());
            buttonPanel.add(button1);
        }
        private void addInputRow(String columnHeader){
            add(new Label(columnHeader));
            TextField textField = new TextField(50);
            textField.setName(columnHeader);
            textFields.add(textField);
            add(textField, "wrap");
        }

        class TestCsvIndex implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {
                int size = csvList.size();
                for(int i = 1; i < size; i++){
                    System.out.println(csvList.get(i)[csvColumnIndex.get("city")]);
                }
            }
        }


        class GetCsvFile implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               int result = fileChooser.showOpenDialog(MainPanel.this);
               if (result == JFileChooser.APPROVE_OPTION) {
                   try {
                       CSVReader reader = new CSVReader(new FileReader(fileChooser.getSelectedFile().getPath()));
                       csvList = reader.readAll();
                       System.out.print("total elements = " + csvList.size() + " \n");
                       System.out.print("total elements in element " + csvList.get(0).length + " \n");
                       int index = 0;
                       for(String s: csvList.get(0)){
                           csvColumnIndex.put(s.trim().toLowerCase(), index++);
                       }



                   }
                   catch (FileNotFoundException exception){
                       exception.printStackTrace();
                   }
                   catch (Exception exception){
                       exception.printStackTrace();
                   }

               }
            }
        }
    }

}
