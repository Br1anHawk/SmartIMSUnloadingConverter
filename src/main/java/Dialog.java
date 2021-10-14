import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.io.File;

public class Dialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonConvertUploading;
    private JButton buttonLoadAndFillDifferentiatedRatesFileReport;
    private JTextField textFieldColumnNumberOfMeterNumber;
    private JTextField textFieldColumnNumberOfMeterReadings;
    private JFileChooser fileChooser;

    private UploadingConverter uploadingConverter;

    public Dialog() {
        super((Window) null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonConvertUploading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xls"));
                int isFileSelectedInt = fileChooser.showOpenDialog(contentPane);
                if (isFileSelectedInt == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    uploadingConverter = new UploadingConverter(selectedFile);
                }
            }
        });

        buttonLoadAndFillDifferentiatedRatesFileReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
                int isFileSelectedInt = fileChooser.showOpenDialog(contentPane);
                if (isFileSelectedInt == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    uploadingConverter.loadDifferentiatedRatesFileReport(selectedFile, textFieldColumnNumberOfMeterNumber.getText(), textFieldColumnNumberOfMeterReadings.getText());
                    uploadingConverter.fillDifferentiatedRatesFileReportWithDataOfSubscribers();
                }
            }
        });
    }

    public static void main(String[] args) {
        Dialog dialog = new Dialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
