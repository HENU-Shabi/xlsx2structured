package xyz.luchengeng.spread.common.model

data class Statement(
val group : String,
val orientation : Orientation?,
val type : CellType,
val prop : String?,
val id : Boolean,
val required : Boolean,
val default : String?,
val token : Char?,
val end : Boolean = false,
val desc : String?,
val infinite : Boolean = true)

