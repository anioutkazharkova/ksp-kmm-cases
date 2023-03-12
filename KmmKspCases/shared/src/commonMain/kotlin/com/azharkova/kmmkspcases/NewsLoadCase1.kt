package com.azharkova.kmmkspcases

import com.azharkova.core.GenericUseCase
import com.azharkova.kmmkspcases.data.NewsList

//После генерации
class NewsLoadCase1 {
    companion object {
        fun usecase() = usecase
    }

    object usecase : GenericUseCase<Unit, NewsList> {

        val repo by lazy {
            NewsApiImpl()
        }
        override suspend fun execute(param: Unit?): NewsList {
            return repo.loadNews()
        }

    }
}