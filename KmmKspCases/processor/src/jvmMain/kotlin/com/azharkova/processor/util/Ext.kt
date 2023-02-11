package com.azharkova.processor.util

import com.azharkova.annotations.HEADER
import com.azharkova.network.httpMethod
import com.azharkova.network.method
import com.azharkova.processor.data.*
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.File

@OptIn(KspExperimental::class)
fun KSFunctionDeclaration.getHeadersAnnotation(): Headers? {
    return this.getAnnotationsByType(HEADER::class).firstOrNull()?.let { headers ->
        return Headers(headers.value.toList())
    }
}


fun KSFunctionDeclaration.getAnnotationByName(name: String): KSAnnotation? {
    return this.annotations.toList().firstOrNull { it.shortName.asString() == name }
}

fun <T> KSAnnotation.getArgumentValueByName(name: String): T? {
    return this.arguments.firstOrNull { it.name?.asString() == name }?.value as? T
}


val KSFunctionDeclaration.isSuspend: Boolean
    get() = (this).modifiers.contains(Modifier.SUSPEND)


fun KSType?.resolveTypeName(): String {
    //TODO: Find better way to handle type alias Types
    return this.toString().removePrefix("[typealias ").removeSuffix("]")
}


fun String.prefixIfNotEmpty(s: String): String {
    return (s + this).takeIf { this.isNotEmpty() } ?: this
}

fun String.postfixIfNotEmpty(s: String): String {
    return (this + s).takeIf { this.isNotEmpty() } ?: this
}

fun String.surroundIfNotEmpty(prefix: String = "", postFix: String = ""): String {
    return this.prefixIfNotEmpty(prefix).postfixIfNotEmpty(postFix)
}


fun String.surroundWith(s: String): String {
    return s + this + s
}

fun KSFunctionDeclaration.parseHTTPMethodAnnotation(name: String): HttpMethodAnnotation? {
    return when (val annotation = this.getAnnotationByName(name)) {
        null -> {
            null
        }

        else -> {
                val value = annotation.getArgumentValueByName<String>("value") ?: ""
                HttpMethodAnnotation(value, name)
            }

        }
    }

/**
 * Gets the imports of a class by reading the imports from the file
 * which contains the class
 *  TODO: Find better way to get imports
 */
fun KSClassDeclaration.getFileImports(): List<String> {
    val importList =
        File(this.containingFile!!.filePath)
            .readLines()
            .filter { it.trimStart().startsWith("import") }
            .toMutableSet()

    //importList.add(ktorfitClass.packageName + "." + ktorfitClass.name)
    //importList.add("de.jensklingenberg.ktorfit.internal.*")

    return importList.map { it.removePrefix("import ") }
}


fun String.removeWhiteSpaces(): String {
    return this.replace("\\s".toRegex(), "")
}


fun FileSpec.Builder.addImports(imports: List<String>): FileSpec.Builder {

    imports.forEach {
        /**
         * Wildcard imports are not allowed by KotlinPoet, as a workaround * is replaced with WILDCARDIMPORT, and it will be replaced again
         * after Kotlin Poet generated the source code
         */
        val packageName = it.substringBeforeLast(".")
        val className = it.substringAfterLast(".").replace("*", WILDCARDIMPORT)

        this.addImport(packageName, className)
    }
    return this
}

const val WILDCARDIMPORT = "WILDCARDIMPORT"


fun KSAnnotated.getRequestTypeAnnotations(): ClassName? {
    val requestTypeClazz = RequestType::class
    val filteredAnnotations = this.annotations.filter {
        it.shortName.getShortName() == requestTypeClazz.simpleName
                && it.annotationType.resolve().declaration.qualifiedName?.asString() == requestTypeClazz.qualifiedName
    }
    return filteredAnnotations.mapNotNull {
        it.arguments.map { arg ->
            (arg.value as KSType).toClassName()
        }.firstOrNull()
    }.firstOrNull()
}

data class KtorfitClass(val name: String, val packageName: String, val objectName: String)

val clientClass = KtorfitClass("NetworkClient", "com.azharkova.network", "client")
//val requestDataClass = KtorfitClass("RequestData", "com.azharkova.network", "getData")
val ktorfitExtClass = KtorfitClass("Platform", "com.azharkova", "")
val pathDataClass = KtorfitClass("PathData", "de.jensklingenberg.ktorfit.internal", "")


fun getRequestDataArgumentText(functionData: FunctionData): String {
    val methodAnnotation = functionData.httpMethodAnnotation
    //METHOD
    val method = "httpMethod=\"${methodAnnotation.httpMethod}\""
    //HEADERS
    //val headersText = "headers =${getHeadersArgumentText(functionData.annotations, functionData.parameterDataList)}"
    //BODY
   // val body = getBodyDataText(functionData.parameterDataList)
    //URL
    val urlPath = "path = \"${functionData.httpMethodAnnotation.path}\""
    //val pathsText = getPathsText(functionData.parameterDataList)
   // val queryText = getQueryArgumentText(functionData.parameterDataList)
   // val fieldsText = getFieldArgumentsText(functionData.parameterDataList)
    //val partsText = getPartsArgumentText(functionData.parameterDataList)
   // val builderText = getRequestBuilderText(functionData.parameterDataList)


    val args = listOf(

        urlPath,
        method,
      //  headersText,
       // body,
        //queryText,
        //fieldsText,
       // partsText,
       // builderText,
        //pathsText
    ).filter { it.isNotEmpty() }.joinToString(",\n") { it }

    return "$args \n"
}

/*
fun getRelativeUrlArgumentText(methodAnnotation: HttpMethodAnnotation, params: List<ParameterData>): String {

    var urlPath = ""

    if (methodAnnotation.path.isNotEmpty()) {
        urlPath = methodAnnotation.path
    } else {
        params.firstOrNull { it.hasAnnotation<Url>() }?.let {
            urlPath = "\${" + it.name + "}"
        }
    }

    return "relativeUrl=\"$urlPath\""
}*/