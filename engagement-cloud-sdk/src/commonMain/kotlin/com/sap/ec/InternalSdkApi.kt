package com.sap.ec

@RequiresOptIn(
    message = "This is an internal SDK API. It should not be used by external consumers.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
annotation class InternalSdkApi

// DELIBERATE detekt violation for T5 negative test — DELETE before merge.
@Suppress("unused")
private fun magicNumberTest(): Int {
    return 42 + 73 + 314 + 1729 + 6174  // MagicNumber rule should fire
}
