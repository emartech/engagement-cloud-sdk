package com.emarsys

import android.app.Activity

data class AndroidEmarsysConfig(
    override val applicationCode: String?,
    override val merchantId: String?,
    override val sharedSecret: String?,
    val launchActivityClass: Class<Activity>? = null
): SdkConfig
