package com.azharkova.processor.core

import com.azharkova.core.IInteractor
import com.azharkova.core.IModule
import com.azharkova.core.IView
import com.azharkova.processor.util.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName

fun ModuleData.generateClassSource():String {
    val classData = this

    val interactorFunction = com.squareup.kotlinpoet.FunSpec.builder("interactor")
        .receiver(TypeVariableName(classData.view.resolveTypeName()))
        .returns(classData.interactor!!.toClassName().copy(nullable = true))
        .addStatement("return interactor as? ${classData.interactor.resolveTypeName()}")

    val setupFunction = com.squareup.kotlinpoet.FunSpec.builder("setup")
        .receiver(TypeVariableName(classData.view.resolveTypeName()))
        .addStatement("this.interactor = ConfigFactory.instance.create(this)")
        .addStatement("this.interactor?.attachView()")

    val configurateFunction = com.squareup.kotlinpoet.FunSpec.builder("configurate")
        .addModifiers(com.squareup.kotlinpoet.KModifier.OVERRIDE)
        .addParameter("view", IView::class.java)
        .returns(IInteractor::class.java)

    configurateFunction.addStatement("val interactor = ${classData.interactor.resolveTypeName()}()")
    configurateFunction.addStatement("val presenter = ${classData.presenter.resolveTypeName()}()")
    configurateFunction.addStatement("interactor.presenter = presenter")
    configurateFunction.addStatement("presenter.view = view as? ${classData.view.resolveTypeName()}")
    //configurateFunction.addStatement("(view as? ${classData.view.resolveTypeName()})?.setupInteractor(interactor)")
    configurateFunction.addStatement("return interactor")

    val implClassName = "${classData.name}Impl"
    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(implClassName)
        .addSuperinterface(ClassName(classData.packageName, classData.name))
        .addSuperinterface(ClassName("",IModule::class.java.name))

        .addFunction(configurateFunction.build())
        .build()


    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addImports(classData.imports + listOf("${classData.packageName}.ConfigFactory"))
        .addFunction(setupFunction.build())
        .addType(implClassSpec)
        .addFunction(interactorFunction.build())
        .build().toString().replace(WILDCARDIMPORT, "*")
}