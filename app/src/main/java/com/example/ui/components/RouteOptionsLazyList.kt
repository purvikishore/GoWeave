package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RouteOption
import com.example.model.RoutePreference
import com.example.model.TransportMode
import com.example.ui.theme.*

/**
 * A dedicated LazyList component for displaying multimodal route options retrieved
 * by the GoWeave routing engine.
 *
 * Each item prominently features:
 * - Transport mode icons with leg-by-leg chain indicators (Bus, Metro, Auto, Cab, Walk)
 * - Estimated total travel time (duration in minutes, departure and arrival times)
 * - Estimated total cost (fare in INR, itemized savings compared to alternatives)
 * - Transfer points count and walking distance
 * - Route classification badges (Best Balanced, Fastest, Cheapest)
 * - Live disruption alerts and eco/carbon savings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteOptionsLazyList(
    routes: List<RouteOption>,
    selectedRouteId: String?,
    onSelectRoute: (RouteOption) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    onViewRouteDetails: ((RouteOption) -> Unit)? = null,
    onBookRoute: ((RouteOption) -> Unit)? = null
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("route_options_lazy_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Optional Header Slot (e.g., filters, disruption alert banners, search summary)
        if (headerContent != null) {
            item(key = "route_list_header") {
                headerContent()
            }
        }

        // Empty state when no routes match criteria
        if (routes.isEmpty()) {
            item(key = "route_list_empty_state") {
                RouteEmptyStateCard(modifier = Modifier.fillMaxWidth())
            }
        } else {
            // Lazy List Items with stable keys and animations
            items(
                items = routes,
                key = { it.id }
            ) { route ->
                val isSelected = route.id == selectedRouteId
                RouteOptionItemCard(
                    route = route,
                    isSelected = isSelected,
                    onSelect = { onSelectRoute(route) },
                    onViewDetails = onViewRouteDetails?.let { { it(route) } },
                    onBook = onBookRoute?.let { { it(route) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .testTag("route_option_item_${route.id}")
                )
            }
        }

        // Optional Footer Slot (e.g. insights, eco notes, legal/disclaimer)
        if (footerContent != null) {
            item(key = "route_list_footer") {
                footerContent()
            }
        }
    }
}

/**
 * High-fidelity individual item card displaying transport modes, cost, and estimated time.
 */
@Composable
fun RouteOptionItemCard(
    route: RouteOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    onViewDetails: (() -> Unit)? = null,
    onBook: (() -> Unit)? = null
) {
    val borderColor = if (isSelected) TransitTeal else Color(0xFFE2E8F0)
    val containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
    val elevation = if (isSelected) 4.dp else 1.dp

    Card(
        modifier = modifier
            .clickable(onClick = onSelect)
            .testTag("route_card_${route.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Top Bar: Preference Badge & Total Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route Classification Badge
                val (badgeBg, badgeText) = when (route.preferenceType) {
                    RoutePreference.BEST_BALANCED -> Pair(Color(0xFFCCFBF1), TransitTeal)
                    RoutePreference.CHEAPEST -> Pair(Color(0xFFDCFCE7), SuccessGreen)
                    RoutePreference.FASTEST -> Pair(Color(0xFFE0F2FE), CabBlue)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badgeIcon = when (route.preferenceType) {
                            RoutePreference.BEST_BALANCED -> Icons.Default.Balance
                            RoutePreference.CHEAPEST -> Icons.Default.Savings
                            RoutePreference.FASTEST -> Icons.Default.Bolt
                        }
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = route.badgeTag.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Estimated Cost Display
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${route.totalFare.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = " total",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    if (route.preferenceType == RoutePreference.CHEAPEST) {
                        Text(
                            text = "Save up to 85%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    } else if (route.preferenceType == RoutePreference.BEST_BALANCED) {
                        Text(
                            text = "Saves ₹215 vs Cab",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Route Title & Estimated Travel Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )

                // Total Estimated Duration Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TransitNavy.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Estimated Duration",
                            tint = TransitNavy,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${route.totalDurationMinutes} mins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                    }
                }
            }

            // Timetable & Transfer Details
            Text(
                text = "${route.departureTime} – ${route.arrivalTime} • ${route.transfersCount} transfer${if (route.transfersCount != 1) "s" else ""} • ${route.totalWalkMeters}m walk",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Transport Mode Icons Chain (Bus, Metro, Auto, Cab, Walk)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    route.segments.forEachIndexed { index, seg ->
                        val (icon, color) = getTransportModeDetails(seg.mode)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = color.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = seg.mode.displayName,
                                    tint = color,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "${seg.durationMinutes}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TransitNavy
                                )
                            }
                        }

                        if (index < route.segments.size - 1) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Transfer to next mode",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Key Highlights & Eco Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First highlight
                route.keyHighlights.firstOrNull()?.let { highlight ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = highlight,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Carbon Savings Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF0FDF4)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "-${route.carbonSavedKg}kg CO₂",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            // 5. Active Traffic Disruption Alert Banner (if any)
            if (route.disruptionAlert != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${route.disruptionAlert.headline} (AI rerouted)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 6. Action buttons if selected
            if (isSelected && (onViewDetails != null || onBook != null)) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onViewDetails != null) {
                        OutlinedButton(
                            onClick = onViewDetails,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TransitNavy)
                        ) {
                            Text(
                                text = "Directions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                        }
                    }

                    if (onBook != null) {
                        Button(
                            onClick = onBook,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                        ) {
                            Text(
                                text = "Book (₹${route.totalFare.toInt()})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Placeholder card displayed when no routes match the search or filter criteria.
 */
@Composable
fun RouteEmptyStateCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No route options match current criteria",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try adjusting your budget filter or choosing a different departure time.",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }
    }
}
