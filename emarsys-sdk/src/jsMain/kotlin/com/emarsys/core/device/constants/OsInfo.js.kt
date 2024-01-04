package com.emarsys.core.device.constants

enum class OsInfo(val value: String, val versionPrefix: String) {
    WindowsPhone("Windows Phone", "OS"),
    Windows(value = "Win", versionPrefix = "NT"),
    IPhone(value = "iPhone", versionPrefix = "OS"),
    IPad(value = "iPad", versionPrefix = "OS"),
    Kindle(value = "Silk", versionPrefix = "Silk"),
    Android(value = "Android", versionPrefix = "Android"),
    Playbook(value = "PlayBook", versionPrefix = "OS"),
    Blackberry(value = "BlackBerry", versionPrefix = "/"),
    Macintosh(value = "Mac", versionPrefix = "OS X"),
    Linux(value = "Linux", versionPrefix = "rv"),
    Palm(value = "Palm", versionPrefix = "PalmOS"),
    Unknown("UnknownOS", "UnknownOS")
}