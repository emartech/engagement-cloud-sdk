#!/bin/bash
# verify-visibility-sdk841.sh
#
# Verification script for SDK-841: Checks that visibility modifiers are
# correctly applied to jsHtml and commonComposeMain source sets.
#
# This script serves as the RED/GREEN test for the visibility audit:
# - RED: Before adding internal modifiers, this script reports failures
# - GREEN: After adding internal modifiers, all checks pass
#
# Usage: ./engagement-cloud-sdk/scripts/verify-visibility-sdk841.sh
# Run from the repository root.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SDK_SRC="$REPO_ROOT/engagement-cloud-sdk/src"

JSHTML_DIR="$SDK_SRC/jsHtml/kotlin"
COMPOSE_DIR="$SDK_SRC/commonComposeMain/kotlin"

ERRORS=0
CHECKS=0

pass() {
    CHECKS=$((CHECKS + 1))
    echo "  PASS: $1"
}

fail() {
    CHECKS=$((CHECKS + 1))
    ERRORS=$((ERRORS + 1))
    echo "  FAIL: $1"
}

echo "=== SDK-841 Visibility Audit: jsHtml ==="
echo ""

# -----------------------------------------------------------------------
# jsHtml: @JsExport types MUST remain public (no internal keyword)
# -----------------------------------------------------------------------
echo "-- Checking @JsExport types remain public --"

CUSTOM_ELEMENTS="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/CustomElements.kt"
if grep -q "@JsExport" "$CUSTOM_ELEMENTS"; then
    if grep -q "^internal.*abstract class ComposeCustomElement\|^internal class ECMessagingListElement\|^internal class ECMessagingCompactListElement" "$CUSTOM_ELEMENTS"; then
        fail "CustomElements.kt: @JsExport types should NOT be internal"
    else
        pass "CustomElements.kt: @JsExport types are public"
    fi
else
    fail "CustomElements.kt: @JsExport annotation not found"
fi

# -----------------------------------------------------------------------
# jsHtml: actual fun initializeCustomElements() must be internal actual
# (expect is: internal expect fun initializeCustomElements())
# -----------------------------------------------------------------------
echo ""
echo "-- Checking actual/expect alignment --"

JSHTML_INIT="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/CustomElementInitialization.kt"
if grep -q "^internal actual fun initializeCustomElements()" "$JSHTML_INIT"; then
    pass "jsHtml CustomElementInitialization.kt: actual has internal modifier"
else
    fail "jsHtml CustomElementInitialization.kt: actual fun initializeCustomElements() should be 'internal actual fun'"
fi

COMPOSE_INIT="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/CustomElementInitialization.kt"
if grep -q "^internal actual fun initializeCustomElements()" "$COMPOSE_INIT"; then
    pass "commonComposeMain CustomElementInitialization.kt: actual has internal modifier"
else
    fail "commonComposeMain CustomElementInitialization.kt: actual fun initializeCustomElements() should be 'internal actual fun'"
fi

# -----------------------------------------------------------------------
# jsHtml: Non-@JsExport types MUST have internal modifier
# -----------------------------------------------------------------------
echo ""
echo "-- Checking jsHtml non-@JsExport types are internal --"

# EmbeddedMessagingUiConstants object
JSHTML_CONSTANTS="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/EmbeddedMessagingUiConstants.kt"
if grep -q "^internal object EmbeddedMessagingUiConstants" "$JSHTML_CONSTANTS"; then
    pass "EmbeddedMessagingUiConstants.kt: object is internal"
else
    fail "EmbeddedMessagingUiConstants.kt: object should be 'internal object EmbeddedMessagingUiConstants'"
fi

# EmbeddedMessagingStyleSheet object
JSHTML_STYLESHEET="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/theme/EmbeddedMessagingStyleSheet.kt"
if grep -q "^internal object EmbeddedMessagingStyleSheet" "$JSHTML_STYLESHEET"; then
    pass "EmbeddedMessagingStyleSheet.kt: object is internal"
else
    fail "EmbeddedMessagingStyleSheet.kt: object should be 'internal object EmbeddedMessagingStyleSheet'"
fi

# CssColorVar data class
JSHTML_CSSVAR="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/theme/CssVariables.kt"
if grep -q "^internal data class CssColorVar" "$JSHTML_CSSVAR"; then
    pass "CssVariables.kt: CssColorVar is internal"
else
    fail "CssVariables.kt: CssColorVar should be 'internal data class CssColorVar'"
