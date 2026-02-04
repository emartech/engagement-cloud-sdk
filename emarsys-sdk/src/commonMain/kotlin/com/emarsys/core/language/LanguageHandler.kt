package com.emarsys.core.language

import com.emarsys.SdkConstants.LANGUAGE_STORAGE_KEY
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.exceptions.SdkException.PreconditionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LanguageHandler(
    private val languageTagValidator: LanguageTagValidatorApi,
    private val stringStorage: StringStorageApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val logger: Logger
) : LanguageHandlerApi {

    override suspend fun handleLanguage(language: String?) {
        if (language != null) {
            if (languageTagValidator.isValid(language)) {
                stringStorage.put(LANGUAGE_STORAGE_KEY, language)
                sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.RegisterDeviceInfo())
            } else {
                val message = "Language $language is not supported!"
                logger.error(message)
                throw PreconditionFailedException(message)
            }
        } else {
            stringStorage.put(LANGUAGE_STORAGE_KEY, null)
            sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.RegisterDeviceInfo())
        }
    }
}
