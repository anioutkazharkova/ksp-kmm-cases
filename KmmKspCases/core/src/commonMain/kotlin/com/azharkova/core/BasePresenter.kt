package com.azharkova.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

abstract class BasePresenter<T:IView> : IPresenter<T> {
    override var view: T? = null

    fun onAttachView(view: T){
        this.view = view
    }

    fun onDetachView() {
        this.view = null
    }
}
interface  IView {
    var interactor: IInteractor?
}

interface IModule {
    fun configurate(view: IView): IInteractor
}