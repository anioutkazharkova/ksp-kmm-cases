package com.azharkova.core

import com.azharkova.core.CoroutineUseCase
import kotlinx.coroutines.*

data class ParamIn(var value: Any? = null)

data class ParamOut(var value: Any? = null)

interface BaseUseCase<in P, out R> {
    suspend fun execute(params: P):R? = null
}

interface IUseCase {
    suspend fun execute(paramIn: ParamIn):ParamOut = ParamOut()
}


abstract public class CoroutineUseCase<in T:Any, out R:Any>(
    val dispatcher: CoroutineDispatcher = ioDispatcher
) {
    /**
     * Реализация UseCase
     */
    open suspend fun<T:Any, R: Any> execute(param: T): R = kotlin.TODO()

    open suspend fun executeSimple(param: T): R = kotlin.TODO()
    /**
     * Выполняет UseCase
     */
    suspend operator fun invoke(param: T): Result<R> = withContext(dispatcher) {
        runCatching { executeSimple(param) }
    }
    inline public suspend fun<reified T:Any, R: Any> request(param: T): Result<R> = withContext(Dispatchers.Default) {
       print("test usecase")
        runCatching {
            execute<T,R>(param) }
    }
}


public interface  SuspendUseCase {
    /**
     * Реализация UseCase
     */
    suspend fun execute(param: Any? = null): Any


    /*open suspend fun<T:Any, R: Any> execute(param: T): R =  kotlin.TODO()

    suspend fun executeSimple(param: T): R = kotlin.TODO()

    inline public suspend fun<reified T:Any, R: Any> request(param: T): Result<R> = withContext(Dispatchers.Default) {
        print("test usecase")
        runCatching {
            execute(param) }
    }*/
}

public suspend fun SuspendUseCase.request(param: Any? = null): Result<*> = withContext(Dispatchers.Default) {
    print("test usecase")
    runCatching {
        execute(param) }
}

