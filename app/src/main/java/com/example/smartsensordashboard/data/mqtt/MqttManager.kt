package com.example.smartsensordashboard.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class MqttManager @Inject constructor(
    private val context: Context
) {

    private var mqttClient: MqttAndroidClient? = null

    private val _incomingMessages = MutableSharedFlow<String>()
    val incomingMessages = _incomingMessages.asSharedFlow()

    fun connect(
        serverUri: String,
        clientId: String = MqttClient.generateClientId(),
        onConnected: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        mqttClient = MqttAndroidClient(context, serverUri, clientId).apply {
            setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection lost", cause)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val msg = "From [$topic]: ${message.toString()}"
                    Log.d("MQTT", msg)
                    _incomingMessages.tryEmit(msg)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "Delivery complete")
                }
            })

            val options = MqttConnectOptions().apply {
                isCleanSession = true
            }

            connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connected to broker")
                    onConnected()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Failed to connect", exception)
                    onFailure(exception ?: Exception("Unknown error"))
                }
            })
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "Subscribed to $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Failed to subscribe $topic", exception)
            }
        })
    }

    fun publish(topic: String, message: String, qos: Int = 1, retained: Boolean = false) {
        val mqttMessage = MqttMessage().apply {
            payload = message.toByteArray()
            this.qos = qos
            isRetained = retained
        }

        mqttClient?.publish(topic, mqttMessage)
    }

    fun disconnect() {
        mqttClient?.disconnect()
    }
}
