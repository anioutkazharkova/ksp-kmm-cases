package com.azharkova.processor.core

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName

data class ModuleData(
    var name: String = "",
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    var presenter: KSType? = null,
    var interactor: KSType? = null,
    var view: KSType? = null
)
