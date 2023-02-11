package com.azharkova.network

import io.ktor.http.*

enum class Method (var value: String){
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");
}

fun Method.httpMethod():HttpMethod {
    return when(this) {
        Method.GET -> HttpMethod.Get
        Method.POST -> HttpMethod.Post
        Method.DELETE -> HttpMethod.Delete
        Method.PUT -> HttpMethod.Put
        Method.PATCH -> HttpMethod.Patch
    }
}

fun String.method():Method {
    return Method.values().firstOrNull { it.value == this } ?: Method.GET
}

fun String.httpMethod():HttpMethod {
    return Method.values().firstOrNull { it.value == this }?.httpMethod() ?: HttpMethod.Get
}