package com.example.londonbuses.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.londonbuses.data.models.Journey
import com.example.londonbuses.data.models.JourneyLeg
import com.example.londonbuses.ui.LondonBusesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyPlannerScreen(viewModel: LondonBusesViewModel) {
    val apiKey by viewModel.apiKey.collectAsState()
    val fromQuery by viewModel.journeyFromQuery.collectAsState()
    val toQuery by viewModel.journeyToQuery.collectAsState()
    val isJourneyLoading by viewModel.isJourneyLoading.collectAsState()
    val journeyResults by viewModel.journeyResults.collectAsState()
    val journeyError by viewModel.journeyError.collectAsState()

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

        // Inputs Section
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Plan Bus Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11B22)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Origin Input
                OutlinedTextField(
                    value = fromQuery,
                    onValueChange = { viewModel.updateJourneyFromQuery(it) },
                    label = { Text("From (e.g., SW1A 1AA, Trafalgar Square)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Origin") },
                    trailingIcon = {
                        if (fromQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateJourneyFromQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE11B22),
                        focusedLabelColor = Color(0xFFE11B22)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Destination Input
                OutlinedTextField(
                    value = toQuery,
                    onValueChange = { viewModel.updateJourneyToQuery(it) },
                    label = { Text("To (e.g., Kings Cross, Westminster)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Destination") },
                    trailingIcon = {
                        if (toQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateJourneyToQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE11B22),
                        focusedLabelColor = Color(0xFFE11B22)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Button
                Button(
                    onClick = { viewModel.searchJourney() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11B22)),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fromQuery.isNotEmpty() && toQuery.isNotEmpty() && !isJourneyLoading
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Journeys")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State Feedback / Journey List
        if (isJourneyLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE11B22))
            }
        } else if (journeyError != null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = journeyError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (journeyResults == null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Enter your starting point and destination to find the best London Bus routes and step-by-step directions.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            val list = journeyResults?.journeys ?: emptyList()
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { journey ->
                    JourneyCard(journey = journey)
                }
            }
        }
    }
}

@Composable
fun JourneyCard(journey: Journey) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Duration and times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Duration: ${journey.duration ?: 0} mins",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11B22)
                    )
                    Text(
                        text = "Depart: ${journey.startDateTime?.substringAfter("T")?.substring(0, 5) ?: ""} - Arrive: ${journey.arrivalDateTime?.substringAfter("T")?.substring(0, 5) ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Badge(containerColor = Color(0xFFE11B22).copy(alpha = 0.1f)) {
                    Text(
                        text = "Bus Connection",
                        color = Color(0xFFE11B22),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Leg details
            journey.legs.forEachIndexed { index, leg ->
                LegRow(leg = leg)
                if (index < journey.legs.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LegRow(leg: JourneyLeg) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFE11B22), shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val modeLabel = leg.mode?.id?.uppercase() ?: leg.mode?.name?.uppercase() ?: "WALK"
            Text(
                text = modeLabel,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leg.instruction?.summary ?: "Take connection",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "From: ${leg.departurePoint?.commonName ?: ""} to ${leg.arrivalPoint?.commonName ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (leg.duration != null) {
                Text(
                    text = "Duration: ${leg.duration} mins",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE11B22),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
