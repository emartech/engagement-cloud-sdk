package com.emarsys.sample.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SdkTestScreen(
    viewModel: SdkTestScreenViewModel = getSdkTestScreenViewModel()
) {
    val eventName by viewModel.eventName.collectAsState()
    val switchValue by viewModel.switchValue.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Hello Team SDK!")
        
        Button(onClick = { viewModel.initializeSdk() }) {
            Text("Init SDK")
        }

        Button(onClick = { viewModel.enableSdkAndLinkContact() }) {
            Text("enable SDK & link contact")
        }

        TextField(
            value = eventName,
            onValueChange = { viewModel.updateEventName(it) }
        )
        
        Button(onClick = { viewModel.trackCustomEvent() }) {
            Text("trackCustomEvent")
        }
        
        Switch(
            checked = switchValue,
            onCheckedChange = { viewModel.toggleInAppDnd(it) }
        )

        Text("InApp DND: $switchValue")
    }
}
