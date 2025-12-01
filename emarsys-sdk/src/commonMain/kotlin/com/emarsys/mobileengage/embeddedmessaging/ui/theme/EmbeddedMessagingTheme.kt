package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

@Composable
fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    val embeddedMessagingContext: EmbeddedMessagingContextApi? = koin.getOrNull()

    embeddedMessagingContext?.let {
        val themeMapper = ThemeMapper(it)
        val designValues = themeMapper.mapMisc()

        CompositionLocalProvider(
            LocalDesignValues provides designValues
        ) {
            MaterialTheme(
                colorScheme = themeMapper.mapColorScheme(),
                typography = themeMapper.mapTypography(),
                content = content
            )
        }
    } ?: CompositionLocalProvider(
        LocalDesignValues provides EmbeddedMessagingDesignValues()
    ) {
        MaterialTheme { content() }
    }
}