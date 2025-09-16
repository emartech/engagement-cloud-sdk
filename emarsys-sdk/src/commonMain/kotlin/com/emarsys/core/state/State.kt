package com.emarsys.core.state

internal interface State {

    val name: String

    fun prepare()

    suspend fun active(): Result<Unit>

    fun relax()

}