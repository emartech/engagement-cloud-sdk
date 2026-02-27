package com.sap.ec.util

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.collections.get
import kotlin.test.Test

class JsonExtensionsTests {

    @Test
    fun toMap_shouldReturnEmptyMap_whenJsonObjectIsEmpty() {
        val jsonObject = buildJsonObject {}

        val result = jsonObject.toMap()

        result.shouldBeEmpty()
    }

    @Test
    fun toMap_shouldConvertStringPrimitive() {
        val jsonObject = buildJsonObject {
            put("key", "value")
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("key" to "value")
    }

    @Test
    fun toMap_shouldConvertIntPrimitive() {
        val jsonObject = buildJsonObject {
            put("number", 42)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("number" to 42)
    }

    @Test
    fun toMap_shouldConvertLongPrimitive() {
        val jsonObject = buildJsonObject {
            put("number", 9223372036854775807L)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("number" to 9223372036854775807L)
    }

    @Test
    fun toMap_shouldConvertDoublePrimitive() {
        val jsonObject = buildJsonObject {
            put("number", 3.14)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("number" to 3.14)
    }

    @Test
    fun toMap_shouldConvertBooleanPrimitive() {
        val jsonObject = buildJsonObject {
            put("flag", true)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("flag" to true)
    }

    @Test
    fun toMap_shouldConvertMultiplePrimitives() {
        val jsonObject = buildJsonObject {
            put("string", "value")
            put("int", 42)
            put("bool", false)
            put("double", 2.71)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf(
            "string" to "value",
            "int" to 42,
            "bool" to false,
            "double" to 2.71
        )
    }

    @Test
    fun toMap_shouldConvertNestedJsonObject() {
        val jsonObject = buildJsonObject {
            put("outer", buildJsonObject {
                put("inner", "value")
            })
        }

        val result = jsonObject.toMap()

        result["outer"].shouldBeInstanceOf<Map<*, *>>()
        (result["outer"] as Map<*, *>) shouldBe mapOf("inner" to "value")
    }

    @Test
    fun toMap_shouldConvertJsonArray() {
        val jsonObject = buildJsonObject {
            put("array", buildJsonArray {
                add("item1")
                add("item2")
                add("item3")
            })
        }

        val result = jsonObject.toMap()

        result["array"].shouldBeInstanceOf<List<*>>()
        (result["array"] as List<*>) shouldContainExactly listOf("item1", "item2", "item3")
    }

    @Test
    fun toMap_shouldConvertJsonArrayWithMixedTypes() {
        val jsonObject = buildJsonObject {
            put("array", buildJsonArray {
                add("string")
                add(42)
                add(true)
                add(3.14)
            })
        }

        val result = jsonObject.toMap()

        result["array"].shouldBeInstanceOf<List<*>>()
        (result["array"] as List<*>) shouldContainExactly listOf("string", 42, true, 3.14)
    }

    @Test
    fun toMap_shouldConvertComplexNestedStructure() {
        val jsonObject = buildJsonObject {
            put("test", buildJsonObject {
                put("stringValue", "testName")
                put("intValue", 30)
                put("listValue", buildJsonArray {
                    add(JsonPrimitive("value1"))
                    add(JsonPrimitive("value2"))
                })
            })
        }

        val result = jsonObject.toMap()

        val user = result["test"] as Map<*, *>
        user["stringValue"] shouldBe "testName"
        user["intValue"] shouldBe 30
        (user["listValue"] as List<*>) shouldContainExactly listOf("value1", "value2")
    }

    @Test
    fun toMap_shouldOmitNullValues() {
        val jsonObject = buildJsonObject {
            put("key1", "value")
            put("key2", JsonNull)
        }

        val result = jsonObject.toMap()

        result shouldBe mapOf("key1" to "value")
    }

    @Test
    fun toJsonObject_shouldReturnEmptyJsonObject_whenMapIsEmpty() {
        val map = emptyMap<String, Any>()

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {}
    }

    @Test
    fun toJsonObject_shouldConvertString() {
        val map = mapOf("key" to "value")

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("key", "value")
        }
    }

    @Test
    fun toJsonObject_shouldConvertInt() {
        val map = mapOf("number" to 42)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("number", 42)
        }
    }

    @Test
    fun toJsonObject_shouldConvertLong() {
        val map = mapOf("number" to 1232342312345L)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("number", 1232342312345L)
        }
    }

    @Test
    fun toJsonObject_shouldConvertDouble() {
        val map = mapOf("number" to 3.14)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("number", 3.14)
        }
    }

    @Test
    fun toJsonObject_shouldConvertBoolean() {
        val map = mapOf("flag" to true)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("flag", true)
        }
    }

    @Test
    fun toJsonObject_shouldConvertWithVariousTypes() {
        val map = mapOf(
            "string" to "value",
            "int" to 42,
            "bool" to false,
            "double" to 2.71
        )

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("string", "value")
            put("int", 42)
            put("bool", false)
            put("double", 2.71)
        }
    }

