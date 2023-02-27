package com.azharkova.kmmkspcases

import com.azharkova.core.Api
import com.azharkova.core.BindRequest
import com.azharkova.core.RequestType
import com.azharkova.kmmkspcases.data.NewsList
import com.azharkova.network.GET

@Api
interface TestApi {
    @GET(LOAD_CRUNCH)
    @BindRequest(RequestType.NEWS)
    suspend fun loadNews(): NewsList

    companion object {
        private val apiKey = "5b86b7593caa4f009fea285cc74129e2"
        private const val LOAD_CRUNCH = "https://newsapi.org/v2/top-headlines?sources=techcrunch&apiKey=5b86b7593caa4f009fea285cc74129e2"
    }
}