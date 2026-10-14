package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.LocationCategory
import com.example.model.RoutePreference
import com.example.model.TransitLocation
import com.example.ui.components.LocationSearchDialog
import com.example.ui.components.TicketTimePickerDialog
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onNavigateToResults: () -> Unit,
    onNavigateToAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOriginDialog by remember { mutableStateOf(false) }
    var showDestDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedFamousCategory by remember { mutableStateOf(LocationCategory.ALL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Hero Card with Generated Illustration Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.goweave_hero),
                contentDescription = "GoWeave Multimodal Transit",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                TransitNavy.copy(alpha = 0.85f),
                                TransitNavy
                            ),
                            startY = 60f
                        )
                    )
            )

            // Hero Headlines
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TransitTeal.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, TransitTeal.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TransitTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI-POWERED MULTIMODAL PLATFORM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "One Destination. Every Connection.\nOne Smart Journey.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 26.sp
                )
                Text(
                    text = "Every Route, Woven Together across ${uiState.serviceAreaName}.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Journey Search Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plan Your Commute",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Combines BMTC bus, Namma Metro, Auto & walking in 1 tap",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Starting Location Box (Search Engine trigger)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOriginDialog = true }
                        .testTag("origin_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TransitTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TripOrigin,
                                contentDescription = null,
                                tint = TransitTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STARTING LOCATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = uiState.origin.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TransitNavy
                            )
                            Text(
                                text = "${uiState.origin.area} • Tap to search & recommend",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search starting location",
                            tint = TransitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Swap Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.swapOriginDestination() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .testTag("swap_locations_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap Locations",
                            tint = TransitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Destination Box (Search Engine trigger)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDestDialog = true }
                        .testTag("destination_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DESTINATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = uiState.destination.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TransitNavy
                            )
                            Text(
                                text = "${uiState.destination.area} • Tap to search & recommend",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search destination",
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ticket Booking Time & Route Goal Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Booking Time Selector Chip (Clickable for Ticket Time Dialog)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePickerDialog = true }
                            .testTag("departure_time_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BOOKING TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                Text(uiState.departureTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Time",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Preference Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = TransitTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("ROUTING GOAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(uiState.selectedPreference.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preference Buttons (Cheapest, Fastest, Best Balanced)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoutePreference.values().forEach { pref ->
                        val isSelected = uiState.selectedPreference == pref
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TransitNavy else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setPreference(pref) }
                        ) {
                            Text(
                                text = when (pref) {
                                    RoutePreference.CHEAPEST -> "Cheapest"
                                    RoutePreference.FASTEST -> "Fastest"
                                    RoutePreference.BEST_BALANCED -> "Balanced"
                                },
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // "Plan My Journey" CTA Button
                Button(
                    onClick = {
                        viewModel.searchRoutes()
                        onNavigateToResults()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("plan_my_journey_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = TransitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Plan My Journey",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Commute Fast-Track Section (Home to Work & Work to Home Ride Recommendations)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Commute Quick Rides",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )
                    Text(
                        text = "1-tap recommended routes between your saved locations",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Home to Work Recommendation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.planHomeToWorkCommute()
                        onNavigateToResults()
                    }
                    .testTag("home_to_work_commute_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏠", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Home → 💼 Work Morning Ride",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "08:30 AM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${uiState.defaultHome.name} → ${uiState.defaultWork.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Auto Feeder (8m) → Purple Line Metro (24m) → Covered Walk",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹65",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            Text(
                                text = " • 42 mins",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.planHomeToWorkCommute()
                                onNavigateToResults()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TransitTeal),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Book Morning Ride", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Work to Home Recommendation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.planWorkToHomeCommute()
                        onNavigateToResults()
                    }
                    .testTag("work_to_home_commute_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💼", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Work → 🏠 Home Evening Ride",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "06:15 PM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${uiState.defaultWork.name} → ${uiState.defaultHome.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Metro Express to ${uiState.defaultHome.area} → Auto Last-Mile (Road bypass)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹65",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            Text(
                                text = " • 45 mins",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.planWorkToHomeCommute()
                                onNavigateToResults()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Book Evening Ride", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Default Locations Quick Management Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SAVED DEFAULT COMMUTES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(
                            text = "Home: ${uiState.defaultHome.name.take(16)}... | Work: ${uiState.defaultWork.name.take(16)}...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitNavy
                        )
                    }

                    TextButton(onClick = { showOriginDialog = true }) {
                        Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitTeal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Suggestions to Famous Places in Bangalore (Parks, Malls, Hospitals, Tech Parks, Heritage)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Famous Places in Bangalore",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Text(
                text = "One-tap route planning to popular parks, shopping malls & hospitals",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocationCategory.values().forEach { cat ->
                    val isSelected = selectedFamousCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFamousCategory = cat },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cat.label, fontSize = 11.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TransitNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Famous Destination Cards Horizontal List
            val famousPlaces = remember(selectedFamousCategory, viewModel.allLocations) {
                viewModel.allLocations.filter { loc ->
                    selectedFamousCategory == LocationCategory.ALL || loc.category == selectedFamousCategory
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                famousPlaces.forEach { place ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable {
                                viewModel.setDestination(place)
                                viewModel.searchRoutes()
                                onNavigateToResults()
                            }
                            .testTag("famous_place_${place.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "${place.category.emoji} ${place.category.label}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TransitNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.DirectionsTransit,
                                    contentDescription = null,
                                    tint = TransitTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = place.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy,
                                maxLines = 1
                            )
                            Text(
                                text = "${place.area} • ${place.landmark}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 2,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.setDestination(place)
                                    viewModel.searchRoutes()
                                    onNavigateToResults()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Plan Route Here", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Commute Companion Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToAi() }
                .testTag("ai_companion_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
            border = BorderStroke(1.dp, TransitTeal.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(TransitTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Companion",
                        tint = TransitTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GoWeave AI Commute Advisor",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Ask questions, avoid peak crowds & get live transit advice",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TransitTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Core Platform Pillars Card
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Why GoWeave?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePillarCard(
                    title = "Unified Checkout",
                    desc = "Pay once for bus, metro & auto legs together.",
                    icon = Icons.Default.Payment,
                    modifier = Modifier.weight(1f)
                )
                FeaturePillarCard(
                    title = "Live Rerouting",
                    desc = "AI detects road delays and re-optimizes routes.",
                    icon = Icons.AutoMirrored.Filled.AltRoute,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Origin Location Search Engine Dialog with Autocomplete & Recommendations
    if (showOriginDialog) {
        LocationSearchDialog(
            title = "Search Starting Location",
            allLocations = viewModel.allLocations,
            onSelectLocation = {
                viewModel.setOrigin(it)
                showOriginDialog = false
            },
            onSetAsDefaultHome = {
                viewModel.setDefaultHomeLocation(it)
            },
            onSetAsDefaultWork = {
                viewModel.setDefaultWorkLocation(it)
            },
            onSetAsCustomDefault = { label, loc ->
                viewModel.setCustomDefaultLocation(label, loc)
            },
            onDismiss = { showOriginDialog = false }
        )
    }

    // Destination Location Search Engine Dialog with Autocomplete & Recommendations
    if (showDestDialog) {
        LocationSearchDialog(
            title = "Search Destination",
            allLocations = viewModel.allLocations,
            onSelectLocation = {
                viewModel.setDestination(it)
                showDestDialog = false
            },
            onSetAsDefaultHome = {
                viewModel.setDefaultHomeLocation(it)
            },
            onSetAsDefaultWork = {
                viewModel.setDefaultWorkLocation(it)
            },
            onSetAsCustomDefault = { label, loc ->
                viewModel.setCustomDefaultLocation(label, loc)
            },
            onDismiss = { showDestDialog = false }
        )
    }

    // Time Selection for Ticket Booking Dialog
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

@Composable
fun FeaturePillarCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TransitTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TransitTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 15.sp)
        }
    }
}
