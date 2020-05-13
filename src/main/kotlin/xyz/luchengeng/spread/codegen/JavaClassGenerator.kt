package xyz.luchengeng.spread.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import xyz.luchengeng.spread.model.CellType
import xyz.luchengeng.spread.model.Statement
import javax.lang.model.element.Modifier


class JavaClassGenerator(private val pkg : String, private val name : String) {
    fun gen(stmts : Collection<Statement>) : List<JavaFile>{
        val pojo = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
        val classes = mutableListOf(pojo)
        for(stmt in stmts){
            if(stmt.orientation == null)
                addField(stmt.group,stmt.token!=null,stmt.type,pojo)
        }
        forEachRepeatedGroup(stmts){
            val elem = TypeSpec.classBuilder(it[0].group.capitalize().dropLast(1))
                    .addModifiers(Modifier.PUBLIC)
            for(stmt in it){
                if(stmt.prop != null)addField(stmt.prop,stmt.token!=null,stmt.type,elem)
            }
            classes.add(elem)
            val list = ClassName.get("java.util", "List")
            val group = ClassName.get("${pkg}.${name}",it[0].group.capitalize().dropLast(1))
            pojo.addField(ParameterizedTypeName.get(list,group),it[0].group)
        }

        return mutableListOf<JavaFile>().apply {
            for (clazz in classes){
                this.add(JavaFile.builder(pkg,clazz.build()).build())
            }
        }
    }
    private fun forEachRepeatedGroup(stmts: Collection<Statement>, func : (props : List<Statement>)->Unit){
        val map = mutableMapOf<String, MutableList<Statement>>()
        for(stmt in stmts){
            if(!map.contains(stmt.group))map[stmt.group] = mutableListOf()
            map[stmt.group]!!.add(stmt)
        }
        for(entry in map.filter {
            var has = false
            for (stmt in it.value){
                if(stmt.prop != null){
                    has = true
                }
            }
            has
        }.values){
            func(entry)
        }
    }

    private fun addField(name : String, tokenize : Boolean,type: CellType, pojo : TypeSpec.Builder){
        if(name == ""){
            return
        }
        when (type.type) {
            CellType.Type.STRING -> {
                if(!tokenize)pojo.addField(String::class.java,name)
                else{
                    val list = ClassName.get("java.util", "List")
                    pojo.addField(ParameterizedTypeName.get(list,ClassName.get("java.lang", "String")),name)
                }
            }
            CellType.Type.NUMBER->{
                if(!tokenize)pojo.addField(Double::class.java,name)else{
                    val list = ClassName.get("java.util", "List")
                    pojo.addField(ParameterizedTypeName.get(list,ClassName.get("java.lang", "Double")),name)
                }
            }
            CellType.Type.BOOLEAN->{
                if(!tokenize)pojo.addField(Boolean::class.java,name)else{
                    val list = ClassName.get("java.util", "List")
                    pojo.addField(ParameterizedTypeName.get(list,ClassName.get("java.lang", "Boolean")),name)
                }
            }
            CellType.Type.ENUM->{
                val enum = TypeSpec.enumBuilder(name.capitalize())
                        .addModifiers(Modifier.PUBLIC)
                for ((_, backing) in type.map?.entries!!) {
                    enum.addEnumConstant(backing)
                }
                if(!tokenize) {
                    pojo.addField(ClassName.get("${pkg}.${name}", name.capitalize()), name)
                }else{
                    val list = ClassName.get("java.util", "List")
                    pojo.addField(ParameterizedTypeName.get(list,ClassName.get("${pkg}.${name}", name.capitalize())),name)
                }
                pojo.addType(enum.addModifiers(Modifier.STATIC).build())
            }
        }
    }
}