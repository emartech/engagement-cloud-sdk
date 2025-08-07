package com.emarsys.api.deeplink

import android.app.Activity
import android.content.Intent

/**
 * Interface for performing deep link tracking operations.
 *
 * Emarsys SDK automatically handles deep link tracking in most cases, with only one exception:
 * manual tracking is needed when your Activity has onNewIntent overridden. In that case, you
 * can track the deep link using the [trackDeepLink] method of this interface.
 */
interface AndroidDeepLinkApi {

    /**
     * Tracks a deep link interaction.
     *
     * This operation extracts the URI from the provided [Intent] and tracks it as a deep link
     * interaction. It ensures that the deep link is tracked only once per activity lifecycle.
     *
     * @param activity The activity from which the deep link interaction originates.
     * @param intent The intent containing the deep link URI.
     */
    suspend fun trackDeepLink(activity: Activity, intent: Intent)
}