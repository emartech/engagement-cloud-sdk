package com.emarsys.core.language

import com.emarsys.SdkConstants.LANGUAGE_STORAGE_KEY
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow

internal class LanguageHandler(
    private val languageTagValidator: LanguageTagValidatorApi,
    private val stringStorage: StringStorageApi,
    private val sdkEvents: MutableSharedFlow<SdkEvent>,
    private val logger: Logger
): LanguageHandlerApi {

    override suspend fun handleLanguage(language: String?) {
        if (language != null) {
            if (languageTagValidator.isValid(language)) {
                    stringStorage.put(LANGUAGE_STORAGE_KEY, language)
                    sdkEvents.emit(SdkEvent.Internal.Sdk.DeviceInfoUpdateRequired())
            } else {
                val message = "Language $language is not supported!"
                logger.debug(LanguageHandler::class.simpleName!!, message)
                throw PreconditionFailedException(message)
            }
        } else {
            stringStorage.put(LANGUAGE_STORAGE_KEY, null)
            sdkEvents.emit(SdkEvent.Internal.Sdk.DeviceInfoUpdateRequired())
        }
    }
}
