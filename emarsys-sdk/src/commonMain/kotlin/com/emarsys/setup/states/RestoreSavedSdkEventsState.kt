package com.emarsys.setup.states

import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RestoreSavedSdkEventsState(
    private val eventsDao: EventsDaoApi,
    private val sdkEventEmitter: SdkEventEmitterApi,
    private val sdkLogger: Logger
) : State {

    override val name: String = "restoreSavedSdkEventsState"

    override fun prepare() {}

    override suspend fun active() {
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
    }

    override fun relax() {}
}