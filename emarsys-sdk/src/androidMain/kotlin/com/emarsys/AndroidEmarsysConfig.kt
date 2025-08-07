package com.emarsys

import com.emarsys.config.SdkConfig
import com.emarsys.util.JsonUtil
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
 * Configuration class for enabling the Emarsys SDK on Android.
 *
 * @property applicationCode The application code of your application.
 * @property merchantId The merchant ID.
 * @property launchActivityClass The class of the activity to be launched when a launch application action is performed.
 */
@Serializable(with = AndroidEmarsysConfigSerializer::class)
data class AndroidEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null,
    val launchActivityClass: Class<*>? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?,
        merchantId: String?,
        sharedSecret: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode,
            merchantId = merchantId,
            sharedSecret = sharedSecret
        )
    }
}

object AndroidEmarsysConfigSerializer : KSerializer<AndroidEmarsysConfig> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AndroidEmarsysConfig", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AndroidEmarsysConfig {
        val jsonObject = JsonUtil.json.decodeFromString<JsonObject>(decoder.decodeString())
        return AndroidEmarsysConfig(
            applicationCode = jsonObject["applicationCode"]?.jsonPrimitive?.contentOrNull,
            merchantId = jsonObject["merchantId"]?.jsonPrimitive?.contentOrNull,
            sharedSecret = jsonObject["sharedSecret"]?.jsonPrimitive?.contentOrNull,
            launchActivityClass = jsonObject["launchActivityClass"]?.jsonPrimitive?.contentOrNull?.let {
                Class.forName(it)
            }
        )
    }

    override fun serialize(encoder: Encoder, value: AndroidEmarsysConfig) {
        val jsonObject = buildJsonObject {
            put("applicationCode", JsonPrimitive(value.applicationCode))
            put("merchantId", JsonPrimitive(value.merchantId))
            put("sharedSecret", JsonPrimitive(value.sharedSecret))
            put("launchActivityClass", JsonPrimitive(value.launchActivityClass?.name))
        }
        val jsonString = JsonUtil.json.encodeToString(jsonObject)
        encoder.encodeString(jsonString)
    }
}


