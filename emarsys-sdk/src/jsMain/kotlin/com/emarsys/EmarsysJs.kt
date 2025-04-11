package com.emarsys

import com.emarsys.api.contact.ContactApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlin.js.Promise

fun main() {
    EmarsysJs().init()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysJs")
class EmarsysJs {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     *
     * @return A promise that resolves when the initialization is complete.
     */
    fun init(): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.initialize()
        }
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jsEmarsysConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.enableTracking(jsEmarsysConfig)
        }
    }

    /**
     * Links a contact to the SDK using the specified contact field ID and value.
     *
     * @param contactFieldId The ID of the contact field.
     * @param contactFieldValue The value of the contact field.
     * @return A promise that resolves when the contact is linked.
     */
    fun linkContact(contactFieldId: Int, contactFieldValue: String): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.linkContact(contactFieldId, contactFieldValue)
        }
    }

    /**
     * Links an authenticated contact to the SDK using the specified contact field ID and OpenID token.
     * Authenticated contacts are already verified through any OpenID provider like Google or Apple
     *
     * @param contactFieldId The ID of the contact field.
     * @param openIdToken The OpenID token for authentication.
     */
    fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.linkAuthenticatedContact(contactFieldId, openIdToken)
        }
    }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     *
     * @return A promise that resolves when the contact is unlinked.
     */
    fun unlinkContact(): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.unlinkContact()
        }
    }

    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     *
     * @param eventName The name of the custom event.
     * @param eventPayload Optional payload for the event.
     * @return A promise that resolves when the event is tracked.
     */
    fun trackCustomEvent(eventName: String, eventPayload: Map<String, String>?): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.trackCustomEvent(eventName, eventPayload)
        }
    }

}