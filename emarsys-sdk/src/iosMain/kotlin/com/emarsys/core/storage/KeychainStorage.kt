package com.emarsys.core.storage

import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemCopyMatching
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal class KeychainStorage : KeychainStorageApi {

    override fun readString(key: String): String? {
        val data = readData(key) ?: return null
        return NSString.create(data, NSUTF8StringEncoding)?.toString()
    }

    override fun readData(key: String): NSData? {
        return memScoped {
            val keyCFString = CFStringCreateWithCString(null, key, kCFStringEncodingUTF8)
            
            val keys = allocArrayOf(
                kSecClass,
                kSecAttrAccount,
                kSecReturnData,
                kSecMatchLimit
            )

            val values = allocArrayOf(
                kSecClassGenericPassword,
                keyCFString,
                kCFBooleanTrue,
                kSecMatchLimitOne
            )

            val query = CFDictionaryCreate(
                kCFAllocatorDefault,
                keys.reinterpret(),
                values.reinterpret(),
                4,
                null, 
                null
            )

            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, resultPtr.ptr)
            
            CFRelease(query)
            CFRelease(keyCFString)

            if (status == errSecSuccess) {
                val value = resultPtr.value
                if (value != null) {
                    return@memScoped interpretObjCPointer<NSData>(value.rawValue)
                }
            }
            null
        }
    }

}
