package com.example.londonbuses.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.londonbuses.data.models.StopPoint
import com.example.londonbuses.ui.LondonBusesViewModel
import com.example.londonbuses.utils.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyStopsScreen(viewModel: LondonBusesViewModel) {
    val deviceLocation by viewModel.deviceLocation.collectAsState()
    val isNearbyLoading by viewModel.isNearbyLoading.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val nearbyError by viewModel.nearbyError.collectAsState()

    // Dialog state
    val selectedStop by viewModel.selectedStop.collectAsState()
    val selectedStopPredictions by viewModel.selectedStopPredictions.collectAsState()
    val isSelectedStopLoading by viewModel.isSelectedStopLoading.collectAsState()

    // Location Simulation State
    var showSimulator by remember { mutableStateOf(false) }
    var simLatText by remember { mutableStateOf(deviceLocation.latitude.toString()) }
    var simLonText by remember { mutableStateOf(deviceLocation.longitude.toString()) }

    // Sync input fields when location updates
    LaunchedEffect(deviceLocation) {
        simLatText = deviceLocation.latitude.toString()
        simLonText = deviceLocation.longitude.toString()
    }

    // Auto-fetch on first load if empty
    LaunchedEffect(Unit) {
        if (nearbyStops.isEmpty()) {
            viewModel.fetchNearbyStops()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Current Location Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Coordinates",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("Lat: %.5f, Lon: %.5f", deviceLocation.latitude, deviceLocation.longitude),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row {
                        IconButton(onClick = { showSimulator = !showSimulator }) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Simulate Location",
                                tint = if (showSimulator) Color(0xFFE11B22) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.fetchNearbyStops() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFFE11B22))
                        }
                    }
                }

                // GPS Fetch Button
                Button(
                    onClick = { viewModel.fetchLocation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("Refresh GPS Location", color = Color.White)
                }
            }
        }

        // Simulator controls drawer
        if (showSimulator) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Location Simulator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = simLatText,
                            onValueChange = { simLatText = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = simLonText,
                            onValueChange = { simLonText = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Preset locations inside London
                        TextButton(onClick = {
                            // Westminster
                            viewModel.setSimulatedLocation(51.5007, -0.1246)
                        }) {
                            Text("Westminster", color = Color(0xFFE11B22))
                        }
                        TextButton(onClick = {
                            // Kings Cross
                            viewModel.setSimulatedLocation(51.5309, -0.1233)
                        }) {
                            Text("Kings Cross", color = Color(0xFFE11B22))
                        }
                        Button(
                            onClick = {
                                val lat = simLatText.toDoubleOrNull()
                                val lon = simLonText.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    viewModel.setSimulatedLocation(lat, lon)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22))
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State feedback
        if (isNearbyLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE11B22))
            }
        } else if (nearbyError != null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = nearbyError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { viewModel.fetchNearbyStops() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22))
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else if (nearbyStops.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "No nearby bus stops found within 1000m of this location.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            // Stops List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nearbyStops) { stop ->
                    NearbyStopRow(
                        stop = stop,
                        deviceLat = deviceLocation.latitude,
                        deviceLon = deviceLocation.longitude,
                        onClick = { viewModel.selectStop(stop) }
                    )
                }
            }
        }

        // Show Predictions Dialog when a stop is selected
        if (selectedStop != null) {
            Dialog(onDismissRequest = { viewModel.selectStop(null) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedStop?.displayName ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!selectedStop?.stopLetter.isNullOrEmpty()) {
                                    Text(
                                        text = "Stop ${selectedStop?.stopLetter}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.selectStop(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "Live approaching buses & times:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (isSelectedStopLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFE11B22))
                            }
                        } else if (selectedStopPredictions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No approaching buses found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedStopPredictions.size) { idx ->
                                    val pred = selectedStopPredictions[idx]
                                    PredictionRow(prediction = pred)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyStopRow(
    stop: StopPoint,
    deviceLat: Double,
    deviceLon: Double,
    onClick: () -> Unit
) {
    val distance = LocationHelper.calculateDistanceMeters(
        deviceLat,
        deviceLon,
        stop.lat,
        stop.lon
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stop.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!stop.stopLetter.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = Color.LightGray) {
                            Text(stop.stopLetter ?: "", color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    val distStr = if (distance < 1000) {
                        "${distance.toInt()}m away"
                    } else {
                        String.format("%.1fkm away", distance / 1000.0)
                    }
                    Text(
                        text = distStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Click action indicator
            Box(
                modifier = Modifier
                    .background(Color(0xFFE11B22), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Arrivals",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
