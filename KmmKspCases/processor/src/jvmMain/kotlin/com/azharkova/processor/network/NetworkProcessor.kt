package com.azharkova.processor.network

import com.azharkova.core.Api
import com.azharkova.network.*
import com.azharkova.processor.core.ApiData
import com.azharkova.processor.core.generateClassSource
import com.azharkova.processor.data.ClassInfo
import com.azharkova.processor.util.getImplClassFileSource
import com.azharkova.processor.util.toClassData
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter

class NetworkProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
       return NetworkProcessor(environment)
    }

}
class NetworkProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor{
    private val logger: KSPLogger = env.logger
    private val codeGenerator = env.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classDataList = getAnnotatedFunctions(resolver).groupBy { it.closestClassDeclaration()!! }
            .map { (classDec) ->
              classDec.toClassData(logger, resolver)
            }
        generateImplClass(classDataList, codeGenerator)
        val apiData = getApi(resolver).map { api ->
            val className = api.simpleName.asString()
            val packageName = api.packageName.asString()
            ApiData(className,packageName)
        }.toList()

        if (apiData.isNotEmpty()) {
            logger.warn(apiData.generateClassSource())
            logger.warn("has api")
           generateApiFactory(apiData, codeGenerator)
        }

        return emptyList()
    }

    private fun getApi(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(Api::class.java.name).map { logger.warn(it.annotations.toString())
            it}
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    /**
     * Returns a list of all [KSFunctionDeclaration] which are annotated with a Http Method Annotation
     */
    private fun getAnnotatedFunctions(resolver: Resolver): List<KSFunctionDeclaration> {
        val getAnnotated = resolver.getSymbolsWithAnnotation((GET::class.java).name).toList()
        val postAnnotated = resolver.getSymbolsWithAnnotation(POST::class.java.name).toList()
        val putAnnotated = resolver.getSymbolsWithAnnotation(PUT::class.java.name).toList()
        val deleteAnnotated = resolver.getSymbolsWithAnnotation(DELETE::class.java.name).toList()
        val headAnnotated = resolver.getSymbolsWithAnnotation(HEADER::class.java.name).toList()


        val ksAnnotatedList =
            getAnnotated + postAnnotated + putAnnotated + deleteAnnotated + headAnnotated
        return ksAnnotatedList.map { it as KSFunctionDeclaration }
    }
}

fun generateImplClass(classDataList: List<ClassInfo>, codeGenerator: CodeGenerator) {
    classDataList.forEach { classData ->
        val fileSource = classData.getImplClassFileSource()

        val packageName = classData.packageName
        val className = classData.name
        val fileName = "${className}Impl"

        codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(fileSource)
            }
        }
    }
}

fun generateApiFactory(apiList: List<ApiData>, codeGenerator: CodeGenerator) {
    val source = apiList.generateClassSource()
    val packageName = apiList.first().packageName
    val fileName = "ApiFactory"
    codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
        OutputStreamWriter(output).use { writer ->
            writer.write(source)
        }
    }

}
