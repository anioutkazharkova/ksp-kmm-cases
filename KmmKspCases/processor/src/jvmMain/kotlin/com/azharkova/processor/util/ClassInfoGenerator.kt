package com.azharkova.processor.util

import com.azharkova.network.IApi
import com.azharkova.processor.data.ClassInfo
import com.azharkova.processor.data.FunctionData
import com.squareup.kotlinpoet.*

/**
 * Transform a [ClassData] to a [FileSpec] for KotlinPoet
 */
fun ClassInfo.getImplClassFileSource(): String {
    val classData = this

    val optinAnnotation = com.squareup.kotlinpoet.AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
        .build()

    /**
     * public fun Ktorfit.createExampleApi(): ExampleApi = _ExampleApiImpl(KtorfitClient(this)).also { it.setClient(KtorfitClient(this)) }
     */
    val createExtensionFunctionSpec = com.squareup.kotlinpoet.FunSpec.builder("create${classData.name}")
        .addAnnotation(
            optinAnnotation
        )
        .addModifiers(classData.modifiers)
        .addStatement("return ${classData.name}Impl().also{ it.setClient(NetworkClient()) }")
        .receiver(TypeVariableName(ktorfitExtClass.name))
        .returns(TypeVariableName(classData.name))
        .build()

    /**
     * public override fun setClient(client: KtorfitClient): Unit {
     *     this.client = client
     *   }
     */
    val setClientFunction = com.squareup.kotlinpoet.FunSpec.builder("setClient")
        .addModifiers(com.squareup.kotlinpoet.KModifier.OVERRIDE)
        .addParameter("client", TypeVariableName(clientClass.name))
        .addStatement("this.client = client")
        .build()

    val properties = classData.properties.map { property ->
        val propBuilder = com.squareup.kotlinpoet.PropertySpec.builder(
            property.simpleName.asString(),
            TypeVariableName(property.type.resolve().resolveTypeName())
        )
            .addModifiers(com.squareup.kotlinpoet.KModifier.OVERRIDE)
            .mutable(property.isMutable)
            .getter(
                com.squareup.kotlinpoet.FunSpec.getterBuilder()
                    .addStatement("TODO(\"Not yet implemented\")")
                    .build()
            )

        if (property.isMutable) {
            propBuilder.setter(
                com.squareup.kotlinpoet.FunSpec.setterBuilder()
                    .addParameter("value", TypeVariableName(property.type.resolve().resolveTypeName()))
                    .build()
            )
        }

        propBuilder.build()
    }

    val implClassName = "${classData.name}Impl"

    val clientProperty = com.squareup.kotlinpoet.PropertySpec
        .builder(
            "client",
            TypeVariableName(clientClass.name),

        )
        .addModifiers(com.squareup.kotlinpoet.KModifier.PRIVATE, KModifier.LATEINIT)
       // .initializer("client")
        .mutable(true)
        .build()
    val initConstructor = FunSpec.constructorBuilder()
        //.addParameter("client", TypeVariableName(clientClass.name))
        .addStatement("this.client = ${clientClass.name}()")
        .build()
    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(implClassName)
        .addAnnotation(
            optinAnnotation
        )
        .primaryConstructor(initConstructor)
        .addModifiers(classData.modifiers)
        .addSuperinterface(ClassName(classData.packageName, classData.name))
        .addSuperinterface(IApi::class.java.asTypeName())
        .addFunctions(classData.functions.map { it.toFunSpec() }.flatten())
        .addFunction(setClientFunction)
        .addProperty(
            clientProperty
        )
        .addProperties(properties)
        .build()

    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addImports(classData.imports + listOf("com.azharkova.network.NetworkClient"))
        .addType(implClassSpec)
        .addFunction(createExtensionFunctionSpec)

        .build().toString().replace(WILDCARDIMPORT, "*")
}

fun FunctionData.toFunSpec(): List<FunSpec> {
    val returnTypeName = this.returnType.name
    val typeWithoutOuterType = if (!returnTypeName.contains("Result")) returnTypeName else  returnTypeName.substringAfter("<").substringBeforeLast(">")
    val nullableText = if (!this.returnType.name.endsWith("?")) {
        ""
    } else {
        ""
    }
    return if (this.parameterDataList.any { it.hasRequestTypeAnnotation() }) {
        listOf(
            FunSpec.builder(this.name)
                .addModifiers(mutableListOf(KModifier.PRIVATE).also {
                    if (this.isSuspend) {
                        it.add(KModifier.SUSPEND)
                    }
                })
                .returns(TypeVariableName(this.returnType.name))
                .addParameters(this.parameterDataList.map {
                    ParameterSpec(it.name, it.requestTypeClassName!!)
                })
               .addStatement(
                    if (this.isSuspend) {
                        "return ${clientClass.objectName}.getData<${typeWithoutOuterType}>(${getRequestDataArgumentText(
                            this
                        )})" + nullableText
                    } else {
                        "return ${clientClass.objectName}.getData<${typeWithoutOuterType}>(${getRequestDataArgumentText(
                            this
                        )})" + nullableText
                    }
                )
                .build(),

            FunSpec.builder(this.name)
                .addModifiers(mutableListOf(KModifier.OVERRIDE).also {
                    if (this.isSuspend) {
                        it.add(KModifier.SUSPEND)
                    }
                })
                .returns(TypeVariableName(this.returnType.name))
                .addParameters(this.parameterDataList.map {
                    ParameterSpec(it.name, TypeVariableName(it.type.name))
                })
                .build()
        )
    } else {
        listOf(FunSpec.builder(this.name)
            .addModifiers(mutableListOf(KModifier.OVERRIDE).also {
                if (this.isSuspend) {
                    it.add(KModifier.SUSPEND)
                }
            })
            .returns(TypeVariableName(this.returnType.name))
            .addParameters(this.parameterDataList.map {
                ParameterSpec(it.name, TypeVariableName(it.type.name))
            })

            .addStatement(
                if (this.isSuspend) {
                    "return ${clientClass.objectName}.getData<${typeWithoutOuterType}>(${  getRequestDataArgumentText(
                        this
                    )})" + nullableText
                } else {
                    "return ${clientClass.objectName}.request<${typeWithoutOuterType}>(${  getRequestDataArgumentText(
                        this
                    )})" + nullableText
                }
            )
            .build()
        )
    }
}