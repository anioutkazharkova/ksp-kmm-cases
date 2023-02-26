package com.azharkova.processor.core

import com.azharkova.processor.data.ClazzInfo
import com.azharkova.processor.data.ReturnsTypeData

data class UsecaseData (
    var name: String = "",
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    var requestClazz: ClazzInfo? = null,
    var apiName: String? = null,
    var methodName: String? = null,
    val paramType: ReturnsTypeData? = null,
    val returnsTypeData: ReturnsTypeData? = null
 )