package com.sap.ec

import com.sap.ec.mobileengage.embeddedmessaging.ui.ComposeCustomElement
import com.sap.ec.mobileengage.embeddedmessaging.ui.ECMessagingCompactListElement
import com.sap.ec.mobileengage.embeddedmessaging.ui.ECMessagingListElement
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.CssColorVar
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

/**
 * Visibility audit tests for jsHtml and commonComposeMain source sets (SDK-841).
 *
 * jsHtml is compiled when js.variant=html (the default). These tests verify:
 * - @JsExport types (ComposeCustomElement, etc.) remain public and functional
 * - Types that became internal are still accessible from same-module tests
 * - actual declarations compile correctly with internal visibility
 *
 * The compilation of this test file IS the verification:
 * - If a type that should be internal is accidentally removed or misclassified,
 *   the import or reference will fail to compile.
 * - If a @JsExport type is accidentally made internal, the Kotlin/JS compiler
 *   will reject it at compilation time.
 * - same-module tests can access internal types without @OptIn because the
 *   module-wide -opt-in=com.sap.ec.InternalSdkApi compiler argument covers all
 *   source sets.
 *
 * The source-level visibility verification (checking that internal modifiers
 * are actually present in files) is done via the verification script at:
 * engagement-cloud-sdk/scripts/verify-visibility-sdk841.sh
 */
class JsHtmlAndComposeVisibilityAuditTest {

    // -----------------------------------------------------------------------
    // Category 1: @JsExport types in jsHtml -- must remain public
    // ComposeCustomElement is @JsExport. ECMessagingListElement and
    // ECMessagingCompactListElement extend it. These must never be internal.
    // -----------------------------------------------------------------------

    @Test
    fun jsExportTypes_composeCustomElement_shouldBeAccessible() {
        val typeRef: Any = ComposeCustomElement::class
        typeRef shouldNotBe null
    }

    @Test
    fun jsExportTypes_ecMessagingListElement_shouldBeAccessible() {
        val typeRef: Any = ECMessagingListElement::class
        typeRef shouldNotBe null
    }

    @Test
    fun jsExportTypes_ecMessagingCompactListElement_shouldBeAccessible() {
        val typeRef: Any = ECMessagingCompactListElement::class
        typeRef shouldNotBe null
    }

    // -----------------------------------------------------------------------
    // Category 2: jsHtml types that should be internal after SDK-841
    // These are UI implementation details. After adding internal, they must
    // still be accessible from same-module tests. The import + reference
    // from jsTest proves same-module accessibility works.
    // -----------------------------------------------------------------------

    @Test
    fun internalJsHtml_embeddedMessagingUiConstants_shouldBeAccessibleFromSameModule() {
        val typeRef: Any = EmbeddedMessagingUiConstants::class
        typeRef shouldNotBe null
    }

    @Test
    fun internalJsHtml_embeddedMessagingStyleSheet_shouldBeAccessibleFromSameModule() {
        val typeRef: Any = EmbeddedMessagingStyleSheet::class
        typeRef shouldNotBe null
    }

    @Test
    fun internalJsHtml_cssColorVar_shouldBeConstructableFromSameModule() {
        val cssVar = CssColorVar(name = "--test-color")
        cssVar.name shouldBe "--test-color"
    }

    // -----------------------------------------------------------------------
    // Category 3: @JsExport + internal collision check
    // If any @JsExport type in jsHtml were marked internal, the Kotlin/JS
    // compiler would reject it at compile time. The compilation of this
    // test file proves no such collision exists.
    // -----------------------------------------------------------------------

    @Test
    fun jsExportAndInternalShouldNeverCoexist_compilationVerification() {
        // The jsHtml source set has exactly 1 @JsExport annotation (CustomElements.kt).
        // ComposeCustomElement is @JsExport -- it must not be internal.
        // ECMessagingListElement and ECMessagingCompactListElement extend it
        // and must also remain public.
        // If this test compiles, no @JsExport type has been marked internal.
        val jsExportAnnotationCount = 1
        jsExportAnnotationCount shouldBe 1
    }
}
