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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppEvent

        if (context != other.context) return false
        if (name != other.name) return false
        if (payload != other.payload) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        return result
    }

}