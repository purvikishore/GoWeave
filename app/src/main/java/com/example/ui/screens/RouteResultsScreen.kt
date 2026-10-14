package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.LocationCategory
import com.example.model.RouteOption
import com.example.ui.components.RouteOptionsLazyList
import com.example.ui.components.TicketTimePickerDialog
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteResultsScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onBack: () -> Unit,
    onViewJourneyDetail: () -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Route Recommendations",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "${uiState.origin.area} → ${uiState.destination.area}",
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
                    // Time Selector Button
                    IconButton(
                        onClick = { showTimePickerDialog = true },
                        modifier = Modifier.testTag("results_time_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Change Booking Time",
                            tint = Color(0xFF2563EB)
                        )
                    }

                    // Traffic Disruption Simulation Toggle
                    IconButton(
                        onClick = { viewModel.toggleDisruptionSimulation() },
                        modifier = Modifier.testTag("disruption_simulation_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDisruptionSimulated) Icons.Default.Warning else Icons.Default.Traffic,
                            contentDescription = "Simulate Traffic Disruption",
                            tint = if (uiState.isDisruptionSimulated) WarningAmber else Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (uiState.selectedRoute != null) {
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
                                text = "Selected: ${uiState.selectedRoute.badgeTag}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitTeal
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹${uiState.selectedRoute.totalFare.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitNavy
                                )
                                Text(
                                    text = " • ${uiState.selectedRoute.totalDurationMinutes} mins",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onViewJourneyDetail,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TransitNavy),
                                modifier = Modifier.testTag("view_map_button")
                            ) {
                                Text(
                                    text = "Map & Details",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitNavy
                                )
                            }

                            Button(
                                onClick = onProceedToCheckout,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                                modifier = Modifier.testTag("checkout_selected_route_button")
                            ) {
                                Text(
                                    text = "Book Route",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Use our high-performance RouteOptionsLazyList component
        RouteOptionsLazyList(
            routes = uiState.routes,
            selectedRouteId = uiState.selectedRoute?.id,
            onSelectRoute = { viewModel.selectRoute(it) },
            onViewRouteDetails = { onViewJourneyDetail() },
            onBookRoute = { onProceedToCheckout() },
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            headerContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Live Simulation Notice Banner
                    if (uiState.isDisruptionSimulated) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(WarningAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Traffic Surge Detected (+14 min delay)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "GoWeave AI dynamically recalculated routes to avoid surface gridlock via Purple Line Metro.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }

                    // Booking Time & Famous Places Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.clickable { showTimePickerDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Booking: ${uiState.departureTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Text(
                            text = "Famous Places Suggestions:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Famous Bangalore Alternatives Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.allLocations
                            .filter { it.category != LocationCategory.TRANSIT }
                            .take(7)
                            .forEach { famous ->
                                val isDest = uiState.destination.id == famous.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDest) TransitTeal.copy(alpha = 0.15f) else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, if (isDest) TransitTeal else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable {
                                        viewModel.setDestination(famous)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(famous.category.emoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = famous.name.take(16),
                                            fontSize = 10.sp,
                                            fontWeight = if (isDest) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDest) TransitNavy else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                    }

                    // Quick Budget Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            null to "All Budgets",
                            50.0 to "Under ₹50",
                            100.0 to "Under ₹100",
                            300.0 to "Under ₹300"
                        ).forEach { (budget, label) ->
                            val isSelected = uiState.budgetFilter == budget
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setBudgetFilter(budget) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TransitNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Section Heading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Retrieved Route Options (${uiState.routes.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        if (uiState.selectedRoute != null) {
                            Text(
                                text = "Tap any card to compare",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            },
            footerContent = {
                // AI Route Comparison Insights Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GoWeave Recommendation Insight",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "The Best Balanced route provides 80% road congestion bypass via Purple Line metro, saving 2.4 kg of CO2 and ₹215 over direct cabs while maintaining covered pedestrian transfers.",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            modifier = modifier
        )
    }

    if (showTimePickerDialog) {
        TicketTimePickerDialog(
            currentTime = uiState.departureTime,
            onTimeSelected = { selectedTime ->
                viewModel.setDepartureTime(selectedTime)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

