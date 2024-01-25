package com.emarsys.api.predict.model

interface Logic {
    val logicName: String
    val data: Map<String, String>
    val variants: List<String>
}