package com.emarsys.api.config

import com.emarsys.core.device.NotificationSettings
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSConfigApi {
    fun getContactFieldId(): Promise<Int?>
    fun getApplicationCode(): Promise<String?>
    fun getClientId(): Promise<String>
    fun getLanguageCode(): Promise<String>
    fun getSdkVersion(): Promise<String>
    fun changeApplicationCode(applicationCode: String): Promise<Unit>
    fun setLanguage(language: String): Promise<Unit>
    fun resetLanguage(): Promise<Unit>
    fun getPushSettings(): Promise<NotificationSettings>
}