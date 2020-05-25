package xyz.luchengeng.spread.common.exception

class InvalidInputException(val sheetName : String?,val row : Int?,val col : Int?,val type : InvalidInputType) : Exception() {
    constructor(type: InvalidInputType) : this(null as String?,null as Int?,null as Int?,type)
    enum class InvalidInputType(s: String) {
        REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING"),
        INVALID_ENUM_VALUE("INVALID_ENUM_VALUE"),
        INVALID_NUMBER_VALUE("INVALID_NUMBER_VALUE"),
        INVALID_BOOLEAN_VALUE("INVALID_BOOLEAN_VALUE"),
        INVALID_TOKEN("INVALID_TOKEN")
    }
}