fi

# setVar extension function
if grep -q "^internal fun StyleScope.setVar" "$JSHTML_CSSVAR"; then
    pass "CssVariables.kt: setVar is internal"
else
    fail "CssVariables.kt: setVar should be 'internal fun StyleScope.setVar'"
fi

# EmbeddedMessagingTheme composable
JSHTML_THEME="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/theme/EmbeddedMessagingTheme.kt"
if grep -q "^internal fun EmbeddedMessagingTheme" "$JSHTML_THEME"; then
    pass "EmbeddedMessagingTheme.kt: function is internal"
else
    fail "EmbeddedMessagingTheme.kt: function should be 'internal fun EmbeddedMessagingTheme'"
fi

# CategoriesDialogView composable
JSHTML_CATDIALOG="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/category/CategoriesDialogView.kt"
if grep -q "^internal fun CategoriesDialogView" "$JSHTML_CATDIALOG"; then
    pass "CategoriesDialogView.kt: function is internal"
else
    fail "CategoriesDialogView.kt: function should be 'internal fun CategoriesDialogView'"
fi

# CategorySelectorButton composable
JSHTML_CATBTN="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/category/CategorySelectorButton.kt"
if grep -q "^internal fun CategorySelectorButton" "$JSHTML_CATBTN"; then
    pass "CategorySelectorButton.kt: function is internal"
else
    fail "CategorySelectorButton.kt: function should be 'internal fun CategorySelectorButton'"
fi

# DeleteMessageDialogView composable
JSHTML_DELETE="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/delete/DeleteMessageDialogView.kt"
if grep -q "^internal fun DeleteMessageDialogView" "$JSHTML_DELETE"; then
    pass "DeleteMessageDialogView.kt: function is internal"
else
    fail "DeleteMessageDialogView.kt: function should be 'internal fun DeleteMessageDialogView'"
fi

# MessageDetailView composable
JSHTML_DETAIL="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/detail/MessageDetailView.kt"
if grep -q "^internal fun MessageDetailView" "$JSHTML_DETAIL"; then
    pass "MessageDetailView.kt: function is internal"
else
    fail "MessageDetailView.kt: function should be 'internal fun MessageDetailView'"
fi

# MessageItemView composable
JSHTML_ITEM="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/item/MessageItemView.kt"
if grep -q "^internal fun MessageItemView" "$JSHTML_ITEM"; then
    pass "MessageItemView.kt: MessageItemView is internal"
else
    fail "MessageItemView.kt: MessageItemView should be 'internal fun MessageItemView'"
fi

# LoadingSpinner composable
if grep -q "^internal fun LoadingSpinner" "$JSHTML_ITEM"; then
    pass "MessageItemView.kt: LoadingSpinner is internal"
else
    fail "MessageItemView.kt: LoadingSpinner should be 'internal fun LoadingSpinner'"
fi

# CompactListView composable
JSHTML_COMPACT="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/CompactListView.kt"
if grep -q "^internal fun CompactListView" "$JSHTML_COMPACT"; then
    pass "CompactListView.kt: function is internal"
else
    fail "CompactListView.kt: function should be 'internal fun CompactListView'"
fi

# ListPageView and its public functions
JSHTML_LISTPAGE="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/ListPageView.kt"
if grep -q "^internal fun ListPageView" "$JSHTML_LISTPAGE"; then
    pass "ListPageView.kt: ListPageView is internal"
else
    fail "ListPageView.kt: ListPageView should be 'internal fun ListPageView'"
fi

# ListView composable
JSHTML_LISTVIEW="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/ListView.kt"
if grep -q "^internal fun ListView" "$JSHTML_LISTVIEW"; then
    pass "ListView.kt: function is internal"
else
    fail "ListView.kt: function should be 'internal fun ListView'"
fi

# PlaceholderMessageItemView composable
JSHTML_PLACEHOLDER_ITEM="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/placeholders/PlaceholderMessageItemView.kt"
if grep -q "^internal fun PlaceholderMessageItemView" "$JSHTML_PLACEHOLDER_ITEM"; then
    pass "PlaceholderMessageItemView.kt: function is internal"
else
    fail "PlaceholderMessageItemView.kt: function should be 'internal fun PlaceholderMessageItemView'"
fi

