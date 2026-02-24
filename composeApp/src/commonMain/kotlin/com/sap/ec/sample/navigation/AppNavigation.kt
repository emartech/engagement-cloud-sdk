package com.sap.ec.sample.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.EmbeddedMessagingCompactView
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.EmbeddedMessagingView
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
                    EmbeddedMessagingView(showFilters = true)
                }

                NavigationTab.EM_CUSTOM_ITEM -> {
                    EmbeddedMessagingCompactView(
                        onNavigate = {
                            println("Navigate to message details")
                        },
                        customMessageItem = { viewModel: CustomMessageItemViewModelApi, isSelected: Boolean ->
                            CustomMessageItemView(viewModel)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomMessageItemView(viewModel: CustomMessageItemViewModelApi) {
    MaterialTheme {
        Card(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    viewModel.title,
                    style = MaterialTheme.typography.h4
                )
                Text(
                    viewModel.lead,
                    style = MaterialTheme.typography.body1
                )
                Divider()
                Text(
                    "Is message pinned? -> ${viewModel.isPinned}",
                    style = MaterialTheme.typography.caption
                )
            }

        }
    }
}

