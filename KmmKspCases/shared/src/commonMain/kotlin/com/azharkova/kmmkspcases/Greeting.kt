package com.azharkova.kmmkspcases

import com.azharkova.core.*
import com.azharkova.kmmkspcases.data.NewsList
import com.azharkova.network.GET
import com.azharkova.network.POST
import kotlinx.coroutines.runBlocking

@Api
interface TestApi {
    @POST("url")
    @BindRequest(RequestType.TEST)
   suspend fun test():List<String>

   @GET("url2")
   @BindRequest(RequestType.SECOND)
   suspend fun second():List<Int>

    @GET("url2")
    @BindRequest(RequestType.THIRD)
    suspend fun third(value: String):List<Int>


    @GET(LOAD_CRUNCH)
    @BindRequest(RequestType.NEWS)
    suspend fun loadNews():NewsList

    companion object {
        private val apiKey = "5b86b7593caa4f009fea285cc74129e2"
        private const val LOAD_CRUNCH = "https://newsapi.org/v2/top-headlines?sources=techcrunch&apiKey=5b86b7593caa4f009fea285cc74129e2"
    }
}

@UseCase(repo = TestApi::class, request = RequestType.TEST)
interface TestUseCase

@UseCase(repo = TestApi::class, request = RequestType.SECOND)
interface SecondUseCase

class Greeting {
    private val platform: Platform = getPlatform()


    fun greet(): String {
        runBlocking {
            try {
                val k = NewsLoadCase.usecase()
                print(k)
                val data = k.execute()
                val t = k
                print(data)//.request<Unit, List<String>>(Unit)
            } catch (e: Exception) {
                print(e.message)
            }
        }
         //  val list = TestCase.usecase().invoke()
//val list: List<Int> = TestCase.usecase().invoke("path")
//TestCase.usecase().invoke("path")
       // }
        return "Hello, ${platform.name}!"
    }
}





