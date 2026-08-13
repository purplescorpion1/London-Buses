package com.example.londonbuses.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.londonbuses.ui.LondonBusesViewModel

@Composable
fun SettingsScreen(viewModel: LondonBusesViewModel) {
    val savedApiKey by viewModel.apiKey.collectAsState()
    var apiKeyInput by remember { mutableStateOf(savedApiKey) }
    var isSavedSuccessfully by remember { mutableStateOf(false) }

    // Sync input when saved api key changes
    LaunchedEffect(savedApiKey) {
        apiKeyInput = savedApiKey
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "API Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE11B22)
        )

        Text(
            text = "To access London bus timetables, routes, and live arrival times, configure your Transport for London (TfL) Developer credentials below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // API Key Input
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = {
                apiKeyInput = it
                isSavedSuccessfully = false
            },
            label = { Text("TfL API Key (app_key)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFE11B22)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE11B22),
                focusedLabelColor = Color(0xFFE11B22)
            )
        )

        // Save Button
        Button(
            onClick = {
                viewModel.saveApiKey(apiKeyInput)
                isSavedSuccessfully = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Key", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Success Feedback
        if (isSavedSuccessfully) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "API Key saved successfully on your device!",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Help Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE11B22))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to get a TfL API Key?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "1. Register an account on the TfL Developer Portal (https://api-portal.tfl.gov.uk).\n" +
                            "2. Subscribe to the '500 Requests per min' product tier.\n" +
                            "3. Copy either your Primary Key or Secondary Key from your Profile page.\n" +
                            "4. Paste it into the input field above and click 'Save Key'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

            }
        }
    }
}
