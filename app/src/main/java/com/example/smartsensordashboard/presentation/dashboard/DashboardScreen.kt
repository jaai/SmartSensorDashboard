package com.example.smartsensordashboard.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val messages by viewModel.mqttMessages.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MQTT Sensor Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.publishMessage(input)
                    input = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publish")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Received Messages:")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(messages) { msg ->
                    Text("• $msg", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
