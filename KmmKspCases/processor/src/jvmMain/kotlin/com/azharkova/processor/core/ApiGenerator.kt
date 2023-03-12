package com.azharkova.processor.core

import com.azharkova.core.IInteractor
import com.azharkova.core.IView
import com.azharkova.network.IApi
import com.azharkova.processor.util.WILDCARDIMPORT
import com.azharkova.processor.util.addImports
import com.azharkova.processor.util.resolveTypeName
import com.squareup.kotlinpoet.*

fun List<ApiData>.generateClassSource(): String {
    val classData = this
    val packageName = classData.first().packageName
    val className = "ApiFactory"
    val imports = mutableListOf<String>()
    classData.forEach {
        imports.addAll(it.imports)
    }
    val companion = TypeSpec.companionObjectBuilder()
        .apply {
            classData.forEach { api ->
                addProperty(PropertySpec.builder("${api.name}",TypeVariableName(api.name)).delegate(CodeBlock.builder()
                    .beginControlFlow("lazy(mode = %T.SYNCHRONIZED)", LazyThreadSafetyMode::class.asTypeName())
                    .add("${api.name}Impl()")
                    .endControlFlow()
                    .build()).build())
            }
        }.build()
    val clazz = TypeSpec.classBuilder("ApiFactory").addType(companion).build()
    return FileSpec.builder(packageName, className)
        .addFileComment("Generated automatically")
        .addType(clazz)
        .addImports(imports)
        .build().toString().replace(WILDCARDIMPORT, "*")
}
