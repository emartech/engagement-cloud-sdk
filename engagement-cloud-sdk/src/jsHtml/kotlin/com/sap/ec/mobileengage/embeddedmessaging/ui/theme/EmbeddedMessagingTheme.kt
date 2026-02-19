package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.StringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.TranslationMapper
import org.jetbrains.compose.web.css.Style

@Composable
fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    Style(EmbeddedMessagingStyleSheet)

    val embeddedMessagingContext: EmbeddedMessagingContextApi? = koin.getOrNull()

    embeddedMessagingContext?.let {
        val themeMapper =
            ThemeMapper(it)
        val designValues = themeMapper.mapMisc()
        val translationMapper = TranslationMapper()
        val stringValues = translationMapper.map(it)
        themeMapper.generateThemeCSS()

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
