package com.emarsys.networking.clients.contact

import com.emarsys.context.SdkContextApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.EmarsysHeaders
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ContactClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkContextApi: SdkContextApi,
    private val json: Json
) : ContactClientApi {
    override suspend fun linkContact(
        contactFieldId: Int,
        contactFieldValue: String?,
        openIdToken: String?
    ): Response {
        val requestBody = json.encodeToString(
            LinkContactRequestBody(
                contactFieldId,
                contactFieldValue,
                openIdToken
            )
        )
        val url = urlFactory.create(EmarsysUrlType.LINK_CONTACT)
        val headers = mutableMapOf<String, Any?>()

        if (sdkContextApi.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContextApi.config!!.merchantId
        }

        val request = UrlRequest(
            url,
            HttpMethod.Post,
            requestBody,
            headers
        )
        val response = emarsysClient.send(request)

        return response
    }

    override suspend fun unLinkContact(): Response {
        val url = urlFactory.create(EmarsysUrlType.UNLINK_CONTACT)
        val headers = mutableMapOf<String, Any?>()

        if (sdkContextApi.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContextApi.config!!.merchantId
        }

        val request = UrlRequest(url, HttpMethod.Post, null, headers)

        val response = emarsysClient.send(request)

        return response
    }
}