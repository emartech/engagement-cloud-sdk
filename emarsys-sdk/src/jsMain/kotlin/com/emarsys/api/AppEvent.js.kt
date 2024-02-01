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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as AppEvent

        if (name != other.name) return false
        if (payload != other.payload) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        return result
    }

}