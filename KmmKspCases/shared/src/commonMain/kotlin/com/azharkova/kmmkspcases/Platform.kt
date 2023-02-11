package com.azharkova.kmmkspcases

import com.azharkova.core.*

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@ConfigModule(interactor = TestInteractor::class,view = ITestView::class, presenter = TestPresenter::class)
interface TestModule



@View
interface ITestView : IView

interface ITestPresenter: IPresenter<ITestView>

interface ITestInteractor : IInteractor

@Presenter
class TestPresenter: BasePresenter<ITestView>(), ITestPresenter

@Interactor
class TestInteractor : BaseInteractor<ITestPresenter>(), ITestInteractor {
    fun calculate() {
        print("5")
    }
}