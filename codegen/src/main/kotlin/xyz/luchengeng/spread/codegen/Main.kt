package xyz.luchengeng.spread.codegen

import xyz.luchengeng.spread.schema.ConfigBuilder
import java.io.File

fun main(args : Array<String>){
    val classes = EntityClassGenerator(args[0],args[1]).gen(ConfigBuilder().fromSpreadSheet(File(args[2])).build().values)
    for(clazz in classes){
        clazz.writeTo(File(args[3]))
    }
}