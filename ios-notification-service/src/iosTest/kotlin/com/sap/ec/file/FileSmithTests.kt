package com.sap.ec.iosNotificationService.file

import com.sap.ec.iosNotificationService.provider.Provider
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.dataUsingEncoding
import kotlin.test.BeforeTest
import kotlin.test.Test

class FileSmithTests {

    private lateinit var fileSmith: FileSmith
    private lateinit var mockUUIDProvider: Provider<NSUUID>

    private val uuid1 = NSUUID()
    private val uuid2 = NSUUID()
    private val uuid3 = NSUUID()
    private val uuid4 = NSUUID()
    private val uuid5 = NSUUID()
    private val uuid6 = NSUUID()

    @BeforeTest
    fun setup() = runTest {
        mockUUIDProvider = mock<Provider<NSUUID>> {
            every { provide() } sequentiallyReturns listOf(uuid1, uuid2, uuid3, uuid4, uuid5, uuid6)
        }
        fileSmith = FileSmith(mockUUIDProvider)
    }

    @Test
    fun tmpFileUrl_shouldReturnWith_uuidNamedUrl() = runTest {
        val result = fileSmith.tmpFileUrl("image/png")

        result!!.absoluteString()?.endsWith("${uuid1.UUIDString()}/${uuid2.UUIDString()}.png") shouldBe true
    }

    @Test
    fun move_shouldMoveFile_toDesiredUrl() = runTest {
        val tmpUrl = fileSmith.tmpFileUrl("file/txt")
        val tmpUrl2 = fileSmith.tmpFileUrl("file/txt")
        val fileManager = NSFileManager.defaultManager()
        val fileContent = ("TestText" as NSString).dataUsingEncoding(NSUTF8StringEncoding)

        fileManager.createFileAtPath(tmpUrl!!.path()!!, fileContent, null)

        fileSmith.move(tmpUrl, tmpUrl2!!)

        fileManager.fileExistsAtPath(tmpUrl.path()!!) shouldBe false
        fileManager.fileExistsAtPath(tmpUrl2.path()!!) shouldBe true
    }

}
