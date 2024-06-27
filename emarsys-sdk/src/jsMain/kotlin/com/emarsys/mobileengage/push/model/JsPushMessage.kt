package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.PushData
import com.emarsys.mobileengage.push.PushMessage
import kotlinx.serialization.Serializable

@Serializable
data class JsPushMessage(
    override val messageId: String,
    override val title: String,
    override val body: String,
    override val iconUrlString: String? = null,
    override val imageUrlString: String? = null,
    override val data: PushData<JsPlatformData>
) : PushMessage<JsPlatformData>
