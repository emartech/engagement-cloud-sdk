package com.sap.ec.sample

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.sap.ec.sample.navigation.AppNavigation
import com.sap.ec.sample.navigation.NavigationTab

@Composable
fun App() {
    var selectedTab by rememberSaveable { mutableStateOf(NavigationTab.SDK_TEST) }
    
    MaterialTheme {
        AppNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}