package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.StringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.TranslationMapper
import org.jetbrains.compose.web.css.Style

@Composable
internal fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    Style(EmbeddedMessagingStyleSheet)

    val embeddedMessagingContext: EmbeddedMessagingContextApi? = koin.getOrNull()

    embeddedMessagingContext?.let {
        val metaData by embeddedMessagingContext.metaData.collectAsState()
        val themeMapper =
            ThemeMapper(metaData)
        val designValues = themeMapper.mapMisc()
        val translationMapper = TranslationMapper()
        val stringValues = translationMapper.map(metaData)
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
