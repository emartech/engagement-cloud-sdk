package com.sap.ec.core.resource

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class MetadataReaderTest {
    private companion object {
        const val TEST_KEY = "testKey"
        const val TEST_VALUE = 10
        const val DEFAULT_VALUE = 2
    }

    private lateinit var metadataReader: MetadataReader
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var bundle: Bundle
    private lateinit var applicationInfo: ApplicationInfo

    @Before
    fun setup() {
        bundle = Bundle()
        applicationInfo = ApplicationInfo().apply {
            metaData = bundle
        }
        mockPackageManager = mockk(relaxed = true)
        every { mockPackageManager.getApplicationInfo(any(), any<Int>()) } returns applicationInfo
        mockContext = mockk(relaxed = true)
        every { mockContext.packageManager } returns mockPackageManager

        metadataReader = MetadataReader(mockContext)
    }

    @Test
    fun getInt_shouldReturnValue_ifExists() {
        bundle.putInt(TEST_KEY, TEST_VALUE)

        metadataReader.getInt(TEST_KEY) shouldBe TEST_VALUE
    }

    @Test
    fun getInt_shouldReturnValue_ifDefaultIsProvided_butValueIsFound() {
        bundle.putInt(TEST_KEY, TEST_VALUE)

        metadataReader.getInt(TEST_KEY, DEFAULT_VALUE) shouldBe TEST_VALUE
    }

    @Test
    fun getInt_shouldReturnDefaultValue_ifProvided_andValueIsNotFound() {
        metadataReader.getInt(TEST_KEY, DEFAULT_VALUE) shouldBe DEFAULT_VALUE
    }

    @Test
    fun getInt_shouldReturn_0_ifValueIsNotFound() {
        metadataReader.getInt(TEST_KEY) shouldBe 0
    }

    @Test
    fun getInt_shouldReturn_0_ifPackageNameIsNotFound() {
        every { mockPackageManager.getApplicationInfo(any(), any<Int>()) } throws PackageManager.NameNotFoundException()

        metadataReader.getInt(TEST_KEY) shouldBe 0
    }

    @Test
    fun getInt_shouldReturn_DefaultValue_ifPackageNameIsNotFound_butDefaultValueIsProvided() {
        every { mockPackageManager.getApplicationInfo(any(), any<Int>()) } throws PackageManager.NameNotFoundException()

        metadataReader.getInt(TEST_KEY, DEFAULT_VALUE) shouldBe DEFAULT_VALUE
    }
}