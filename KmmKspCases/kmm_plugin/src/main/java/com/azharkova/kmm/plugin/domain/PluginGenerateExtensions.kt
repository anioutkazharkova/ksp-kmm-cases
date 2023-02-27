package com.azharkova.kmm.plugin.domain

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump

class PluginGenerationExtension (private val messageCollector: MessageCollector,) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
       UseCaseLoweringPass(pluginContext, messageCollector).lower(moduleFragment)
        messageCollector.report(
            CompilerMessageSeverity.WARNING, "***Hello from compiler")
    }
}