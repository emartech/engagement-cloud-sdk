package com.sap.ec.api.push

import kotlin.experimental.ExperimentalObjCName

/**
 * Options for configuring how a registered notification delegate receives notifications.
 *
 * @param includeEngagementCloudMessages When `true`, the delegate will also receive notifications
 * that are processed by the Engagement Cloud SDK. When `false` (default), the delegate only
 * receives non-Engagement Cloud notifications.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("NotificationCenterDelegateRegistrationOptions")
data class NotificationCenterDelegateRegistrationOptions(
    val includeEngagementCloudMessages: Boolean = false
)
