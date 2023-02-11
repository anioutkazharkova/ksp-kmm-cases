package com.azharkova.processor.core

import com.azharkova.core.IInteractor
import com.azharkova.core.IModule
import com.azharkova.core.IView
import com.azharkova.processor.util.WILDCARDIMPORT
import com.azharkova.processor.util.addImports
import com.azharkova.processor.util.resolveTypeName
import com.squareup.kotlinpoet.*

fun List<ModuleData>.generateClassSource():String {
val classData = this
    val packageName = classData.first().packageName
    val implClassName = "ConfigFactory"


    val imports = mutableListOf<String>()
    classData.forEach {
        imports.addAll(it.imports)
    }

    val companion = TypeSpec.companionObjectBuilder().addProperty(
        PropertySpec.builder("instance", ClassName(packageName, implClassName))
        .mutable(false)
        .initializer("${implClassName}()")
        .build())
        .build()


    val createFunction = com.squareup.kotlinpoet.FunSpec.builder("create")
        .addParameter("view", IView::class.java)
        .returns(IInteractor::class.asTypeName().copy(nullable = true))

        createFunction.addStatement("val configurator = ")
        createFunction.beginControlFlow("when (view)")
        .apply {
            classData.forEach {
                addStatement("is ${it.view.resolveTypeName()} -> ${it.name}Impl()")
            }
        }.addStatement("else -> null").endControlFlow()
            .addStatement("return configurator?.configurate(view)")
    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(implClassName)
        .addFunction(createFunction.build())
        .addType(companion)
        .build()
    return com.squareup.kotlinpoet.FileSpec.builder(packageName, implClassName)
        .addFileComment("Generated automatically")
        .addImports(imports)
        .addType(implClassSpec)
        .build().toString().replace(WILDCARDIMPORT, "*")
}