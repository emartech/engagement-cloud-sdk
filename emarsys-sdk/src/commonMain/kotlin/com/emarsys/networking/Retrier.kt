package com.emarsys.networking

import com.emarsys.core.exceptions.RetryLimitReachedException
import kotlinx.coroutines.delay
import kotlin.time.Duration

open class Retrier {
    protected open suspend fun <T> retry(
        retryCount: Int,
        retryDelay: Duration,
        shouldRetry: (T) -> Boolean,
        logic: suspend () -> T
    ): T {
        var retryError: Throwable? = null
        repeat(retryCount) {
            try {
                val result = logic()
                if (shouldRetry(result)) {
                    delay(retryDelay.inWholeMilliseconds)
                } else {
                    return result
                }
            } catch (e: Throwable) {
                retryError = e
            }
        }
        if (retryError == null) {
            throw RetryLimitReachedException("Retry failed after $retryCount retries")
        } else {
            throw retryError!!
        }
    }
}