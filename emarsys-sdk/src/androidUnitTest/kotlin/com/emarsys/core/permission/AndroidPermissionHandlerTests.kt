package com.emarsys.core.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emarsys.core.device.AndroidVersionUtils
import com.emarsys.mobileengage.permission.AndroidPermissionHandler
import com.emarsys.mobileengage.permission.AndroidPermissionHandler.Companion.PERMISSION_REQUEST_CODE
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class AndroidPermissionHandlerTests {

    private lateinit var mockContext: Context
    private lateinit var mockCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog

    private lateinit var androidPermissionHandler: AndroidPermissionHandler

    @Before
    fun setup() {
        mockkStatic("androidx.core.content.ContextCompat")
        mockkStatic("androidx.core.app.ActivityCompat")
        mockkObject(AndroidVersionUtils)
        mockContext = mockk<Context>()
        mockCurrentActivityWatchdog = mockk<TransitionSafeCurrentActivityWatchdog>()
        androidPermissionHandler = AndroidPermissionHandler(
            mockContext,
            mockCurrentActivityWatchdog
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun requestPushPermission_shouldNotDoAnything_whenAndroidVersionIsBelowTiramisu() = runTest {
        every { AndroidVersionUtils.isTiramisuOrAbove } returns false

        androidPermissionHandler.requestPushPermission()

        verify(exactly = 0) { ContextCompat.checkSelfPermission(any(), any()) }
        verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
    }

    @Test
    fun requestPushPermission_shouldNoRequestPermission_whenAndroidVersionIsAtLeastTiramisu_andPermissionIsAlreadyGranted() =
        runTest {
            every { AndroidVersionUtils.isTiramisuOrAbove } returns true
            every {
                ContextCompat.checkSelfPermission(
                    mockContext,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } returns PackageManager.PERMISSION_GRANTED

            androidPermissionHandler.requestPushPermission()

            verify {
                ContextCompat.checkSelfPermission(
                    mockContext,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
        }

    @Test
    fun requestPushPermission_shouldRequestPermission_whenAndroidVersionIsAtLeastTiramisu_andPermissionIsNotGranted() =
        runTest {
            every { AndroidVersionUtils.isTiramisuOrAbove } returns true
            every {
                ContextCompat.checkSelfPermission(
                    mockContext,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } returns PackageManager.PERMISSION_DENIED
            every { ActivityCompat.requestPermissions(any(), any(), any()) } returns Unit
            val mockActivity = mockk<Activity>()
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity

            androidPermissionHandler.requestPushPermission()

            verify {
                ContextCompat.checkSelfPermission(
                    mockContext,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            verify {
                ActivityCompat.requestPermissions(
                    mockActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
}