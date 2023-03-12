package com.azharkova.kmmkspcases

import com.azharkova.core.*
import com.azharkova.kmmkspcases.data.NewsList

@ConfigModule(interactor = NewsListInteractor::class,view = INewsListView::class, presenter = NewsListPresenter::class)
interface NewsListModule



@View
interface INewsListView : IView {
    fun setupNews(data: NewsList)
}

interface INewsListPresenter: IPresenter<INewsListView> {
    fun setupNews(data: NewsList)
}

interface INewsListInteractor : IInteractor {
    fun loadNews()
}

@Presenter
class NewsListPresenter: BasePresenter<INewsListView>(), INewsListPresenter {
    override fun setupNews(data: NewsList) {
        view?.setupNews(data)
    }
}

