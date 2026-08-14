@file:JsModule("bowser")
@file:JsNonModule

package com.sap.ec.npm_dependencies.bowser

external interface BowserPlatform {
    // can be "bot, "desktop", "mobile", "tablet", "tv"
    val type: String?
    val vendor: String?
}

external interface BowserParserResult {
    val platform: BowserPlatform?
}


@JsName("parse")
external fun parseUserAgent(userAgent: String, clientHints: Any?): BowserParserResult