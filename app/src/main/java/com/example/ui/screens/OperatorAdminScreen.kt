package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.getTransportModeDetails
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@Composable
fun OperatorAdminScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        // Admin Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TransitTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = TransitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Transport Integration Monitor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ONDC Mobility & GTFS-RT Telemetry Gateway",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disruption Test Simulator Card
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulate Network Disruption",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                        Text(
                            text = "Inject congestion delays on Outer Ring Road to test dynamic AI route re-optimization",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Switch(
                        checked = uiState.isDisruptionSimulated,
                        onCheckedChange = { viewModel.toggleDisruptionSimulation() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WarningAmber,
                            checkedTrackColor = WarningAmber.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("admin_disruption_switch")
                    )
                }

                if (uiState.isDisruptionSimulated) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "STATUS: ACTIVE DISRUPTION • AI automatically reroutes bus connections through Purple Line Metro.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Operator Feeds Status
        Text(
            text = "Connected Operator Feeds",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TransitNavy
        )
        Text(
            text = "Real-time health, response latency, and active vehicles",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(10.dp))

        uiState.operatorStatuses.forEach { op ->
            val (icon, color) = getTransportModeDetails(op.mode)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = op.operatorName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (op.status == "OPERATIONAL") Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = if (op.status == "OPERATIONAL") "LIVE" else "PROTOTYPE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (op.status == "OPERATIONAL") SuccessGreen else CabBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${op.feedType} • Latency: ${op.latencyMs}ms • Synced: ${op.lastSyncTime}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "${op.activeVehicles} active units reporting telemetry",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitTeal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transport Partnership Readiness Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ONDC Open Mobility Architecture",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GoWeave is engineered on the Beckn Protocol specification. Any authorized transport operator (Metro, BMTC, Auto unions) can expose their catalog endpoints to participate in single-booking settlement without proprietary lock-in.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
