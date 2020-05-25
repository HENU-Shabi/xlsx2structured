package xyz.luchengeng.spread.common.model

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import xyz.luchengeng.spread.common.exception.BadSyntaxException

class CellType(
        val type : CellType.Type,
    val trueRep: String?=null,
    val falseRep: String?=null,
    val map: Map<String, String>?=null
){
    enum class Type(s: String) {
        STRING("STRING"),
        NUMBER("NUMBER"),
        BOOLEAN("BOOLEAN"),
        ENUM("ENUM")
    }
}

fun getType(matcher: Matcher) : CellType{
    when {
        matcher.group("type") == "string" -> {
            return CellType(CellType.Type.STRING)
        }
        matcher.group("type") == "number" -> {
            return CellType(CellType.Type.NUMBER)
        }
        matcher.group("boolean")!=null -> {
            return CellType(type = CellType.Type.BOOLEAN,trueRep = matcher.group("true")?:throw BadSyntaxException(),falseRep = matcher.group("false")?:throw BadSyntaxException())
        }
        else -> {
            val mapPattern : Pattern = Pattern.compile("(?P<key>[^\\s]*)[\\s]*as[\\s]*(?P<value>[^\\s]*)")
            val enumeral = (matcher.group("enumeral")?:throw BadSyntaxException()).split(',')
            val map = mutableMapOf<String,String>()
            for(entry in enumeral){
                val matcher = mapPattern.matcher(entry);
                matcher.matches()
                map[matcher.group("key")?:throw BadSyntaxException()]=matcher.group("value")?:throw BadSyntaxException()
            }
            return CellType(type = CellType.Type.ENUM,map=map)
        }
    }
}