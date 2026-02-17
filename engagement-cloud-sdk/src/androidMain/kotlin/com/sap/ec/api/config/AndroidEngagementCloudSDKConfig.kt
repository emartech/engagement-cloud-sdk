package com.sap.ec.api.config

import com.sap.ec.config.SdkConfig
import com.sap.ec.util.JsonUtil
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Configuration class for enabling the Engagement Cloud SDK on Android.
 *
 * @property applicationCode The application code of your application.
 * @property launchActivityClass The class of the activity to be launched when a launch application action is performed.
 */
@Serializable(with = AndroidECConfigSerializer::class)
data class AndroidEngagementCloudSDKConfig(
    override val applicationCode: String? = null,
    val launchActivityClass: Class<*>? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}

object AndroidECConfigSerializer : KSerializer<AndroidEngagementCloudSDKConfig> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AndroidEngagementCloudSDKConfig", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AndroidEngagementCloudSDKConfig {
        val jsonObject = JsonUtil.json.decodeFromString<JsonObject>(decoder.decodeString())
        return AndroidEngagementCloudSDKConfig(
            applicationCode = jsonObject["applicationCode"]?.jsonPrimitive?.contentOrNull,
            launchActivityClass = jsonObject["launchActivityClass"]?.jsonPrimitive?.contentOrNull?.let {
                Class.forName(it)
            }
        )
    }

    override fun serialize(encoder: Encoder, value: AndroidEngagementCloudSDKConfig) {
        val jsonObject = buildJsonObject {
            put("applicationCode", JsonPrimitive(value.applicationCode))
            put("launchActivityClass", JsonPrimitive(value.launchActivityClass?.name))
        }
        val jsonString = JsonUtil.json.encodeToString(jsonObject)
        encoder.encodeString(jsonString)
    }
}


