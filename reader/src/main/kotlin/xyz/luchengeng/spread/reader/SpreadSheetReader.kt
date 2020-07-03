package xyz.luchengeng.spread.reader

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import xyz.luchengeng.spread.common.exception.InvalidInputException
import xyz.luchengeng.spread.common.model.Orientation
import xyz.luchengeng.spread.common.model.Statement
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.regex.Pattern

class SpreadSheetReader(private val rawStatements : MutableMap<Triple<String,Int,Int>, Statement>) {
    private lateinit var workbook : XSSFWorkbook
    private lateinit var root : JsonObject
    @Throws(InvalidInputException::class)
    fun read(path : String ) : JsonObject{
        root = JsonObject()
        val file = FileInputStream(File(path))
        workbook = XSSFWorkbook(file)
        iterateContent()
        return root
    }
    @Throws(InvalidInputException::class)
    fun read(inputStream: InputStream): JsonObject{
        root = JsonObject()
        workbook = XSSFWorkbook(inputStream)
        iterateContent()
        return root
    }
    private fun iterateContent(){
        for((k,v) in rawStatements){
            if(v.orientation == null && !v.end){
                root.addTypedProp(v.group,getValue(getCell(k),stmt = v),v.type,k)
            }else{
                if(!v.end) readEach(v,k,v.infinite)
            }
        }
        for((k,v) in rawStatements){
            if(v.token != null){
                val group = root[v.group]
                root.remove(v.group);
                root.add(v.group,tokenize(k,group, v.token!!,v.prop))
            }
        }
        for((_,v) in rawStatements){
            if(v.orientation != null){
                val group = root[v.group]
                if(group is JsonArray) break
                val transposed = transposeEach(group as JsonObject)
                root.remove(v.group);
                root.add(v.group,transposed)
            }
        }
    }
    @Throws(InvalidInputException::class)
    private fun tokenize(k:Triple<String,Int,Int>,elem : JsonElement,token : Char,propName : String?)  : JsonElement{
        when {
            elem.isJsonArray -> {
                val arr = JsonArray()
                for(i in elem.asJsonArray){
                    val jArr = JsonArray()
                    if(i.isJsonNull) continue
                    for( str in i.asString.split(token)){
                        jArr.add(str)
                    }
                    arr.add(jArr)
                }
                return arr
            }
            elem.isJsonPrimitive -> {
                val jArr = JsonArray()
                for( str in elem.asString.split(token)){
                    jArr.add(str)
                }
                return jArr
            }
            elem.isJsonObject -> {
                val obj = JsonObject()
                for(prop in elem.asJsonObject.entrySet()){
                    if(prop.key == propName)obj.add(prop.key,tokenize(k,prop.value,token,null))else obj.add(prop.key,prop.value)
                }
                return obj
            }
            else->throw InvalidInputException(k.first,k.second,k.third,InvalidInputException.InvalidInputType.INVALID_TOKEN)
        }
    }
    private fun readEach(stmt : Statement,tripe : Triple<String,Int,Int>,infinite : Boolean = true){
        val group : JsonObject=  if(root.has(stmt.group))root.get(stmt.group) as JsonObject else root.add(stmt.group,JsonObject()).run { root.get(stmt.group) } as JsonObject
        if((getValue(getCell(tripe),stmt)!="" && infinite)||(rawStatements[tripe]?.end != true && !infinite)){
            val arr : JsonArray = if(group.has(stmt.prop))group.get(stmt.prop) as JsonArray else group.add(stmt.prop,JsonArray()).run { group.get(stmt.prop) } as JsonArray
            arr.addTyped(getValue(getCell(tripe),stmt),stmt.type,tripe)
            if(stmt.orientation == Orientation.COLUMN) {
                readEach(stmt, Triple(tripe.first, tripe.second + 1, tripe.third),infinite)
            }
            else {
                readEach(stmt, Triple(tripe.first, tripe.second, tripe.third + 1),infinite)
            }
        }
    }
    private fun getCell(sheet : String,row : Int,cell : Int) : Cell?{
        return this.workbook.getSheet(sheet)?.getRow(row)?.getCell(cell)
    }
    private fun getCell(tripe : Triple<String,Int,Int>) : Cell?{
        val (sheet,row,cell) = tripe
        return getCell(sheet, row, cell)
    }
    private fun emptyOrDefault(stmt: Statement) : String = stmt.default?:""

