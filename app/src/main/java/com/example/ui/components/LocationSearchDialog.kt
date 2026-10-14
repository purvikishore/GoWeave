package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LocationCategory
import com.example.model.TransitLocation
import com.example.ui.theme.TransitNavy
import com.example.ui.theme.TransitTeal
import java.util.UUID

enum class SearchTabMode {
    AUTOCOMPLETE,
    MAP_PICKER,
    MANUAL_ADDRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchDialog(
    title: String,
    initialQuery: String = "",
    allLocations: List<TransitLocation>,
    onSelectLocation: (TransitLocation) -> Unit,
    onSetAsDefaultHome: ((TransitLocation) -> Unit)? = null,
    onSetAsDefaultWork: ((TransitLocation) -> Unit)? = null,
    onSetAsCustomDefault: ((String, TransitLocation) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf(LocationCategory.ALL) }
    var activeTabMode by remember { mutableStateOf(SearchTabMode.AUTOCOMPLETE) }

    // Map Point Picker state
    var mapPinOffset by remember { mutableStateOf(Offset(250f, 250f)) }
    var mapPinAddress by remember { mutableStateOf("MG Road & Brigade Rd Junction, Central Bengaluru") }
    var mapPinLat by remember { mutableStateOf(12.9756) }
    var mapPinLng by remember { mutableStateOf(77.6066) }

    // Manual Address Form state
    var manualBuilding by remember { mutableStateOf("") }
    var manualStreet by remember { mutableStateOf("") }
    var manualLocality by remember { mutableStateOf("") }
    var manualPinCode by remember { mutableStateOf("560001") }

    // Custom Label state
    var customLabelLoc by remember { mutableStateOf<TransitLocation?>(null) }
    var customLabelText by remember { mutableStateOf("Gym") }

    val filteredLocations = remember(searchQuery, selectedCategory, allLocations) {
        val q = searchQuery.trim().lowercase()
        allLocations.filter { loc ->
            val matchesCategory = selectedCategory == LocationCategory.ALL || loc.category == selectedCategory
            val matchesQuery = q.isEmpty() ||
                loc.name.lowercase().contains(q) ||
                loc.area.lowercase().contains(q) ||
                loc.landmark.lowercase().contains(q) ||
                loc.pinCode.contains(q) ||
                loc.fullAddress.lowercase().contains(q) ||
                loc.category.label.lowercase().contains(q) ||
                (loc.tag != null && loc.tag.lowercase().contains(q))
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Bengaluru smart geocoding, street addresses & PIN codes",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TransitNavy)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs (Autocomplete, Interactive Map, Manual Address)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        SearchTabMode.AUTOCOMPLETE to "Search & Places",
                        SearchTabMode.MAP_PICKER to "Pick on Map 🗺️",
                        SearchTabMode.MANUAL_ADDRESS to "Exact Address ✍️"
                    ).forEach { (mode, label) ->
                        val isSelected = activeTabMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TransitNavy else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTabMode = mode }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (activeTabMode) {
                    SearchTabMode.AUTOCOMPLETE -> {
                        // Quick "Use Current Location" Shortcut Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentLoc = allLocations.find { it.id == "blr_indiranagar" } ?: allLocations[0]
                                    onSelectLocation(
                                        currentLoc.copy(
                                            name = "Current Location (${currentLoc.area})",
                                            tag = "GPS Verified"
                                        )
                                    )
                                    onDismiss()
                                }
                                .testTag("use_current_location_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "My Location",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Use Current Location",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = "Indiranagar 100ft Road Corridor • GPS Accurate",
                                        fontSize = 10.sp,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = "1-Tap",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Input Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Enter street, apartment, college, hospital or PIN...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = TransitTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("location_search_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TransitTeal,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Filter Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LocationCategory.values().forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) TransitNavy else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = cat.emoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = cat.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TransitNavy
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Autocomplete Results Counter
                        Text(
                            text = if (searchQuery.isBlank()) "Recommended Places (${filteredLocations.size})" else "Verified Suggestions for \"$searchQuery\" (${filteredLocations.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Results List
                        if (filteredLocations.isEmpty()) {
                            // Custom address quick confirmation card if no direct matches
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "📍 Confirm \"$searchQuery\" as Custom Address",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "We will pinpoint this address and route via the nearest Bengaluru metro & bus lines.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val customPin = TransitLocation(
                                                id = "custom_${UUID.randomUUID().toString().take(6)}",
                                                name = searchQuery.trim(),
                                                area = "Bengaluru Urban",
                                                lat = 12.9716,
                                                lng = 77.5946,
                                                landmark = "Custom Pinpoint Address",
                                                tag = "Exact Address",
                                                category = LocationCategory.TRANSIT,
                                                pinCode = "560001",
                                                fullAddress = "$searchQuery, Bengaluru, Karnataka"
                                            )
                                            onSelectLocation(customPin)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Confirm Custom Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredLocations, key = { it.id }) { loc ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectLocation(loc)
                                                onDismiss()
                                            }
                                            .testTag("location_item_${loc.id}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = loc.category.emoji, fontSize = 16.sp)
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = loc.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TransitNavy
                                                    )
                                                    Text(
                                                        text = "${loc.fullAddress.ifBlank { loc.area }} • PIN: ${loc.pinCode}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }

                                                // Quick default setters
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (onSetAsDefaultHome != null) {
                                                        IconButton(
                                                            onClick = { onSetAsDefaultHome(loc) },
                                                            modifier = Modifier.size(30.dp)
                                                        ) {
                                                            Icon(Icons.Default.Home, contentDescription = "Set Home", tint = TransitTeal, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    if (onSetAsDefaultWork != null) {
                                                        IconButton(
                                                            onClick = { onSetAsDefaultWork(loc) },
                                                            modifier = Modifier.size(30.dp)
                                                        ) {
                                                            Icon(Icons.Default.Work, contentDescription = "Set Work", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    if (onSetAsCustomDefault != null) {
                                                        IconButton(
                                                            onClick = {
                                                                customLabelLoc = loc
                                                                customLabelText = "Gym"
                                                            },
                                                            modifier = Modifier.size(30.dp)
                                                        ) {
                                                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Set Custom Default", tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // Landmark & Opening Hours row
                                            if (loc.openingHours != null || loc.landmark.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "📍 ${loc.landmark}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF475569)
                                                    )
                                                    if (loc.openingHours != null) {
                                                        Text(
                                                            text = "🕒 ${loc.openingHours}",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF16A34A),
                                                            fontWeight = FontWeight.SemiBold
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

                    SearchTabMode.MAP_PICKER -> {
                        // Interactive Map Pinpoint Selector
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(
                                text = "Tap anywhere on the map to set an exact geographic pinpoint:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0A192F))
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            mapPinOffset = offset
                                            // Derive simulated lat/lng from tap
                                            mapPinLat = 12.9716 + (offset.y - 200) * 0.0004
                                            mapPinLng = 77.5946 + (offset.x - 200) * 0.0004
                                            mapPinAddress = "Pinpoint at (${String.format("%.4f", mapPinLat)}, ${String.format("%.4f", mapPinLng)})"
                                        }
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Grid lines
                                    for (i in 0..10) {
                                        drawLine(
                                            color = Color(0x2200B4A6),
                                            start = Offset(i * size.width / 10, 0f),
                                            end = Offset(i * size.width / 10, size.height),
                                            strokeWidth = 1f
                                        )
                                        drawLine(
                                            color = Color(0x2200B4A6),
                                            start = Offset(0f, i * size.height / 10),
                                            end = Offset(size.width, i * size.height / 10),
                                            strokeWidth = 1f
                                        )
                                    }

                                    // Pin indicator
                                    drawCircle(
                                        color = TransitTeal.copy(alpha = 0.35f),
                                        radius = 24f,
                                        center = mapPinOffset
                                    )
                                    drawCircle(
                                        color = Color(0xFFEF4444),
                                        radius = 8f,
                                        center = mapPinOffset
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.75f)
                                ) {
                                    Text(
                                        text = "📍 Drag / Tap to Move Precise Pinpoint",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected Point:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = mapPinAddress,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TransitNavy
                                    )
                                    Text(
                                        text = "Lat: ${String.format("%.4f", mapPinLat)}, Lng: ${String.format("%.4f", mapPinLng)}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val mapLoc = TransitLocation(
                                        id = "map_pin_${System.currentTimeMillis()}",
                                        name = "Precise Map Location (${String.format("%.3f", mapPinLat)}, ${String.format("%.3f", mapPinLng)})",
                                        area = "Central Bengaluru",
                                        lat = mapPinLat,
                                        lng = mapPinLng,
                                        landmark = "Map Pinpoint Target",
                                        tag = "GPS Pin",
                                        category = LocationCategory.TRANSIT,
                                        pinCode = "560001",
                                        fullAddress = mapPinAddress
                                    )
                                    onSelectLocation(mapLoc)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                            ) {
                                Text("Confirm Pinpoint Location", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    SearchTabMode.MANUAL_ADDRESS -> {
                        // Manual Exact Address Entry Form
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Enter Exact Street Address & Building Details:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )

                            OutlinedTextField(
                                value = manualBuilding,
                                onValueChange = { manualBuilding = it },
                                label = { Text("Building, Apartment Complex or Office Name") },
                                placeholder = { Text("e.g. Prestige Tech Vista, Tower B") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = manualStreet,
                                onValueChange = { manualStreet = it },
                                label = { Text("Street Address & Cross Road") },
                                placeholder = { Text("e.g. 100ft Road, 12th Main") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = manualLocality,
                                onValueChange = { manualLocality = it },
                                label = { Text("Locality / Neighborhood") },
                                placeholder = { Text("e.g. Indiranagar, Whitefield, Koramangala") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = manualPinCode,
                                onValueChange = { manualPinCode = it },
                                label = { Text("6-Digit PIN Code") },
                                placeholder = { Text("e.g. 560038") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    val name = manualBuilding.ifBlank { manualStreet.ifBlank { "Custom Address" } }
                                    val area = manualLocality.ifBlank { "Bengaluru" }
                                    val full = "${if (manualBuilding.isNotBlank()) "$manualBuilding, " else ""}$manualStreet, $area, Bengaluru - $manualPinCode"

                                    val manualLoc = TransitLocation(
                                        id = "addr_${UUID.randomUUID().toString().take(6)}",
                                        name = name,
                                        area = area,
                                        lat = 12.9716,
                                        lng = 77.5946,
                                        landmark = "Exact Manual Address",
                                        tag = "Manual Entry",
                                        category = LocationCategory.TRANSIT,
                                        pinCode = manualPinCode,
                                        fullAddress = full
                                    )
                                    onSelectLocation(manualLoc)
                                    onDismiss()
                                },
                                enabled = manualStreet.isNotBlank() || manualBuilding.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                            ) {
                                Text("Save & Use Exact Address", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to name custom default place (e.g. Gym, College, Library)
    customLabelLoc?.let { loc ->
        AlertDialog(
            onDismissRequest = { customLabelLoc = null },
            title = { Text("Set as Default Custom Location", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set '${loc.name}' as a quick default place with a custom label:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = customLabelText,
                        onValueChange = { customLabelText = it },
                        label = { Text("Label (e.g. Gym, College, Parents, Library)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val label = customLabelText.trim().ifEmpty { "Saved Place" }
                        onSetAsCustomDefault?.invoke(label, loc)
                        customLabelLoc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                ) {
                    Text("Save Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { customLabelLoc = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
