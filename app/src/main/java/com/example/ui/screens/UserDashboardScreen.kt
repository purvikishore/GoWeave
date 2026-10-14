package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookingRecord
import com.example.ui.components.DigitalTicketPassCard
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@Composable
fun UserDashboardScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onPlanSavedTrip: () -> Unit,
    onViewTicketDetails: (BookingRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedBookingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        // User Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(TransitTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PK",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Purvi Kishore",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "purvikishore21@gmail.com",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TransitTeal.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "GREEN COMMUTER • 18.6 KG CO2 SAVED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Daily Commute Planner Card
        Text(
            text = "Daily Commute Plan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TransitNavy
        )
        Spacer(modifier = Modifier.height(8.dp))

        uiState.dailyCommutes.forEach { commute ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = commute.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = commute.activeDays,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${commute.origin.name} → ${commute.destination.name}",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Target Arrival: ${commute.targetArrivalTime} • ${commute.preferredMode.label}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.setOrigin(commute.origin)
                            viewModel.setDestination(commute.destination)
                            viewModel.setPreference(commute.preferredMode)
                            viewModel.searchRoutes()
                            onPlanSavedTrip()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("plan_daily_commute_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsTransit,
                                contentDescription = null,
                                tint = TransitTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Today's Optimal Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Saved Places
        Text(
            text = "Saved Places",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TransitNavy
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            uiState.savedPlaces.forEach { place ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable {
                            viewModel.setDestination(place.location)
                            viewModel.searchRoutes()
                            onPlanSavedTrip()
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = when (place.category) {
                                "HOME" -> Icons.Default.Home
                                "WORK" -> Icons.Default.Work
                                else -> Icons.Default.FitnessCenter
                            },
                            contentDescription = null,
                            tint = TransitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = place.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = place.address,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Booking History & Active Passes
        Text(
            text = "Booking History & Passes (${uiState.bookings.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TransitNavy
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.bookings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No bookings yet. Plan and book your first multimodal trip!",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            uiState.bookings.forEach { bk ->
                val isExpanded = expandedBookingId == bk.bookingId
                val isCancelled = bk.status == "CANCELLED"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCancelled) Color(0xFFF8FAFC) else Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Booking #${bk.bookingId}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCancelled) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = bk.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCancelled) DangerRed else SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${bk.originName} → ${bk.destinationName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "${bk.bookedAt} • Paid via ${bk.paymentMethod} • ₹${bk.totalAmount.toInt()}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row: View Passes / Cancel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    expandedBookingId = if (isExpanded) null else bk.bookingId
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isExpanded) "Hide Passes" else "Show QR Passes (${bk.tickets.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isCancelled) {
                                TextButton(
                                    onClick = { viewModel.cancelBooking(bk.bookingId) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                                ) {
                                    Text("Cancel & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Expanded Ticket Pass View
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            bk.tickets.forEach { tkt ->
                                DigitalTicketPassCard(
                                    ticket = tkt,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
