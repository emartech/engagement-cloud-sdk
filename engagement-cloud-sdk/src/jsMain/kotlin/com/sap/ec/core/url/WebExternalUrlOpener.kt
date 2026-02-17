package com.sap.ec.core.url

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.models.HtmlTarget
import com.sap.ec.mobileengage.action.models.OpenExternalUrlActionModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.Window
import org.w3c.dom.url.URL


class WebExternalUrlOpener(
    private val window: Window,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(actionModel: OpenExternalUrlActionModel) {
        val target = actionModel.target ?: HtmlTarget.BLANK
        try {
            val parsedUrl = URL(actionModel.url)
            window.open(parsedUrl.href, target.raw)?.also { it.focus() }
        } catch (e: Throwable) {
            sdkLogger.error(
                "open-external-url-failed",
                e,
                buildJsonObject {
                    put("url", actionModel.url)
                    put("target", target.raw)
                })
        }
    }
}