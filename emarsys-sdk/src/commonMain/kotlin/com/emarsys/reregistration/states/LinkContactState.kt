package com.emarsys.reregistration.states

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.session.SessionContext
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.model.SdkEvent

internal class LinkContactState(
    private val sessionContext: SessionContext,
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name = "linkContactState"

    override fun prepare() {}

    override suspend fun active() {
        sdkLogger.debug("Linking contact")
        if (sessionContext.contactFieldValue != null) {
            sdkLogger.debug("Register LinkContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkContact(
                    contactFieldId = sdkContext.contactFieldId,
                    contactFieldValue = sessionContext.contactFieldValue!!
                )
            )
        } else if (sessionContext.openIdToken != null) {
            sdkLogger.debug("Register LinkAuthenticatedContact event.")
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                    contactFieldId = sdkContext.contactFieldId,
                    openIdToken = sessionContext.openIdToken!!
                )
            )
        } else {
            sdkLogger.debug("No contactFieldValue or openIdToken available.")
        }
    }

    override fun relax() {}
}