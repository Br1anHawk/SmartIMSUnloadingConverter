import com.linuxense.javadbf.DBFDataType
import com.linuxense.javadbf.DBFField
import com.linuxense.javadbf.DBFWriter
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UploadingConverter(
    private val uploadingFile: File
) {
    private var uploadingSheet: Sheet
    private var uploadingWorkbook: Workbook

    private var uploadingDataOfSubscribersHousehold: MutableList<Subscriber> = mutableListOf()
    private var uploadingDataOfSubscribersLegal: MutableList<Subscriber> = mutableListOf()

    private var differentiatedRatesFileReport: File? = null
    private var differentiatedRatesFileReportColumnNumberOfMeterNumber = 2
    private var differentiatedRatesFileReportColumnNumberOfMeterReadings = 4

    init {
        val fileInputStream = FileInputStream(uploadingFile)
        uploadingWorkbook = HSSFWorkbook(fileInputStream)
        uploadingSheet = uploadingWorkbook.getSheetAt(0)
        uploadingWorkbook.close()
        fileInputStream.close()
        getDataFromUploadingSheet()
        createDBFUploadingFile(UPLOADING_DBF_HOUSEHOLD_FILE_NAME, uploadingDataOfSubscribersHousehold)
        createDBFUploadingFile(UPLOADING_DBF_LEGAL_FILE_NAME, uploadingDataOfSubscribersLegal)
    }

    private fun getDataFromUploadingSheet() {
        var contentSheetLineNumberPosition = 2
        while (true) {
            val rowContent = uploadingSheet.getRow(contentSheetLineNumberPosition)
            if (rowContent.getCell(COLUMN_NUMBER_ID).cellType == CellType.BLANK) {
                break
            }
            if(isErrorDataInRow(rowContent)) {
                contentSheetLineNumberPosition++
                continue
            }

            val code = rowContent.getCell(COLUMN_NUMBER_CODE).stringCellValue.trim()
            val addressProperties: MutableList<String> = mutableListOf()
            for (property in rowContent.getCell(COLUMN_NUMBER_ADDRESS).stringCellValue.split(", ").toTypedArray()) {
                addressProperties.add(property)
            }
            while (addressProperties.size < 5) addressProperties.add("")

            var meterReadingSum = 0.0
            when (rowContent.getCell(COLUMN_NUMBER_METER_READING_SUM).cellType) {
                CellType.NUMERIC -> meterReadingSum = rowContent.getCell(COLUMN_NUMBER_METER_READING_SUM).numericCellValue
                CellType.BLANK -> meterReadingSum = rowContent.getCell(COLUMN_NUMBER_METER_READING_SUM_ALTERNATIVE).numericCellValue
            }
            var meterReadingRateOne = 0.0
            when (rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_ONE).cellType) {
                CellType.NUMERIC -> meterReadingRateOne = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_ONE).numericCellValue
                CellType.BLANK -> meterReadingRateOne = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_ONE_ALTERNATIVE).numericCellValue
            }
            var meterReadingRateTwo = 0.0
            when (rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_TWO).cellType) {
                CellType.NUMERIC -> meterReadingRateTwo = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_TWO).numericCellValue
                CellType.BLANK -> meterReadingRateTwo = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_TWO_ALTERNATIVE).numericCellValue
            }

            val subscriber = Subscriber(
                rowContent.getCell(COLUMN_NUMBER_DATE_OF_READINGS).dateCellValue,
                rowContent.getCell(COLUMN_NUMBER_METER_NUMBER).stringCellValue,
                code,
                Address(
                    addressProperties[0],
                    addressProperties[1],
                    addressProperties[2],
                    addressProperties[3],
                    addressProperties[4]
                ),
                meterReadingSum,
                meterReadingRateOne,
                meterReadingRateTwo
            )
            if (code.toLongOrNull() != null) {
                if (code.length == PERSONAL_ACCOUNT_LENGTH) {
                    uploadingDataOfSubscribersHousehold.add(subscriber)
                } else {
                    uploadingDataOfSubscribersLegal.add(subscriber)
                }
            } else {
                uploadingDataOfSubscribersLegal.add(subscriber)
            }
            contentSheetLineNumberPosition++
        }
    }

    private fun isErrorDataInRow(row: Row): Boolean {
        var isError = false
        if (row.getCell(COLUMN_NUMBER_CODE).cellType == CellType.BLANK) isError = true
        if (row.getCell(COLUMN_NUMBER_ADDRESS).cellType == CellType.BLANK) isError = true

        return isError
    }

    private fun createDBFUploadingFile(prefixFileName: String, uploadingData: MutableList<Subscriber>) {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")
        val fileName = prefixFileName + formatter.format(calendar.time) + ".dbf"
        val fos = FileOutputStream(fileName)
        val writerDBF = DBFWriter(fos)
        val fields: MutableList<DBFField> = mutableListOf()

        var field = DBFField()
        field.name = "NUMABON"
        field.type = DBFDataType.CHARACTER
        field.length = 15
        fields.add(field)

        field = DBFField()
        field.name = "ZAVOD"
        field.type = DBFDataType.CHARACTER
        field.length = 20
        fields.add(field)

        field = DBFField()
        field.name = "TIP_SC"
        field.type = DBFDataType.CHARACTER
        field.length = 20
        fields.add(field)

        field = DBFField()
        field.name = "NOM_SC"
        field.type = DBFDataType.CHARACTER
        field.length = 20
        fields.add(field)

        field = DBFField()
        field.name = "UL"
        field.type = DBFDataType.CHARACTER
        field.length = 100
        fields.add(field)

        field = DBFField()
        field.name = "DOM"
        field.type = DBFDataType.CHARACTER
        field.length = 4
        fields.add(field)

        field = DBFField()
        field.name = "KORP"
        field.type = DBFDataType.CHARACTER
        field.length = 4
        fields.add(field)

        field = DBFField()
        field.name = "KV"
        field.type = DBFDataType.CHARACTER
        field.length = 4
        fields.add(field)

        field = DBFField()
        field.name = "DAT"
        field.type = DBFDataType.DATE
        //field.length = 8
        fields.add(field)

        field = DBFField()
        field.name = "POKP_ALL"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_1"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_2"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_3"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_4"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_5"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_6"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_7"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKP_8"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_ALL"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_1"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_2"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_3"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_4"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_5"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_6"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_7"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        field = DBFField()
        field.name = "POKO_8"
        field.type = DBFDataType.NUMERIC
        field.length = 12
        field.decimalCount = 2
        fields.add(field)

        writerDBF.setFields(fields.toTypedArray())

        val rowData: MutableList<Any> = mutableListOf()
        for (subscriber in uploadingData) {
            rowData.add(subscriber.code)
            rowData.add(DBF_PROPERTY_ZAVOD)
            rowData.add("")
            rowData.add(subscriber.meterNumber)
            rowData.add(subscriber.address.street)
            rowData.add(subscriber.address.buildingNumber)
            rowData.add("")
            rowData.add(subscriber.address.accountingPoint)
            rowData.add(subscriber.dateOfReadings)
            rowData.add(subscriber.meterReadingSum)
            for (i in 1..17) {
                rowData.add(0.0)
            }
            writerDBF.addRecord(rowData.toTypedArray())
            rowData.clear()
        }
        writerDBF.close()

        fos.flush()
        fos.close()
    }

    fun loadDifferentiatedRatesFileReport(file: File, columnNumberOfMeterNumber: String, columnNumberOfMeterReadings: String) {
        differentiatedRatesFileReport = file
        columnNumberOfMeterNumber.toIntOrNull()?.let { differentiatedRatesFileReportColumnNumberOfMeterNumber = it }
        columnNumberOfMeterReadings.toIntOrNull()?.let { differentiatedRatesFileReportColumnNumberOfMeterReadings = it }
    }

    fun fillDifferentiatedRatesFileReportWithDataOfSubscribers() {
        differentiatedRatesFileReport ?: return
        val differentiatedRatesFileReport = requireNotNull(this.differentiatedRatesFileReport)

        val fileInputStream = FileInputStream(differentiatedRatesFileReport)
        val workbook = XSSFWorkbook(fileInputStream)
        val sheet = workbook.getSheetAt(0)
        fileInputStream.close()

        var contentSheetLineNumberPosition = 1
        while (sheet.getRow(contentSheetLineNumberPosition) == null) {
            contentSheetLineNumberPosition++
            continue
        }

        while (true) {
            val rowContent = sheet.getRow(contentSheetLineNumberPosition)
            rowContent ?: break
            var meterNumber = ""
            val cell = rowContent.getCell(differentiatedRatesFileReportColumnNumberOfMeterNumber)
            if (cell == null) {
                contentSheetLineNumberPosition++
                continue
            }
            when (cell.cellType) {
                CellType.STRING -> {
                    val cellContent = cell.stringCellValue
                    if (cellContent.toIntOrNull() != null) {
                        meterNumber = cellContent
                    } else {
                        contentSheetLineNumberPosition++
                        continue
                    }
                }
                CellType.NUMERIC -> meterNumber = cell.numericCellValue.toInt().toString()
            }

            val subscriber = uploadingDataOfSubscribersHousehold.find { it.meterNumber == meterNumber }
            rowContent.getCell(differentiatedRatesFileReportColumnNumberOfMeterReadings).setCellValue(subscriber?.meterReadingSum?.toInt().toString())
            contentSheetLineNumberPosition++
            sheet.getRow(contentSheetLineNumberPosition).getCell(differentiatedRatesFileReportColumnNumberOfMeterReadings).setCellValue(subscriber?.meterReadingRateOne?.toInt().toString())
            contentSheetLineNumberPosition++
            sheet.getRow(contentSheetLineNumberPosition).getCell(differentiatedRatesFileReportColumnNumberOfMeterReadings).setCellValue(subscriber?.meterReadingRateTwo?.toInt().toString())
            contentSheetLineNumberPosition++
        }

        val fileOutputStream = FileOutputStream(differentiatedRatesFileReport)
        workbook.write(fileOutputStream)
        workbook.close()
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    private fun correctUploading() {

        //save()
    }

    private fun save() {
        val fileOutputStream = FileOutputStream(uploadingFile)
        uploadingWorkbook.write(fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    companion object {
        const val COLUMN_NUMBER_ID = 0
        const val COLUMN_NUMBER_DATE_OF_READINGS = 1
        const val COLUMN_NUMBER_METER_NUMBER = 2
        const val COLUMN_NUMBER_CODE = 4
        const val COLUMN_NUMBER_ADDRESS = 5

        const val COLUMN_NUMBER_METER_READING_SUM = 6
        const val COLUMN_NUMBER_METER_READING_SUM_ALTERNATIVE = 10

        const val COLUMN_NUMBER_METER_READING_RATE_ONE = 7
        const val COLUMN_NUMBER_METER_READING_RATE_ONE_ALTERNATIVE = 11

        const val COLUMN_NUMBER_METER_READING_RATE_TWO = 8
        const val COLUMN_NUMBER_METER_READING_RATE_TWO_ALTERNATIVE = 12

        const val PERSONAL_ACCOUNT_LENGTH = 9

        const val UPLOADING_DBF_HOUSEHOLD_FILE_NAME = "Smart_IMS_uploading_household_"
        const val UPLOADING_DBF_LEGAL_FILE_NAME = "Smart_IMS_uploading_legal_"
        const val DBF_PROPERTY_ZAVOD = "TeleTec"

        const val DATA_NO_FOUND = "\u2014" //long dash
        const val WORKING = "Работает"
        const val NOT_WORKING = "Не работает"
    }
}