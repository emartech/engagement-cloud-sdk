package com.sap.ec.reregistration.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
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
        return if (sdkContext.contactFieldValue != null) {
            sdkLogger.debug("Register LinkContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkContact(
                    contactFieldValue = sdkContext.contactFieldValue!!
                )
            ).await<Response>().mapToUnitOrFailure()
        } else if (sdkContext.openIdToken != null) {
            sdkLogger.debug("Register LinkAuthenticatedContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
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