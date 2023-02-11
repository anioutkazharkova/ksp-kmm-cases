package com.azharkova.kmmkspcases

import com.azharkova.core.*
import com.azharkova.network.POST


interface TestApi {
    @POST("url")
   suspend fun test():Result<List<String>>
}

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
