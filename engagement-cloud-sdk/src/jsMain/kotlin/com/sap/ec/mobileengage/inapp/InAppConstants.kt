package com.sap.ec.mobileengage.inapp

import com.sap.ec.mobileengage.inapp.InAppConstants.IFRAME_ID_PREFIX

object InAppConstants {
    const val IFRAME_ID_PREFIX = "ec-if-"
}

fun String.toIframeId(): String {
    return "$IFRAME_ID_PREFIX$this"
}