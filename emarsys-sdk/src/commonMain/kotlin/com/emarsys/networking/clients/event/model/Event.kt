package com.emarsys.networking.clients.event.model

import com.emarsys.SdkConstants.APP_START_EVENT_NAME
import com.emarsys.SdkConstants.DISMISS_EVENT_NAME
import com.emarsys.SdkConstants.INAPP_VIEWED_EVENT_NAME
import com.emarsys.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.PUSH_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_END_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_START_EVENT_NAME
import com.emarsys.core.providers.TimestampProvider
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
sealed interface SdkEvent {
    val name: String
    val timestamp: Instant
    val attributes: JsonObject?

    sealed interface External : SdkEvent {
        @Serializable
        @SerialName("custom")
        data class Custom(
            override val name: String,
            override val attributes: JsonObject? = null,
            override val timestamp: Instant = TimestampProvider().provide(),
        ) : External

        sealed interface Api : External {
            data class Push(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class InApp(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class SilentPush(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class OnEventAction(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class BadgeCount(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api
        }
    }

    sealed interface Internal : SdkEvent {

        interface Reporting : Internal

        @Serializable
        @SerialName("internal")
        sealed class Sdk(override val name: String) : Internal {
            data class Metric(
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(name)

            data class AppStart(
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(APP_START_EVENT_NAME)

            data class SessionStart(
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(SESSION_START_EVENT_NAME)

            data class SessionEnd(
                val duration: Long,
                override val attributes: JsonObject? = buildJsonObject {
                    put(
                        "duration",
                        JsonPrimitive(duration.toString())
                    )
                },
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(SESSION_END_EVENT_NAME)

            data class Dismiss(
                val campaignId: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(DISMISS_EVENT_NAME)
        }

        sealed class Push(
            override val name: String
        ) : Internal {
            data class Clicked(
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Push(PUSH_CLICKED_EVENT_NAME), Reporting
        }

        sealed class InApp(override val name: String) : Internal {
            data class Viewed(
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : InApp(INAPP_VIEWED_EVENT_NAME), Reporting

            data class ButtonClicked(
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : InApp(IN_APP_BUTTON_CLICKED_EVENT_NAME), Reporting
        }

        data class SilentPush(
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        data class OnEventAction(
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        data class BadgeCount(
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal
    }
}

