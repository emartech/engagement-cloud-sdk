package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CategorySelectorButton(
    isCategorySelectionActive: Boolean,
    onClick: () -> Unit
) {
    EmbeddedMessagingTheme {
        Button({
            onClick { onClick() }
            style {
                height(FLOATING_ACTION_BUTTON_SIZE)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                padding(8.px, 16.px)
                borderRadius(4.px)
                border(0.px)
                cursor("pointer")
                fontSize(14.px)
                fontWeight(500)
                property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                
                if (isCategorySelectionActive) {
                    backgroundColor(Color("var(--color-primary)"))
                    color(Color("var(--color-on-primary)"))
                } else {
                    backgroundColor(Color("var(--color-surface-variant)"))
                    color(Color("var(--color-on-surface-variant)"))
                }
            }
        }) {
            // Simple text icon representation (could use SVG or icon font)
            Span({
                style {
                    fontSize(18.px)
                }
            }) {
                Text(if (isCategorySelectionActive) "◆" else "◇")
            }
            
            Span {
                Text(LocalStringResources.current.categoriesFilterButtonLabel)
            }
        }
    }
}
