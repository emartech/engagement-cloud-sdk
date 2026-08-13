package com.sap.ec.core.device

internal interface DeviceCategoryProviderApi {
    suspend fun getDeviceCategory(): DeviceCategory
}
