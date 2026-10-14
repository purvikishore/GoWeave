package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransportMode
import com.example.ui.components.SegmentTimelineItem
import com.example.ui.components.TransitMapCanvas
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyDetailScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onBack: () -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val route = uiState.selectedRoute ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = route.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "${route.totalDurationMinutes} mins • ₹${route.totalFare.toInt()} total fare",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TransitNavy
                        )
                    }
                },
                actions = {
                    // Navigation simulation trigger
                    FilledTonalButton(
                        onClick = {
                            if (uiState.isLiveNavActive) viewModel.stopNavigation() else viewModel.startNavigation()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (uiState.isLiveNavActive) WarningAmber.copy(alpha = 0.2f) else TransitTeal.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isLiveNavActive) Icons.Default.Pause else Icons.Default.Navigation,
                                contentDescription = null,
                                tint = if (uiState.isLiveNavActive) WarningAmber else TransitNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isLiveNavActive) "Stop" else "Navigate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Unified Single Fare",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${route.totalFare.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            Text(
                                text = " all ${route.segments.size} segments",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onProceedToCheckout,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("proceed_to_checkout_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Unified Checkout",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Interactive Transit Canvas Map
            TransitMapCanvas(
                route = route,
                activeSegmentIndex = uiState.activeNavSegmentIndex,
                isNavigationActive = uiState.isLiveNavActive
            )

            // Live Navigation Tracker Bar
            if (uiState.isLiveNavActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val activeSegment = route.segments.getOrNull(uiState.activeNavSegmentIndex)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT LEG (${uiState.activeNavSegmentIndex + 1}/${route.segments.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitTeal,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = activeSegment?.routeLabel ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Head towards: ${activeSegment?.toStation}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = { viewModel.nextNavSegment() },
                            colors = ButtonDefaults.buttonColors(containerColor = TransitTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (uiState.activeNavSegmentIndex < route.segments.size - 1) "Next Leg" else "Finish",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Eco & Health Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CO2 SAVED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            Text("${route.carbonSavedKg} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = CabBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CALORIES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            Text("${route.caloriesBurned} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFAF5FF),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MetroPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("RELIABILITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                            Text("${route.reliabilityScorePercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF581C87))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step-by-Step Directions Breakdown
            Text(
                text = "Step-by-Step Multimodal Directions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Text(
                text = "Includes transfer buffers and pedestrian walkways",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            route.segments.forEachIndexed { index, seg ->
                SegmentTimelineItem(
                    segment = seg,
                    isFirst = index == 0,
                    isLast = index == route.segments.size - 1
                )
            }
        }
    }
}
