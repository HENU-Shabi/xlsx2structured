import xyz.luchengeng.spread.reader.AbstractBuilder
import xyz.luchengeng.spread.reader.ReaderBuilder
import java.io.File

@ExperimentalStdlibApi
fun main(){
   ReaderBuilder().fromConfig(File("D:\\real_estate.json")).build()
}