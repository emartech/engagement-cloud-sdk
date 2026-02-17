package com.sap.ec.sample.screen

import com.sap.ec.EngagementCloud
import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.sample.enableTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SdkTestScreenViewModel {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _eventName = MutableStateFlow("")
    val eventName: StateFlow<String> = _eventName.asStateFlow()

    private val _switchValue = MutableStateFlow(false)
    val switchValue: StateFlow<Boolean> = _switchValue.asStateFlow()

    private val _inlineInAppViewCount = MutableStateFlow(0)
    val inlineInAppViewCount: StateFlow<Int> = _inlineInAppViewCount.asStateFlow()

    fun updateEventName(newValue: String) {
        _eventName.value = newValue
    }

    fun updateSwitchValue(newValue: Boolean) {
        _switchValue.value = newValue
    }

    fun initializeSdk() {
        viewModelScope.launch {
            EngagementCloud.initialize()
        }
    }

    fun enableSdkAndLinkContact() {
        viewModelScope.launch {
            enableTracking()
            EngagementCloud.contact.link("test@test.com")
        }
    }

    fun disableSdk() {
        viewModelScope.launch {
            EngagementCloud.setup.disableTracking()
        }
    }

    fun unLinkContact() {
        viewModelScope.launch {
            EngagementCloud.contact.unlink()
        }
    }

    fun linkContact() {
        viewModelScope.launch {
            EngagementCloud.contact.link("test@test.com")
        }
    }

    fun trackCustomEvent() {
        val eventNameValue = _eventName.value
        if (eventNameValue.isNotBlank()) {
            viewModelScope.launch {
                EngagementCloud.event.track(CustomEvent(eventNameValue, null))
            }
        }
    }

    fun trackNavigateEvent(){
        viewModelScope.launch {
            EngagementCloud.event.track(
                NavigateEvent(
                    location = "https://www.sap.com"
                )
            )
        }
    }

    fun toggleInAppDnd(enabled: Boolean) {
        _switchValue.value = enabled
        viewModelScope.launch {
            if (enabled) {
                EngagementCloud.inApp.pause()
            } else {
                EngagementCloud.inApp.resume()
            }
        }
    }

    fun addInlineInAppView() {
        _inlineInAppViewCount.value++
    }
}
