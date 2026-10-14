package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveVehicle
import com.example.model.TransitLocation
import com.example.model.TransportMode
import com.example.ui.theme.*
import kotlin.math.sqrt

@Composable
fun LiveVehicleMapCanvas(
    vehicles: List<LiveVehicle>,
    stations: List<TransitLocation>,
    selectedVehicleId: String?,
    onSelectVehicle: (LiveVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vehicleMapAnim")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    val textMeasurer = rememberTextMeasurer()

    // Keep track of mapped vehicle screen positions for click detection
    var vehicleClickTargets by remember { mutableStateOf<List<Pair<LiveVehicle, Offset>>>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A1322))
            .testTag("live_vehicle_map_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(vehicles) {
                    detectTapGestures { tapOffset ->
                        // Check if tapped near any vehicle icon within 28dp radius
                        val radiusPx = 36.dp.toPx()
                        val hit = vehicleClickTargets.find { (_, pos) ->
                            val dx = tapOffset.x - pos.x
                            val dy = tapOffset.y - pos.y
                            sqrt(dx * dx + dy * dy) <= radiusPx
                        }
                        if (hit != null) {
                            onSelectVehicle(hit.first)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Subtle futuristic city navigation grid
            val gridColor = Color(0xFF14243B)
            for (i in 1..7) {
                val y = height * (i / 8f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                )
            }
            for (j in 1..8) {
                val x = width * (j / 9f)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                )
            }

            // 2. City Road Arterials & Corridors
            val arterialColor = Color(0xFF1E3A5F)
            drawLine(
                color = arterialColor,
                start = Offset(width * 0.15f, height * 0.2f),
                end = Offset(width * 0.85f, height * 0.8f),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF254B7A),
                start = Offset(width * 0.15f, height * 0.8f),
                end = Offset(width * 0.85f, height * 0.25f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = arterialColor,
                start = Offset(width * 0.1f, height * 0.5f),
                end = Offset(width * 0.9f, height * 0.5f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3. Station Nodes
            val stationPositions = mutableListOf<Offset>()
            stations.take(6).forEachIndexed { idx, st ->
                val px = when (idx) {
                    0 -> width * 0.20f
                    1 -> width * 0.50f
                    2 -> width * 0.80f
                    3 -> width * 0.35f
                    4 -> width * 0.65f
                    else -> width * 0.85f
                }
                val py = when (idx) {
                    0 -> height * 0.25f
                    1 -> height * 0.22f
                    2 -> height * 0.35f
                    3 -> height * 0.68f
                    4 -> height * 0.72f
                    else -> height * 0.85f
                }
                val nodeOffset = Offset(px, py)
                stationPositions.add(nodeOffset)

                // Station Ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = 8.dp.toPx(),
                    center = nodeOffset
                )
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 4.dp.toPx(),
                    center = nodeOffset
                )

                // Station Name Label
                val stText = textMeasurer.measure(
                    text = st.name.take(14),
                    style = TextStyle(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                )
                drawText(
                    textLayoutResult = stText,
                    topLeft = Offset(px - (stText.size.width / 2f), py + 10.dp.toPx())
                )
            }

            // 4. Real-Time Moving Vehicles (Buses, Cabs, Autos)
            val currentTargets = mutableListOf<Pair<LiveVehicle, Offset>>()

            vehicles.forEachIndexed { index, veh ->
                // Map vehicle coordinates relative to map bounds
                val normalizedX = ((index * 0.23f + 0.18f) % 0.75f + 0.12f) * width
                val normalizedY = ((index * 0.31f + 0.24f) % 0.65f + 0.18f) * height
                val pos = Offset(normalizedX, normalizedY)
                currentTargets.add(Pair(veh, pos))

                val isSelected = veh.id == selectedVehicleId

                val (vehColor, vehHalo) = when (veh.mode) {
                    TransportMode.BUS -> Pair(BusOrange, BusOrange.copy(alpha = 0.35f))
                    TransportMode.CAB -> Pair(CabBlue, CabBlue.copy(alpha = 0.35f))
                    TransportMode.AUTO -> Pair(AutoYellow, AutoYellow.copy(alpha = 0.35f))
                    TransportMode.METRO -> Pair(MetroPurple, MetroPurple.copy(alpha = 0.35f))
                    else -> Pair(TransitTeal, TransitTeal.copy(alpha = 0.35f))
                }

                // Pulsing GPS Halo
                val pulseRadius = if (isSelected) 24.dp.toPx() * (1f + pulseRing * 0.3f) else 14.dp.toPx()
                drawCircle(
                    color = if (isSelected) TransitTeal.copy(alpha = pulseRing * 0.5f) else vehHalo,
                    radius = pulseRadius,
                    center = pos
                )

                // Vehicle Core Body
                val coreRadius = if (isSelected) 10.dp.toPx() else 8.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = coreRadius + 2.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = vehColor,
                    radius = coreRadius,
                    center = pos
                )

                // Heading pointer arrow
                rotate(degrees = veh.headingDegrees, pivot = pos) {
                    val arrowPath = Path().apply {
                        moveTo(pos.x, pos.y - coreRadius - 5.dp.toPx())
                        lineTo(pos.x - 4.dp.toPx(), pos.y - coreRadius)
                        lineTo(pos.x + 4.dp.toPx(), pos.y - coreRadius)
                        close()
                    }
                    drawPath(arrowPath, color = Color.White)
                }

                // Callout Tag on map (Vehicle Number & Live Speed)
                val label = "${veh.vehicleNumber} • ${veh.speedKmh} km/h"
                val textLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )

                // Tag Background Pill
                val pillWidth = textLayout.size.width + 16.dp.toPx()
                val pillHeight = textLayout.size.height + 8.dp.toPx()
                val pillTopLeft = Offset(pos.x - (pillWidth / 2f), pos.y - coreRadius - pillHeight - 4.dp.toPx())

                drawRoundRect(
                    color = if (isSelected) TransitTeal else Color(0xDD0F1E36),
                    topLeft = pillTopLeft,
                    size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(pillTopLeft.x + 8.dp.toPx(), pillTopLeft.y + 4.dp.toPx())
                )
            }

            vehicleClickTargets = currentTargets
        }

        // Top Status Header Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TransitTeal)
            )
            Text(
                text = "LIVE GPS FLEET TRACKER",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Bottom Ticker Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = TransitTeal,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${vehicles.size} Vehicles Streaming (Tap to inspect)",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
