package xyz.luchengeng.spread.test

import com.google.gson.Gson
import xyz.luchengeng.spread.codegen.JavaClassGenerator
import xyz.luchengeng.spread.schema.ConfigBuilder
import xyz.luchengeng.spread.schema.ReaderBuilder
import java.io.InputStreamReader

class Test
    @ExperimentalStdlibApi
    fun main(args: Array<String>) {
        val config = ConfigBuilder().fromSpreadSheet(Test::class.java.getResourceAsStream("/schema.xlsx")).build()
        val json = ReaderBuilder().fromConfig(Gson().toJson(config).toString()).build().read(Test::class.java.getResourceAsStream("/data.xlsx")).toString()
        JavaClassGenerator("xyz.luchengeng.entity","Power").gen(config.values).forEach{
            it.writeTo(System.out)
        }
        println(json)
    }


