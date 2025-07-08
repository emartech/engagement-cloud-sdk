package com.emarsys.event

import com.emarsys.SdkConstants.APPLY_APPCODE_BASED_REMOTE_CONFIG_EVENT_NAME
import com.emarsys.SdkConstants.APPLY_GLOBAL_REMOTE_CONFIG_EVENT_NAME
import com.emarsys.SdkConstants.APP_START_EVENT_NAME
import com.emarsys.SdkConstants.CHANGE_APP_CODE_NAME
import com.emarsys.SdkConstants.CHANGE_MERCHANT_ID_NAME
import com.emarsys.SdkConstants.CLEAR_PUSH_TOKEN_EVENT_NAME
import com.emarsys.SdkConstants.DEVICE_INFO_READY_EVENT_NAME
import com.emarsys.SdkConstants.DEVICE_INFO_UPDATE_REQUIRED_EVENT_NAME
import com.emarsys.SdkConstants.DISMISS_EVENT_NAME
import com.emarsys.SdkConstants.INAPP_VIEWED_EVENT_NAME
import com.emarsys.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.LINK_AUTHENTICATED_CONTACT_NAME
import com.emarsys.SdkConstants.LINK_CONTACT_NAME
import com.emarsys.SdkConstants.LOG_EVENT_NAME
import com.emarsys.SdkConstants.METRIC_EVENT_NAME
import com.emarsys.SdkConstants.PUSH_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.REGISTER_DEVICE_INFO_EVENT_NAME
import com.emarsys.SdkConstants.REGISTER_PUSH_TOKEN_EVENT_NAME
import com.emarsys.SdkConstants.REMOTE_CONFIG_UPDATE_REQUIRED_EVENT_NAME
import com.emarsys.SdkConstants.REREGISTRATION_REQUIRED_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_END_EVENT_NAME
import com.emarsys.SdkConstants.SESSION_START_EVENT_NAME
import com.emarsys.SdkConstants.TRACK_DEEPLINK_NAME
import com.emarsys.SdkConstants.UNLINK_CONTACT_NAME
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.core.providers.UUIDProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface OnlineSdkEvent : SdkEvent {
    var nackCount: Int

    suspend fun ack(eventsDao: EventsDaoApi, sdkLogger: Logger) {
        try {
            eventsDao.removeEvent(this)
        } catch (exception: Exception) {
            sdkLogger.error(
                "OnlineSdkEvent - ack: error acking OnlineSdkEvent",
                exception,
                buildJsonObject {
                    put("event", this.toString())
                },
                isRemoteLog = this !is SdkEvent.Internal.LogEvent
            )
        }
    }

    suspend fun nack(eventsDao: EventsDaoApi, sdkLogger: Logger) {
        try {
            this.nackCount++
            if (this.nackCount >= 3) {
                eventsDao.removeEvent(this)
            } else {
                eventsDao.upsertEvent(this)
            }
        } catch (exception: Exception) {
            sdkLogger.error(
                "OnlineSdkEvent - nack: error nack OnlineSdkEvent",
                exception,
                buildJsonObject {
                    put("event", this.toString())
                },
                isRemoteLog = this !is SdkEvent.Internal.LogEvent
            )
        }
    }
}

suspend fun List<OnlineSdkEvent>.ack(eventsDao: EventsDaoApi, sdkLogger: Logger) {
    this.forEach {
        it.ack(eventsDao, sdkLogger)
    }
}

suspend fun List<OnlineSdkEvent>.nack(eventsDao: EventsDaoApi, sdkLogger: Logger) {
    this.forEach {
        it.nack(eventsDao, sdkLogger)
    }
}

@OptIn(ExperimentalSerializationApi::class,ExperimentalTime::class)
@Serializable
@JsonClassDiscriminator("fullClassName")
sealed interface SdkEvent {
    val id: String
    val type: String
    val name: String
    val timestamp: Instant
    val attributes: JsonObject?

    sealed interface External : SdkEvent {

        @Serializable
        data class Custom(
            override val type: String = "custom",
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject? = null,
            override val timestamp: Instant = TimestampProvider().provide(),
            override var nackCount: Int = 0
        ) : External, OnlineSdkEvent

        sealed interface Api : External {

            data class AppEvent(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override val type: String = ExternalEventTypes.APP_EVENT.name.lowercase()
            ) : Api

            data class BadgeCountEvent(
                override val id: String = UUIDProvider().provide(),
                override val name: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override val type: String = ExternalEventTypes.BADGE_COUNT.name.lowercase(),
                val badgeCount: Int,
                val method: String
            ) : Api
        }
    }

    @Serializable
    sealed interface Internal : SdkEvent {

        interface Reporting : Internal, OnlineSdkEvent

        interface Custom : Internal, OnlineSdkEvent

        interface LogEvent : Internal, OnlineSdkEvent

