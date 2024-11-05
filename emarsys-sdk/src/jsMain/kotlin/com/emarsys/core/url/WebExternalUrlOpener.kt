package com.emarsys.core.url

import com.emarsys.core.log.Logger
import org.w3c.dom.Window
import org.w3c.dom.url.URL


class WebExternalUrlOpener(
    private val window: Window,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    companion object {
        const val BLANK_TARGET = "_blank"
    }

    override suspend fun open(url: String) {
        try {
            val parsedUrl = URL(url)
            window.open(parsedUrl.href, BLANK_TARGET)?.also { it.focus() }
        } catch (e: Throwable) {
            sdkLogger.error("open-external-url-failed", e, mapOf("url" to url))
        }
    }
}