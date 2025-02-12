
package com.emarsys.api.push

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCClass
import objcnames.classes.Protocol
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.NSUInteger

@OptIn(ExperimentalForeignApi::class)
class TestUserNotificationCenterDelegate: NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun description(): String? {
        return null
    }

    @OptIn(BetaInteropApi::class)
    override fun `class`(): ObjCClass? {
        return null
    }

    override fun conformsToProtocol(aProtocol: Protocol?): Boolean {
        return true
    }

    override fun hash(): NSUInteger {
        return 0u
    }

    override fun isEqual(`object`: Any?): Boolean {
        return true
    }

    @OptIn(BetaInteropApi::class)
    override fun isKindOfClass(aClass: ObjCClass?): Boolean {
        return true
    }

    @OptIn(BetaInteropApi::class)
    override fun isMemberOfClass(aClass: ObjCClass?): Boolean {
        return true
    }

    override fun isProxy(): Boolean {
        return true
    }

    override fun performSelector(aSelector: COpaquePointer?, withObject: Any?): Any? {
        return null
    }

    override fun performSelector(aSelector: COpaquePointer?): Any? {
        return null
    }

    override fun performSelector(
        aSelector: COpaquePointer?,
        withObject: Any?,
        _withObject: Any?
    ): Any? {
        return null
    }

    override fun respondsToSelector(aSelector: COpaquePointer?): Boolean {
        return true
    }

    @OptIn(BetaInteropApi::class)
    override fun superclass(): ObjCClass? {
        return null
    }
}