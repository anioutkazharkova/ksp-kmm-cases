package com.azharkova.network

import io.ktor.client.*

actual fun createHttpClient(): HttpClient {
    return androidHttpClient()
}