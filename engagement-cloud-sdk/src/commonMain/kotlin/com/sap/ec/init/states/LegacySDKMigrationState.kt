package com.sap.ec.init.states

import com.sap.ec.core.state.State

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect class LegacySDKMigrationState: State {
    override val name: String
    override fun prepare()
    override suspend fun active(): Result<Unit>
    override fun relax()
}
