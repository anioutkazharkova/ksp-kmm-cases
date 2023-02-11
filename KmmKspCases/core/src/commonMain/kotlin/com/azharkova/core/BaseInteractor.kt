package com.azharkova.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

abstract class BaseInteractor<T:IPresenter<*>>():IInteractor {
    protected lateinit var scope: CoroutineScope
    protected lateinit var job: Job

    var presenter: T? = null

    override fun attachView() {
        job = SupervisorJob()
        scope = CoroutineScope(job + uiDispatcher)
    }

   override fun detachView() {
        job.cancel()
    }
}

interface IInteractor {

    fun attachView()

    fun detachView()
}

interface IPresenter<T:IView> {
    var view: T?
}