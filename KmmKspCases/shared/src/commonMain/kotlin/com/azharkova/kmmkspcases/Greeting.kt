package com.azharkova.kmmkspcases

import com.azharkova.core.*
import com.azharkova.kmmkspcases.data.NewsList
import kotlinx.coroutines.runBlocking

class Greeting {
    private val platform: Platform = getPlatform()


    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}





