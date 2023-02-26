package com.azharkova.processor.core

import com.google.devtools.ksp.symbol.KSType

data class ApiData(  var name: String = "",
                var packageName: String = "",
                var imports: List<String> = emptyList(),)
