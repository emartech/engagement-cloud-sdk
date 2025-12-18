package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.StringResources
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.TranslationMapper
import org.jetbrains.compose.web.css.Style

@Composable
fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    ThemeMapper.generateDefaultThemeCSS()
    Style(EmbeddedMessagingStyleSheet)

    val embeddedMessagingContext: EmbeddedMessagingContextApi? = koin.getOrNull()

    embeddedMessagingContext?.let {
        val themeMapper = ThemeMapper(it)
        val designValues = themeMapper.mapMisc()
        val translationMapper = TranslationMapper()
        val stringValues = translationMapper.map(it)

        CompositionLocalProvider(
            LocalDesignValues provides designValues,
            LocalStringResources provides stringValues
        ) {
            content()
        }
    } ?: run {
        CompositionLocalProvider(
            LocalDesignValues provides EmbeddedMessagingDesignValues(),
            LocalStringResources provides StringResources()
        ) {
            content()
        }
    }
}
