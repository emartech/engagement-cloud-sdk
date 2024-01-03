package com.emarsys.core.state

interface State {

    val name: String

    fun prepare()

    suspend fun active()

    fun relax()

}