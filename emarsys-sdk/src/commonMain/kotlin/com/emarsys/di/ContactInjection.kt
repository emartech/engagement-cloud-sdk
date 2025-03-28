package com.emarsys.di

import com.emarsys.api.contact.Contact
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.contact.ContactCall
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactContextApi
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInstance
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.LoggingContact
import com.emarsys.core.collections.PersistentList
import com.emarsys.networking.clients.contact.ContactClient
import com.emarsys.networking.clients.contact.ContactClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandler
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object ContactInjection {
    val contactModules = module {
        single<ContactTokenHandlerApi> {
            ContactTokenHandler(
                sessionContext = get(),
                sdkLogger = get { parametersOf(ContactTokenHandler::class.simpleName) }
            )
        }
        single<ContactClientApi> { ContactClient }
        single<MutableList<ContactCall>>(named(PersistentListTypes.ContactCall)) {
            PersistentList(
                id = PersistentListIds.CONTACT_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = ContactCall.serializer(),
                elements = listOf()
            )
        }
        single<ContactContextApi> {
            ContactContext(
                calls = get(named(PersistentListTypes.ContactCall))
            )
        }
        single<ContactInstance>(named(InstanceType.Logging)) {
            LoggingContact(
                logger = get { parametersOf(LoggingContact::class.simpleName) },
            )
        }
        single<ContactInstance>(named(InstanceType.Gatherer)) {
            ContactGatherer(
                context = get(),
                sdkLogger = get { parametersOf(ContactGatherer::class.simpleName) },
            )
        }
        single<ContactInstance>(named(InstanceType.Internal)) {
            ContactInternal(
                contactContext = get(),
                sdkLogger = get { parametersOf(ContactInternal::class.simpleName) },
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow))
            )
        }
        single<ContactApi> {
            Contact(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}