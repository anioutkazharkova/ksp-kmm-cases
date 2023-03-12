package com.azharkova.kmmkspcases

import com.azharkova.core.BaseInteractor
import com.azharkova.core.Interactor
import com.azharkova.core.SuspendUseCase
import com.azharkova.core.request
import com.azharkova.kmmkspcases.data.NewsList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Interactor
class NewsListInteractor () : BaseInteractor<INewsListPresenter>(), INewsListInteractor {

    override fun loadNews() {
        scope.launch {
            NewsLoadCase.usecase().request().onSuccess { data ->
                (data as? NewsList)?.let {
                    presenter?.setupNews(it)
                }
            }
        }
    }
}