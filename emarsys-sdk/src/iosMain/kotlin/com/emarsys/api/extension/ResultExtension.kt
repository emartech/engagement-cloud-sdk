package com.emarsys.api.extension

fun <T>Result<T>.throwErrorFromResult() {
    if (this.isFailure) {
        this.exceptionOrNull()?.let {
            throw it
        }
    }
}