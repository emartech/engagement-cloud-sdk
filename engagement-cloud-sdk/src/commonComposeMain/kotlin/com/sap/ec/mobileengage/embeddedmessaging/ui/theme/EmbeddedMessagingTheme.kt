package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.StringResources
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.TranslationMapper

@Composable
internal fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
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
            MaterialTheme(
                colorScheme = themeMapper.mapColorScheme(),
                typography = themeMapper.mapTypography(),
                shapes = themeMapper.mapShapes(),
                content = content
            )
        }
    } ?: CompositionLocalProvider(
        LocalDesignValues provides EmbeddedMessagingDesignValues(),
        LocalStringResources provides StringResources()
    ) {
        MaterialTheme { content() }
    }
}