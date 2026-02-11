package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.InAppConstants.IFRAME_ID_PREFIX

object InAppConstants {
    const val IFRAME_ID_PREFIX = "ec-if-"
}

fun String.toIframeId(): String {
    return "$IFRAME_ID_PREFIX$this"
}