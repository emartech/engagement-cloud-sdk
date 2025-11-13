package com.emarsys.sample

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.sample.navigation.AppNavigation
import com.emarsys.sample.navigation.NavigationTab

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(NavigationTab.SDK_TEST) }
    
    MaterialTheme {
        AppNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}