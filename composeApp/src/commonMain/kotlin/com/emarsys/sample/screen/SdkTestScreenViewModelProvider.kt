package com.emarsys.sample.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


@Composable
fun getSdkTestScreenViewModel(): SdkTestScreenViewModel {
    return remember { SdkTestScreenViewModel() }
}
