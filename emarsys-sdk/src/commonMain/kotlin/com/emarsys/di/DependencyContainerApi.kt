package com.emarsys.di

import com.emarsys.api.contact.ContactApi
import com.emarsys.providers.Provider

interface DependencyContainerApi {

    val contactApi: ContactApi

    val uuidProvider: Provider<String>

}