# PlaceholderMessageList composable
JSHTML_PLACEHOLDER_LIST="$JSHTML_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/placeholders/PlaceholderMessageList.kt"
if grep -q "^internal fun PlaceholderMessageList" "$JSHTML_PLACEHOLDER_LIST"; then
    pass "PlaceholderMessageList.kt: function is internal"
else
    fail "PlaceholderMessageList.kt: function should be 'internal fun PlaceholderMessageList'"
fi

# -----------------------------------------------------------------------
# commonComposeMain: ALL types MUST have internal modifier
# -----------------------------------------------------------------------
echo ""
echo "=== SDK-841 Visibility Audit: commonComposeMain ==="
echo ""
echo "-- Checking all commonComposeMain types are internal --"

# CategoriesDialogView
COMPOSE_CATDIALOG="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/category/CategoriesDialogView.kt"
if grep -q "^internal fun CategoriesDialogView" "$COMPOSE_CATDIALOG"; then
    pass "CategoriesDialogView.kt: function is internal"
else
    fail "CategoriesDialogView.kt: function should be 'internal fun CategoriesDialogView'"
fi

# CategorySelectorButton
COMPOSE_CATBTN="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/category/CategorySelectorButton.kt"
if grep -q "^internal fun CategorySelectorButton" "$COMPOSE_CATBTN"; then
    pass "CategorySelectorButton.kt: function is internal"
else
    fail "CategorySelectorButton.kt: function should be 'internal fun CategorySelectorButton'"
fi

# MessageDetailView
COMPOSE_DETAIL="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/detail/MessageDetailView.kt"
if grep -q "^internal fun MessageDetailView" "$COMPOSE_DETAIL"; then
    pass "MessageDetailView.kt: function is internal"
else
    fail "MessageDetailView.kt: function should be 'internal fun MessageDetailView'"
fi

# EmbeddedMessagingUiConstants
COMPOSE_CONSTANTS="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/EmbeddedMessagingUiConstants.kt"
if grep -q "^internal object EmbeddedMessagingUiConstants" "$COMPOSE_CONSTANTS"; then
    pass "EmbeddedMessagingUiConstants.kt: object is internal"
else
    fail "EmbeddedMessagingUiConstants.kt: object should be 'internal object EmbeddedMessagingUiConstants'"
fi

# DeleteMessageItemConfirmationDialog
COMPOSE_DELETE="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/item/DeleteMessageItemConfirmationDialog.kt"
if grep -q "^internal fun DeleteMessageItemConfirmationDialog" "$COMPOSE_DELETE"; then
    pass "DeleteMessageItemConfirmationDialog.kt: function is internal"
else
    fail "DeleteMessageItemConfirmationDialog.kt: function should be 'internal fun DeleteMessageItemConfirmationDialog'"
fi

# MessageItemView
COMPOSE_ITEM="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/item/MessageItemView.kt"
if grep -q "^internal fun MessageItemView" "$COMPOSE_ITEM"; then
    pass "MessageItemView.kt: MessageItemView is internal"
else
    fail "MessageItemView.kt: MessageItemView should be 'internal fun MessageItemView'"
fi

# LoadingSpinner
if grep -q "^internal fun LoadingSpinner" "$COMPOSE_ITEM"; then
    pass "MessageItemView.kt: LoadingSpinner is internal"
else
    fail "MessageItemView.kt: LoadingSpinner should be 'internal fun LoadingSpinner'"
fi

# onScreenTime extension
COMPOSE_ONSCREEN="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/item/onScreenTime.kt"
if grep -q "^internal fun Modifier.onScreenTime" "$COMPOSE_ONSCREEN"; then
    pass "onScreenTime.kt: extension function is internal"
else
    fail "onScreenTime.kt: extension should be 'internal fun Modifier.onScreenTime'"
fi

# CompactListView / EmbeddedMessagingCompactView (uses @InternalSdkApi, not internal -- cross-module entry point)
COMPOSE_COMPACT="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/CompactListView.kt"
if grep -q "@com.sap.ec.InternalSdkApi" "$COMPOSE_COMPACT" && grep -q "^fun EmbeddedMessagingCompactView" "$COMPOSE_COMPACT"; then
    pass "CompactListView.kt: EmbeddedMessagingCompactView has @InternalSdkApi"
elif grep -q "^internal fun EmbeddedMessagingCompactView" "$COMPOSE_COMPACT"; then
    pass "CompactListView.kt: EmbeddedMessagingCompactView is internal"
