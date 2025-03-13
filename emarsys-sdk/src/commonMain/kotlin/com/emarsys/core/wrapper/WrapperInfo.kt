package com.emarsys.core.wrapper

import kotlinx.serialization.Serializable

@Serializable
data class WrapperInfo(val platformWrapper: String, val wrapperVersion: String)
