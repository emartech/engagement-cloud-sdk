package com.sap.ec.di

import com.sap.ec.api.contact.Contact
import com.sap.ec.api.contact.ContactApi
import com.sap.ec.api.contact.ContactCall
import com.sap.ec.api.contact.ContactContext
import com.sap.ec.api.contact.ContactContextApi
import com.sap.ec.api.contact.ContactGatherer
import com.sap.ec.api.contact.ContactInstance
import com.sap.ec.api.contact.ContactInternal
import com.sap.ec.api.contact.LoggingContact
import com.sap.ec.core.collections.PersistentList
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.contact.ContactClient
import com.sap.ec.networking.clients.contact.ContactTokenHandler
import com.sap.ec.networking.clients.contact.ContactTokenHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object ContactInjection {
    val contactModules = module {
        single<ContactTokenHandlerApi> {
            ContactTokenHandler(
                requestContext = get(),
                sdkLogger = get { parametersOf(ContactTokenHandler::class.simpleName) }
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Contact)) {
            ContactClient(
                ecClient = get(named(NetworkClientTypes.EC)),
                clientExceptionHandler = get(),
                sdkEventManager = get(),
                urlFactory = get(),
                sdkContext = get(),
                contactTokenHandler = get(),
                ecSdkSession = get(),
                eventsDao = get(),
                json = get(),
                sdkLogger = get { parametersOf(ContactClient::class.simpleName) },
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
            )
        }
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
                sdkEventDistributor = get()
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