package com.emarsys.api

actual class AppEvent actual constructor() {
    private lateinit var platformName: String
    private var platformPayload: Map<String, String>? = null

    constructor(platformName: String, platformPayload: Map<String, String>?) : this() {
        this.platformName = platformName
        this.platformPayload = platformPayload
    }

    actual val name: String
        get() = platformName
    actual val payload: Map<String, String>?
        get() = platformPayload

}