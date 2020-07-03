package xyz.luchengeng.spread.schema

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

fun main(args : Array<String>){
    val arr = ConfigBuilder().fromSpreadSheet(FileInputStream(File(args[0]))).build()
    val str = arr.toString()
    val fileWriter = FileWriter(File(args[1]))
    fileWriter.write(str)
    fileWriter.flush()
    fileWriter.close()
}