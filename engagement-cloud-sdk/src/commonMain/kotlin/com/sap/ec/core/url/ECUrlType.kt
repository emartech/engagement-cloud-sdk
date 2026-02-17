package com.sap.ec.core.url

internal sealed interface ECUrlType {
    data object ChangeApplicationCode : ECUrlType
    data object LinkContact : ECUrlType
    data object UnlinkContact : ECUrlType
    data object RefreshToken : ECUrlType
    data object ChangeMerchantId : ECUrlType
    data object PushToken : ECUrlType
    data object ClearPushToken : ECUrlType
    data object Event : ECUrlType
    data object RegisterDeviceInfo : ECUrlType
    data object RemoteConfigSignature : ECUrlType
    data object RemoteConfig : ECUrlType
    data object GlobalRemoteConfigSignature : ECUrlType
    data object GlobalRemoteConfig : ECUrlType
    data object DeepLink : ECUrlType
    data object Logging : ECUrlType
    data object FetchEmbeddedMessages : ECUrlType
    data object FetchBadgeCount : ECUrlType
    data object FetchMeta : ECUrlType
    data object UpdateTagsForMessages : ECUrlType
    data object FetchInlineInAppMessages : ECUrlType
}