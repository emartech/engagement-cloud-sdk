package com.sap.ec

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the @InternalSdkApi opt-in annotation.
 *
 * The annotation's primary purpose is compilation gating: external consumers
 * cannot use @InternalSdkApi-annotated types without explicit @OptIn.
 * Within this module, the compiler arg `-opt-in=com.sap.ec.InternalSdkApi`
 * makes annotated types freely usable.
 *
 * These tests verify:
 * 1. The annotation exists and can be applied to classes, interfaces, functions, and properties
 * 2. Annotated types are usable within the opted-in module
 * 3. The annotation targets cover the required use cases (CLASS, FUNCTION, PROPERTY)
 */
class InternalSdkApiTest {

    @InternalSdkApi
    class SampleAnnotatedClass {
        fun value(): String = "internal-api"
    }

    @InternalSdkApi
    interface SampleInternalInterface {
        fun doWork(): String
    }

    @InternalSdkApi
    open class BaseInternal {
        open fun name(): String = "base"
    }

    class DerivedFromInternal : BaseInternal() {
        override fun name(): String = "derived"
    }

    @InternalSdkApi
    class TypeA(val id: String)

    @InternalSdkApi
    class TypeB(val label: String)

    @InternalSdkApi
    fun sampleAnnotatedFunction(): String = "internal-function"

    @InternalSdkApi
    val sampleAnnotatedProperty: String = "internal-property"

    @Test
    fun annotatedClassShouldBeUsableWithinOptedInModule() {
        val instance = SampleAnnotatedClass()
        assertEquals("internal-api", instance.value())
    }

    @Test
    fun annotatedFunctionShouldBeUsableWithinOptedInModule() {
        val result = sampleAnnotatedFunction()
        assertEquals("internal-function", result)
    }

    @Test
    fun annotatedPropertyShouldBeUsableWithinOptedInModule() {
        assertEquals("internal-property", sampleAnnotatedProperty)
    }

    @Test
    fun annotationShouldBeApplicableToInterfaces() {
        val impl = object : SampleInternalInterface {
            override fun doWork(): String = "work-done"
        }

        assertEquals("work-done", impl.doWork())
    }

    @Test
    fun annotatedTypeShouldSupportInheritance() {
        val instance: BaseInternal = DerivedFromInternal()
        assertEquals("derived", instance.name())
    }

    @Test
    fun multipleAnnotatedTypesShouldCoexist() {
        val a = TypeA("a1")
        val b = TypeB("b1")

        assertEquals("a1", a.id)
        assertEquals("b1", b.label)
    }
}
