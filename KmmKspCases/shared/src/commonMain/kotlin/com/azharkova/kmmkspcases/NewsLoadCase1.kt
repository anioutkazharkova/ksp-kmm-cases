package com.azharkova.kmmkspcases

import com.azharkova.core.GenericUseCase
import com.azharkova.core.SuspendUseCase
import com.azharkova.kmmkspcases.data.NewsList

//После генерации
class NewsLoadCase1 {
    companion object {
        fun usecase() = usecase
    }

    object usecase : SuspendUseCase {

        val repo by lazy {
            NewsApiImpl()
        }
        override suspend fun execute(param: Any?): Any {
            return repo.loadNews()
        }

    }
}