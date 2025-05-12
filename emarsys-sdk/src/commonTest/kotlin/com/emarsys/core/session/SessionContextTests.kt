package com.emarsys.core.session

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SessionContextTests {

    companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val REFRESH_TOKEN = "testRefreshToken"
    }

    @Test
    fun testClearSessionTokens() {
        val sessionContext = SessionContext(contactToken = CONTACT_TOKEN, refreshToken = REFRESH_TOKEN)

        sessionContext.clearSessionTokens()

        sessionContext.contactToken shouldBe null
        sessionContext.refreshToken shouldBe null
    }

}
