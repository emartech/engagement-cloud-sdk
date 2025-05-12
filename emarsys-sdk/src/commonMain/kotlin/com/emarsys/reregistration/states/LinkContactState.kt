package com.emarsys.reregistration.states

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.model.SdkEvent

internal class LinkContactState(
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name = "linkContactState"

    override fun prepare() {}

    override suspend fun active() {
        sdkLogger.debug("Linking contact")
        if (sdkContext.contactFieldValue != null) {
            sdkLogger.debug("Register LinkContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkContact(
                    contactFieldId = sdkContext.contactFieldId,
                    contactFieldValue = sdkContext.contactFieldValue!!
                )
            )
        } else if (sdkContext.openIdToken != null) {
            sdkLogger.debug("Register LinkAuthenticatedContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                    contactFieldId = sdkContext.contactFieldId,
                    openIdToken = sdkContext.openIdToken!!
                )
            )
        } else {
            sdkLogger.debug("No contactFieldValue or openIdToken available.")
        }
    }

    override fun relax() {}
}