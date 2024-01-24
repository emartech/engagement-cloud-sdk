package com.emarsys.networking.clients.device

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import io.ktor.http.Url

class DeviceClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sessionContext: SessionContext
) : DeviceClientApi {
    override suspend fun registerDeviceInfo() {
        val request = createRequest()
        handleResponse(emarsysClient.send(request))
    }

    private fun createRequest(): UrlRequest {
        val deviceInfoString = deviceInfoCollector.collect()
        val url = urlFactory.create(REGISTER_DEVICE_INFO)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }

    private fun handleResponse(response: Response) {
        try {
            val body: RegisterDeviceInfoResponseBody = response.body()
            sessionContext.refreshToken = body.refreshToken
            sessionContext.contactToken = body.contactToken
        } catch (ignored: Exception) {
            // TODO
        }
    }
}