package com.emarsys.sample.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    BottomNavigation {
        BottomNavigationItem(
            selected = selectedTab == NavigationTab.SDK_TEST,
            onClick = { onTabSelected(NavigationTab.SDK_TEST) },
            icon = { Text("SDK") },
            label = { Text("Test") }
        )
        BottomNavigationItem(
            selected = selectedTab == NavigationTab.EMBEDDED_MESSAGING,
            onClick = { onTabSelected(NavigationTab.EMBEDDED_MESSAGING) },
            icon = { Text("Embedded") },
            label = { Text("Messages") }
        )
    }
}
