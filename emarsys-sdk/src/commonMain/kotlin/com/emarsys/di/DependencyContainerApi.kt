package com.emarsys.di

import com.emarsys.api.contact.ContactApi
import com.emarsys.api.push.PushApi
import com.emarsys.providers.Provider

interface DependencyContainerApi {

    val contactApi: ContactApi

    val pushApi: PushApi

    val uuidProvider: Provider<String>

}
