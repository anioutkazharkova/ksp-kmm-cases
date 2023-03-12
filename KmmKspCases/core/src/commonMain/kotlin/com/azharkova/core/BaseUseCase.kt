package com.azharkova.core

import com.azharkova.core.CoroutineUseCase
import kotlinx.coroutines.*

interface BaseUseCase<in P, out R> {
    suspend fun execute(params: P):R? = null
}


abstract public class CoroutineUseCase<in T, out R>(
    val dispatcher: CoroutineDispatcher = ioDispatcher
) {
    /**
     * Реализация UseCase
     */
   abstract suspend fun execute(param: T): R

    suspend operator fun invoke(param: T): Result<R> = withContext(dispatcher) {
        runCatching { execute(param) }
    }
}

public interface GenericUseCase<T : Any,R: Any> {
    suspend fun execute(param: T? = null): R
}

public suspend fun<T:Any,R:Any> GenericUseCase<T,R>.request(param: T? = null): Result<R> = withContext(
    ioDispatcher) {
    print("test usecase")
    try {
        runCatching {
            execute(param)
        }
    } catch (e: Throwable) {
        print(e.message)
        throw  e
    }
}

public interface  SuspendUseCase {
    /**
     * Реализация UseCase
     */
    suspend fun execute(param: Any? = null): Any
}

public suspend fun SuspendUseCase.request(param: Any? = null): Result<*> = withContext(Dispatchers.Default) {
    print("test usecase")
    runCatching {
        execute(param)
    }
}

