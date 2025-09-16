package com.emarsys.reregistration.states

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.response.mapToUnitOrFailure
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
        return if (sdkContext.contactFieldValue != null) {
            sdkLogger.debug("Register LinkContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkContact(
                    contactFieldId = sdkContext.contactFieldId,
                    contactFieldValue = sdkContext.contactFieldValue!!
                )
            ).await<Response>().mapToUnitOrFailure()
        } else if (sdkContext.openIdToken != null) {
            sdkLogger.debug("Register LinkAuthenticatedContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                    contactFieldId = sdkContext.contactFieldId,
                    openIdToken = sdkContext.openIdToken!!
                )
            ).await<Response>().mapToUnitOrFailure()
        } else {
            sdkLogger.debug("No contactFieldValue or openIdToken available.")
            Result.success(Unit)
        }
    }

    override fun relax() {}
}