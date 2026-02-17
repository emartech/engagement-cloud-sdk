package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class RestoreSavedSdkEventsState(
    private val eventsDao: EventsDaoApi,
    private val sdkEventEmitter: SdkEventEmitterApi,
    private val sdkLogger: Logger
) : State {

    override val name: String = "restoreSavedSdkEventsState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Restoring saved SDK events")
        eventsDao.getEvents().collect {
            try {
                sdkEventEmitter.emitEvent(it)
            } catch (exception: Exception) {
                sdkLogger.error(
                    "RestoreSavedSdkEventsState - active",
                    exception,
                    buildJsonObject {
                        put("event", it.toString())
                    }
                )
            }
        }

        return Result.success(Unit)
    }

    override fun relax() {}
}