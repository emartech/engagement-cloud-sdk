package com.emarsys.event

import com.emarsys.SdkConstants.APPLY_APPCODE_BASED_REMOTE_CONFIG_EVENT_NAME
import com.emarsys.SdkConstants.APPLY_GLOBAL_REMOTE_CONFIG_EVENT_NAME
import com.emarsys.SdkConstants.APP_START_EVENT_NAME
import com.emarsys.SdkConstants.CHANGE_APP_CODE_NAME
import com.emarsys.SdkConstants.CLEAR_PUSH_TOKEN_EVENT_NAME
import com.emarsys.SdkConstants.DEVICE_INFO_UPDATE_REQUIRED_EVENT_NAME
import com.emarsys.SdkConstants.DISMISS_EVENT_NAME
import com.emarsys.SdkConstants.INAPP_VIEWED_EVENT_NAME
import com.emarsys.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.LINK_AUTHENTICATED_CONTACT_NAME
import com.emarsys.SdkConstants.LINK_CONTACT_NAME
import com.emarsys.SdkConstants.LOG_EVENT_NAME
import com.emarsys.SdkConstants.METRIC_EVENT_NAME
import com.emarsys.SdkConstants.ON_EVENT_ACTION_ORIGIN
import com.emarsys.SdkConstants.ON_EVENT_ACTION_REPORTING_EVENT_NAME
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
import com.emarsys.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.emarsys.mobileengage.inapp.InAppMessage
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

@OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
@Serializable
@JsonClassDiscriminator("fullClassName")
sealed interface SdkEvent {
    val id: String
    val type: String
    val timestamp: Instant

    @Serializable
    sealed interface DeviceEvent : SdkEvent {
        val name: String
        val attributes: JsonObject?
    }

    sealed interface External : SdkEvent {

        @Serializable
        data class Custom(
            override val type: String = "custom",
            override val id: String = UUIDProvider().provide(),
            override val name: String,
            override val attributes: JsonObject? = null,
            override val timestamp: Instant = TimestampProvider().provide(),
            override var nackCount: Int = 0
        ) : External, OnlineSdkEvent, DeviceEvent

        @Serializable
        sealed interface Api : External {


            /**
             * Represents an event defined by the SAP Emarsys platform user.
             *
             * @property id A unique identifier for the event.
             * @property name The name of the event.
             * @property attributes Additional attributes associated with the event.
             * @property timestamp The timestamp when the event occurred.
             * @property type The type of the event, which is always "app_event".
             */
            @Serializable
            data class AppEvent(
                override val id: String = UUIDProvider().provide(),
                val name: String,
                val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override val type: String = ExternalEventTypes.APP_EVENT.name.lowercase()
            ) : Api

            /**
             * Represents a badge count event tracked by the SDK.
             *
             * This event is used to be notified about changes to the badge count,
             * typically for notifications or app icons.
             *
             * @property id A unique identifier for the event.
             * @property timestamp The timestamp when the event occurred.
             * @property type The type of the event, which is always "badge_count".
             * @property badgeCount The badge count value that should be set or added
             * to the current badge count.
             * @property method The method used to update the badge count.
             *
             * Possible values:
             * - SET
             * - ADD
             *
             */
            @Serializable
            data class BadgeCountEvent(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val type: String = ExternalEventTypes.BADGE_COUNT.name.lowercase(),
                val badgeCount: Int,
                val method: String
            ) : Api
        }
    }

    @Serializable
    sealed interface Internal : SdkEvent {

        sealed interface EmbeddedMessaging : Internal, OnlineSdkEvent {

            @Serializable
            data class FetchBadgeCount(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "fetchBadgeCount",
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int
            ) : EmbeddedMessaging

            @Serializable
            data class FetchMessages(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "fetchMessages",
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int,
                val offset: Int,
                val filterUnreadMessages: Boolean = false,
                val categoryIds: List<Int> = emptyList()
            ) : EmbeddedMessaging

            @Serializable
            data class FetchNextPage(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "fetchNextPage",
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int,
                val offset: Int,
                val categoryIds: List<Int>,
                val filterUnreadMessages: Boolean = false
            ) : EmbeddedMessaging

            @Serializable
            data class NextPage(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "nextPage",
                override val timestamp: Instant = TimestampProvider().provide()
            ) : Internal

            @Serializable
            data class ResetPagination(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "resetPagination",
                override val timestamp: Instant = TimestampProvider().provide()
            ) : Internal

            @Serializable
            data class FetchMeta(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "fetchMeta",
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0
            ) : EmbeddedMessaging, SetupFlowEvent

            @Serializable
            data class UpdateTagsForMessages(
                override val id: String = UUIDProvider().provide(),
                override val type: String = "updateTagsForMessages",
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int,
                val updateData: List<MessageTagUpdate>
            ) : EmbeddedMessaging
        }

        sealed interface Reporting : Internal, OnlineSdkEvent, DeviceEvent {
            val reporting: String?
            val trackingInfo: String
        }

        sealed interface Custom : Internal, OnlineSdkEvent, DeviceEvent

        sealed interface LogEvent : Internal, OnlineSdkEvent

        sealed interface SetupFlowEvent : Internal, OnlineSdkEvent

        sealed interface OperationalEvent: Internal, OnlineSdkEvent {
            val applicationCode: String?
        }

        @Serializable
        sealed class Sdk : Internal {
            override val type: String = "internal"

            sealed class Answer() : Sdk() {

                override val id: String = UUIDProvider().provide()
                abstract val originId: String
                override val timestamp: Instant = TimestampProvider().provide()