        interface SetupFlowEvent : Internal, OnlineSdkEvent

        @Serializable
        sealed class Sdk(override val name: String) : Internal {
            override val type: String = "internal"

            sealed class Answer(val eventName: String) : Sdk(eventName) {
                open val originId: String = UUIDProvider().provide()
                open val success: Boolean = true
                open val throwable: Throwable? = null

                data class Ready(
                    override val id: String = UUIDProvider().provide(),
                    override val originId: String,
                    override val attributes: JsonObject? = null,
                    override val timestamp: Instant = TimestampProvider().provide(),
                ) : Answer(DEVICE_INFO_READY_EVENT_NAME)
            }

            @Serializable
            data class DeviceInfoUpdateRequired(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(DEVICE_INFO_UPDATE_REQUIRED_EVENT_NAME)

            @Serializable
            data class Log(
                val level: LogLevel,
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(LOG_EVENT_NAME), LogEvent

            @Serializable
            data class Metric(
                val level: LogLevel = LogLevel.Metric,
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(METRIC_EVENT_NAME), LogEvent

            @Serializable
            data class Dismiss(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(DISMISS_EVENT_NAME)

            @Serializable
            data class ReregistrationRequired(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(REREGISTRATION_REQUIRED_EVENT_NAME)

            @Serializable
            data class RegisterDeviceInfo(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(REGISTER_DEVICE_INFO_EVENT_NAME), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class RegisterPushToken(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val pushToken: String,
            ) : Sdk(REGISTER_PUSH_TOKEN_EVENT_NAME), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ClearPushToken(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(CLEAR_PUSH_TOKEN_EVENT_NAME), OnlineSdkEvent

            @Serializable
            data class RemoteConfigUpdateRequired(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
            ) : Sdk(REMOTE_CONFIG_UPDATE_REQUIRED_EVENT_NAME)

            @Serializable
            data class ApplyAppCodeBasedRemoteConfig(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(APPLY_APPCODE_BASED_REMOTE_CONFIG_EVENT_NAME), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ApplyGlobalRemoteConfig(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : Sdk(APPLY_GLOBAL_REMOTE_CONFIG_EVENT_NAME), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ChangeAppCode(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val applicationCode: String
            ) : Sdk(CHANGE_APP_CODE_NAME), OnlineSdkEvent

            @Serializable
            data class ChangeMerchantId(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val merchantId: String
            ) : Sdk(CHANGE_MERCHANT_ID_NAME), OnlineSdkEvent

            @Serializable
            data class LinkContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val attributes: JsonObject? = null,
                override var nackCount: Int = 0,
                val contactFieldId: Int? = null,
                val contactFieldValue: String
            ) : Sdk(LINK_CONTACT_NAME), OnlineSdkEvent

            @Serializable
            data class LinkAuthenticatedContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val attributes: JsonObject? = null,
                override var nackCount: Int = 0,
                val contactFieldId: Int? = null,
                val openIdToken: String
            ) : Sdk(LINK_AUTHENTICATED_CONTACT_NAME), OnlineSdkEvent

            @Serializable
            data class UnlinkContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val attributes: JsonObject? = null,
                override var nackCount: Int = 0,
            ) : Sdk(UNLINK_CONTACT_NAME), OnlineSdkEvent

            @Serializable
            data class TrackDeepLink(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val attributes: JsonObject? = null,
                override var nackCount: Int = 0,
                val trackingId: String
            ) : Sdk(TRACK_DEEPLINK_NAME), OnlineSdkEvent

            @Serializable
            data class AppStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : Sdk(APP_START_EVENT_NAME), Custom, SetupFlowEvent

            @Serializable
            data class SessionStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : Sdk(SESSION_START_EVENT_NAME), Custom

            @Serializable
            data class SessionEnd(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val duration: Long
            ) : Sdk(SESSION_END_EVENT_NAME), Custom
        }

        @Serializable
        sealed class Push(
            override val name: String
        ) : Internal {
            override val type: String = "internal"

            @Serializable
            data class Clicked(
                override val id: String = UUIDProvider().provide(),
                val reporting: String? = null,
                val trackingInfo: String,
                val origin: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : Push(PUSH_CLICKED_EVENT_NAME), Reporting
        }

        @Serializable
        sealed class InApp(override val name: String) : Internal {
            override val type: String = "internal"

            @Serializable
            data class Viewed(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : InApp(INAPP_VIEWED_EVENT_NAME), Reporting

            @Serializable
            data class ButtonClicked(
                override val id: String = UUIDProvider().provide(),
                val reporting: String,
                val trackingInfo: String,
                val origin: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : InApp(IN_APP_BUTTON_CLICKED_EVENT_NAME), Reporting
        }

        @Serializable
        data class SilentPush(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class OnEventAction(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class BadgeCount(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject?,
            override val timestamp: Instant,
        ) : Internal
    }
}