else
    fail "CompactListView.kt: EmbeddedMessagingCompactView should be 'internal fun' or '@InternalSdkApi fun'"
fi

# ListPageView / EmbeddedMessagingView (uses @InternalSdkApi, not internal -- cross-module entry point)
COMPOSE_LISTPAGE="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/ListPageView.kt"
if grep -q "@com.sap.ec.InternalSdkApi" "$COMPOSE_LISTPAGE" && grep -q "^fun EmbeddedMessagingView" "$COMPOSE_LISTPAGE"; then
    pass "ListPageView.kt: EmbeddedMessagingView has @InternalSdkApi"
elif grep -q "^internal fun EmbeddedMessagingView" "$COMPOSE_LISTPAGE"; then
    pass "ListPageView.kt: EmbeddedMessagingView is internal"
else
    fail "ListPageView.kt: EmbeddedMessagingView should be 'internal fun' or '@InternalSdkApi fun'"
fi

# AdaptiveCardContainer
if grep -q "^internal fun AdaptiveCardContainer" "$COMPOSE_LISTPAGE"; then
    pass "ListPageView.kt: AdaptiveCardContainer is internal"
else
    fail "ListPageView.kt: AdaptiveCardContainer should be 'internal fun AdaptiveCardContainer'"
fi

# MessageItemsListPane
COMPOSE_LISTPANE="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/MessageItemsListPane.kt"
if grep -q "^internal fun MessageItemsListPane" "$COMPOSE_LISTPANE"; then
    pass "MessageItemsListPane.kt: MessageItemsListPane is internal"
else
    fail "MessageItemsListPane.kt: MessageItemsListPane should be 'internal fun MessageItemsListPane'"
fi

# BoxWithDeleteIcon
if grep -q "^internal fun BoxWithDeleteIcon" "$COMPOSE_LISTPANE"; then
    pass "MessageItemsListPane.kt: BoxWithDeleteIcon is internal"
else
    fail "MessageItemsListPane.kt: BoxWithDeleteIcon should be 'internal fun BoxWithDeleteIcon'"
fi

# Effect interface
COMPOSE_EFFECT="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/placeholders/Effect.kt"
if grep -q "^internal interface Effect" "$COMPOSE_EFFECT"; then
    pass "Effect.kt: interface is internal"
else
    fail "Effect.kt: interface should be 'internal interface Effect'"
fi

# PlaceholderMessageList
COMPOSE_PLACEHOLDER="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/placeholders/PlaceholderMessageList.kt"
if grep -q "^internal fun PlaceholderMessageList" "$COMPOSE_PLACEHOLDER"; then
    pass "PlaceholderMessageList.kt: function is internal"
else
    fail "PlaceholderMessageList.kt: function should be 'internal fun PlaceholderMessageList'"
fi

# ShimmerEffect / Shimmer data class
COMPOSE_SHIMMER="$COMPOSE_DIR/com/sap/ec/mobileengage/embeddedmessaging/ui/list/placeholders/ShimmerEffect.kt"
if grep -q "^internal data class Shimmer" "$COMPOSE_SHIMMER"; then
    pass "ShimmerEffect.kt: Shimmer is internal"
else
    fail "ShimmerEffect.kt: Shimmer should be 'internal data class Shimmer'"
fi

# InlineInAppView (public overload uses @InternalSdkApi -- cross-module entry point)
COMPOSE_INAPP="$COMPOSE_DIR/com/sap/ec/mobileengage/inapp/view/InlineInAppView.kt"
if grep -q "@com.sap.ec.InternalSdkApi" "$COMPOSE_INAPP" && grep -q "^fun InlineInAppView" "$COMPOSE_INAPP"; then
    pass "InlineInAppView.kt: public overload has @InternalSdkApi"
elif grep -q "^internal fun InlineInAppView" "$COMPOSE_INAPP"; then
    pass "InlineInAppView.kt: public overload is internal"
else
    fail "InlineInAppView.kt: public overload should be 'internal fun' or '@InternalSdkApi fun'"
fi

# -----------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------
echo ""
echo "=== Summary ==="
echo "Checks: $CHECKS"
echo "Passed: $((CHECKS - ERRORS))"
echo "Failed: $ERRORS"
echo ""

if [ "$ERRORS" -gt 0 ]; then
    echo "RESULT: FAIL -- $ERRORS visibility checks failed"
    exit 1
else
    echo "RESULT: PASS -- all visibility checks passed"
    exit 0
fi
