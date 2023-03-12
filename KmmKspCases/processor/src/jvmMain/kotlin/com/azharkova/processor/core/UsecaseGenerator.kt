package com.azharkova.processor.core

import com.azharkova.core.CoroutineUseCase
import com.azharkova.core.UseCase
import com.azharkova.processor.util.WILDCARDIMPORT
import com.azharkova.processor.util.addImports
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

fun UsecaseData.generateClassSource(): String {
    val classData = this
    val implClassName = "${classData.name}Impl"

    val inputType =
        if (classData.paramType == null) Unit::class.java.name else classData.paramType?.name.orEmpty()
    val output =
        if (classData.returnsTypeData == null) Unit::class.java.name else classData.returnsTypeData?.name.orEmpty()
    val funcParam = if (inputType.contains("Unit")) "" else "param"
    val executeFunc = FunSpec.builder("execute")
        .addModifiers(KModifier.OVERRIDE)
        .addModifiers(KModifier.SUSPEND)
        .addParameter("parameter", TypeVariableName(inputType))
        .addStatement("return repo.${classData.requestClazz?.functions?.firstOrNull()?.name.orEmpty()}(${funcParam})")
        .returns(TypeVariableName(output))
        .build()

    val initConstructor = FunSpec.constructorBuilder()
        .addParameter("repo", TypeVariableName(classData.apiName.orEmpty()))
        .addStatement("this.repo = ${classData.apiName?.orEmpty()}Impl()")
        .build()

    val property = PropertySpec.builder("repo", TypeVariableName(classData.apiName.orEmpty()))
        .initializer("repo")
        .mutable(true)
        .addModifiers(KModifier.PRIVATE)
        .build()

    val implClassSpec = TypeSpec.classBuilder(implClassName)
        .superclass(
            CoroutineUseCase::class.asTypeName().parameterizedBy(
                TypeVariableName(inputType),
                TypeVariableName(output)
            )
        )
        .addSuperinterface(TypeVariableName(classData.name))
        .primaryConstructor(initConstructor)
        .addProperty(property)
        .addFunction(executeFunc).build()


    return FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addType(implClassSpec)
        .addImports(
            classData.imports + listOf(
                "com.azharkova.kmmkspcases.data.*",
                "${classData.packageName}.${classData.name}",
                "${classData.requestClazz?.packageName}.${classData.requestClazz?.name}",
                "${UseCase::class.java.name}",
                "kotlinx.coroutines.*"
            )
        )
        .build().toString().replace(WILDCARDIMPORT, "*")
}