package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RouteOption
import com.example.model.TransportMode
import com.example.ui.theme.*

@Composable
fun TransitMapCanvas(
    route: RouteOption,
    modifier: Modifier = Modifier,
    activeSegmentIndex: Int = 0,
    isNavigationActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicleProgress"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F1E36))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Subtle transit grid lines
            val gridColor = Color(0xFF1E2D4A)
            for (i in 1..5) {
                val y = height * (i / 6f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            for (j in 1..6) {
                val x = width * (j / 7f)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // 2. Multimodal route segments coordinates
            val nodeCount = (route.segments.size + 1).coerceAtLeast(2)
            val points = mutableListOf<Offset>()
            val padX = width * 0.12f
            val usableWidth = width - (2 * padX)

            points.add(Offset(padX, height * 0.72f)) // Origin
            if (nodeCount >= 3) {
                points.add(Offset(padX + usableWidth * 0.30f, height * 0.55f))
            }
            if (nodeCount >= 4) {
                points.add(Offset(padX + usableWidth * 0.70f, height * 0.32f))
            }
            points.add(Offset(padX + usableWidth, height * 0.22f)) // Destination

            // 3. Draw route paths between nodes
            for (i in 0 until route.segments.size) {
                val p1 = points.getOrElse(i) { points.first() }
                val p2 = points.getOrElse(i + 1) { points.last() }
                val segment = route.segments[i]

                val modeColor = when (segment.mode) {
                    TransportMode.METRO -> MetroPurple
                    TransportMode.BUS -> BusOrange
                    TransportMode.AUTO -> AutoYellow
                    TransportMode.CAB -> CabBlue
                    TransportMode.WALK -> Color(0xFF94A3B8)
                }

                val strokeStyle = if (segment.mode == TransportMode.WALK) {
                    Stroke(width = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f))
                } else {
                    Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                }

                // Draw segment path
                drawLine(
                    color = modeColor,
                    start = p1,
                    end = p2,
                    strokeWidth = strokeStyle.width,
                    pathEffect = strokeStyle.pathEffect
                )

                // If this is the active segment or navigation is on, draw moving vehicle dot
                val isActiveSegment = if (isNavigationActive) i == activeSegmentIndex else i == 0
                if (isActiveSegment) {
                    val currentPos = Offset(
                        x = p1.x + (p2.x - p1.x) * vehicleProgress,
                        y = p1.y + (p2.y - p1.y) * vehicleProgress
                    )
                    // Glow ring
                    drawCircle(
                        color = TransitTeal.copy(alpha = pulseAlpha * 0.4f),
                        radius = 16.dp.toPx(),
                        center = currentPos
                    )
                    // Core vehicle dot
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = currentPos
                    )
                    drawCircle(
                        color = modeColor,
                        radius = 4.dp.toPx(),
                        center = currentPos
                    )
                }
            }

            // 4. Station Nodes
            points.forEachIndexed { index, pt ->
                val isOrigin = index == 0
                val isDestination = index == points.size - 1
                val isInterchange = !isOrigin && !isDestination

                val haloColor = when {
                    isOrigin -> TransitTeal
                    isDestination -> DangerRed
                    else -> MetroPurple
                }

                // Outer halo
                drawCircle(
                    color = haloColor.copy(alpha = 0.25f),
                    radius = if (isOrigin || isDestination) 14.dp.toPx() else 10.dp.toPx(),
                    center = pt
                )

                // White ring
                drawCircle(
                    color = Color.White,
                    radius = if (isOrigin || isDestination) 8.dp.toPx() else 6.dp.toPx(),
                    center = pt
                )

                // Inner core
                drawCircle(
                    color = haloColor,
                    radius = if (isOrigin || isDestination) 5.dp.toPx() else 3.5.dp.toPx(),
                    center = pt
                )

                // Text label
                val label = when {
                    isOrigin -> "START"
                    isDestination -> "END"
                    else -> "TRANSFER ${index}"
                }

                val measuredText = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(pt.x - (measuredText.size.width / 2f), pt.y + 16.dp.toPx())
                )
            }
        }

        // Overlay: Top Badge with route mode pills
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.65f))
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
                text = "LIVE BENGALURU TRANSIT MAP",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Overlay: Route stats pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsTransit,
                contentDescription = null,
                tint = TransitTeal,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${route.totalDurationMinutes}m • ₹${route.totalFare.toInt()} • ${route.segments.size} legs",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
