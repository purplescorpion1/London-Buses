package com.example.londonbuses.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import com.example.londonbuses.data.models.ArrivalPrediction
import com.example.londonbuses.data.models.StopPoint
import com.example.londonbuses.ui.LondonBusesViewModel
import com.example.londonbuses.utils.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreen(viewModel: LondonBusesViewModel) {
    val apiKey by viewModel.apiKey.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val lineRouteSequence by viewModel.lineRouteSequence.collectAsState()
    val lineArrivals by viewModel.lineArrivals.collectAsState()
    val deviceLocation by viewModel.deviceLocation.collectAsState()
    val lineStatus by viewModel.lineStatus.collectAsState()
    val isLineStatusLoading by viewModel.isLineStatusLoading.collectAsState()

    var selectedDirection by remember { mutableStateOf("outbound") }

    // Dialog State
    val selectedStop by viewModel.selectedStop.collectAsState()
    val selectedStopPredictions by viewModel.selectedStopPredictions.collectAsState()
    val isSelectedStopLoading by viewModel.isSelectedStopLoading.collectAsState()
    val stopDisruptions by viewModel.stopDisruptions.collectAsState()
    val selectedStopTimetable by viewModel.selectedStopTimetable.collectAsState()
    val isSelectedStopTimetableLoading by viewModel.isSelectedStopTimetableLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // API Key Warning Banner
        if (apiKey.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "No API Key configured. Please go to Settings to add your TfL API key to enable requests.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Search Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search Bus Route (e.g., 72, 14, 220)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Bus") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE11B22),
                    focusedLabelColor = Color(0xFFE11B22)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.searchBusRoute(searchQuery) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22)),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State Feedback
        if (isSearchLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE11B22))
            }
        } else if (searchError != null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = searchError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (lineRouteSequence == null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Enter a bus route number above to view all of its stops, expected arrival times, and nearest stops.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            // Display Route Sequence
            val seq = lineRouteSequence!!

            // Filter branch sequences matching direction
            val matchingSequences = seq.stopPointSequences.filter {
                it.direction.equals(selectedDirection, ignoreCase = true)
            }

            // Fallback: if no direct matching sequences, list whatever sequences we have
            val activeSequence = matchingSequences.firstOrNull() ?: seq.stopPointSequences.firstOrNull()

            val stops = activeSequence?.stopPoint ?: emptyList()

            // Find closest stop to device coordinates
            val closestStop = remember(stops, deviceLocation) {
                if (stops.isEmpty()) null
                else {
                    stops.minByOrNull { stop ->
                        LocationHelper.calculateDistanceMeters(
                            deviceLocation.latitude,
                            deviceLocation.longitude,
                            stop.lat,
                            stop.lon
                        )
                    }
                }
            }

            // Destination header name
            val destinationName = activeSequence?.stopPoint?.lastOrNull()?.displayName ?: "Unknown Destination"

            // Selector tabs for direction
            TabRow(
                selectedTabIndex = if (selectedDirection == "outbound") 0 else 1,
                contentColor = Color(0xFFE11B22),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Tab(
                    selected = selectedDirection == "outbound",
                    onClick = { selectedDirection = "outbound" },
                    text = { Text("Outbound") }
                )
                Tab(
                    selected = selectedDirection == "inbound",
                    onClick = { selectedDirection = "inbound" },
                    text = { Text("Inbound") }
                )
            }

            // Direction Destination Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Route ${seq.lineName.uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Towards: $destinationName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Line status badge
                        lineStatus?.lineStatuses?.firstOrNull()?.let { status ->
                            val severityDesc = status.statusSeverityDescription ?: "Unknown"
                            val isGoodService = severityDesc.contains("Good Service", ignoreCase = true)
                            Badge(
                                containerColor = if (isGoodService) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                contentColor = Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = severityDesc,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Display status reason if disrupted
                    lineStatus?.lineStatuses?.firstOrNull()?.reason?.let { reason ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Stops List along Route
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(stops) { index, stop ->
                    val isClosest = stop.id == closestStop?.id
                    val distance = LocationHelper.calculateDistanceMeters(
                        deviceLocation.latitude,
                        deviceLocation.longitude,
                        stop.lat,
                        stop.lon
                    )

                    // Get predicted arrivals for this stop
                    val predictions = lineArrivals[stop.id]?.sortedBy { it.timeToStation ?: Int.MAX_VALUE } ?: emptyList()

                    StopRow(
                        stop = stop,
                        index = index + 1,
                        isClosest = isClosest,
                        distanceMeters = distance,
                        predictions = predictions,
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

                        // Active stop disruptions/closures
                        if (stopDisruptions.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "⚠️ Active Stop Warning:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    stopDisruptions.forEach { disruption ->
                                        Text(
                                            text = disruption.description ?: "Stop disruption active.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "All buses arriving at this stop:",
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
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "No live approaching buses found.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                val scheduleList = selectedStopTimetable?.timetable?.routes?.flatMap { route ->
                                    route.schedules.flatMap { schedule ->
                                        schedule.knownJourneys.map { it.displayTime }
                                    }
                                }?.distinct()?.sorted() ?: emptyList()

                                if (isSelectedStopTimetableLoading) {
                                    CircularProgressIndicator(color = Color(0xFFE11B22), modifier = Modifier.align(Alignment.CenterHorizontally))
                                } else if (scheduleList.isNotEmpty()) {
                                    Text(
                                        text = "Scheduled Timetable Fallback:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE11B22),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Text(
                                        text = "Bus ${selectedStopTimetable?.lineName?.uppercase() ?: ""} is scheduled to arrive at: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 150.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(scheduleList.size) { idx ->
                                            val time = scheduleList[idx]
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Scheduled Departure: $time",
                                                    modifier = Modifier.padding(8.dp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("No scheduled timetable found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
fun StopRow(
    stop: StopPoint,
    index: Int,
    isClosest: Boolean,
    distanceMeters: Double,
    predictions: List<ArrivalPrediction>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isClosest) {
                Color(0xFFFFF0F1) // Soft red background for closest stop
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isClosest) {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE11B22)))
        } else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Index
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isClosest) Color(0xFFE11B22) else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Stop details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stop.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isClosest) Color(0xFFB00F14) else MaterialTheme.colorScheme.onSurface
                        )
                        if (!stop.stopLetter.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = Color.LightGray) {
                                Text(stop.stopLetter ?: "", color = Color.Black)
                            }
                        }
                    }

                    // Distance
                    val distStr = if (distanceMeters < 1000) {
                        "${distanceMeters.toInt()}m away"
                    } else {
                        String.format("%.1fkm away", distanceMeters / 1000.0)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isClosest) Color(0xFFE11B22) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = distStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isClosest) {
                    Badge(containerColor = Color(0xFFE11B22)) {
                        Text("NEAREST", color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }

            // Expected Times display
            if (predictions.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live arrivals: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11B22)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = predictions.take(3).joinToString { it.displayArrival },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionRow(prediction: ArrivalPrediction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Bus number badge
            Box(
                modifier = Modifier
                    .background(Color(0xFFE11B22), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prediction.lineName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "To: ${prediction.destinationName ?: "Unknown Destination"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!prediction.towards.isNullOrEmpty()) {
                    Text(
                        text = "Towards: ${prediction.towards}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = prediction.displayArrival,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE11B22)
        )
    }
}
