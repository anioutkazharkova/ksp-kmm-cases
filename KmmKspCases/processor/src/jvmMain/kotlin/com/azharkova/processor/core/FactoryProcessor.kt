package com.azharkova.processor.core

import com.azharkova.processor.data.ClassInfo
import com.azharkova.processor.util.getImplClassFileSource
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter

class FactoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FactoryProcessor(environment)
    }

}

class FactoryProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor  {
    private val logger: KSPLogger = env.logger
    private val codeGenerator = env.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {

        logger.warn(if (resolver == null) "NO " else "YES")
        logger.warn("TEST")

        val modules = getModules(resolver)
        val data = modules.map { module ->
            val className = module.simpleName.asString()
            val packageName = module.packageName.asString()
           getModuleAnnotation(module)?.let { annotation ->
                processModuleAnnotation(annotation)
            }?.apply {
                this.name = className
              this.packageName = packageName
          }
        }.filterNotNull().toList()

        if (data.isNotEmpty()) {
            generateImplClass(data, codeGenerator)
            generateFactoryClass(data, codeGenerator)
        }
        return emptyList()
    }

    private fun processModuleAnnotation(annotation: KSAnnotation):ModuleData {
        val presenter = getParamValue(annotation, "presenter")
        val interactor = getParamValue(annotation, "interactor")
        val view = getParamValue(annotation, "view")
        var import = mutableListOf<String>().apply {
            presenter?.declaration?.let {
                this.add("${it.packageName.asString()}.${it.simpleName.asString()}")
            }
            interactor?.declaration?.let {
                this.add("${it.packageName.asString()}.${it.simpleName.asString()}")
            }
            view?.declaration?.let {
                this.add("${it.packageName.asString()}.${it.simpleName.asString()}")
            }
        }

        return ModuleData(interactor = interactor, presenter = presenter, view = view, imports = import)
    }

    private fun getModules(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(com.azharkova.core.ConfigModule::class.java.name).map { logger.warn(it.annotations.toString())
        it}
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    private fun getModuleAnnotation(declaration: KSAnnotated): KSAnnotation? {
        return declaration.annotations
            .filter { annotation -> annotation.shortName.asString() == "ConfigModule" }
            .find { annotation ->
                annotation.annotationType
                    .resolve()
                    .declaration
                    .qualifiedName
                    ?.asString() == "com.azharkova.core.ConfigModule"
            }
    }

    private fun getParamValue(annotation: KSAnnotation, paramName: String): KSType? {
        val annotationArgument = annotation.arguments
            .find { argument -> argument.name?.asString() == paramName }
        logger.warn(annotationArgument?.value.toString())
        val annotationArgumentValue = annotationArgument?.value as? KSType

        return annotationArgumentValue
    }

    fun generateImplClass(moduleDataList: List<ModuleData>, codeGenerator: CodeGenerator) {
        moduleDataList.forEach { classData ->
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

    fun generateFactoryClass(moduleDataList: List<ModuleData>, codeGenerator: CodeGenerator) {

        val fileSource = moduleDataList.generateClassSource()
        val packageName = moduleDataList.first().packageName
        val fileName = "ConfigFactory"

        codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(fileSource)
            }
        }
    }

}