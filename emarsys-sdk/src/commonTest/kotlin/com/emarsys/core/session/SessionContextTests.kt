package com.emarsys.core.session

import com.emarsys.core.networking.context.RequestContext
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SessionContextTests {

    companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val REFRESH_TOKEN = "testRefreshToken"
    }

    @Test
    fun testClearSessionTokens() {
        val requestContext =
            RequestContext(contactToken = CONTACT_TOKEN, refreshToken = REFRESH_TOKEN)

        requestContext.clearTokens()

        requestContext.contactToken shouldBe null
        requestContext.refreshToken shouldBe null
    }

}
