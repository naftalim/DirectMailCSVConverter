import com.opencsv.CSVReader;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;


import static java.util.Arrays.asList;

/**
 * Created by naftali on 7/21/16.
 */
public class MainFrame extends JFrame {

    List<String> requiredColumnHeaders = asList("seqno", "ADDRESS1", "CITY", "STATE", "ZIP", "phone1", "VIN", "MAKE", "MODEL", "YEAR", "email", "year2", "make2", "model2","first", "last");
    List<String> allColumnHeaders = asList("seqno","cust_no", "name_line", "ADDRESS1", "CITY", "STATE", "ZIP", "phone1", "VIN", "MAKE", "MODEL", "YEAR", "deldate","email","lastactive","created", "year2", "make2", "model2","rextra", "rclean", "tradein_va", "rrough", "tclean", "tavg", "trough", "first", "last");
    HashMap<String, Integer> excelHeaderToCsvRowIndexMap = new HashMap<>();
    JFileChooser fileChooser = new JFileChooser();
    List<String[]> csvList;
    JScrollPane scrollPane = new JScrollPane();
    DefaultListModel listModel = new DefaultListModel();
    JList<String> csvColumnListElement = new JList<>(listModel);
    Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
    JPanel scrollPanel = new JPanel();
    String[] csvColumnHeaders = new String[] {""};
    HashMap<String, Integer> csvColumnIndex = new HashMap<>();
    JPanel mainPanel;
    File lastOpenedDirectory;
    String lastOpenedFileName;
    JButton excelButton;

