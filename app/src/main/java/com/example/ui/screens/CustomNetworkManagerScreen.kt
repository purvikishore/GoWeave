package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
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
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomNetworkManagerScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    modifier: Modifier = Modifier
) {
    var serviceAreaInput by remember { mutableStateOf(uiState.serviceAreaName) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showAddStationDialog by remember { mutableStateOf(false) }
    var showAddLineDialog by remember { mutableStateOf(false) }
    var batchInputText by remember { mutableStateOf("") }
    var showBatchSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Custom Service Area Manager",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Upload & manage your local routes and stations",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_all_data_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Mock Data",
                            tint = DangerRed
                        )
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
            // Service Area Title Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ACTIVE SERVICE AREA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.serviceAreaName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${uiState.locations.size} Stations • ${uiState.customLines.size} Lines active across network",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAddStationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TransitTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_station_button")
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Station", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showAddLineDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_line_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Route Line", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Fast Reset / Starter Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DangerRed)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Mock Data", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.loadStarterLocalTemplate("My City District") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TransitNavy)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TransitNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Starter Set", color = TransitNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Batch Text / Paste Upload Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Batch Upload Stations & Routes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )
                    Text(
                        text = "Paste text lines or JSON representing your local stops and lines",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = batchInputText,
                        onValueChange = { batchInputText = it },
                        placeholder = {
                            Text(
                                "Example:\nSTATION: Main Street, Downtown, 12.97, 77.59\nSTATION: West Avenue, West Sector, 12.95, 77.58\nBUS: Route 10, Downtown Express, Main Street, West Avenue\nMETRO: Red Line, Central Metro, Main Street, West Avenue",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("batch_upload_text_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TransitTeal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                batchInputText = "STATION: City Center Station, Central, 12.971, 77.594\nSTATION: North Park Terminal, North, 12.985, 77.605\nSTATION: South Tech Corridor, South, 12.935, 77.618\nBUS: Bus 12, Crosstown Link, City Center Station, South Tech Corridor\nMETRO: Blue Line, Express Transit, City Center Station, North Park Terminal"
                            }
                        ) {
                            Text("Insert Sample Template", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (batchInputText.isNotBlank()) {
                                    val (sCount, lCount) = viewModel.importFromBatchText(batchInputText)
                                    showBatchSuccessMessage = "Uploaded $sCount stations and $lCount route lines!"
                                    batchInputText = ""
                                }
                            },
                            enabled = batchInputText.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                            modifier = Modifier.testTag("submit_batch_upload_button")
                        ) {
                            Text("Import All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (showBatchSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = showBatchSuccessMessage!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Configured Stations List
            Text(
                text = "Active Stations & Stops (${uiState.locations.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.locations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No stations configured yet. Click '+ Station' or use batch upload above.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                uiState.locations.forEach { loc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(TransitTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = TransitTeal, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(loc.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                                    Text("${loc.area} • ${loc.landmark}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }

                            IconButton(onClick = { viewModel.removeStation(loc.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Configured Lines List
            Text(
                text = "Active Transit Lines (${uiState.customLines.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.customLines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No route lines configured yet. Click '+ Route Line' to add buses or metro.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                uiState.customLines.forEach { line ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (line.mode == TransportMode.METRO) MetroPurple else BusOrange
                            ) {
                                Text(
                                    text = line.lineCode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.lineName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                                Text("${line.mode.displayName} • Every ${line.frequencyMinutes}m • Base ₹${line.baseFare.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Clearing Data
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Transport Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will wipe the mock data so you can upload your real local bus routes, metro lines, and station names.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearMockTransportData("My Local City")
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Clear All Data", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive Dialog: Add Station
    if (showAddStationDialog) {
        var sName by remember { mutableStateOf("") }
        var sArea by remember { mutableStateOf("") }
        var sLandmark by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStationDialog = false },
            title = { Text("Add New Station / Stop", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text("Station / Stop Name") },
                        placeholder = { Text("e.g. City Hall Plaza") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sArea,
                        onValueChange = { sArea = it },
                        label = { Text("District / Neighborhood") },
                        placeholder = { Text("e.g. Downtown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sLandmark,
                        onValueChange = { sLandmark = it },
                        label = { Text("Landmark / Reference") },
                        placeholder = { Text("e.g. Near Civic Center") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sName.isNotBlank()) {
                            viewModel.addNewStation(
                                name = sName,
                                area = sArea.ifBlank { "Local District" },
                                landmark = sLandmark.ifBlank { "Main Stop" }
                            )
                            showAddStationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                ) {
                    Text("Save Station")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Interactive Dialog: Add Route Line
    if (showAddLineDialog) {
        var lCode by remember { mutableStateOf("") }
        var lName by remember { mutableStateOf("") }
        var isMetro by remember { mutableStateOf(false) }
        var fareStr by remember { mutableStateOf("20") }

        AlertDialog(
            onDismissRequest = { showAddLineDialog = false },
            title = { Text("Add Bus or Metro Route", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = lCode,
                        onValueChange = { lCode = it },
                        label = { Text("Line Code / Number") },
                        placeholder = { Text("e.g. Route 42 or Blue Line") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lName,
                        onValueChange = { lName = it },
                        label = { Text("Line Name / Description") },
                        placeholder = { Text("e.g. Downtown Express") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isMetro,
                            onClick = { isMetro = false },
                            label = { Text("Bus Route") }
                        )
                        FilterChip(
                            selected = isMetro,
                            onClick = { isMetro = true },
                            label = { Text("Metro Train") }
                        )
                    }
                    OutlinedTextField(
                        value = fareStr,
                        onValueChange = { fareStr = it },
                        label = { Text("Base Fare (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (lCode.isNotBlank()) {
                            viewModel.addNewLine(
                                code = lCode,
                                name = lName.ifBlank { "$lCode Service" },
                                mode = if (isMetro) TransportMode.METRO else TransportMode.BUS,
                                fare = fareStr.toDoubleOrNull() ?: 20.0
                            )
                            showAddLineDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                ) {
                    Text("Save Route Line")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLineDialog = false }) { Text("Cancel") }
            }
        )
    }
}
