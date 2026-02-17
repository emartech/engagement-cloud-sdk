package com.sap.ec.core.wrapper

import kotlinx.serialization.Serializable

@Serializable
data class WrapperInfo(val platformWrapper: String, val wrapperVersion: String)
