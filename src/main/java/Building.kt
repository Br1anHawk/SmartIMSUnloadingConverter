class Building (
     val address: String,
     val balancePoints: MutableList<AccountingPoint> = mutableListOf(),
     val flats: MutableList<AccountingPoint> = mutableListOf()
) {
    var balancedMetersConsumption = 0.0
    var flatsConsumption = 0.0
    var relativeImbalance = 0.0
    var absoluteImbalance = 0.0

    fun calculateBalancedConsumption() {
        for (accountingPoint in balancePoints) {
            if (accountingPoint.indications.isNotEmpty()) {
                val consumption = accountingPoint.indications[accountingPoint.indications.size - 1].rateSum - accountingPoint.indications[0].rateSum
                balancedMetersConsumption += consumption
            }
        }
        for (accountingPoint in flats) {
            if (accountingPoint.indications.isNotEmpty()) {
                val consumption = accountingPoint.indications[accountingPoint.indications.size - 1].rateSum - accountingPoint.indications[0].rateSum
                flatsConsumption += consumption
            }
        }
        absoluteImbalance = balancedMetersConsumption - flatsConsumption
        relativeImbalance = absoluteImbalance / balancedMetersConsumption * 100
    }
}