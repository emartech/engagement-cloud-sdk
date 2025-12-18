package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private const val FILTER_ALT_ICON_PATH =
    "M4.25 5.61C6.27 8.2 10 13 10 13v6c0 .55.45 1 1 1h2c.55 0 1-.45 1-1v-6s3.72-4.8 5.74-7.39A1 1 0 0 0 18.95 4H5.04c-.83 0-1.3.95-.79 1.61z"

@Composable
fun CategorySelectorButton(
    isCategorySelectionActive: Boolean,
    onClick: () -> Unit
) {
    Button({
        onClick { onClick() }
        classes(
            EmbeddedMessagingStyleSheet.categorySelectorButton,
            if (isCategorySelectionActive) EmbeddedMessagingStyleSheet.categorySelectorButtonActive
            else EmbeddedMessagingStyleSheet.categorySelectorButtonInactive
        )
    }) {
        SvgIcon(
            path = FILTER_ALT_ICON_PATH,
            className = EmbeddedMessagingStyleSheet.categorySelectorIcon
        )
        Span({
            style { paddingLeft(8.px) }
        }) {
            Text(LocalStringResources.current.categoriesFilterButtonLabel)
        }
    }
}
