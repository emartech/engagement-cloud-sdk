package com.sap.ec.api.push

import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        return try {
            decoder.decodeBoolean()
        } catch (e: Exception) {
            try {
                decoder.decodeInt() != 0
            } catch (e2: Exception) {
                false
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}

@Serializable
data class PushUserInfo(
    val ems: Ems,
    val notification: Notification
)

@Serializable
data class SilentPushUserInfo(
    val ems: Ems,
    val notification: SilentNotification
)

@Serializable
data class Notification(
    @Serializable(with = FlexibleBooleanSerializer::class)
    val silent: Boolean = false,
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class SilentNotification(
    @Serializable(with = FlexibleBooleanSerializer::class)
    val silent: Boolean = false,
    val defaultAction: BasicActionModel? = null,
    val actions: List<BasicActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class Ems(
    val version: String,
    val trackingInfo: String
)
