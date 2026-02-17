package com.sap.ec.core.storage

import org.w3c.dom.Storage

class StringStorage(private val storage: Storage): StringStorageApi {

    override fun put(key: String, value: String?) {
        if (value == null) {
            storage.removeItem(key)
        } else {
            storage.setItem(key, value)
        }
    }

    override fun get(key: String): String? {
        return storage.getItem(key)
    }

}
