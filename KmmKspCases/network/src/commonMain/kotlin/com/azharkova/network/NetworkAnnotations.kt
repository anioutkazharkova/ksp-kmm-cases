package com.azharkova.network

@Target(AnnotationTarget.FUNCTION)
annotation class GET(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class POST(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class PUT(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class DELETE(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class HEADER(vararg val value: String)