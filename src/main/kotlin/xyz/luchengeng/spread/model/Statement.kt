package xyz.luchengeng.spread.model

data class Statement(
val group : String,
val orientation : Orientation?,
val type : CellType,
val prop : String?,
val id : Boolean,
val required : Boolean,
val default : String?,
val token : Char?)

enum class Orientation(s: String) {
    ROW("ROW"),
    COLUMN("COLUMN")
}