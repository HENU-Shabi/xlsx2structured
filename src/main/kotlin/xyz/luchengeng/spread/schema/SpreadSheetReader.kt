package xyz.luchengeng.spread.schema

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.PatternFormatting
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import xyz.luchengeng.spread.exception.InvalidInputException
import xyz.luchengeng.spread.model.Orientation
import xyz.luchengeng.spread.model.Statement
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern

class SpreadSheetReader(private val rawStatements : MutableMap<Triple<String,Int,Int>, Statement>) {
    private lateinit var workbook : XSSFWorkbook
    private val root = JsonObject()
    fun read(path : String ) : String{

        val file = FileInputStream(File(path))
        workbook = XSSFWorkbook(file)
        for((k,v) in rawStatements){
            if(v.orientation == null){
                root.addTypedProp(v.group,getValue(getCell(k),stmt = v),v.type)
            }else{
                readEach(v,k)
            }
        }
        return root.toString()
    }
    private fun readEach(stmt : Statement,tripe : Triple<String,Int,Int>){
        val group : JsonObject=  if(root.has(stmt.group))root.get(stmt.group) as JsonObject else root.add(stmt.group,JsonObject()).run { root.get(stmt.group) } as JsonObject
        if(stmt.id && getValue(getCell(tripe),stmt)!=""){
            val arr : JsonArray = if(group.has(stmt.prop))group.get(stmt.prop) as JsonArray else group.add(stmt.prop,JsonArray()).run { group.get(stmt.prop) } as JsonArray
            arr.addTyped(getValue(getCell(tripe),stmt),stmt.type)
            if(stmt.orientation == Orientation.COLUMN)readEach(stmt, Triple(tripe.first,tripe.second+1,tripe.third))else readEach(stmt, Triple(tripe.first,tripe.second,tripe.third+1))
        }
    }
    private fun getCell(sheet : String,row : Int,cell : Int) : Cell{
        return this.workbook.getSheet(sheet).getRow(row).getCell(cell)
    }
    private fun getCell(tripe : Triple<String,Int,Int>) : Cell{
        val (sheet,row,cell) = tripe
        return getCell(sheet, row, cell)
    }
    private fun getValue(cell : Cell,stmt : Statement) : String{
        val rawVal=  when(cell.cellType){
            CellType.NUMERIC->cell.numericCellValue.toString()
            CellType.STRING->cell.stringCellValue
            else->""
        }
        if(isStringEmpty(rawVal) && stmt.required){
            throw InvalidInputException()
        }
        if(isStringEmpty(rawVal)){
            if(stmt.default!=null){
                return stmt.default
            }else{
                return ""
            }
        }
        return rawVal
    }
    private fun isStringEmpty(content : String) : Boolean{
        val pattern = Pattern.compile("\\s*")
        return pattern.matcher(content).matches()
    }
}

fun JsonObject.addTypedProp(name : String,value : String,type : xyz.luchengeng.spread.model.CellType){
    when(type.type){
        xyz.luchengeng.spread.model.CellType.Type.BOOLEAN->{
            if(type.trueRep == value){
                this.addProperty(name,true)
            }
            if(type.falseRep == value){
                this.addProperty(name,false)
            }
            throw InvalidInputException()
        }
        xyz.luchengeng.spread.model.CellType.Type.NUMBER->{
            this.addProperty(name,value.toDouble())
        }
        xyz.luchengeng.spread.model.CellType.Type.STRING->{
            this.addProperty(name,value)
        }
        xyz.luchengeng.spread.model.CellType.Type.ENUM->{
            if(type.map?.containsKey(value) == true){
                this.addProperty(name,type.map[value])
            }else throw InvalidInputException()
        }
    }
}

fun JsonArray.addTyped(value : String,type : xyz.luchengeng.spread.model.CellType){
    when(type.type){
        xyz.luchengeng.spread.model.CellType.Type.BOOLEAN->{
            if(type.trueRep == value){
                this.add(true)
            }
            if(type.falseRep == value){
                this.add(false)
            }
            throw InvalidInputException()
        }
        xyz.luchengeng.spread.model.CellType.Type.NUMBER->{
            this.add(value.toDouble())
        }
        xyz.luchengeng.spread.model.CellType.Type.STRING->{
            this.add(value)
        }
        xyz.luchengeng.spread.model.CellType.Type.ENUM->{
            if(type.map?.containsKey(value) == true){
                this.add(type.map[value])
            }else throw InvalidInputException()
        }
    }
}