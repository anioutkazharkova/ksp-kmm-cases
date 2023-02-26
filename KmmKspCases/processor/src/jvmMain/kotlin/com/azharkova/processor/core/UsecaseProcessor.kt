package com.azharkova.processor.core

import com.azharkova.core.BindRequest
import com.azharkova.processor.data.toClazzData
import com.azharkova.processor.data.toFunctionsData
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter

class UsecaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UsecaseProcessor(environment)
    }

}

class UsecaseProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    private val logger: KSPLogger = env.logger
    private val codeGenerator = env.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val cases = getUsecases(resolver)
        cases.mapNotNull { it.toUsecaseData(resolver) }.apply {
            this.forEach {
                logger.warn(it.name)
            }
            generateImplClass(this.toList(),codeGenerator)
        }
        return emptyList()
    }

    private fun KSClassDeclaration.toUsecaseData(resolver: Resolver):UsecaseData? {
        val usecase = this
        logger.warn(usecase.simpleName.asString())
        val annotation = getAnnotation(usecase, "UseCase", "com.azharkova.core.UseCase")
        return annotation?.let { annotation ->
            val api = getParamValue(annotation, "repo")
            val method = getParamValueSimple(annotation, "request")

            val requestClazz = getFunction(
                resolver,
                BindRequest::class.java.name,
                method.toString(),
                logger
            ).groupBy { it.closestClassDeclaration()!! }.map { (classDesc, funcs) ->
                classDesc.toClazzData(logger, resolver, funcs)
            }.firstOrNull { it.name == api?.declaration?.simpleName?.getShortName().toString() }


            UsecaseData(
                usecase.simpleName.asString(),
                usecase.packageName.asString(),
                requestClazz = requestClazz,
                apiName = api?.declaration?.simpleName?.asString(),
                methodName = method?.toString(),
                paramType = requestClazz?.functions?.firstOrNull()?.parameterDataList?.firstOrNull()?.type,
                returnsTypeData = requestClazz?.functions?.firstOrNull()?.returnType
            )


        } ?: null
    }

    private fun getUsecases(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(com.azharkova.core.UseCase::class.java.name).map { logger.warn(it.annotations.toString())
            it}
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    fun generateImplClass(useCase:  List<UsecaseData>, codeGenerator: CodeGenerator) {
        useCase.forEach { classData ->
            val fileSource = classData.generateClassSource()

            val packageName = classData.packageName
            val className = classData.name
            val fileName = "_${className}Impl"

            codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(fileSource)
                }
            }
        }
    }

}