    @Test
    fun toJsonObject_shouldConvertNestedMap() {
        val map = mapOf(
            "outer" to mapOf("inner" to "value")
        )

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("outer", buildJsonObject {
                put("inner", "value")
            })
        }
    }

    @Test
    fun toJsonObject_shouldConvertList() {
        val map = mapOf(
            "array" to listOf("item1", "item2", "item3")
        )

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("array", buildJsonArray {
                add("item1")
                add("item2")
                add("item3")
            })
        }
    }

    @Test
    fun toJsonObject_shouldConvertListWithMixedTypes() {
        val map = mapOf(
            "array" to listOf("string", 42, true, 3.14)
        )

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("array", buildJsonArray {
                add(JsonPrimitive("string"))
                add(JsonPrimitive(42))
                add(JsonPrimitive(true))
                add(JsonPrimitive(3.14))
            })
        }
    }

    @Test
    fun toJsonObject_shouldConvertComplexNestedStructure() {
        val map = mapOf(
            "user" to mapOf(
                "name" to "John",
                "age" to 30,
                "tags" to listOf("developer", "kotlin")
            )
        )

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("user", buildJsonObject {
                put("name", "John")
                put("age", 30)
                put("tags", buildJsonArray {
                    add(JsonPrimitive("developer"))
                    add(JsonPrimitive("kotlin"))
                })
            })
        }
    }

    @Test
    fun toJsonObject_shouldConvertNull() {
        val map: Map<String, Any?> = mapOf("key" to null)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("key", JsonNull)
        }
    }

    @Test
    fun toJsonObject_shouldConvertChar() {
        val map = mapOf("char" to 'A')

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("char", "A")
        }
    }

    @Test
    fun toJsonObject_shouldConvertUnknownTypeToString() {
        data class CustomClass(val value: String)
        val map = mapOf("custom" to CustomClass("test"))

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("custom", "CustomClass(value=test)")
        }
    }

    @Test
    fun toJsonObject_shouldPassThroughJsonElement() {
        val jsonElement = JsonPrimitive("value")
        val map = mapOf("key" to jsonElement)

        val result = map.toJsonObject()

        result shouldBe buildJsonObject {
            put("key", "value")
        }
    }

    @Test
    fun data_shouldBePreserved_forSimpleMap() {
        val original = mapOf(
            "string" to "value",
            "int" to 42,
            "bool" to true
        )

        val jsonObject = original.toJsonObject()
        val result = jsonObject.toMap()

        result shouldBe original
    }

    @Test
    fun data_shouldBePreserved_forNestedStructure() {
        val original = mapOf(
            "user" to mapOf(
                "name" to "John",
                "age" to 30,
                "active" to true
            ),
            "tags" to listOf("kotlin", "multiplatform")
        )

        val jsonObject = original.toJsonObject()
        val result = jsonObject.toMap()

        result shouldBe original
    }

    @Test
    fun data_shouldBePreserved_forJsonObject() {
        val original = buildJsonObject {
            put("string", "value")
            put("int", 42)
            put("nested", buildJsonObject {
                put("inner", "data")
            })
        }

        val map = original.toMap()
        val result = map.toJsonObject()

        result shouldBe original
    }
}