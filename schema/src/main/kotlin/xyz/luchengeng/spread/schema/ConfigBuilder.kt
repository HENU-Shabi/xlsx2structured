package xyz.luchengeng.spread.schema

import com.google.re2j.Pattern
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import java.io.File

import java.io.FileInputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import xyz.luchengeng.spread.common.exception.BadSyntaxException
import xyz.luchengeng.spread.common.model.Orientation
import xyz.luchengeng.spread.common.model.Statement
import xyz.luchengeng.spread.common.model.getType
import java.io.InputStream


class ConfigBuilder {
    private var rawStatements : MutableMap<Triple<String,Int,Int>,Statement> = mutableMapOf()
    fun fromSpreadSheet(path : String): ConfigBuilder {
        val file = FileInputStream(File(path))
        val workbook = XSSFWorkbook(file)
        iterateWorkbook(workbook)
        return this
    }
    private fun iterateWorkbook(workbook: XSSFWorkbook){
        for(sheet in workbook.sheetIterator()){
            for(row in sheet.rowIterator()){
                for(cell in row.cellIterator()){
                    if(tagPattern.matcher(cellValueOf(cell)).matches())rawStatements[Triple(cell.sheet.sheetName,cell.address.row,cell.address.column)] =this.parse(cellValueOf(cell))
                }
            }
        }
    }
    fun fromSpreadSheet(inputStream: InputStream) : ConfigBuilder{
        iterateWorkbook(XSSFWorkbook(inputStream))
        return this
    }
    fun fromSpreadSheet(file : File) = fromSpreadSheet(file.path)
    fun build() : MutableMap<Triple<String,Int,Int>,Statement>{
        /*this.rawStatements = this.rawStatements.filter {
            it.value.group != ""
        }.toMutableMap()*/
        return this.rawStatements
    }
    private fun parse(tagStr : String) : Statement{
        val tag = tagPattern.matcher(tagStr)
        if(!tag.matches()) throw BadSyntaxException()
        val lang = langPattern.matcher(tag.group("stmt"))
        val end = endPattern.matcher(tag.group("stmt")).lookingAt()
        if(end){
            return Statement("",null,
                xyz.luchengeng.spread.common.model.CellType(xyz.luchengeng.spread.common.model.CellType.Type.NUMBER),null,false,false,null,null,true)
        }
        lang.lookingAt()
        val repetition = lang.group("repetition")?:throw BadSyntaxException()
        var orientation : Orientation?=null;
        var prop : String?=null
        val type  = getType(lang)
        val group = lang.group("group")?:throw BadSyntaxException()

        val infinite = lang.group("infinite")!=null
        if(repetition == "each"){
            orientation = if((lang.group("orientation")?:throw BadSyntaxException()) == "row"){Orientation.ROW}else{Orientation.COLUMN}
            prop = lang.group("prop")?:throw BadSyntaxException()
        }
        return Statement(group,orientation,type,prop, parseArgs(lang.group("args"),"id") != null,parseArgs(lang.group("args"),"required") != null,token = (parseArgs(lang.group("args"),"token"))?.get(0),default =  parseArgs(lang.group("args"),"defaultValue"),end = false,infinite = infinite)
    }

    private fun parseArgs(args : String?, groupName : String) : String?{
        if(args == null) return null
        for(pattern in patternList){
           val matcher =  pattern.matcher(args)
            if(matcher.lookingAt()){
                try {
                    if (matcher.group(groupName) != null) return matcher.group(groupName)
                }catch (e : Exception){
                }
            }
        }
        return null
    }

    private fun cellValueOf(cell : Cell) : String{
        return when(cell.cellType){
            CellType.NUMERIC->cell.numericCellValue.toString()
            CellType.STRING->cell.stringCellValue
            else->""
        }
    }
    companion object{
        private val tagPattern : Pattern = Pattern.compile("__\\\$\\{(?P<stmt>.*)}\\\$__")
        private val endPattern : Pattern = Pattern.compile(".*end\\s*each.*")
        private val langPattern : Pattern = Pattern.compile("(?P<end>end)?\\s*(?P<infinite>infinite)?\\s*(?P<repetition>each|single)\\s*(?P<orientation>row|column)?\\s*(?P<type>string|number|((?P<boolean>boolean)\\(((?P<true>[^\\s]*)\\s*as\\s*(true|false))\\s*,\\s*((?P<false>[^\\s]*)\\s*as\\s*(true|false))\\))|(enum\\((?P<enumeral>(.+)(,\\s*.+)*)\\)))\\s*(?P<group>[^\\s]*)\\s*((as)\\s*(?P<prop>[^\\s]*))?\\s*(?P<args>.*)?")
        private val patternList = listOf(Pattern.compile(".*(?P<id>id).*"),Pattern.compile(".*(?P<required>required).*"),Pattern.compile(".*((?P<tokenize>tokenize)\\s*(?P<token>[^\\s])).*"),
            Pattern.compile(".*((?P<default>default)\\s*(?P<defaultValue>[^\\s]+)).*"))
    }
}
