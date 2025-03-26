package com.emarsys.api.predict

internal interface PredictContextApi {
    val calls: MutableList<PredictCall>
}