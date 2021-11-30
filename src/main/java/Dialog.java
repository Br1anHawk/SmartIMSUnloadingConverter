import com.toedter.calendar.JSpinnerDateEditor;
import consumption.BalancedConsumption;
import org.apache.poi.ss.formula.functions.T;
import unloading.UploadingConverter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
    private JTextField textFieldColumnNumberOfPersonalAccount;
    private JButton buttonCreateDiffRatesUnloading;
    private JList listExceptionDates;
    private JButton buttonAddExceptionDate;
    private JButton buttonDeleteExceptionDate;
    private JSpinnerDateEditor exceptionDateSpinner;
    private JFileChooser fileChooser;

    private UploadingConverter uploadingConverter;
    private BalancedConsumption balancedConsumption;

    private DefaultListModel<Date> listModelExceptionDates = new DefaultListModel<>();

    public Dialog() {
        super((Window) null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonLoadAndFillDifferentiatedRatesFileReport.setEnabled(false);
        buttonCreateDiffRatesUnloading.setEnabled(false);
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

        exceptionDateSpinner.setDate(uploadingDateTarget.getTime());
        exceptionDateSpinner.setDateFormatString((new SimpleDateFormat("dd-MM-yyyy")).toPattern());

        listExceptionDates.setModel(listModelExceptionDates);
        ((DefaultListCellRenderer) listExceptionDates.getCellRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

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
                    try {
                        uploadingConverter = new UploadingConverter(selectedFile, uploadingDateTarget, getListFromModel(listModelExceptionDates));
                        buttonLoadAndFillDifferentiatedRatesFileReport.setEnabled(true);
                        buttonCreateDiffRatesUnloading.setEnabled(true);
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage());
                    }
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
                            textFieldColumnNumberOfPersonalAccount.getText(),
                            textFieldColumnNumberOfMeterNumber.getText(),
                            textFieldColumnNumberOfMeterReadings.getText(),
                            textFieldColumnNumberOfRemark.getText()
                    );
                    try {
                        uploadingConverter.fillDifferentiatedRatesFileReportWithDataOfSubscribers();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage());
                    }
                }
            }
        });

        buttonCreateDiffRatesUnloading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
                int isFileSelectedInt = fileChooser.showOpenDialog(contentPane);
                if (isFileSelectedInt == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    uploadingConverter.loadDifferentiatedRatesFileReport(
                            selectedFile,
                            textFieldColumnNumberOfPersonalAccount.getText(),
                            textFieldColumnNumberOfMeterNumber.getText(),
                            textFieldColumnNumberOfMeterReadings.getText(),
                            textFieldColumnNumberOfRemark.getText()
                    );
                    try {
                        uploadingConverter.createDifferentiatedRatesFileUnloadingWithDataOfSubscribers();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage());
                    }
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
                    try {
                        balancedConsumption = new BalancedConsumption(selectedFile);
                        balancedConsumption.saveReportToTheFile();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage());
                    }
                }
            }
        });

        dateTargetChooser.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
            }
        });

        buttonAddExceptionDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listModelExceptionDates.addElement(new DateFormatted(exceptionDateSpinner.getDate().getTime()));
            }
        });

        buttonDeleteExceptionDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listModelExceptionDates.removeElement(listExceptionDates.getSelectedValue());
            }
        });

    }

    public static void main(String[] args) {
        Dialog dialog = new Dialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    class DateFormatted extends Date {

        public DateFormatted(long date) {
            super(date);
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(this);
        }
    }

    private static <T> List<T> getListFromModel(DefaultListModel<T> listModel) {
        List<T> list = new LinkedList<>();
        for (int i = 0; i < listModel.size(); i++) {
            list.add(listModel.elementAt(i));
        }
        return list;
    }
}
