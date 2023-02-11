package com.azharkova.processor.data

import com.azharkova.processor.util.getParamAnnotationList
import com.azharkova.processor.util.getRequestTypeAnnotations
import com.azharkova.processor.util.resolveTypeName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier

data class ClassInfo(
    val name: String,
    val packageName: String,
    val functions: List<FunctionData>,
    val imports: List<String>,
    val superClasses: List<String> = emptyList(),
    val properties: List<KSPropertyDeclaration> = emptyList(),
    val modifiers: List<KModifier> = emptyList()
)

data class FunctionData(
    val name: String,
    val returnType: ReturnTypeData,
    val isSuspend: Boolean = false,
    val parameterDataList: List<ParameterData>,
    val annotations: List<FunctionAnnotation> = emptyList(),
    val httpMethodAnnotation: HttpMethodAnnotation
)

data class ParameterData(
    val name: String,
    val type: ReturnTypeData,
    val annotations: List<ParameterAnnotation> = emptyList(),
    val requestTypeClassName: ClassName? = null,
    val hasRequestBuilderAnno: Boolean = false) {
    inline fun <reified T> findAnnotationOrNull(): T? {
        return this.annotations.firstOrNull { it is T } as? T
    }

    inline fun <reified T> hasAnnotation(): Boolean {
        return this.findAnnotationOrNull<T>() != null
    }

    fun hasRequestTypeAnnotation(): Boolean {
        return this.requestTypeClassName != null
    }
}

fun KSValueParameter.createParameterData(logger: KSPLogger): ParameterData {
    val ksValueParameter = this

    val parameterAnnotations = ksValueParameter.getParamAnnotationList(logger)
    val requestTypeAnnotationClassName = ksValueParameter.getRequestTypeAnnotations()

    val parameterName = ksValueParameter.name?.asString() ?: ""
    val parameterType = ksValueParameter.type.resolve()
    val hasRequestBuilderAnno = false


    val type = if (hasRequestBuilderAnno) {
        ReturnTypeData(
            "HttpRequestBuilder.()->Unit",
            "HttpRequestBuilder.()->Unit"
        )
    } else {
        ReturnTypeData(
            parameterType.resolveTypeName(),
            parameterType.declaration.qualifiedName?.asString() ?: ""
        )
    }

    return ParameterData(
        parameterName,
        type,
        parameterAnnotations,
        requestTypeAnnotationClassName,
        hasRequestBuilderAnno
    )
}

data class ReturnTypeData(val name: String, val qualifiedName: String)

open class ParameterAnnotation
class RequestBuilder : ParameterAnnotation()
class RequestType : ParameterAnnotation()

open class FunctionAnnotation

class Headers(val path: List<String>) : FunctionAnnotation()

open class HttpMethodAnnotation(open val path: String, open val httpMethod: String) : FunctionAnnotation()
