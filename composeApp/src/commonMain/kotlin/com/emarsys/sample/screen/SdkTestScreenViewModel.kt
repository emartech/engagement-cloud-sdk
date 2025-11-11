package com.emarsys.sample.screen

import com.emarsys.Emarsys
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.sample.enableTracking
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

    fun updateEventName(newValue: String) {
        _eventName.value = newValue
    }

    fun updateSwitchValue(newValue: Boolean) {
        _switchValue.value = newValue
    }

    fun initializeSdk() {
        viewModelScope.launch {
            Emarsys.initialize()
        }
    }

    fun enableSdkAndLinkContact() {
        viewModelScope.launch {
            enableTracking()
            Emarsys.contact.link("test@test.com")
        }
    }

    fun trackCustomEvent() {
        val eventNameValue = _eventName.value
        if (eventNameValue.isNotBlank()) {
            viewModelScope.launch {
                Emarsys.event.track(CustomEvent(eventNameValue, null))
            }
        }
    }

    fun toggleInAppDnd(enabled: Boolean) {
        _switchValue.value = enabled
        viewModelScope.launch {
            if (enabled) {
                Emarsys.inApp.pause()
            } else {
                Emarsys.inApp.resume()
            }
        }
    }
}
