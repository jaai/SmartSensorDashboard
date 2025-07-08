package com.example.smartsensordashboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsensordashboard.data.mqtt.MqttManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mqttManager: MqttManager
) : ViewModel() {

    private val _mqttMessages = MutableStateFlow<List<String>>(emptyList())
    val mqttMessages: StateFlow<List<String>> = _mqttMessages

    private val topic = "test/sensor"

    init {
        connectToBroker()
        viewModelScope.launch {
            mqttManager.incomingMessages.collect { message ->
                appendMessage(message)
            }
        }
    }

    private fun connectToBroker() {
        mqttManager.connect(
            serverUri = "tcp://broker.hivemq.com:1883",
            onConnected = {
                mqttManager.subscribe(topic)
            },
            onFailure = {
                appendMessage("MQTT connection failed: ${it.message}")
            }
        )
    }

    fun publishMessage(message: String) {
        mqttManager.publish(topic, message)
    }

    private fun appendMessage(message: String) {
        _mqttMessages.value = _mqttMessages.value + message
    }
}
