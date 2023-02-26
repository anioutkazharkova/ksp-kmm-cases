package com.azharkova.processor.util

import com.azharkova.network.Method
import com.azharkova.processor.data.*
import com.azharkova.processor.util.TypeData.Companion.getMyType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ksp.toKModifier

fun KSClassDeclaration.toClassData(logger: KSPLogger, resolver: Resolver): ClassInfo {
    val ksClassDeclaration = this
    val imports = ksClassDeclaration.getFileImports().toMutableList()
    val packageName = ksClassDeclaration.packageName.asString()
    val className = ksClassDeclaration.simpleName.asString()

    val isJavaClass = ksClassDeclaration.origin.name == "JAVA"

    val isInterface = ksClassDeclaration.classKind == ClassKind.INTERFACE

    val hasTypeParameters = ksClassDeclaration.typeParameters.isNotEmpty()


    val functionDataList: List<FunctionData> =
        ksClassDeclaration.getDeclaredFunctions().toList().map { funcDeclaration ->
            return@map funcDeclaration.toFunctionData(logger, imports, packageName, resolver)
        }


    if (functionDataList.any { it.parameterDataList.any { param -> param.hasRequestTypeAnnotation() } }) {
        imports.add("kotlin.reflect.cast")
    }

    val supertypes =
        ksClassDeclaration.superTypes.toList().filterNot {
            /** In KSP Any is a supertype of an interface */
            it.resolve().resolveTypeName() == "Any"
        }.mapNotNull { it.resolve().declaration.qualifiedName?.asString() }
    val properties = ksClassDeclaration.getAllProperties().toList()


    return ClassInfo(
        name = className,
        packageName = packageName,
        functions = functionDataList,
        imports = imports,
        superClasses = supertypes,
        properties = properties,
        modifiers = ksClassDeclaration.modifiers.mapNotNull { it.toKModifier() })
}

/**
 * Collect all [HttpMethodAnnotation] from a [KSFunctionDeclaration]
 * @return list of [HttpMethodAnnotation]
 */
fun getHttpMethodAnnotations(ksFunctionDeclaration: KSFunctionDeclaration): List<HttpMethodAnnotation> {
    val getAnno = ksFunctionDeclaration.parseHTTPMethodAnnotation("GET")
    val putAnno = ksFunctionDeclaration.parseHTTPMethodAnnotation("PUT")
    val postAnno = ksFunctionDeclaration.parseHTTPMethodAnnotation("POST")
    val deleteAnno = ksFunctionDeclaration.parseHTTPMethodAnnotation("DELETE")
    val headAnno = ksFunctionDeclaration.parseHTTPMethodAnnotation("HEADER")

    return listOfNotNull(getAnno, postAnno, putAnno, deleteAnno, headAnno)
}

fun KSFunctionDeclaration.toFunctionData(
    logger: KSPLogger,
    imports: List<String>,
    packageName: String,
    resolver: Resolver
): FunctionData {
    val funcDeclaration = this
    val functionName = funcDeclaration.simpleName.asString()
    val functionParameters = funcDeclaration.parameters.map { it.createParameterData( logger) }

    val typeData = getMyType(
        funcDeclaration.returnType?.resolve().resolveTypeName().removeWhiteSpaces(),
        imports,
        packageName,
        resolver
    )

    val returnType = ReturnTypeData(
        funcDeclaration.returnType?.resolve().resolveTypeName(),
        typeData.toString()
    )

    val functionAnnotationList = mutableListOf<FunctionAnnotation>()

    funcDeclaration.getHeadersAnnotation()?.let { headers ->
        functionAnnotationList.add(headers)
    }


    val httpMethodAnnoList = getHttpMethodAnnotations(funcDeclaration)

    val firstHttpMethodAnnotation = httpMethodAnnoList.first()


    return FunctionData(
        functionName,
        returnType,
        funcDeclaration.isSuspend,
        functionParameters,
        functionAnnotationList,
        firstHttpMethodAnnotation
    )
}


fun KSValueParameter.getParamAnnotationList(logger: KSPLogger): List<ParameterAnnotation> {

    val paramAnnos = mutableListOf<ParameterAnnotation>()

    return paramAnnos
}