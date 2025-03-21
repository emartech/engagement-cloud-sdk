package com.emarsys.core.language

import com.emarsys.SdkConstants.LANGUAGE_STORAGE_KEY
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow

internal class LanguageHandler(
    private val supportedLanguagesProvider: Provider<List<String>>,
    private val stringStorage: StringStorageApi,
    private val sdkEvents: MutableSharedFlow<SdkEvent>,
    private val logger: Logger
): LanguageHandlerApi {

    override suspend fun handleLanguage(language: String?) {
        if (language != null) {
            try {
                supportedLanguagesProvider.provide().first {
                    it.lowercase() == language.lowercase()
                }.let {
                    stringStorage.put(LANGUAGE_STORAGE_KEY, it)
                    sdkEvents.emit(SdkEvent.Internal.Sdk.DeviceInfoUpdateRequired())
                }
            } catch (e: Throwable) {
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