    @Throws(InvalidInputException::class)
    private fun getValue(cell : Cell?,stmt : Statement) : String{
        if(cell == null) return emptyOrDefault(stmt)
        val rawVal=  when(cell.cellType){
            CellType.NUMERIC->cell.numericCellValue.toString()
            CellType.STRING->cell.stringCellValue
            else->emptyOrDefault(stmt)
        }
        if(isStringEmpty(rawVal) && stmt.required){
            throw InvalidInputException(cell.sheet.sheetName,row = cell.address.row,col = cell.address.column,type = InvalidInputException.InvalidInputType.REQUIRED_FIELD_MISSING)
        }
        if(isStringEmpty(rawVal)){
            if(stmt.default!=null){
                return stmt.default!!
            }else{
                return emptyOrDefault(stmt)
            }
        }
        return rawVal
    }
    private fun isStringEmpty(content : String) : Boolean{
        val pattern = Pattern.compile("\\s*")
        return pattern.matcher(content).matches()
    }

    private fun transposeEach(root : JsonObject) : JsonArray{
        val newRoot = JsonArray()
        for(index in 0 until root.entrySet().iterator().next().value.asJsonArray.size()) {
            val obj = JsonObject();
            for ((name, arr) in root.entrySet()) {
                if(arr.asJsonArray.size() > index) obj.add(name,arr.asJsonArray[index])
            }
            newRoot.add(obj)
        }
        return newRoot
    }
}
@Throws(InvalidInputException::class)
private fun JsonObject.addTypedProp(name : String,value : String,type : xyz.luchengeng.spread.common.model.CellType,pos : Triple<String,Int,Int>){
    val (sheet,row,col) = pos
    if(name == "") return
    if(value == ""){this.addProperty(name,null as String?);return}
    when(type.type){
        xyz.luchengeng.spread.common.model.CellType.Type.BOOLEAN->{
            if(type.trueRep == value){
                this.addProperty(name,true)
            }
            if(type.falseRep == value){
                this.addProperty(name,false)
            }
            throw InvalidInputException(sheet,row,col,InvalidInputException.InvalidInputType.INVALID_BOOLEAN_VALUE)
        }
        xyz.luchengeng.spread.common.model.CellType.Type.NUMBER->{
            try {
                this.addProperty(name, value.toDouble())
            }catch ( e : Exception){
                throw InvalidInputException(sheet,row,col,InvalidInputException.InvalidInputType.INVALID_NUMBER_VALUE)
            }
        }
        xyz.luchengeng.spread.common.model.CellType.Type.STRING->{
            this.addProperty(name,value)
        }
        xyz.luchengeng.spread.common.model.CellType.Type.ENUM->{
            if(type.map?.containsKey(value) == true){
                this.addProperty(name, type.map!![value])
            }else throw InvalidInputException(sheet,row,col,InvalidInputException.InvalidInputType.INVALID_ENUM_VALUE)
        }
    }
}
@Throws(InvalidInputException::class)
private fun JsonArray.addTyped(value : String,type : xyz.luchengeng.spread.common.model.CellType,pos : Triple<String,Int,Int>){
    val (sheet,row,col) = pos
    if(value == ""){
        this.add(null as JsonElement?)
        return
    }
    when(type.type){
        xyz.luchengeng.spread.common.model.CellType.Type.BOOLEAN->{
            if(type.trueRep == value){
                this.add(true)
                return
            }
            if(type.falseRep == value){
                this.add(false)
                return
            }
            throw InvalidInputException(sheet,row,col,InvalidInputException.InvalidInputType.INVALID_BOOLEAN_VALUE)
        }
        xyz.luchengeng.spread.common.model.CellType.Type.NUMBER->{
            this.add(value.toDouble())
        }
        xyz.luchengeng.spread.common.model.CellType.Type.STRING->{
            this.add(value)
        }
        xyz.luchengeng.spread.common.model.CellType.Type.ENUM->{
            if(type.map?.containsKey(value) == true){
                this.add(type.map!![value])
                return
            }else throw InvalidInputException(sheet,row,col,InvalidInputException.InvalidInputType.INVALID_ENUM_VALUE)
        }
    }
}