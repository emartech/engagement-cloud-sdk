package com.sap.ec.di

import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactory
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactoryApi
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapper
import com.sap.ec.networking.clients.recommendation.RecommendationResponseMapperApi
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal object RecommendationInjection {
    val recommendationModules = module {
        singleOf(::RecommendationRequestFactory) { bind<RecommendationRequestFactoryApi>() }
        single<RecommendationResponseMapperApi> {
            RecommendationResponseMapper(
                json = get(),
                sdkLogger = get { parametersOf(RecommendationResponseMapper::class.simpleName) }
            )
        }
    }
}