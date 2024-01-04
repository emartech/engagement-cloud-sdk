package com.emarsys.core.device.constants

enum class BrowserInfo(val value: String, val versionPrefix: String) {
    Chrome(value = "Chrome", versionPrefix = "Chrome"),
    Firefox(value = "Firefox", versionPrefix = "Firefox"),
    Safari(value = "Safari", versionPrefix = "Version"),
    InternetExplorer(value = "MSIE", versionPrefix = "MSIE"),
    Opera(value = "Opera", versionPrefix = "Opera"),
    BlackBerry(value = "CLDC", versionPrefix = "CLDC"),
    Mozilla(value = "Mozilla", versionPrefix = "Mozilla"),
    Unknown("UnknownBrowser", "UnknownBrowser")
}