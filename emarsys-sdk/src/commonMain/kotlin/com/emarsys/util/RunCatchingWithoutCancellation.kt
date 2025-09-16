package com.emarsys.util

import kotlinx.coroutines.CancellationException


suspend inline fun <R> runCatchingWithoutCancellation(block: suspend () -> R): Result<R> {
    return try {
        val result: R = block()
        Result.success(result)
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}