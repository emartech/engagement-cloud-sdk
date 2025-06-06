package com.emarsys.core.exceptions

sealed class SdkException(message: String) : RuntimeException(message) {
    class InvalidApplicationCodeException(override val message: String) : SdkException(message)
}
