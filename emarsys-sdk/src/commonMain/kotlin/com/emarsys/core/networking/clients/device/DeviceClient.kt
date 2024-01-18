package com.emarsys.core.networking.clients.device

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.networking.EmarsysHeaders
import com.emarsys.providers.Provider
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.url.FactoryApi
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class DeviceClient(
    private val networkClient: NetworkClientApi,
    private val urlFactory: FactoryApi<EmarsysUrlType, String>,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sessionContext: SessionContext,
    private val timestampProvider: Provider<Instant>,
    private val json: Json
) : DeviceClientApi {
    override suspend fun registerDeviceInfo() {
        val request = createRequest()
        handleResponse(networkClient.send(request))
    }

    private fun createRequest(): UrlRequest {
        val deviceInfoString = deviceInfoCollector.collect()
        val deviceInfo = json.decodeFromString<DeviceInfo>(deviceInfoString)
        val url = Url(urlFactory.create(REGISTER_DEVICE_INFO))
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString,
            mapOf(
                EmarsysHeaders.CLIENT_ID_HEADER to deviceInfo.hardwareId,
                EmarsysHeaders.REQUEST_ORDER_HEADER to timestampProvider.provide().toString()
            )
        )
    }

    private fun handleResponse(response: Response) {
        val body: RegisterDeviceInfoResponseBody = response.body()
        sessionContext.refreshToken = body.refreshToken
        sessionContext.contactToken = body.contactToken
    }
}