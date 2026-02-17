package com.sap.ec.core.providers.platform

import com.sap.ec.SdkConstants

internal actual class PlatformCategoryProvider : PlatformCategoryProviderApi {

    actual override fun provide(): String = SdkConstants.WEB_PLATFORM_CATEGORY
}