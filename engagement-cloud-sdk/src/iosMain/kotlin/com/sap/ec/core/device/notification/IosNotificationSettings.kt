package com.sap.ec.core.device.notification

import com.sap.ec.core.device.IosAlertStyle
import com.sap.ec.core.device.IosAuthorizationStatus
import com.sap.ec.core.device.IosNotificationSetting
import com.sap.ec.core.device.IosShowPreviewSetting
import kotlinx.serialization.Serializable

@Serializable
data class IosNotificationSettings(
    val authorizationStatus: IosAuthorizationStatus,
    val soundSetting: IosNotificationSetting,
    val badgeSetting: IosNotificationSetting,
    val alertSetting: IosNotificationSetting,
    val notificationCenterSetting: IosNotificationSetting,
    val lockScreenSetting: IosNotificationSetting,
    val carPlaySetting: IosNotificationSetting,
    val alertStyle: IosAlertStyle,
    val showPreviewsSetting: IosShowPreviewSetting,
    val criticalAlertSetting: IosNotificationSetting,
    val providesAppNotificationSettings: Boolean = false,
    // Last 2 property is only available from iOS 15.0
    val scheduledDeliverySetting: IosNotificationSetting,
    val timeSensitiveSetting: IosNotificationSetting
)