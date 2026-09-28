package com.sap.ec.disable.states

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.recommendation.CartItem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class ClearRecommendationCartState(
    private val cartItemStorage: ThreadSafePersistentStoreApi<CartItem>,
    private val sdkLogger: Logger
) : State {
    override val name = "clearRecommendationCart"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return try {
            cartItemStorage.clear()
            Result.success(Unit)
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Exception thrown during clearing cartItemStorage", e)
            Result.failure(e)
        }
    }

    override fun relax() {
    }
}