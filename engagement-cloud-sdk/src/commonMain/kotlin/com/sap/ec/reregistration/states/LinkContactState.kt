package com.sap.ec.reregistration.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.response.mapToUnitOrFailure
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LinkContactState(
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name = "linkContactState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Linking contact")
        if (sdkContext.onContactLinkingFailed == null) {
            sdkLogger.debug("No onContactLinkingFailed callback provided, skipping contact linking.")
            return Result.success(Unit)
        }

        val linkContactData = try {
            sdkContext.onContactLinkingFailed?.invoke()
        } catch (e: Exception) {
            sdkLogger.debug("Error invoking onContactLinkingFailed callback: ${e.message}", e)
            return Result.failure(e)
        }

        return linkContactData?.let { linkContactData ->
            sdkLogger.debug("Register LinkContact event.")
            sdkEventDistributor.registerEvent(
                linkContactData.toLinkContactEvent()
            ).await<Response>().mapToUnitOrFailure()
        } ?: run {
            sdkLogger.debug("No contact linking data provided, skipping contact linking.")
            Result.success(Unit)
        }
    }


    override fun relax() {}
}