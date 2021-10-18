package consumption

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import unloading.UploadingConverter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BalancedConsumption(
    private val uploadingFile: File
) {
    private val buildings: MutableList<Building> = mutableListOf()

    init {
        loadDataFromUploadingFile()
    }

    private fun loadDataFromUploadingFile() {
        val fileInputStream = FileInputStream(uploadingFile)
        val uploadingWorkbook = HSSFWorkbook(fileInputStream)
        val uploadingSheet = uploadingWorkbook.getSheetAt(0)
        uploadingWorkbook.close()
        fileInputStream.close()

        var contentSheetLineNumberPosition = 2
        while (true) {
            val rowContent = uploadingSheet.getRow(contentSheetLineNumberPosition)
            if (rowContent.getCell(COLUMN_NUMBER_ID).cellType == CellType.BLANK) {
                break
            }

            val date = rowContent.getCell(COLUMN_NUMBER_DATE_OF_READINGS).dateCellValue
            val meterNumber = rowContent.getCell(COLUMN_NUMBER_METER_NUMBER).stringCellValue
            var address = rowContent.getCell(COLUMN_NUMBER_ADDRESS).stringCellValue.toLowerCase().trim()

            if (address.isEmpty()) {
                contentSheetLineNumberPosition++
                continue
            }

            val delimiter = ", "
            val accountingPointName = address.substring(address.lastIndexOf(delimiter) + delimiter.length)
            address = address.substring(0, address.lastIndexOf(delimiter))

            var meterReadingSum = 0.0
            when (rowContent.getCell(UploadingConverter.COLUMN_NUMBER_METER_READING_SUM).cellType) {
                CellType.NUMERIC -> meterReadingSum = rowContent.getCell(COLUMN_NUMBER_METER_READING_SUM).numericCellValue
                CellType.BLANK -> meterReadingSum = rowContent.getCell(COLUMN_NUMBER_METER_READING_SUM_ALTERNATIVE).numericCellValue
            }
            if (meterReadingSum.isNaN()) meterReadingSum = 0.0
            var meterReadingRateOne = 0.0
            when (rowContent.getCell(UploadingConverter.COLUMN_NUMBER_METER_READING_RATE_ONE).cellType) {
                CellType.NUMERIC -> meterReadingRateOne = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_ONE).numericCellValue
                CellType.BLANK -> meterReadingRateOne = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_ONE_ALTERNATIVE).numericCellValue
            }
            if (meterReadingRateOne.isNaN()) meterReadingRateOne = 0.0
            var meterReadingRateTwo = 0.0
            when (rowContent.getCell(UploadingConverter.COLUMN_NUMBER_METER_READING_RATE_TWO).cellType) {
                CellType.NUMERIC -> meterReadingRateTwo = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_TWO).numericCellValue
                CellType.BLANK -> meterReadingRateTwo = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_TWO_ALTERNATIVE).numericCellValue
            }
            if (meterReadingRateTwo.isNaN()) meterReadingRateTwo = 0.0
            var meterReadingRateThree = 0.0
            when (rowContent.getCell(UploadingConverter.COLUMN_NUMBER_METER_READING_RATE_THREE).cellType) {
                CellType.NUMERIC -> meterReadingRateThree = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_THREE).numericCellValue
                CellType.BLANK -> meterReadingRateThree = rowContent.getCell(COLUMN_NUMBER_METER_READING_RATE_THREE_ALTERNATIVE).numericCellValue
            }
            if (meterReadingRateThree.isNaN()) meterReadingRateThree = 0.0

            val indication = Indication(
                    date = date,
                    rateSum = meterReadingSum,
                    rateOne = meterReadingRateOne,
                    rateTwo = meterReadingRateTwo,
                    rateThree = meterReadingRateThree
            )
            addIndication(
                    address = address,
                    accountingPointName = accountingPointName,
                    meterNumber = meterNumber,
                    indication = indication
            )
            contentSheetLineNumberPosition++
        }
        buildings.forEach { it.calculateBalancedConsumption() }
    }

    private fun addIndication(
            address: String,
            accountingPointName: String,
            meterNumber: String,
            indication: Indication
    ) {
        var accountingPointType = AccountingPointType.APARTMENT
        if (accountingPointName.toIntOrNull() == null) {
            if (!accountingPointName.contains(Regex(BALANCE_METER_CRITERIA))) return
            accountingPointType = AccountingPointType.BALANCED
        }
        var building = buildings.find { it.address == address }
        building = building ?: Building(address).also { buildings.add(it) }
        var accountingPoint = building.balancePoints.find { it.meterNumber == meterNumber }
        accountingPoint = accountingPoint ?: building.flats.find { it.meterNumber == meterNumber }
        accountingPoint = accountingPoint ?: AccountingPoint(accountingPointName, meterNumber).also {
            when(accountingPointType) {
                AccountingPointType.BALANCED -> building.balancePoints.add(it)
                AccountingPointType.APARTMENT -> building.flats.add(it)
            }
        }
        accountingPoint.indications.add(indication)
    }

    fun saveReportToTheFile() {
        val fileOutputStream = FileOutputStream(REPORT_FILE_NAME)
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        var contentRowId = 0
        buildings.forEach {
            val rowContent = sheet.createRow(contentRowId)
            var contentCellId = 0
            rowContent.createCell(contentCellId++).setCellValue(contentRowId.toString())
            rowContent.createCell(contentCellId++).setCellValue(it.address)
            rowContent.createCell(contentCellId++).setCellValue(it.balancedMetersConsumption)
            rowContent.createCell(contentCellId++).setCellValue(it.flatsConsumption)
            rowContent.createCell(contentCellId++).setCellValue(it.relativeImbalance)
            rowContent.createCell(contentCellId++).setCellValue(it.absoluteImbalance)
            contentRowId++
        }

        workbook.write(fileOutputStream)
        workbook.close()
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

        const val COLUMN_NUMBER_METER_READING_RATE_THREE = 9
        const val COLUMN_NUMBER_METER_READING_RATE_THREE_ALTERNATIVE = 13

        const val BALANCE_METER_CRITERIA = "бал"

        const val REPORT_FILE_NAME = "Smart_balanced_consumption_report.xlsx"

        const val PERSONAL_ACCOUNT_LENGTH = 9

        const val DATA_NO_FOUND = "\u2014" //long dash

    }
}