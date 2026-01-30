package com.emarsys.core.providers.platform

import com.emarsys.SdkConstants

internal actual class PlatformCategoryProvider : PlatformCategoryProviderApi {

    actual override fun provide(): String = SdkConstants.MOBILE_PLATFORM_CATEGORY
}