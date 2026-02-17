package com.sap.ec.core.collections

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MutableListTests {

    @Test
    fun testDequeue() = runTest {
        val expected = listOf("value1", "value2", "value3")
        val list = mutableListOf("value1", "value2", "value3")
        val resultList = mutableListOf<String>()

        list.dequeue {
            resultList.add(it)
        }

        list.size shouldBe 0
        resultList.toList() shouldBe expected
    }

    @Test
    fun testDequeue_backward() = runTest {
        val expected = listOf("value3", "value2", "value1")
        val list = mutableListOf("value1", "value2", "value3")
        val resultList = mutableListOf<String>()

        list.dequeue(backward = true) {
            resultList.add(it)
        }

        list.size shouldBe 0
        resultList.toList() shouldBe expected
    }

}