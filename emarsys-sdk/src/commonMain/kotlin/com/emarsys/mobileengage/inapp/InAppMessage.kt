package com.emarsys.mobileengage.inapp

data class InAppMessage(
    private val html: String? = null,
    private val url: String? = null
) {
    fun content(): String {
        return html ?: url ?: ""
    }
}
