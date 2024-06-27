package com.emarsys.api.push

object PushConstants {
    const val PUSH_TOKEN_STORAGE_KEY = "emsPushToken"
    const val LAST_SENT_PUSH_TOKEN_STORAGE_KEY = "emsLastSentPushToken"
    const val PUSH_TOKEN_INTENT_KEY = "pushToken"
    const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
    const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
    const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
    const val PUSH_NOTIFICATION_ICON_NAME = "com.emarsys.sample.small_notification_icon"
    const val DEFAULT_TAP_ACTION_ID = "defaultTapActionId"
    const val INTENT_EXTRA_ACTION_KEY = "action"
    const val INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY = "defaultTapAction"
    const val INTENT_EXTRA_PAYLOAD_KEY = "payload"
}
