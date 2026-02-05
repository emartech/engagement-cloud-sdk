package com.emarsys.db_migration.states

import com.emarsys.core.state.State

internal expect class LegacySDKMigrationState: State {
    override val name: String
    override fun prepare()
    override suspend fun active(): Result<Unit>
    override fun relax()
}