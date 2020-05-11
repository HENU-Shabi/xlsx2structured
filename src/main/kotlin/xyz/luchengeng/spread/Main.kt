package xyz.luchengeng.spread

import xyz.luchengeng.spread.schema.ConfigBuilder
import xyz.luchengeng.spread.schema.ReaderBuilder
import xyz.luchengeng.spread.schema.SpreadSheetReader

@ExperimentalStdlibApi
fun main(args : Array<String>){
   val stmt = SpreadSheetReader(ReaderBuilder().fromConfig(ConfigBuilder().fromSpreadSheet("C:\\Users\\Administrator\\Desktop\\3获得电力调查表——供电公司填报.xlsx").build())).read("C:\\Users\\Administrator\\Desktop\\电力测试1.xlsx")
}