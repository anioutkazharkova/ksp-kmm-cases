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

    val createFunction = com.squareup.kotlinpoet.FunSpec.builder("resolve")
        .returns(IApi::class.asTypeName().copy(nullable = true))
        .addStatement("val api = ")
        .beginControlFlow("when (this)")
        .apply {
            classData.forEach {
                addStatement("is ${it.name} -> ${it.name}Impl()")
            }
        }.addStatement("else -> null").endControlFlow().addStatement("return api").build()

    val companion = TypeSpec.companionObjectBuilder()
       .addFunction(createFunction)
        .build()
    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(className)
        .addType(companion)
        .build()


    return com.squareup.kotlinpoet.FileSpec.builder(packageName, className)
        .addFileComment("Generated automatically")
      //  .addFunction(createFunction)
      //  .addType(implClassSpec)
        .apply {
        classData.forEach { api ->
           addFunction(FunSpec.builder("resolve")
                .receiver(TypeVariableName(api.name.orEmpty()))
                .addStatement("return ${api.name.orEmpty()}Impl()")
                .returns(TypeVariableName(api.name.orEmpty()))
                .build())
        }
    }
        .addImports(imports)
        .build().toString().replace(WILDCARDIMPORT, "*")
}
