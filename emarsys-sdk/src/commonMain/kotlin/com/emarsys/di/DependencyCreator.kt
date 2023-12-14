package com.emarsys.di

import com.emarsys.core.storage.Storage

interface DependencyCreator {
    fun createStringStorage(): Storage
}