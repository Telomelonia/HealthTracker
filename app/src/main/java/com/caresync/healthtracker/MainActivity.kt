package com.caresync.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthTrackerTheme {
                HealthScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen() {
    var isDataCollected by remember { mutableStateOf(false) }
    var healthInfo by remember { mutableStateOf("No data collected yet") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Health Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = healthInfo,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Simulate collecting data
                isDataCollected = true
                healthInfo = """
                    Heart Rate: 72 bpm
                    Steps: 5,247
                    Sleep: 7.5 hours
                """.trimIndent()
            }) {
                Text(if (isDataCollected) "Refresh Data" else "Collect Health Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // For now, just show a message
                healthInfo = "Data synced successfully!"
            }) {
                Text("Sync with Backend")
            }
        }
    }
}