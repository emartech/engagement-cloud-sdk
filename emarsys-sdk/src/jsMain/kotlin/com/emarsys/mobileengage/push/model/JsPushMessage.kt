package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.PresentablePushData
import com.emarsys.mobileengage.push.PresentablePushMessage
import kotlinx.serialization.Serializable

@Serializable
data class JsPushMessage(
    override val messageId: String,
    override val title: String,
    override val body: String,
    override val iconUrlString: String? = null,
    override val imageUrlString: String? = null,
    override val data: PresentablePushData<JsPlatformData>
) : PresentablePushMessage<JsPlatformData>
