package com.sap.ec.core.validation

fun String.validateNotBlank() {
    if (this.isBlank()) {
        throw IllegalArgumentException("Input parameter is blank!")
    }
}