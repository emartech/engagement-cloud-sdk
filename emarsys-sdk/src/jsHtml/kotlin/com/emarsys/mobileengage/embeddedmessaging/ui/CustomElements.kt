package com.emarsys.mobileengage.embeddedmessaging.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import com.emarsys.mobileengage.embeddedmessaging.ui.list.CompactListView
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageView
import org.jetbrains.compose.web.renderComposable
import web.events.CustomEvent
import web.events.CustomEventInit
import web.events.EventType
import web.html.HTMLElement

external interface CustomElementLifecycle {
    fun connectedCallback()
    fun disconnectedCallback()
    fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?)
}

@Suppress("NON_EXPORTABLE_TYPE")
@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class ComposeCustomElement(
    private val contentFactory: (ComposeCustomElement) -> @Composable (attributes: Map<String, String?>) -> Unit
) : CustomElementLifecycle {
    var element: HTMLElement? = null
    private var composition: Composition? = null
    private val attributes = mutableMapOf<String, String?>()
    private val content: @Composable (attributes: Map<String, String?>) -> Unit by lazy {
        contentFactory(this)
    }

    override fun connectedCallback() {
        element?.let { el ->
            composition?.dispose()
            composition = renderComposable(root = el.asDynamic()) {
                content(attributes.toMap())
            }
        }
    }

    override fun disconnectedCallback() {
        composition?.dispose()
        composition = null
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        attributes[name] = newValue
        element?.let { el ->
            composition?.dispose()
            composition = renderComposable(root = el.asDynamic()) {
                content(attributes.toMap())
            }
        }
    }

    internal fun dispatchCustomEvent(eventName: String) {
        element?.dispatchEvent(
            CustomEvent(
                EventType(eventName), CustomEventInit(
                    detail = js("{}"),
                    bubbles = true,
                    cancelable = true
                )
            )
        )
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class EmarsysMessagingListElement : ComposeCustomElement({ _ ->
    { attributes ->
        val customMessageItemName = attributes["custom-message-item-element-name"]
        val showFilters = attributes["hide-filters"] == null
        ListPageView(customMessageItemName, showFilters = showFilters)
    }
})

@OptIn(ExperimentalJsExport::class)
@JsExport
class EmarsysMessagingCompactListElement : ComposeCustomElement({ self ->
    { attributes ->
        val customMessageItemName = attributes["custom-message-item-element-name"]
        CompactListView(customMessageItemName, navigateToDetailedView = {
            self.dispatchCustomEvent("navigate")
        })
    }
})