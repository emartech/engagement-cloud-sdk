package com.emarsys.api

import android.content.Context

actual class AppEvent actual constructor() {
    private lateinit var platformName: String
    private var platformPayload: Map<String, String>? = null
    private lateinit var context: Context

    constructor(context: Context, platformName: String, platformPayload: Map<String, String>?) : this() {
        this.platformName = platformName
        this.platformPayload = platformPayload
        this.context = context
    }

    actual val name: String
        get() = platformName
    actual val payload: Map<String, String>?
        get() = platformPayload
}