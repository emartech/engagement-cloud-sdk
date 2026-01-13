package com.emarsys.core.url

internal sealed interface EmarsysUrlType {
    data object ChangeApplicationCode : EmarsysUrlType
    data object LinkContact : EmarsysUrlType
    data object UnlinkContact : EmarsysUrlType
    data object RefreshToken : EmarsysUrlType
    data object ChangeMerchantId : EmarsysUrlType
    data object PushToken : EmarsysUrlType
    data object ClearPushToken : EmarsysUrlType
    data object Event : EmarsysUrlType
    data object RegisterDeviceInfo : EmarsysUrlType
    data object RemoteConfigSignature : EmarsysUrlType
    data object RemoteConfig : EmarsysUrlType
    data object GlobalRemoteConfigSignature : EmarsysUrlType
    data object GlobalRemoteConfig : EmarsysUrlType
    data object DeepLink : EmarsysUrlType
    data object Logging : EmarsysUrlType
    data object FetchEmbeddedMessages : EmarsysUrlType
    data object FetchBadgeCount : EmarsysUrlType
    data object FetchMeta : EmarsysUrlType
    data object UpdateTagsForMessages : EmarsysUrlType
    data object FetchInlineInAppMessages : EmarsysUrlType
}