package com.emarsys.di

import com.emarsys.core.log.Logger
import kotlinx.serialization.json.Json

expect class PlatformDependencyCreator(platformContext: PlatformContext, sdkLogger: Logger, json: Json): DependencyCreator