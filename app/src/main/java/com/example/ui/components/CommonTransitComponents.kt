package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

import androidx.compose.material.icons.automirrored.filled.DirectionsWalk

@Composable
fun getTransportModeDetails(mode: TransportMode): Pair<ImageVector, Color> {
    return when (mode) {
        TransportMode.METRO -> Pair(Icons.Default.Subway, MetroPurple)
        TransportMode.BUS -> Pair(Icons.Default.DirectionsBus, BusOrange)
        TransportMode.AUTO -> Pair(Icons.Default.ElectricRickshaw, AutoYellow)
        TransportMode.CAB -> Pair(Icons.Default.LocalTaxi, CabBlue)
        TransportMode.WALK -> Pair(Icons.AutoMirrored.Filled.DirectionsWalk, WalkGray)
    }
}

@Composable
fun RouteCard(
    route: RouteOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) TransitTeal else Color(0xFFE2E8F0)
    val containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("route_card_${route.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badge & Fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (badgeBg, badgeText) = when (route.preferenceType) {
                    RoutePreference.BEST_BALANCED -> Pair(Color(0xFFCCFBF1), TransitTeal)
                    RoutePreference.CHEAPEST -> Pair(Color(0xFFDCFCE7), SuccessGreen)
                    RoutePreference.FASTEST -> Pair(Color(0xFFE0F2FE), CabBlue)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = route.badgeTag.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }

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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Total Duration
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${route.totalDurationMinutes} mins",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TransitNavy
                    )
                }
            }

            Text(
                text = "${route.departureTime} – ${route.arrivalTime} • ${route.transfersCount} transfer${if (route.transfersCount != 1) "s" else ""} • ${route.totalWalkMeters}m walk",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Multimodal Leg Chain
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = seg.mode.displayName,
                                tint = color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${seg.durationMinutes}m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TransitNavy
                            )
                        }
                    }

                    if (index < route.segments.size - 1) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next leg",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Highlights
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                route.keyHighlights.take(2).forEach { highlight ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(13.dp)
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
            }

            // Disruption indicator if active
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
                            text = route.disruptionAlert.headline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentTimelineItem(
    segment: JourneySegment,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getTransportModeDetails(segment.mode)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Left Column: Departure/Arrival time & timeline node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(64.dp)
        ) {
            Text(
                text = segment.departureTime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = segment.mode.displayName,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(color.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Column: Details card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = segment.operatorName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )

                    if (segment.fare > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "₹${segment.fare.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Free Walk",
                            fontSize = 11.sp,
                            color = WalkGray
                        )
                    }
                }

                Text(
                    text = segment.routeLabel,
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "From: ${segment.fromStation}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "To: ${segment.toStation}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )

                if (segment.intermediateStopsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${segment.intermediateStopsCount} intermediate stations (${segment.durationMinutes}m duration)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                if (segment.walkingInstructions != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🚶 ${segment.walkingInstructions}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }

                if (segment.delayMinutes > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Text(
                            text = "⚠️ Delay: +${segment.delayMinutes} mins due to traffic congestion",
                            fontSize = 10.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalTicketPassCard(
    ticket: TransitTicket,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getTransportModeDetails(ticket.mode)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = ticket.operatorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = ticket.legName,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Text(
                    text = "₹${ticket.fare.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulated Barcode / QR display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Digital Boarding QR",
                        tint = TransitNavy,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PNR: ${ticket.pnr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = ticket.validityWindow,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TransitTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = ticket.gateAccessText,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}
