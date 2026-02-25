package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosSetup(private val setup: SetupApi) : IosSetupApi {

    override suspend fun enable(
        config: IosEngagementCloudSDKConfig,
        onContactLinkingFailed: OnContactLinkingFailed
    ) {
        val suspendOnContactLinkingFailed =
            convertOnContactLinkingFailedCallbackToSuspendFunction(onContactLinkingFailed)
        setup.enable(config, suspendOnContactLinkingFailed)
    }

    override suspend fun disable() {
        setup.disable()
    }

    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }

    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: OnContactLinkingFailed) {
        val suspendOnContactLinkingFailed =
            convertOnContactLinkingFailedCallbackToSuspendFunction(onContactLinkingFailed)
        setup.setOnContactLinkingFailedCallback(suspendOnContactLinkingFailed)
    }

    private fun convertOnContactLinkingFailedCallbackToSuspendFunction(
        onContactLinkingFailed: OnContactLinkingFailed
    ): suspend () -> LinkContactData? =
        suspend {
            suspendCancellableCoroutine { continuation ->
                onContactLinkingFailed({
                    continuation.resume(it)
                }, { error ->
                    continuation.resumeWithException(
                        error?.let { RuntimeException(error.localizedDescription) }
                            ?: Exception("Unknown error occurred in onContactLinkingFailedCallback")
                    )
                })
            }

        }
}