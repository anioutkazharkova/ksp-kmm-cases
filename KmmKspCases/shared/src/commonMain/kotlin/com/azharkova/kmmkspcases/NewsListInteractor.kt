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
  /* val useCase by lazy {
       SimpleNewsLoadCaseImpl(ApiFactory.NewsApi)
   }*/
    override fun loadNews() {
       /* scope.launch {
            useCase.invoke(Unit).onSuccess {
                presenter?.setupNews(it)
            }
        }*/
        scope.launch {
            NewsLoadCase.usecase().request().onSuccess { data ->
                (data as? NewsList)?.let {
                    presenter?.setupNews(it)
                }
            }
        }
    }
}