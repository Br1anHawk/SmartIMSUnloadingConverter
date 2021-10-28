import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;
import com.toedter.calendar.demo.DateChooserPanel;
import consumption.BalancedConsumption;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import unloading.UploadingConverter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Dialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonConvertUploading;
    private JButton buttonLoadAndFillDifferentiatedRatesFileReport;
    private JTextField textFieldColumnNumberOfMeterNumber;
    private JTextField textFieldColumnNumberOfMeterReadings;
    private JTextField textFieldColumnNumberOfRemark;
    private JButton buttonSettings;
    private JPanel jPannelSettings;
    private JButton buttonCalculateBalancedConsumption;
    private JSpinnerDateEditor dateTargetChooser;
    private JFileChooser fileChooser;

    private UploadingConverter uploadingConverter;
    private BalancedConsumption balancedConsumption;

    public Dialog() {
        super((Window) null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonLoadAndFillDifferentiatedRatesFileReport.setEnabled(false);
        jPannelSettings.setVisible(false);
        pack();

        Calendar uploadingDateTarget = Calendar.getInstance();
        uploadingDateTarget.set(
                uploadingDateTarget.get(Calendar.YEAR),
                uploadingDateTarget.get(Calendar.MONTH),
                01,
                0, 0, 0
        );
        dateTargetChooser.setDate(uploadingDateTarget.getTime());
        dateTargetChooser.setDateFormatString((new SimpleDateFormat("dd-MM-yyyy")).toPattern());

        buttonConvertUploading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xls"));
                int isFileSelectedInt = fileChooser.showOpenDialog(contentPane);
                if (isFileSelectedInt == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
//                    Calendar uploadingDateTarget = Calendar.getInstance();
//                    uploadingDateTarget.set(
//                            uploadingDateTarget.get(Calendar.YEAR),
//                            uploadingDateTarget.get(Calendar.MONTH),
//                            01,
//                            0, 0, 0
//                    );
                    uploadingDateTarget.setTime(dateTargetChooser.getDate());
                    uploadingConverter = new UploadingConverter(selectedFile, uploadingDateTarget);
                    buttonLoadAndFillDifferentiatedRatesFileReport.setEnabled(true);
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
                    uploadingConverter.loadDifferentiatedRatesFileReport(
                            selectedFile,
                            textFieldColumnNumberOfMeterNumber.getText(),
                            textFieldColumnNumberOfMeterReadings.getText(),
                            textFieldColumnNumberOfRemark.getText()
                    );
                    uploadingConverter.fillDifferentiatedRatesFileReportWithDataOfSubscribers();
                }
            }
        });

        buttonSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (jPannelSettings.isVisible()) {
                    jPannelSettings.setVisible(false);
                } else {
                    jPannelSettings.setVisible(true);
                }
                pack();
            }
        });

        buttonCalculateBalancedConsumption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xls"));
                int isFileSelectedInt = fileChooser.showOpenDialog(contentPane);
                if (isFileSelectedInt == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    balancedConsumption = new BalancedConsumption(selectedFile);
                    balancedConsumption.saveReportToTheFile();
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
