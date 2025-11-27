package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.emarsys.di.SdkKoinIsolationContext.koin

@Composable
fun EmbeddedMessagingTheme(content: @Composable () -> Unit) {
    val themeMapper = ThemeMapper(koin.get())
    val emColorScheme = themeMapper.mapColorScheme()

    MaterialTheme(
        colorScheme = themeMapper.mapColorScheme(),
        content = content
    )
}