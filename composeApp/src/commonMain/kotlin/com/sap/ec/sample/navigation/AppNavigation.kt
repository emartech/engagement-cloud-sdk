package com.sap.ec.sample.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.ListPageView
import com.sap.ec.sample.safeAreaPadding
import com.sap.ec.sample.screen.SdkTestScreen

@Composable
fun AppNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeAreaPadding()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavigationTab.SDK_TEST -> {
                    SdkTestScreen()
                }
                NavigationTab.EMBEDDED_MESSAGING -> {
                    ListPageView(showFilters = true)
                }
            }
        }
    }
}

