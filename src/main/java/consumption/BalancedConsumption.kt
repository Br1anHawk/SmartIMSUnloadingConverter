package consumption

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import unloading.UploadingConverter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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
            val code = rowContent.getCell(COLUMN_NUMBER_CODE).stringCellValue.trim()
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
                    code = code,
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
            code: String,
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
        accountingPoint = accountingPoint ?: AccountingPoint(code, accountingPointName, meterNumber).also {
            when(accountingPointType) {
                AccountingPointType.BALANCED -> building.balancePoints.add(it)
                AccountingPointType.APARTMENT -> building.flats.add(it)
            }
        }
        accountingPoint.indications.add(indication)
    }

    fun saveReportToTheFile() {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd_'${REPORT_FILE_NAME}'_HH-mm-ss")
        val fileName = formatter.format(calendar.time) + ".xlsx"
        val fileOutputStream = FileOutputStream(fileName)
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        val fontHeader = workbook.createFont()
        fontHeader.setFontHeight(FONT_SIZE)
        fontHeader.fontName = FONT_NAME
        fontHeader.bold = true
        val cellStyleHeader = workbook.createCellStyle()
        cellStyleHeader.alignment = HorizontalAlignment.CENTER
        cellStyleHeader.setFont(fontHeader)

        val font = workbook.createFont()
        font.setFontHeight(FONT_SIZE)
        font.fontName = FONT_NAME
        font.bold = false
        val cellStyle = workbook.createCellStyle()
        cellStyle.alignment = HorizontalAlignment.CENTER
        cellStyle.setFont(font)

        var cell: XSSFCell
        var contentRowId = 0
        val rowContent = sheet.createRow(contentRowId)
        var contentCellId = 0
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_NUMBER)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_ADDRESS)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_BUILDING_TYPE)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_BALANCED_METERS_CONSUMPTION)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_FLATS_CONSUMPTION)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_RELATIVE_IMBALANCE)
        cell = rowContent.createCell(contentCellId++)
        cell.cellStyle = cellStyleHeader
        cell.setCellValue(COLUMN_HEADER_ABSOLUTE_IMBALANCE)
        contentRowId++
        val columnsCount = rowContent.lastCellNum

        buildings.forEach {
            val rowContent = sheet.createRow(contentRowId)
            contentCellId = 0
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(contentRowId.toString())
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(it.address)
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(it.buildingType.toString())
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(it.balancedMetersConsumption)
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(it.flatsConsumption)
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(String.format("%.2f" ,it.relativeImbalance))
            cell = rowContent.createCell(contentCellId++)
            cell.cellStyle = cellStyle
            cell.setCellValue(it.absoluteImbalance)
            contentRowId++
        }

        for (columnNumber in 0 until columnsCount) {
            sheet.autoSizeColumn(columnNumber)
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

        const val REPORT_FILE_NAME = "Smart_balanced_consumption_report"
        const val COLUMN_HEADER_NUMBER = "№"
        const val COLUMN_HEADER_ADDRESS = "address"
        const val COLUMN_HEADER_BUILDING_TYPE = "buildingType"
        const val COLUMN_HEADER_BALANCED_METERS_CONSUMPTION = "balancedMetersConsumption"
        const val COLUMN_HEADER_FLATS_CONSUMPTION = "flatsConsumption"
        const val COLUMN_HEADER_RELATIVE_IMBALANCE = "relativeImbalance"
        const val COLUMN_HEADER_ABSOLUTE_IMBALANCE = "absoluteImbalance"

        const val PERSONAL_ACCOUNT_LENGTH = 9

        const val MINIMUM_COUNT_OF_CODES_IN_APARTMENT_BUILDING = 5

        const val FONT_SIZE = 14.0
        const val FONT_NAME = "Times New Roman"
    }
}