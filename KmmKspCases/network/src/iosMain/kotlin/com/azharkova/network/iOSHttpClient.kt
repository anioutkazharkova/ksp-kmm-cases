package com.azharkova.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.engine.ios.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient() : HttpClient {
    return iOSHttpClient()
}

internal  fun iOSHttpClient() = HttpClient(Darwin.create()) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}