                data class Response<T>(
                    override val originId: String,
                    val result: Result<T>
                ) : Answer()
            }

            @Serializable
            data class DeviceInfoUpdateRequired(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                val name: String = DEVICE_INFO_UPDATE_REQUIRED_EVENT_NAME
            ) : Sdk()

            @Serializable
            data class Log(
                val level: LogLevel,
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val attributes: JsonObject? = null,
                val name: String = LOG_EVENT_NAME
            ) : Sdk(), LogEvent

            @Serializable
            data class Metric(
                val level: LogLevel = LogLevel.Metric,
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = METRIC_EVENT_NAME
            ) : Sdk(), LogEvent

            @Serializable
            data class Dismiss(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                val name: String = DISMISS_EVENT_NAME
            ) : Sdk()

            @Serializable
            data class ReregistrationRequired(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                val name: String = REREGISTRATION_REQUIRED_EVENT_NAME
            ) : Sdk()

            @Serializable
            data class RegisterDeviceInfo(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = REGISTER_DEVICE_INFO_EVENT_NAME
            ) : Sdk(), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class RegisterPushToken(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val pushToken: String,
                val name: String = REGISTER_PUSH_TOKEN_EVENT_NAME
            ) : Sdk(), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ClearPushToken(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = CLEAR_PUSH_TOKEN_EVENT_NAME,
                override val applicationCode: String?
            ) : Sdk(), OnlineSdkEvent, OperationalEvent

            @Serializable
            data class RemoteConfigUpdateRequired(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                val name: String = REMOTE_CONFIG_UPDATE_REQUIRED_EVENT_NAME
            ) : Sdk()

            @Serializable
            data class ApplyAppCodeBasedRemoteConfig(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = APPLY_APPCODE_BASED_REMOTE_CONFIG_EVENT_NAME
            ) : Sdk(), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ApplyGlobalRemoteConfig(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = APPLY_GLOBAL_REMOTE_CONFIG_EVENT_NAME
            ) : Sdk(), OnlineSdkEvent, SetupFlowEvent

            @Serializable
            data class ChangeAppCode(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val applicationCode: String,
                val name: String = CHANGE_APP_CODE_NAME
            ) : Sdk(), OnlineSdkEvent

            @Serializable
            data class LinkContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val contactFieldValue: String,
                val name: String = LINK_CONTACT_NAME
            ) : Sdk(), OnlineSdkEvent

            @Serializable
            data class LinkAuthenticatedContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val openIdToken: String,
                val name: String = LINK_AUTHENTICATED_CONTACT_NAME
            ) : Sdk(), OnlineSdkEvent

            @Serializable
            data class UnlinkContact(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val name: String = UNLINK_CONTACT_NAME
            ) : Sdk(), OnlineSdkEvent

            @Serializable
            data class TrackDeepLink(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val trackingId: String,
                val name: String = TRACK_DEEPLINK_NAME
            ) : Sdk(), OnlineSdkEvent

            @Serializable
            data class AppStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                override val name: String = APP_START_EVENT_NAME
            ) : Sdk(), Custom, SetupFlowEvent

            @Serializable
            data class SessionStart(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                override val name: String = SESSION_START_EVENT_NAME
            ) : Sdk(), Custom

            @Serializable
            data class SessionEnd(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
                val duration: Long,
                override val name: String = SESSION_END_EVENT_NAME
            ) : Sdk(), Custom
        }

        @Serializable
        sealed class Push(
            val name: String
        ) : Internal {
            override val type: String = "internal"

            @Serializable
            data class Clicked(
                override val id: String = UUIDProvider().provide(),
                override val reporting: String?,
                override val trackingInfo: String,
                val origin: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : Push(PUSH_CLICKED_EVENT_NAME), Reporting
        }

        @Serializable
        data class OnEventActionExecuted(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val reporting: String?,
            override val trackingInfo: String,
            override val attributes: JsonObject? = null,
            override val timestamp: Instant = TimestampProvider().provide(),
            override var nackCount: Int = 0,
            override val name: String = ON_EVENT_ACTION_REPORTING_EVENT_NAME,
            val origin: String = ON_EVENT_ACTION_ORIGIN,
        ) : Internal, Reporting

        @Serializable
        sealed class InApp(val name: String) : Internal {
            override val type: String = "internal"

            @Serializable
            data class Viewed(
                override val id: String = UUIDProvider().provide(),
                override val attributes: JsonObject?,
                override val timestamp: Instant = TimestampProvider().provide(),
                override val reporting: String,
                override val trackingInfo: String,
                override var nackCount: Int = 0,
            ) : InApp(INAPP_VIEWED_EVENT_NAME), Reporting

            @Serializable
            data class ButtonClicked(
                override val id: String = UUIDProvider().provide(),
                override val reporting: String,
                override val trackingInfo: String,
                val origin: String,
                override val attributes: JsonObject? = null,
                override val timestamp: Instant = TimestampProvider().provide(),
                override var nackCount: Int = 0,
            ) : InApp(IN_APP_BUTTON_CLICKED_EVENT_NAME), Reporting

            @Serializable
            data class Present(
                override val id: String = UUIDProvider().provide(),
                override val timestamp: Instant = TimestampProvider().provide(),
                override val type: String = "inAppPresent",
                val inAppMessage: InAppMessage
            ) : Internal
        }

        @Serializable
        data class SilentPush(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class OnEventAction(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val timestamp: Instant,
        ) : Internal

        @Serializable
        data class BadgeCount(
            override val type: String = "internal",
            override val id: String = UUIDProvider().provide(),
            override val timestamp: Instant,
        ) : Internal
    }
}
