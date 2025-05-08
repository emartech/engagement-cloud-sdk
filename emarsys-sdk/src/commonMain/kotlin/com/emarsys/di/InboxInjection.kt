package com.emarsys.di

import com.emarsys.api.inbox.GathererInbox
import com.emarsys.api.inbox.Inbox
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.inbox.InboxCall
import com.emarsys.api.inbox.InboxContext
import com.emarsys.api.inbox.InboxContextApi
import com.emarsys.api.inbox.InboxInstance
import com.emarsys.api.inbox.InboxInternal
import com.emarsys.api.inbox.LoggingInbox
import com.emarsys.api.inbox.model.Message
import com.emarsys.core.collections.PersistentList
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object InboxInjection {
    const val INBOX_MESSAGES = "inboxMessages"

    val inboxModules = module {
        single<MutableList<InboxCall>>(named(PersistentListTypes.InboxCall)) {
            PersistentList(
                id = PersistentListIds.INBOX_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = InboxCall.serializer(),
                elements = listOf()
            )
        }
        single<MutableList<Message>>(named(INBOX_MESSAGES)) { mutableListOf() }
        single<InboxContextApi> { InboxContext(calls = get(named(PersistentListTypes.InboxCall))) }
        single<InboxInstance>(named(InstanceType.Logging)) {
            LoggingInbox(
                logger = get { parametersOf(LoggingInbox::class.simpleName) },
            )
        }
        single<InboxInstance>(named(InstanceType.Gatherer)) {
            GathererInbox(
                inboxContext = get()
            )
        }
        single<InboxInstance>(named(InstanceType.Internal)) { InboxInternal() }
        single<InboxApi> {
            Inbox(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}