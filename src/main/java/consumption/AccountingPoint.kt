package consumption

data class AccountingPoint (
    val code: String,
    val name: String,
    val meterNumber: String,
    val indications: MutableList<Indication> = mutableListOf()
)