package com.emarsys.core.url

interface ExternalUrlOpenerApi {

    suspend fun open(url: String): Boolean

}
