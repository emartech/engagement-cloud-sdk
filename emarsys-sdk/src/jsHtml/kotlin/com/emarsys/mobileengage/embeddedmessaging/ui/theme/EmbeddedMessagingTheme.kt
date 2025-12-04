package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.StringResources
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.TranslationMapper
import org.jetbrains.compose.web.dom.Style

@Composable
fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    val embeddedMessagingContext: EmbeddedMessagingContextApi? = koin.getOrNull()

    embeddedMessagingContext?.let {
        val themeMapper = ThemeMapper(it)
        val designValues = themeMapper.mapMisc()
        val translationMapper = TranslationMapper()
        val stringValues = translationMapper.map(it)

        // Apply global CSS theme variables
        Style {
            themeMapper.generateThemeCSS()
        }

        CompositionLocalProvider(
            LocalDesignValues provides designValues,
            LocalStringResources provides stringValues
        ) {
            content()
        }
    } ?: run {
        // Default theme without context
        Style {
            ThemeMapper.generateDefaultThemeCSS()
        }

        CompositionLocalProvider(
            LocalDesignValues provides EmbeddedMessagingDesignValues(),
            LocalStringResources provides StringResources()
        ) {
            content()
        }
    }
}
