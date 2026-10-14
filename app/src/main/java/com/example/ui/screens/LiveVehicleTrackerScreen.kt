package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.model.LiveVehicle
import com.example.model.TransportMode
import com.example.ui.components.LiveVehicleMapCanvas
import com.example.ui.components.getTransportModeDetails
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVehicleTrackerScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onBookVehicle: (LiveVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    var modeFilter by remember { mutableStateOf<TransportMode?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf<LiveVehicle?>(null) }

    val filteredVehicles = remember(uiState.liveVehicles, modeFilter, searchQuery) {
        uiState.liveVehicles.filter { v ->
            val matchesMode = modeFilter == null || v.mode == modeFilter
            val matchesSearch = searchQuery.isBlank() ||
                v.vehicleNumber.contains(searchQuery, ignoreCase = true) ||
                v.routeName.contains(searchQuery, ignoreCase = true) ||
                v.nextStop.contains(searchQuery, ignoreCase = true)
            matchesMode && matchesSearch
        }
    }

    LaunchedEffect(filteredVehicles) {
        if (selectedVehicle != null && filteredVehicles.none { it.id == selectedVehicle?.id }) {
            selectedVehicle = filteredVehicles.firstOrNull()
        } else if (selectedVehicle == null) {
            selectedVehicle = filteredVehicles.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Vehicle Tracker",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Real-time bus, cab & auto GPS locations",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE STREAM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search bus line # or cab plate...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_search_bar"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TransitTeal,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transport Mode Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    null to "All (${uiState.liveVehicles.size})",
                    TransportMode.BUS to "Buses",
                    TransportMode.CAB to "Cabs",
                    TransportMode.AUTO to "Autos"
                ).forEach { (mode, label) ->
                    val isSelected = modeFilter == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { modeFilter = mode },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TransitNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Live Vehicle Map Canvas
            LiveVehicleMapCanvas(
                vehicles = filteredVehicles,
                stations = uiState.locations,
                selectedVehicleId = selectedVehicle?.id,
                onSelectVehicle = { selectedVehicle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Vehicle Telematics Detail Card
            if (selectedVehicle != null) {
                val veh = selectedVehicle!!
                val (modeIcon, modeColor) = getTransportModeDetails(veh.mode)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_vehicle_detail_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, TransitTeal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(modeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        tint = modeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = veh.vehicleNumber,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TransitNavy
                                    )
                                    Text(
                                        text = veh.routeName,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = if (veh.mode == TransportMode.BUS) "SEATS AVAILABLE" else "READY TO DISPATCH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Telematics Row (Speed, Heading, ETA)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("SPEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text("${veh.speedKmh} km/h", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("NEXT STOP ETA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text("${veh.etaMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitTeal)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("EST. FARE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text("₹${veh.baseFare.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Next Stop: ${veh.nextStop}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        if (veh.driverName != null) {
                            Text(
                                text = "Assigned Driver: ${veh.driverName} (Rating: ★ ${veh.rating})",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onBookVehicle(veh) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("book_live_vehicle_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (veh.mode == TransportMode.BUS) Icons.Default.ConfirmationNumber else Icons.Default.LocalTaxi,
                                        contentDescription = null,
                                        tint = TransitTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (veh.mode == TransportMode.BUS) "Board / Pass (₹${veh.baseFare.toInt()})" else "Book Ride (₹${veh.baseFare.toInt()})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
