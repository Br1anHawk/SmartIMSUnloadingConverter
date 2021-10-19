package consumption

class Building (
        val address: String,
        val balancePoints: MutableList<AccountingPoint> = mutableListOf(),
        val flats: MutableList<AccountingPoint> = mutableListOf()
) {
    var balancedMetersConsumption = 0.0
    var flatsConsumption = 0.0
    var relativeImbalance = 0.0
    var absoluteImbalance = 0.0
    var buildingType = BuildingType.OTHER_BUILDING

    fun calculateBalancedConsumption() {
        for (accountingPoint in balancePoints) {
            if (accountingPoint.indications.isNotEmpty()) {
                val consumption = accountingPoint.indications[accountingPoint.indications.size - 1].rateSum - accountingPoint.indications[0].rateSum
                balancedMetersConsumption += consumption
            }
        }
        var codesCount = 0
        for (accountingPoint in flats) {
            if (accountingPoint.indications.isNotEmpty()) {
                val consumption = accountingPoint.indications[accountingPoint.indications.size - 1].rateSum - accountingPoint.indications[0].rateSum
                flatsConsumption += consumption
            }
            if (accountingPoint.code.length == BalancedConsumption.PERSONAL_ACCOUNT_LENGTH && accountingPoint.code.toIntOrNull() != null) {
                codesCount++
            }
        }
        if (codesCount >= BalancedConsumption.MINIMUM_COUNT_OF_CODES_IN_APARTMENT_BUILDING) {
            buildingType = BuildingType.APARTMENT_BUILDING
        }
        absoluteImbalance = balancedMetersConsumption - flatsConsumption
        relativeImbalance = absoluteImbalance / balancedMetersConsumption * 100
    }
}