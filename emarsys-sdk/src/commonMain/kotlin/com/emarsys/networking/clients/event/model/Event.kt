package com.emarsys.networking.clients.event.model

import com.emarsys.SdkConstants.APP_START_EVENT_NAME
import com.emarsys.SdkConstants.DISMISS_EVENT_NAME
import com.emarsys.SdkConstants.INAPP_VIEWED_EVENT_NAME
import com.emarsys.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.PUSH_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_END_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_START_EVENT_NAME
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.core.providers.UUIDProvider
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed interface SdkEvent {
    val id: String
    val name: String
    val timestamp: Instant
    val attributes: JsonObject?

    sealed interface External : SdkEvent {
        @Serializable
        @SerialName("custom")
        data class Custom(
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject? = null,
            override val timestamp: Instant = TimestampProvider().provide(),
        ) : External

        sealed interface Api : External {
            data class Push(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class InApp(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class SilentPush(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class OnEventAction(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Api

            data class BadgeCount(
                override val id: String = UUIDProvider().provide(),
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
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(name)

            @Serializable
            data class AppStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(APP_START_EVENT_NAME)

            @Serializable
            data class SessionStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(SESSION_START_EVENT_NAME)

            @Serializable
            data class SessionEnd(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(SESSION_END_EVENT_NAME)

            @Serializable
            @SerialName("internal")
            data class Dismiss(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(DISMISS_EVENT_NAME)
        }

        @Serializable
        sealed class Push(
            override val name: String
        ) : Internal {
            @Serializable
            data class Clicked(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Push(PUSH_CLICKED_EVENT_NAME), Reporting
        }

        @Serializable
        sealed class InApp(override val name: String) : Internal {
            @Serializable
            data class Viewed(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : InApp(INAPP_VIEWED_EVENT_NAME), Reporting

            @Serializable
            data class ButtonClicked(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : InApp(IN_APP_BUTTON_CLICKED_EVENT_NAME), Reporting
        }

        @Serializable
        data class SilentPush(
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class OnEventAction(
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class BadgeCount(
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal
    }
}
