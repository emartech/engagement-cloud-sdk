package com.emarsys.sample.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
        Button(onClick = { viewModel.linkContact() }) {
            Text("link contact (test2@test.com)")
        }

        Button(onClick = { viewModel.unLinkContact() }) {
            Text("unlink contact")
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

        Button(onClick = { viewModel.disableSdk() }) {
            Text("Disable tracking")
        }
    }
}
