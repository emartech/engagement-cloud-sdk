package com.emarsys.networking.clients.contact

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.EmarsysHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlin.collections.set

class ContactClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val json: Json,
    private val sdkLogger: Logger
) : ContactClientApi {
    override suspend fun linkContact(
        contactFieldId: Int,
        contactFieldValue: String?,
        openIdToken: String?
    ): Response {
        sdkLogger.debug("ContactClient - linkContact")
        val requestBody = json.encodeToString(
            LinkContactRequestBody(
                contactFieldId,
                contactFieldValue,
                openIdToken
            )
        )
        val url = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)
        val headers = mutableMapOf<String, Any?>()

        if (sdkContext.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContext.config!!.merchantId
        }

        val request = UrlRequest(
            url,
            HttpMethod.Post,
            requestBody,
            headers
        )
        val response = emarsysClient.send(request)

        contactTokenHandler.handleContactTokens(response)
        return response
    }

    override suspend fun unlinkContact(): Response {
        sdkLogger.debug("ContactClient - unlinkContact")
        val url = urlFactory.create(EmarsysUrlType.UNLINK_CONTACT, null)
        val headers = mutableMapOf<String, Any?>()

        if (sdkContext.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContext.config!!.merchantId
        }

        val request = UrlRequest(url, HttpMethod.Delete, null, headers)

        val response = emarsysClient.send(request)

        contactTokenHandler.handleContactTokens(response)

        return response
    }
}