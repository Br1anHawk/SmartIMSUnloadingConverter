package unloading

import java.util.*

data class Subscriber(
        var dateOfReadings: Date,
        val meterNumber: String,
        val code: String,
        val address: Address,
        val meterReadingSum: Double,
        val meterReadingRateOne: Double,
        val meterReadingRateTwo: Double,
        val meterReadingRateThree: Double
)