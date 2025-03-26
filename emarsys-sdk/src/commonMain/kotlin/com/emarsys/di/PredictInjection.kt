package com.emarsys.di

import com.emarsys.api.predict.GathererPredict
import com.emarsys.api.predict.LoggingPredict
import com.emarsys.api.predict.Predict
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.predict.PredictCall
import com.emarsys.api.predict.PredictContext
import com.emarsys.api.predict.PredictContextApi
import com.emarsys.api.predict.PredictInstance
import com.emarsys.api.predict.PredictInternal
import com.emarsys.core.collections.PersistentList
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object PredictInjection {
    val predictModules = module {
        single<MutableList<PredictCall>>(named(PersistentListTypes.PredictCall)) {
            PersistentList(
                id = PersistentListIds.PREDICT_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = PredictCall.serializer(),
                elements = listOf()
            )
        }
        single<PredictContextApi> {
            PredictContext(
                calls = get(named(PersistentListTypes.PredictCall))
            )
        }
        single<PredictInstance>(named(InstanceType.Logging)) {
            LoggingPredict(
                logger = get { parametersOf(LoggingPredict::class.simpleName) },
            )
        }
        single<PredictInstance>(named(InstanceType.Gatherer)) {
            GathererPredict(
                predictContext = get()
            )
        }
        single<PredictInstance>(named(InstanceType.Internal)) { PredictInternal() }
        single<PredictApi> {
            Predict(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}