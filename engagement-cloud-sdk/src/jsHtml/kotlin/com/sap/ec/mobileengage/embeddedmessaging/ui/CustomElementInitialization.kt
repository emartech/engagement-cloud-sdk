package com.sap.ec.mobileengage.embeddedmessaging.ui

import kotlinx.browser.window

actual fun initializeCustomElements() {
    window.customElements.define(
        "ec-embedded-messaging",
        createBridge(
            factory = { ECMessagingListElement() },
            observedAttributes = arrayOf("custom-message-item-element-name", "hide-filters")
        )
    )
    window.customElements.define(
        "ec-embedded-messaging-compact",
        createBridge(
            factory = { ECMessagingCompactListElement() },
            observedAttributes = arrayOf("custom-message-item-element-name")
        )
    )
    window.customElements.define(
        "ec-inline-inapp-view",
        createBridge(
            factory = { ECInlineInAppView() },
            observedAttributes = arrayOf("view-id")
        )
    )
}

private fun createBridge(factory: () -> ComposeCustomElement, observedAttributes: Array<String>): dynamic {
    return js("""
        (function() {
            function Bridge() {
                var el = Reflect.construct(HTMLElement, [], Bridge);
                el.kotlinInstance = factory();
                el.kotlinInstance.element = el;
                return el;
            }
            Bridge.prototype = Object.create(HTMLElement.prototype);
            Bridge.prototype.constructor = Bridge;
            Bridge.observedAttributes = observedAttributes;
            Bridge.prototype.connectedCallback = function() {
                this.kotlinInstance.connectedCallback();
            };
            Bridge.prototype.disconnectedCallback = function() {
                this.kotlinInstance.disconnectedCallback();
            };
            Bridge.prototype.attributeChangedCallback = function(name, oldValue, newValue) {
                this.kotlinInstance.attributeChangedCallback(name, oldValue, newValue);
            };
            return Bridge;
        })()
    """)
}
