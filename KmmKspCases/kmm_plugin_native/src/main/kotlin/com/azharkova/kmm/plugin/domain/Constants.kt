package com.azharkova.kmm.plugin.domain

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

fun IrPluginContext.executeFunction(): IrSimpleFunction  = this.referenceClass(ClassIds.GENERIC_USE_CASE)?.owner?.functions?.filter {
        it.name.asString() == "execute"
    }?.firstOrNull()!!


internal object Names {
    val DEFAULT_COMPANION = Name.identifier("Companion")
    val USECASE_METHOD = Name.identifier("usecase")
    val USECASE_IMPL = Name.identifier("\$usecase")
    val GENERIC_USE_CASE = Name.identifier("com.azharkova.core.GenericUseCase")
    val COROUTINE_USE_CASE = Name.identifier("com.azharkova.core.SuspendUseCase")
    val UNIT = FqName("kotlin.Unit")
    val ANY = FqName("kotlin.Any")

    val REQUEST = "request"
    val EXECUTE = Name.identifier("execute")
    val REPO = Name.identifier("repo")
    val PARAM = Name.identifier("param")
}


internal object ClassIds {
    val UNIT = ClassId(FqName("kotlin"),Name.identifier("Unit"))
    val ANY = ClassId(FqName("kotlin"),Name.identifier("Any"))
    val GENERIC_USE_CASE = ClassId(FqName("com.azharkova.core"), Name.identifier("GenericUseCase"))
    val COROUTINE_USE_CASE = ClassId(FqName("com.azharkova.core"), Name.identifier("SuspendUseCase"))
}
