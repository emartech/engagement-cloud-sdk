package com.sap.ec.core.language

import com.sap.ec.SdkConstants.LANGUAGE_STORAGE_KEY
import com.sap.ec.context.Features.*
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LanguageHandler(
    private val languageTagValidator: LanguageTagValidatorApi,
    private val stringStorage: StringStorageApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val logger: Logger,
    private val sdkContext: SdkContextApi
) : LanguageHandlerApi {

    override suspend fun handleLanguage(language: String?) {
        if (language != null) {
            if (languageTagValidator.isValid(language)) {
                stringStorage.put(LANGUAGE_STORAGE_KEY, language)
                val result =
                    sdkEventManager.registerEvent(SdkEvent.Internal.Sdk.ChangeLanguage())
                        .await<Unit>().result
                handleNetworkResult(result)
            } else {
                val message = "Language $language is not supported!"
                logger.error(message)
                throw PreconditionFailedException(message)
            }
        } else {
            stringStorage.put(LANGUAGE_STORAGE_KEY, null)
            val result =
                sdkEventManager.registerEvent(SdkEvent.Internal.Sdk.ChangeLanguage())
                    .await<Unit>().result
            handleNetworkResult(result)
        }
    }

    private suspend fun handleNetworkResult(result: Result<Unit>) {
        result.fold(
            onSuccess = {
                if (sdkContext.features.contains(EmbeddedMessaging)) {
                    sdkEventManager.registerEvent(
                        SdkEvent.Internal.EmbeddedMessaging.FetchMeta()
                    ).await<Unit>()
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh()
                    )
                } else {
                    logger.debug("Feature Embedded Messaging is disabled, skipping Fetch Meta data job")
                }
            },
            onFailure = {
                logger.error("Register device info failed", it)
            }
        )
    }
}
