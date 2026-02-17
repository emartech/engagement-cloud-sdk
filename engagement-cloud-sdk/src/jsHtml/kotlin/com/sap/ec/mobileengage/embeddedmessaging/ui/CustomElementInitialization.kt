package com.sap.ec.mobileengage.embeddedmessaging.ui

import kotlinx.browser.window

actual fun initializeCustomElements() {
    window.customElements.define(
        "ec-embedded-messaging-list",
        com.sap.ec.mobileengage.embeddedmessaging.ui.createBridge(
            factory = { com.sap.ec.mobileengage.embeddedmessaging.ui.ECMessagingListElement() },
            observedAttributes = arrayOf("custom-message-item-element-name", "hide-filters")
        )
    )
    window.customElements.define(
        "ec-embedded-messaging-compact-list",
        com.sap.ec.mobileengage.embeddedmessaging.ui.createBridge(
            factory = { com.sap.ec.mobileengage.embeddedmessaging.ui.ECMessagingCompactListElement() },
            observedAttributes = arrayOf("custom-message-item-element-name")
        )
    )
    window.customElements.define(
        "ec-inline-inapp-view",
        com.sap.ec.mobileengage.embeddedmessaging.ui.createBridge(
            factory = { com.sap.ec.mobileengage.embeddedmessaging.ui.ECInlineInAppView() },
            observedAttributes = arrayOf("view-id")
        )
    )
}

private fun createBridge(factory: () -> com.sap.ec.mobileengage.embeddedmessaging.ui.ComposeCustomElement, observedAttributes: Array<String>): dynamic {
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
