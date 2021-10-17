data class AccountingPoint (
    val name: String,
    val meterNumber: String,
    val indications: MutableList<Indication> = mutableListOf()
)