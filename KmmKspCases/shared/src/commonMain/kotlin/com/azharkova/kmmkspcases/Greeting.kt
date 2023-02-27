package com.azharkova.kmmkspcases

import com.azharkova.core.*
import kotlinx.coroutines.runBlocking

class Greeting {
    private val platform: Platform = getPlatform()


    fun greet(): String {
        runBlocking {
            try {
                val k = NewsLoadCase.usecase()
                print(k)
                val data = k.request()
                val t = k
                print(data)//.request<Unit, List<String>>(Unit)
            } catch (e: Exception) {
                print(e.message)
            }
        }
        return "Hello, ${platform.name}!"
    }
}





