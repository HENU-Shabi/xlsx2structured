package xyz.luchengeng.spread.codegen

import com.squareup.javapoet.*
import xyz.luchengeng.spread.common.model.CellType
import xyz.luchengeng.spread.common.model.Statement
import javax.lang.model.element.Modifier


class EntityClassGenerator(private var pkg : String, private val name : String) {
    init{
        pkg = "${pkg}.entity.${name.decapitalize()}"
    }
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
                if(stmt.prop != null)addField(stmt.prop!!,stmt.token!=null,stmt.type,elem)
            }
            classes.add(elem)
            val list = ClassName.get("java.util", "List")
            val group = ClassName.get("${pkg}.${name}",it[0].group.capitalize().dropLast(1))
            pojo.addField(FieldSpec.builder(ParameterizedTypeName.get(list,group),it[0].group)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ClassName.get("javax.persistence", "OneToMany")).build())
        }

        return mutableListOf<JavaFile>().apply {
            for (clazz in classes){
                clazz.addField(FieldSpec.builder(Long::class.java,"id").addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get("javax.persistence", "Id"))
                        .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.persistence", "GeneratedValue"))
                                .addMember("strategy", CodeBlock.of("\$T.AUTO",ClassName.get("javax.persistence", "GenerationType")))
                                .build())
                        .build())
                clazz.addAnnotation(ClassName.get("lombok", "Data"))
                clazz.addAnnotation(ClassName.get("javax.persistence", "Entity"))
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
        val list = ClassName.get("java.util", "List")
        if(name == ""){
            return
        }
        when (type.type) {
            CellType.Type.STRING -> {
                if(!tokenize){

                    pojo.addField(String::class.java,name,Modifier.PUBLIC)
                }
                else{


                    pojo.addField(FieldSpec.builder(ParameterizedTypeName.get(list,ClassName.get("java.lang", "String")),name)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(ClassName.get("javax.persistence", "ElementCollection"))
                            .build())
                }
            }
            CellType.Type.NUMBER->{
                if(!tokenize){
                    pojo.addField(Double::class.java,name,Modifier.PUBLIC)

                }else{


                    pojo.addField(FieldSpec.builder(ParameterizedTypeName.get(list,ClassName.get("java.lang", "Double")),name)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(ClassName.get("javax.persistence", "ElementCollection"))
                            .build())
                }
            }
            CellType.Type.BOOLEAN->{
                if(!tokenize){

                    pojo.addField(Boolean::class.java,name,Modifier.PUBLIC)
                }else{


                    pojo.addField(FieldSpec.builder(ParameterizedTypeName.get(list,ClassName.get("java.lang", "Boolean")),name)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(ClassName.get("javax.persistence", "ElementCollection"))
                            .build())
                }
            }
            CellType.Type.ENUM->{
                val enum = TypeSpec.enumBuilder(name.capitalize())
                        .addModifiers(Modifier.PUBLIC)
                for ((_, backing) in type.map?.entries!!) {
                    enum.addEnumConstant(backing)
                }
                if(!tokenize) {
                    pojo.addField(ClassName.get("${pkg}.${name}", name.capitalize()),name,Modifier.PUBLIC)
                }else{


                    pojo.addField(FieldSpec.builder(ParameterizedTypeName.get(list,ClassName.get("${pkg}.${name}", name.capitalize())),name)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(ClassName.get("javax.persistence", "ElementCollection"))
                            .build())
                }
                pojo.addType(enum.addModifiers(Modifier.STATIC).build())
            }
        }
    }
}