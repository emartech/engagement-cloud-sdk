package com.emarsys.di

import com.emarsys.core.storage.StorageApi

interface DependencyCreator {
    fun createStorage():  StorageApi<String>

}