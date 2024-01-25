package com.emarsys.api

expect class AppEvent() {
    val name: String
    val payload: Map<String, String>?
}