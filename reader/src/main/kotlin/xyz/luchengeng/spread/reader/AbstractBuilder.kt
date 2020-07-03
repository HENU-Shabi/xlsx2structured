package xyz.luchengeng.spread.reader

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.re2j.Pattern
import xyz.luchengeng.spread.common.model.Statement
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader

abstract class AbstractBuilder<T> {
    protected val rawStatements : MutableMap<Triple<String,Int,Int>, Statement> = mutableMapOf()
    @ExperimentalStdlibApi
    fun fromConfig(configString : String) : AbstractBuilder<T>{
        val json = GsonBuilder().registerTypeAdapter((object : TypeToken<Triple<String,Int,Int>>(){}).type,TripeDeserializer).create()
        rawStatements.putAll(json.fromJson(configString,(object : TypeToken<MutableMap<Triple<String,Int,Int>, Statement>>(){}).type))
        return this
    }
    @ExperimentalStdlibApi
    fun fromStream(inputStream : InputStream) : AbstractBuilder<T>{
        val reader = InputStreamReader(inputStream)
        return fromConfig(reader.readText()).also { reader.close() }
    }
    @ExperimentalStdlibApi
    fun fromConfig(file : File) : AbstractBuilder<T>{
        val reader = FileReader(file)
        return fromConfig(file.readText()).also { reader.close() }
    }
    abstract fun build() : T
    object TripeDeserializer : TypeAdapter<Triple<String, Int, Int>>() {
        /**
         * Writes one JSON value (an array, object, string, number, boolean or null)
         * for `value`.
         *
         * @param value the Java object to write. May be null.
         */
        override fun write(out: JsonWriter?, value: Triple<String, Int, Int>?) {
            out?.value(value.toString())
        }
        private val triplePattern : Pattern = Pattern.compile("\\(\\s*(?P<a>.*)\\s*,\\s*(?P<b>.*)\\s*,\\s*(?P<c>.*)\\s*\\)")
        /**
         * Reads one JSON value (an array, object, string, number, boolean or null)
         * and converts it to a Java object. Returns the converted object.
         *
         * @return the converted Java object. May be null.
         */
        override fun read(`in`: JsonReader?): Triple<String, Int, Int>? {
            val matcher = triplePattern.matcher(`in`?.nextString())
            if(matcher.matches()) {
                return Triple<String,Int,Int>(matcher.group("a"),
                matcher.group("b").toInt(),
                matcher.group("c").toInt())
            }else{
                return null;
            }
        }

    }
}