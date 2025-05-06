package com.caresync.healthtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caresync.healthtracker.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(viewModel: HealthViewModel = hiltViewModel()) {
  val healthData by viewModel.healthData.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()
  val anxietyScore by viewModel.anxietyScore.collectAsStateWithLifecycle()
  val anxietyLevel by viewModel.anxietyLevel.collectAsStateWithLifecycle()

  // Check permissions when screen loads
  LaunchedEffect(Unit) {
    viewModel.checkPermissions()
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Health Tracker",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp)
      )

      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.padding(16.dp)
        )
      }

      error?.let {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
          Text(
            text = it,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
          )
        }
      }

      healthData?.let { data ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = "Your Health Summary",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Heart Rate")
              Text(
                text = "${data.heartRate?.toInt() ?: "--"} bpm",
                fontWeight = FontWeight.Medium
              )
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Steps Today")
              Text(
                text = "${data.steps ?: "--"}",
                fontWeight = FontWeight.Medium
              )
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Sleep Hours")
              Text(
                text = "${data.sleepHours ?: "--"} hours",
                fontWeight = FontWeight.Medium
              )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            anxietyScore?.let { score ->
              Column(
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Anxiety Level")
                  Text(
                    text = anxietyLevel ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                    color = getAnxietyColor(score)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                  progress = score.toFloat(),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                  color = getAnxietyColor(score),
                  trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
              }
            }
          }
        }
      } ?: run {
        // No data collected yet
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text(
            text = "No health data collected yet. Tap the button below to collect data.",
            modifier = Modifier.padding(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = { viewModel.collectHealthData() },
        modifier = Modifier.fillMaxWidth(0.8f)
      ) {
        Text(if (healthData != null) "Refresh Health Data" else "Collect Health Data")
      }
    }
  }
}

@Composable
fun getAnxietyColor(score: Double): Color {
  return when {
    score < 0.2 -> Color(0xFF00C853) // Green
    score < 0.4 -> Color(0xFFAEEA00) // Light Green
    score < 0.6 -> Color(0xFFFFD600) // Yellow
    score < 0.8 -> Color(0xFFFF9100) // Orange
    else -> Color(0xFFD50000) // Red
  }
}