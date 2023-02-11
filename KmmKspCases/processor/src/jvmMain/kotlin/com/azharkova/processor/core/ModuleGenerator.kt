package com.azharkova.processor.core

import com.azharkova.core.IInteractor
import com.azharkova.core.IModule
import com.azharkova.core.IView
import com.azharkova.processor.util.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeVariableName

fun ModuleData.generateClassSource():String {
    val classData = this

    val configurateFunction = com.squareup.kotlinpoet.FunSpec.builder("configurate")
        .addModifiers(com.squareup.kotlinpoet.KModifier.OVERRIDE)
        .addParameter("view", IView::class.java)
        .returns(IInteractor::class.java)

    configurateFunction.addStatement("val interactor = ${classData.interactor.resolveTypeName()}()")
    configurateFunction.addStatement("val presenter = ${classData.presenter.resolveTypeName()}()")
    configurateFunction.addStatement("interactor.presenter = presenter")
    configurateFunction.addStatement("presenter.view = view as? ${classData.view.resolveTypeName()}")
    configurateFunction.addStatement("(view as? ${classData.view.resolveTypeName()})?.interactor = interactor")
    configurateFunction.addStatement("return interactor")

    val implClassName = "${classData.name}Impl"
    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(implClassName)
        .addSuperinterface(ClassName(classData.packageName, classData.name))
        .addSuperinterface(ClassName("",IModule::class.java.name))

        .addFunction(configurateFunction.build())
        .build()


    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addImports(classData.imports)
        .addType(implClassSpec)

        .build().toString().replace(WILDCARDIMPORT, "*")
}