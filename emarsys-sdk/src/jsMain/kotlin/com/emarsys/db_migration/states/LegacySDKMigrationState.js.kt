package com.emarsys.db_migration.states

import com.emarsys.core.state.State

internal actual class LegacySDKMigrationState : State {
    actual override val name: String = "legacySDKMigrationState"

    actual override fun prepare() {}

    actual override suspend fun active(): Result<Unit> {
        return Result.success(Unit)
    }

    actual override fun relax() {}
}
