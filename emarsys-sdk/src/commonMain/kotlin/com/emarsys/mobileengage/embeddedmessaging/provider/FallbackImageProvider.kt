package com.emarsys.mobileengage.embeddedmessaging.provider

import com.emarsys.emarsys_sdk.generated.resources.Res

class FallbackImageProvider(): FallbackImageProviderApi {
    override suspend fun provide(): ByteArray {
       return Res.readBytes("files/placeholder.png")
    }
}