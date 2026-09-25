package com.sap.ec.di

import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactory
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactoryApi
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapper
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapperApi
import com.sap.ec.recommendation.CartItem
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object RecommendationInjection {
    val recommendationModules = module {
        single<ThreadSafePersistentStoreApi<CartItem>>(named(ThreadSafePersistentStoreTypes.RecommendationCartItems)) {
            ThreadSafePersistentStore(
                id = PersistentStoreIds.RECOMMENDATION_CART_ITEMS_PERSISTENT_ID,
                storage = get(),
                itemSerializer = CartItem.serializer()
            )
        }
        single<RecommendationRequestFactoryApi> {
            RecommendationRequestFactory(
                urlFactory = get(),
                cartItemStorage = get(named(ThreadSafePersistentStoreTypes.RecommendationCartItems))
            )
        }
        single<RecommendationResponseMapperApi> {
            RecommendationResponseMapper(
                json = get(),
                sdkLogger = get { parametersOf(RecommendationResponseMapper::class.simpleName) }
            )
        }
    }
}