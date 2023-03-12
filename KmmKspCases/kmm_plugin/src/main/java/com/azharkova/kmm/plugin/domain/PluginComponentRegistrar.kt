package com.azharkova.kmm.plugin.domain

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class PluginComponentRegistrar() : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        configuration.kotlinSourceRoots.forEach {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "*** Hello from ***" + it.path
            )
        }

       SyntheticResolveExtension.registerExtension(SyntethicExtension())
      IrGenerationExtension.registerExtension(PluginGenerationExtension(messageCollector = messageCollector))
        val string = "Hello, World!"
        val file = "file.txt"

        //IrGenerationExtension.registerExtension( TemplateIrGenerationExtension(messageCollector, string, file))
    }

}