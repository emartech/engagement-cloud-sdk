package com.sap.ec.api.config

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.api.SdkState
import com.sap.ec.core.device.NotificationSettings

/**
 * Interface for managing SDK configuration settings.
 *
 * This API allows retrieving and modifying various configuration parameters.
 */
interface ConfigApi: AutoRegisterable {

    /**
     * Retrieves the application code configured.
     *
     * @return The application code, or `null` if not configured.
     */
    suspend fun getApplicationCode(): String?

    /**
     * Retrieves the client ID associated with the SDK.
     *
     * @return The client ID as a string.
     */
    suspend fun getClientId(): String

    /**
     * Retrieves the language code currently set for the SDK.
     *
     * @return The language code as a string.
     */
    suspend fun getLanguageCode(): String

    /**
     * Retrieves the version number of the host Application.
     *
     * @return The host Application's version number as a string.
     */
    suspend fun getApplicationVersion(): String

    /**
     * Retrieves the version number of the SDK.
     *
     * @return The SDK version number as a string.
     */
    suspend fun getSdkVersion(): String

    /**
     * Retrieves the current state of the SDK.
     * Possible states include Active, OnHold, Inactive, and Initialized.
     *
     * @return The SDK state as an SdkState enum.
     */
    suspend fun getCurrentSdkState(): SdkState

    /**
     * Changes the application code.
     *
     * This operation validates the provided application code.
     *
     * Example usage:
     * ```kotlin
     * EngagementCloud.config.changeApplicationCode("ABCDE-12345")
     * ```
     *
     * @param applicationCode The new application code to set.
     * @return A [Result] indicating success or failure of the operation.
     * In case of an invalid application code, the result contains an [InvalidApplicationCodeException][com.sap.ec.core.exceptions.SdkException.InvalidApplicationCodeException]
     */
    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>

    /**
     * Sets the language for the SDK.
     *
     * This operation updates the language settings for the SDK, enabling localized functionality.
     *
     * @param language The language code to set (e.g., "en", "de").
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun setLanguage(language: String): Result<Unit>

    /**
     * Resets the language settings for the SDK to the default value.
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun resetLanguage(): Result<Unit>

    /**
     * Retrieves the notification settings configured for the SDK.
     *
     * @return A [NotificationSettings] object containing the current notification settings.
     */
    suspend fun getNotificationSettings(): NotificationSettings
}