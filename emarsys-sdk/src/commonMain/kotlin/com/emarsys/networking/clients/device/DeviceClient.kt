package com.emarsys.networking.clients.device

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import io.ktor.http.HttpMethod

class DeviceClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val contactTokenHandler: ContactTokenHandlerApi
    ) : DeviceClientApi {
    override suspend fun registerDeviceInfo() {
        val request = createRequest()
        contactTokenHandler.handleContactTokens(emarsysClient.send(request))
    }

    private suspend fun createRequest(): UrlRequest {
        val deviceInfoString = deviceInfoCollector.collect()
        val url = urlFactory.create(REGISTER_DEVICE_INFO, null)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }
}