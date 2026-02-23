package com.sap.ec.config

interface SdkConfig {
    val applicationCode: String

    fun copyWith(
        applicationCode: String = this.applicationCode
    ): SdkConfig
}