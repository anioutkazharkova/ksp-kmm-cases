package com.azharkova.kmmkspcases

import com.azharkova.core.IView
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Interactors<T:Any>(private val clazz: KClass<T>, private val view: IView):
    ReadWriteProperty<Any?, T?> {
    private var wref : WeakReference<T>? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        wref = value?.let { WeakReference(it) }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (wref == null) {
            wref = WeakReference(ConfigFactory.instance.create(view) as? T)
        }
        return wref?.get()
    }
}

//simply weakify
public inline fun <reified T:Any> interactors(view: IView) = Interactors(T::class, view)