    MainFrame(){
        mainPanel = new MainPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);
        pack();
    }
    class MainPanel extends JPanel {
        ArrayList<JTextField> textFields = new ArrayList<>();

        MainPanel(){
            setLayout(new MigLayout());
            setUpButtons();
            setUpScrollPane();
            setUpInputRows();
        }
        private void setUpButtons(){
            JPanel buttonPanel = new JPanel();
            JButton button = new JButton("Upload CSV");
            button.addActionListener(new GetCsvFile());
            buttonPanel.add(button);
            excelButton = new JButton("Create Excel");
            excelButton.setEnabled(false);
            excelButton.addActionListener(new ValidateCsvFieldsCreateExcelFile());
            buttonPanel.add(excelButton);
            add(buttonPanel, "south");
        }
        private void setUpScrollPane(){
            DefaultListModel listModel = new DefaultListModel();
            csvColumnListElement = new JList<>(listModel);
            csvColumnListElement.addListSelectionListener(new PutListItemSelectionInClipboard());
            scrollPane.getViewport().add(csvColumnListElement);
            scrollPane.setPreferredSize(new Dimension(200,470));
            scrollPane.setLocation(0,0);
            scrollPanel.add(scrollPane);
            add(scrollPanel, "east");
        }


        private void setUpInputRows(){
            for(String requiredColumnHeader: requiredColumnHeaders){
                addInputRow(requiredColumnHeader);
            }
        }

        private void addInputRow(String columnHeader){
            add(new JLabel(columnHeader));
            JTextField textField = new JTextField(15);
            textField.setEnabled(false);
            textField.setName(columnHeader);
            textField.addMouseListener(new PutContentsOfClipboardIntoTextField());
            textFields.add(textField);
            add(textField, "wrap");
        }
        private boolean validateTextFieldValues(){
            boolean ok = true;
            int fieldsCount = textFields.size();
            for (int i = 0; i < fieldsCount; i++){
                JTextField localTextField = textFields.get(i);

                if(!Arrays.asList(csvColumnHeaders).contains(localTextField.getText().trim())){
                    ok=false;
                    localTextField.setBackground(Color.YELLOW);
                }else{
                    excelHeaderToCsvRowIndexMap.put(localTextField.getName().trim(), csvColumnIndex.get(localTextField.getText().trim()));
                    localTextField.setBackground(Color.WHITE);
                }

            }

            return ok;

        }
        private void saveExcelFile(XSSFWorkbook workBook){
            fileChooser.setSelectedFile(new File(FilenameUtils.getBaseName(lastOpenedFileName) + ".xlsx"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel 2007", "xlsx"));
            int status = fileChooser.showSaveDialog(MainPanel.this);
            FileOutputStream out;
            if(status == JFileChooser.APPROVE_OPTION){
                try {
                    out = new FileOutputStream(fileChooser.getSelectedFile());
                    workBook.write(out);
                    out.close();

                }
                catch(FileNotFoundException fileNotFoundException){
                    fileNotFoundException.printStackTrace();
                }
                catch(IOException ioException){
                    ioException.printStackTrace();
                }

            }
            fileChooser.resetChoosableFileFilters();

        }

        private XSSFWorkbook createExcelFile(){
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Direct Mailing Data");
            XSSFRow headRow = sheet.createRow(0);
            int count = allColumnHeaders.size();
            for(int i = 0; i < count; i++){
                Cell cell = headRow.createCell(i);
                cell.setCellValue(allColumnHeaders.get(i));
            }
            int csvRowCount = csvList.size();
            for(int i=1; i < csvRowCount; i++){
                XSSFRow currentRow = sheet.createRow(i);
                currentRow.createCell(0).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("seqno")]);
                currentRow.createCell(1).setCellValue("");
                currentRow.createCell(2).setCellValue("");
                currentRow.createCell(3).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("ADDRESS1")]);
                currentRow.createCell(4).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("CITY")]);
                currentRow.createCell(5).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("STATE")]);
                currentRow.createCell(6).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("ZIP")]);
                currentRow.createCell(7).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("phone1")]);
                currentRow.createCell(8).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("VIN")]);
                currentRow.createCell(9).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("MAKE")]);
                currentRow.createCell(10).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("MODEL")]);
                currentRow.createCell(11).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("YEAR")]);
                currentRow.createCell(12).setCellValue("");
                currentRow.createCell(13).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("email")]);
                currentRow.createCell(14).setCellValue("");
                currentRow.createCell(15).setCellValue("");
                currentRow.createCell(16).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("year2")]);
                currentRow.createCell(17).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("make2")]);
                currentRow.createCell(18).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("model2")]);
                currentRow.createCell(19).setCellValue("");
                currentRow.createCell(20).setCellValue("");
                currentRow.createCell(21).setCellValue("");
                currentRow.createCell(22).setCellValue("");
                currentRow.createCell(23).setCellValue("");
                currentRow.createCell(24).setCellValue("");
                currentRow.createCell(25).setCellValue("");
                currentRow.createCell(26).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("first")]);
                currentRow.createCell(27).setCellValue(csvList.get(i)[excelHeaderToCsvRowIndexMap.get("last")]);

            }
            return workbook;


        }

        class ValidateCsvFieldsCreateExcelFile implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {
               if(validateTextFieldValues()){
                    XSSFWorkbook workbook = createExcelFile();
                    saveExcelFile(workbook);
               }
            }
        }
        class PutListItemSelectionInClipboard implements ListSelectionListener {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    String selectedValue = (String) list.getSelectedValue();
                    StringSelection stringSelection = new StringSelection(selectedValue);
                    clipBoard.setContents(stringSelection, null);
                }

            }
        }
        class PutContentsOfClipboardIntoTextField extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTextField localTextField = (JTextField) event.getSource();
                    try {
                        localTextField.setText((String) clipBoard.getData(DataFlavor.stringFlavor));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    }
            }
        }


        class GetCsvFile implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               if(!(lastOpenedDirectory==null)){
                   fileChooser.setCurrentDirectory(lastOpenedDirectory);
               }
                fileChooser.setFileFilter(new FileNameExtensionFilter("csv files", "csv"));
               int result = fileChooser.showOpenDialog(MainPanel.this);
               if (result == JFileChooser.APPROVE_OPTION) {
                   try {
                       lastOpenedDirectory = fileChooser.getCurrentDirectory();
                       lastOpenedFileName = FilenameUtils.getBaseName(fileChooser.getSelectedFile().getName());
                       MainFrame.this.setTitle(lastOpenedFileName);
                       CSVReader reader = new CSVReader(new FileReader(fileChooser.getSelectedFile().getPath()));
                       csvList = reader.readAll();
                       System.out.print("total elements = " + csvList.size() + " \n");
                       System.out.print("total elements in element " + csvList.get(0).length + " \n");
                       csvColumnHeaders = csvList.get(0);
                       int index = 0;
                       for(String s: csvColumnHeaders){
                           csvColumnIndex.put(s.trim(), index++);
                           listModel.addElement(s);
                       }
                    for(JTextField t: textFields){
                        t.setText("");
                        t.setBackground(Color.WHITE);
                    }
                    csvColumnListElement.setListData(csvColumnHeaders);
                    excelButton.setEnabled(true);
                   }
                   catch (FileNotFoundException exception){
                       exception.printStackTrace();
                   }
                   catch (Exception exception){
                       exception.printStackTrace();
                   }

               }
                fileChooser.resetChoosableFileFilters();
                for(JTextField textField: textFields){
                    textField.setEnabled(true);
                }
            }
        }
    }

}
