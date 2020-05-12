package xyz.luchengeng.spread

import xyz.luchengeng.spread.codegen.JavaClassGenerator
import xyz.luchengeng.spread.schema.ConfigBuilder
import xyz.luchengeng.spread.schema.ReaderBuilder
import xyz.luchengeng.spread.schema.SpreadSheetReader

@ExperimentalStdlibApi
fun main(args : Array<String>){
   JavaClassGenerator("xyz.luchengeng","Shabi").gen(ConfigBuilder().fromSpreadSheet("C:\\Users\\Administrator\\Desktop\\3获得电力调查表——供电公司填报.xlsx").buildStatements().values).apply {
      for(i in this){
         print(i)
      }
   }
}