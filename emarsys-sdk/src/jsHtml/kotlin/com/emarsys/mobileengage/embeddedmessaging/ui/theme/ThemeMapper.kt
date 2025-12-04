package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun generateThemeCSS() {
        // Generate CSS custom properties for the theme
        org.jetbrains.compose.web.dom.Style {
            """
            :root {
                /* Colors from hardcoded color scheme */
                --color-primary: #526525;
                --color-on-primary: #ffffff;
                --color-primary-container: #9cbd4c;
                --color-on-primary-container: #000000;
                --color-secondary: #365e2c;
                --color-on-secondary: #ffffff;
                --color-secondary-container: #DFE6C5;
                --color-on-secondary-container: #000000;
                --color-tertiary: #6f6334;
                --color-on-tertiary: #ffffff;
                --color-tertiary-container: #b19f58;
                --color-on-tertiary-container: #000000;
                --color-error: #BA1A1A;
                --color-on-error: #FFFFFF;
                --color-error-container: #FFDAD6;
                --color-on-error-container: #410002;
                --color-background: #FDFCF6;
                --color-on-background: #1B1C18;
                --color-surface: #FAFAEE;
                --color-on-surface: #1B1C18;
                --color-surface-variant: #EFEFE2;
                --color-on-surface-variant: #45483C;
                --color-surface-container: #F0EFE8;
                --color-surface-container-high: #EAE9E2;
                --color-surface-container-highest: #E4E3DD;
                --color-surface-container-low: #F6F5EE;
                --color-surface-container-lowest: #FFFFFF;
                --color-surface-dim: #DDD9D1;
                --color-surface-bright: #FDFCF6;
                --color-outline: #c8cebb;
                --color-outline-variant: #ffffff;
                --color-inverse-surface: #30312C;
                --color-inverse-on-surface: #F2F1E9;
                --color-inverse-primary: #f9fbf4;
                --color-scrim: #000000;
                --color-surface-tint: #526525;
                
                /* Typography - Default font sizes */
                --font-size-display-large: 57px;
                --font-size-display-medium: 45px;
                --font-size-display-small: 36px;
                --font-size-headline-large: 32px;
                --font-size-headline-medium: 28px;
                --font-size-headline-small: 24px;
                --font-size-title-large: 22px;
                --font-size-title-medium: 16px;
                --font-size-title-small: 14px;
                --font-size-body-large: 16px;
                --font-size-body-medium: 14px;
                --font-size-body-small: 12px;
                --font-size-label-large: 14px;
                --font-size-label-medium: 12px;
                --font-size-label-small: 11px;
            }
            """.trimIndent()
        }
    }

    fun mapMisc(): EmbeddedMessagingDesignValues {
        return EmbeddedMessagingDesignValues()
    }

    companion object {
        @Composable
        fun generateDefaultThemeCSS() {
            org.jetbrains.compose.web.dom.Style {
                """
                :root {
                    /* Default colors */
                    --color-primary: #526525;
                    --color-on-primary: #ffffff;
                    --color-primary-container: #9cbd4c;
                    --color-on-primary-container: #000000;
                    --color-secondary: #365e2c;
                    --color-on-secondary: #ffffff;
                    --color-secondary-container: #DFE6C5;
                    --color-on-secondary-container: #000000;
                    --color-tertiary: #6f6334;
                    --color-on-tertiary: #ffffff;
                    --color-tertiary-container: #b19f58;
                    --color-on-tertiary-container: #000000;
                    --color-error: #BA1A1A;
                    --color-on-error: #FFFFFF;
                    --color-error-container: #FFDAD6;
                    --color-on-error-container: #410002;
                    --color-background: #FDFCF6;
                    --color-on-background: #1B1C18;
                    --color-surface: #FAFAEE;
                    --color-on-surface: #1B1C18;
                    --color-surface-variant: #EFEFE2;
                    --color-on-surface-variant: #45483C;
                    --color-surface-container: #F0EFE8;
                    --color-surface-container-high: #EAE9E2;
                    --color-surface-container-highest: #E4E3DD;
                    --color-surface-container-low: #F6F5EE;
                    --color-surface-container-lowest: #FFFFFF;
                    --color-surface-dim: #DDD9D1;
                    --color-surface-bright: #FDFCF6;
                    --color-outline: #c8cebb;
                    --color-outline-variant: #ffffff;
                    --color-inverse-surface: #30312C;
                    --color-inverse-on-surface: #F2F1E9;
                    --color-inverse-primary: #f9fbf4;
                    --color-scrim: #000000;
                    --color-surface-tint: #526525;
                    
                    /* Typography - Default font sizes */
                    --font-size-display-large: 57px;
                    --font-size-display-medium: 45px;
                    --font-size-display-small: 36px;
                    --font-size-headline-large: 32px;
                    --font-size-headline-medium: 28px;
                    --font-size-headline-small: 24px;
                    --font-size-title-large: 22px;
                    --font-size-title-medium: 16px;
                    --font-size-title-small: 14px;
                    --font-size-body-large: 16px;
                    --font-size-body-medium: 14px;
                    --font-size-body-small: 12px;
                    --font-size-label-large: 14px;
                    --font-size-label-medium: 12px;
                    --font-size-label-small: 11px;
                }
                """.trimIndent()
            }
        }
    }
}
