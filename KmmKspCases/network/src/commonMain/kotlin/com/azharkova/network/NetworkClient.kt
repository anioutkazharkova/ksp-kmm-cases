package com.azharkova.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

expect fun createHttpClient() : HttpClient

class NetworkClient {
    var httpClient = createHttpClient()

    suspend inline fun<reified T> getData(path: String, headers: Map<String, String> = mapOf(), httpMethod: String): T {
       print(path)
        return try {
            val response = httpClient.get {
                url(path)
                method = httpMethod.httpMethod()
                headers.map {
                    header(it.key, it.value)
                }
            }
            response.body<T>()
        }catch (e: Exception) {
            throw  e
        }
    }
}