package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.LocationCategory
import com.example.model.TransitLocation
import com.example.ui.theme.TransitNavy
import com.example.ui.theme.TransitTeal
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BengaluruPlacesDiscoveryScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onPlanRouteToPlace: (TransitLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LocationCategory.ALL) }
    var selectedLocality by remember { mutableStateOf("All Localities") }

    val localities = listOf(
        "All Localities",
        "Central Bengaluru",
        "Indiranagar",
        "Whitefield",
        "Koramangala",
        "Malleshwaram",
        "Rajajinagar",
        "Basavanagudi",
        "South Bengaluru",
        "North Bengaluru"
    )

    val places = remember(searchQuery, selectedCategory, selectedLocality, viewModel.allLocations) {
        val q = searchQuery.trim().lowercase()
        viewModel.allLocations.filter { loc ->
            val matchesCategory = selectedCategory == LocationCategory.ALL || loc.category == selectedCategory
            val matchesLocality = selectedLocality == "All Localities" ||
                loc.area.contains(selectedLocality, ignoreCase = true) ||
                loc.fullAddress.contains(selectedLocality, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                loc.name.lowercase().contains(q) ||
                loc.area.lowercase().contains(q) ||
                loc.landmark.lowercase().contains(q) ||
                loc.fullAddress.lowercase().contains(q) ||
                loc.pinCode.contains(q)
            matchesCategory && matchesLocality && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bengaluru Places Discovery",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Discover parks, malls, hospitals, colleges & landmarks",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search places across Bengaluru by name, area or PIN...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Places",
                        tint = TransitTeal,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discovery_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TransitTeal,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Locality Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                localities.forEach { locName ->
                    val isSelected = selectedLocality == locName
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) TransitNavy else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isSelected) TransitNavy else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { selectedLocality = locName }
                    ) {
                        Text(
                            text = locName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

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
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TransitTeal.copy(alpha = 0.2f) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) TransitTeal else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cat.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cat.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TransitNavy else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Destinations (${places.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )
                Text(
                    text = "1-Tap Multimodal Route Planning",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Places List
            if (places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ExploreOff, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No places match your filters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                        Text("Try selecting 'All Places' or another locality", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("place_card_${place.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(TransitTeal.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = place.category.emoji, fontSize = 18.sp)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = place.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TransitNavy
                                            )
                                            Text(
                                                text = "${place.area} • PIN: ${place.pinCode}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = place.category.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TransitNavy,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Street address & Landmark
                                Text(
                                    text = place.fullAddress.ifBlank { "${place.landmark}, ${place.area}" },
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )

                                // Useful details & opening hours badges
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (place.openingHours != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = place.openingHours,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }

                                    if (place.usefulDetails != null) {
                                        Text(
                                            text = place.usefulDetails.take(30) + (if (place.usefulDetails.length > 30) "..." else ""),
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Route Planning Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.setOrigin(place)
                                            viewModel.searchRoutes()
                                            onPlanRouteToPlace(place)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, TransitNavy),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("Set as Origin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.setDestination(place)
                                            viewModel.searchRoutes()
                                            onPlanRouteToPlace(place)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
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
            }
        }
    }
}
