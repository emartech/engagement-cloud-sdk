package com.emarsys.di

import com.emarsys.api.contact.ContactApi

interface DependencyContainerApi {

    val contactApi: ContactApi

}
