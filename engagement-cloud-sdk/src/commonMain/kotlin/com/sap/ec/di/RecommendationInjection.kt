package com.sap.ec.di

import com.sap.ec.api.recommendation.LoggingRecommendation
import com.sap.ec.api.recommendation.Recommendation
import com.sap.ec.api.recommendation.RecommendationApi
import com.sap.ec.api.recommendation.RecommendationGatherer
import com.sap.ec.api.recommendation.RecommendationInstance
import com.sap.ec.api.recommendation.RecommendationInternal
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdater
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdaterApi
import com.sap.ec.recommendation.networking.RecommendationRequestFactory
import com.sap.ec.recommendation.networking.RecommendationRequestFactoryApi
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapper
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapperApi
import com.sap.ec.recommendation.models.CartItem
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
        single<RecommendationCartStorageUpdaterApi> {
            RecommendationCartStorageUpdater(
                cartItemStorage = get(named(ThreadSafePersistentStoreTypes.RecommendationCartItems))
            )
        }
        single<RecommendationInstance>(named(InstanceType.Logging)) {
            LoggingRecommendation(
                logger = get { parametersOf(LoggingRecommendation::class.simpleName) },
            )
        }
        single<RecommendationInstance>(named(InstanceType.Gatherer)) {
            RecommendationGatherer(
                sdkLogger = get { parametersOf(RecommendationGatherer::class.simpleName) }
            )
        }
        single<RecommendationInstance>(named(InstanceType.Internal)) {
            RecommendationInternal(
                sdkEventDistributor = get(),
                sdkLogger = get { parametersOf(RecommendationInternal::class.simpleName) }
            )
        }
        single<RecommendationApi> {
            Recommendation(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}