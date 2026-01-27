package com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_MARGIN
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet.shimmerEffect
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
fun PlaceholderMessageItemView() {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            flexDirection(FlexDirection.Row)
            padding(DEFAULT_PADDING)
        }
    }) {
        Div({
            classes(shimmerEffect, EmbeddedMessagingStyleSheet.messageItemImage)
        })

        Div({
            classes(EmbeddedMessagingStyleSheet.messageItemImageSpacer)
        })

        Div({
            classes(EmbeddedMessagingStyleSheet.messageItemContent)
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Start)
                justifyContent(JustifyContent.Center)
                gap(DEFAULT_MARGIN)
            }
        }) {
            Div({
                classes(shimmerEffect, EmbeddedMessagingStyleSheet.messageItemTextPlaceholder)
                style {

                    width(70.percent)
                }
            })

            Div({
                classes(shimmerEffect, EmbeddedMessagingStyleSheet.messageItemTextPlaceholder)
                style {
                    width(90.percent)
                }
            })

            Div({
                classes(shimmerEffect, EmbeddedMessagingStyleSheet.messageItemTextPlaceholder)
                style {
                    width(20.percent)
                }
            })
        }

    }
}
